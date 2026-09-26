package com.denonexus.mgshaders.region.storage;

import com.denonexus.mgshaders.nativebridge.NativeChunkLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFileStorage;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

public final class RegionLz4Cache {

    /*
     * 96 serialized chunks per RegionFileStorage.
     *
     * This is deliberately bounded for low-RAM Android devices.
     */
    private static final int MAX_ENTRIES = 96;

    /*
     * Safety limit for one serialized chunk.
     */
    private static final int MAX_RAW_BYTES =
            8 * 1024 * 1024;

    private static final Map<
            RegionFileStorage,
            LinkedHashMap<Long, Entry>
            > CACHES =
            new WeakHashMap<>();

    private static volatile boolean nativeReady;

    private record Entry(
            ByteBuffer compressed,
            int rawSize
    ) {}

    private RegionLz4Cache() {}

    public static void markNativeReady(
            boolean ready
    ) {
        nativeReady = ready;
    }

    public static CompoundTag get(
            RegionFileStorage storage,
            ChunkPos pos
    ) {

        if (!nativeReady) {
            return null;
        }

        Entry entry;

        synchronized (CACHES) {

            LinkedHashMap<Long, Entry> cache =
                    CACHES.get(storage);

            entry =
                    cache == null
                            ? null
                            : cache.get(
                                    pos.toLong()
                            );
        }

        if (entry == null) {
            return null;
        }

        try {

            ByteBuffer src =
                    entry.compressed()
                            .duplicate()
                            .order(
                                    ByteOrder.nativeOrder()
                            );

            int compressedLength =
                    src.remaining() - 4;

            if (compressedLength <= 0) {
                return null;
            }

            if (entry.rawSize() <= 0 ||
                    entry.rawSize() >
                            MAX_RAW_BYTES) {

                return null;
            }

            ByteBuffer dst =
                    ByteBuffer.allocateDirect(
                            entry.rawSize()
                    ).order(
                            ByteOrder.nativeOrder()
                    );

            int output =
                    NativeChunkLoader
                            .decompressLZ4Direct(
                                    src,
                                    4,
                                    compressedLength,
                                    dst,
                                    0,
                                    entry.rawSize()
                            );

            if (output !=
                    entry.rawSize()) {

                return null;
            }

            byte[] raw =
                    new byte[output];

            dst.position(0)
                    .limit(output)
                    .get(raw);

            try (
                    DataInputStream input =
                            new DataInputStream(
                                    new ByteArrayInputStream(
                                            raw
                                    )
                            )
            ) {

                return NbtIo.read(
                        input,
                        NbtAccounter.unlimitedHeap()
                );
            }

        } catch (Throwable ignored) {

            return null;
        }
    }

    public static void put(
            RegionFileStorage storage,
            ChunkPos pos,
            CompoundTag tag
    ) {

        if (!nativeReady ||
                tag == null) {

            return;
        }

        try {

            ByteArrayOutputStream bytes =
                    new ByteArrayOutputStream(
                            64 * 1024
                    );

            try (
                    DataOutputStream output =
                            new DataOutputStream(bytes)
            ) {

                /*
                 * IMPORTANT:
                 *
                 * Use raw NBT, not gzip.
                 *
                 * This gives LZ4 highly compressible
                 * structured NBT data.
                 */
                NbtIo.write(
                        tag,
                        output
                );
            }

            byte[] raw =
                    bytes.toByteArray();

            if (raw.length == 0 ||
                    raw.length >
                            MAX_RAW_BYTES) {

                return;
            }

            ByteBuffer source =
                    ByteBuffer.allocateDirect(
                            raw.length
                    ).order(
                            ByteOrder.nativeOrder()
                    );

            source.put(raw)
                    .flip();

            int capacity =
                    raw.length
                            +
                            raw.length / 255
                            +
                            64;

            /*
             * First 4 bytes store the original
             * NBT byte length.
             */
            ByteBuffer compressed =
                    ByteBuffer.allocateDirect(
                            capacity + 4
                    ).order(
                            ByteOrder.nativeOrder()
                    );

            int compressedSize =
                    NativeChunkLoader
                            .compressLZ4Direct(
                                    source,
                                    0,
                                    raw.length,
                                    compressed,
                                    4,
                                    capacity
                            );

            if (compressedSize <= 0) {
                return;
            }

            compressed.putInt(
                    0,
                    raw.length
            );

            compressed.position(0)
                    .limit(
                            compressedSize + 4
                    );

            ByteBuffer stored =
                    ByteBuffer.allocateDirect(
                            compressedSize + 4
                    ).order(
                            ByteOrder.nativeOrder()
                    );

            stored.put(compressed)
                    .flip();

            synchronized (CACHES) {

                LinkedHashMap<Long, Entry> cache =
                        CACHES.computeIfAbsent(
                                storage,
                                k ->
                                        new LinkedHashMap<>(
                                                128,
                                                0.75f,
                                                true
                                        )
                        );

                cache.put(
                        pos.toLong(),
                        new Entry(
                                stored,
                                raw.length
                        )
                );

                while (cache.size() >
                        MAX_ENTRIES) {

                    cache.remove(
                            cache.keySet()
                                    .iterator()
                                    .next()
                    );
                }
            }

        } catch (Throwable ignored) {
        }
    }

    public static void invalidate(
            RegionFileStorage storage,
            ChunkPos pos
    ) {

        synchronized (CACHES) {

            LinkedHashMap<Long, Entry> cache =
                    CACHES.get(storage);

            if (cache != null) {
                cache.remove(
                        pos.toLong()
                );
            }
        }
    }
}
