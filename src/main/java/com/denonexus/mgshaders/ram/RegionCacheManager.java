package com.denonexus.mgshaders.ram;

import com.denonexus.mgshaders.config.RegionCacheConfig;
import com.denonexus.mgshaders.nativebridge.NativeChunkLoader;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.logging.Logger;

public class RegionCacheManager {
    private static final Logger LOGGER = Logger.getLogger("MGShaders-RegionCache");

    public enum RegionState { COMPRIMIDA, PRE_CARREGANDO, PRONTA, RESFRIANDO }

    public static class CachedRegion {
        public final int rx, rz;
        public RegionState state = RegionState.PRONTA;
        public int cooldownTicks = 0;
        public final Map<Long, ByteBuffer> compressedChunks = new ConcurrentHashMap<>();
        public final Map<Long, ByteBuffer> uncompressedChunks = new ConcurrentHashMap<>();

        public CachedRegion(int rx, int rz) {
            this.rx = rx;
            this.rz = rz;
        }
    }

    private static final Map<Long, CachedRegion> REGION_MAP = new ConcurrentHashMap<>();

    public static void onTick(double playerX, double playerZ, double velX, double velZ) {
        RegionCacheConfig cfg = RegionCacheConfig.get();
        if (!cfg.regionCacheEnabled) return;

        int curRx = (int) Math.floor(playerX / 512.0);
        int curRz = (int) Math.floor(playerZ / 512.0);

        int projRx = (int) Math.floor((playerX + velX * cfg.lookaheadTicks) / 512.0);
        int projRz = (int) Math.floor((playerZ + velZ * cfg.lookaheadTicks) / 512.0);

        long curKey = regionKey(curRx, curRz);
        long projKey = regionKey(projRx, projRz);

        CachedRegion curReg = REGION_MAP.computeIfAbsent(curKey, k -> new CachedRegion(curRx, curRz));
        if (curReg.state == RegionState.COMPRIMIDA) {
            decompressRegion(curReg);
        }

        if (projKey != curKey) {
            CachedRegion projReg = REGION_MAP.computeIfAbsent(projKey, k -> new CachedRegion(projRx, projRz));
            if (projReg.state == RegionState.COMPRIMIDA) {
                decompressRegion(projReg);
            }
        }

        for (CachedRegion r : REGION_MAP.values()) {
            long key = regionKey(r.rx, r.rz);
            if (key == curKey || key == projKey) continue;

            if (r.state == RegionState.PRONTA) {
                r.state = RegionState.RESFRIANDO;
                r.cooldownTicks = cfg.cooldownTicks;
            } else if (r.state == RegionState.RESFRIANDO) {
                r.cooldownTicks--;
                if (r.cooldownTicks <= 0) {
                    compressAndEvictUncompressed(r);
                }
            }
        }
    }

    public static byte[] getOrFetchChunkData(File file, int chunkX, int chunkZ) throws IOException {
        int rx = chunkX >> 5;
        int rz = chunkZ >> 5;
        long rKey = regionKey(rx, rz);
        long cKey = chunkKey(chunkX, chunkZ);

        CachedRegion reg = REGION_MAP.computeIfAbsent(rKey, k -> new CachedRegion(rx, rz));

        // 1. Tenta pegar direto da RAM descomprimido
        ByteBuffer uncomp = reg.uncompressedChunks.get(cKey);
        if (uncomp != null) {
            byte[] data = new byte[uncomp.remaining()];
            uncomp.duplicate().get(data);
            return data;
        }

        // 2. Tenta pegar comprimido e descomprimir via LZ4 C++
        ByteBuffer comp = reg.compressedChunks.get(cKey);
        if (comp != null) {
            ByteBuffer dst = ByteBuffer.allocateDirect(256 * 1024).order(ByteOrder.nativeOrder());
            int decompSize = NativeChunkLoader.decompressLZ4Direct(comp, 0, comp.remaining(), dst, 0, dst.capacity());
            if (decompSize > 0) {
                byte[] data = new byte[decompSize];
                dst.get(data);
                dst.rewind();
                reg.uncompressedChunks.put(cKey, dst);
                return data;
            }
        }

        // 3. Cache Miss: Lê do disco, comprime com LZ4 e armazena na RAM
        byte[] rawDiskBytes;
        try (FileInputStream fis = new FileInputStream(file)) {
            rawDiskBytes = fis.readAllBytes();
        }

        ByteBuffer srcBuf = ByteBuffer.allocateDirect(rawDiskBytes.length).order(ByteOrder.nativeOrder());
        srcBuf.put(rawDiskBytes).flip();

        ByteBuffer compBuf = ByteBuffer.allocateDirect(rawDiskBytes.length + 1024).order(ByteOrder.nativeOrder());
        int compSize = NativeChunkLoader.compressLZ4Direct(srcBuf, 0, rawDiskBytes.length, compBuf, 0, compBuf.capacity());

        if (compSize > 0) {
            compBuf.limit(compSize);
            reg.compressedChunks.put(cKey, compBuf);
            reg.uncompressedChunks.put(cKey, srcBuf);
        }

        return rawDiskBytes;
    }

    private static void decompressRegion(CachedRegion reg) {
        for (Map.Entry<Long, ByteBuffer> entry : reg.compressedChunks.entrySet()) {
            ByteBuffer comp = entry.getValue();
            ByteBuffer dst = ByteBuffer.allocateDirect(256 * 1024).order(ByteOrder.nativeOrder());
            int read = NativeChunkLoader.decompressLZ4Direct(comp, 0, comp.remaining(), dst, 0, dst.capacity());
            if (read > 0) {
                dst.limit(read);
                reg.uncompressedChunks.put(entry.getKey(), dst);
            }
        }
        reg.state = RegionState.PRONTA;
    }

    private static void compressAndEvictUncompressed(CachedRegion reg) {
        reg.uncompressedChunks.clear();
        reg.state = RegionState.COMPRIMIDA;
    }

    public static void onWorldUnload() {
        LOGGER.info("[MGShaders] Limpando todo o cache de regioes RAM...");
        REGION_MAP.clear();
    }

    public static long regionKey(int rx, int rz) {
        return (((long) rx) & 0xFFFFFFFFL) | ((((long) rz) & 0xFFFFFFFFL) << 32);
    }

    public static long chunkKey(int cx, int cz) {
        return (((long) cx) & 0xFFFFFFFFL) | ((((long) cz) & 0xFFFFFFFFL) << 32);
    }
}
