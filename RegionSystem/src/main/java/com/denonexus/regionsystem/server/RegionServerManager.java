package com.denonexus.regionsystem.server;

import com.denonexus.regionsystem.RegionSystem;
import com.denonexus.regionsystem.region.Region;
import com.denonexus.regionsystem.region.RegionCache;
import com.denonexus.regionsystem.region.RegionPos;
import com.denonexus.regionsystem.region.RegionState;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import java.util.HashSet;
import java.util.Set;

public final class RegionServerManager {

    /*
     * 1 Region = 4x4 chunks.
     */
    public static final int REGION_SIZE = 4;

    public static final int CHUNKS_PER_REGION = 16;

    /*
     * One Region around the player in every direction.
     * 3x3 Regions = 48x48 chunks.
     */
    public static final int REGION_RADIUS = 1;

    public static final int CACHE_SIZE = 49;

    /*
     * Limit work per server tick.
     *
     * This is intentionally conservative because synchronous
     * chunk generation can be expensive.
     */
    private static final int CHUNKS_PER_TICK = 2;

    private final MinecraftServer server;

    private final RegionCache cache =
            new RegionCache(CACHE_SIZE);

    private long tick;

    private int budget;

    public RegionServerManager(
            MinecraftServer server
    ) {
        this.server = server;
    }

    public void start() {

        RegionSystem.LOGGER.info(
                "RegionSystem: {}x{} chunks per Region",
                REGION_SIZE,
                REGION_SIZE
        );

        RegionSystem.LOGGER.info(
                "RegionSystem: cache={} Regions",
                CACHE_SIZE
        );
    }

    public void stop() {

        for (Region region :
                cache.values()) {

            region.clear();
        }

        RegionSystem.LOGGER.info(
                "RegionSystem stopped"
        );
    }

    public void onPlayerJoin(
            ServerPlayer player
    ) {

        RegionSystem.LOGGER.info(
                "Region preload started for {}",
                player.getGameProfile()
                        .name()
        );
    }

    public void onPlayerLeave(
            ServerPlayer player
    ) {
        /*
         * Vanilla continues to own chunk lifecycle,
         * tickets and network tracking.
         */
    }

    public void tick() {

        tick++;

        budget = CHUNKS_PER_TICK;

        if ((tick & 3L) != 0L) {
            return;
        }

        for (ServerLevel level :
                server.getAllLevels()) {

            for (ServerPlayer player :
                    level.players()) {

                if (budget <= 0) {
                    break;
                }

                preloadAround(player);
            }
        }

        evict();
    }

    private void preloadAround(
            ServerPlayer player
    ) {

        ServerLevel level =
                player.serverLevel();

        int chunkX =
                player.blockPosition()
                        .getX() >> 4;

        int chunkZ =
                player.blockPosition()
                        .getZ() >> 4;

        RegionPos center =
                RegionPos.fromChunk(
                        chunkX,
                        chunkZ
                );

        /*
         * Center first, then surrounding Regions.
         */
        for (int radius = 0;
             radius <= REGION_RADIUS;
             radius++) {

            for (int dz = -radius;
                 dz <= radius;
                 dz++) {

                for (int dx = -radius;
                     dx <= radius;
                     dx++) {

                    if (budget <= 0) {
                        return;
                    }

                    RegionPos pos =
                            new RegionPos(
                                    center.x() + dx,
                                    center.z() + dz
                            );

                    Region region =
                            cache.getOrCreate(pos);

                    region.touch(tick);

                    if (region.state()
                            == RegionState.READY) {

                        continue;
                    }

                    loadRegion(
                            level,
                            region
                    );
                }
            }
        }
    }

    private void loadRegion(
            ServerLevel level,
            Region region
    ) {

        if (budget <= 0) {
            return;
        }

        region.state(
                RegionState.PRELOADING
        );

        int minX =
                region.pos().minChunkX();

        int minZ =
                region.pos().minChunkZ();

        for (int z = 0;
             z < REGION_SIZE;
             z++) {

            for (int x = 0;
                 x < REGION_SIZE;
                 x++) {

                if (budget <= 0) {
                    region.state(
                            RegionState.ACTIVE
                    );
                    return;
                }

                int chunkX =
                        minX + x;

                int chunkZ =
                        minZ + z;

                if (region.get(
                        chunkX,
                        chunkZ
                ) != null) {
                    continue;
                }

                try {

                    /*
                     * IMPORTANT:
                     *
                     * We deliberately use Minecraft's own
                     * ServerChunkCache path.
                     *
                     * This means generation remains vanilla:
                     *
                     * generator
                     * -> ChunkStatus
                     * -> lighting
                     * -> structures
                     * -> FULL chunk
                     * -> ChunkMap
                     * -> client packet
                     */
                    ChunkAccess chunk =
                            level.getChunkSource()
                                    .getChunk(
                                            chunkX,
                                            chunkZ,
                                            ChunkStatus.FULL,
                                            false
                                    );

                    if (chunk != null) {

                        region.put(
                                chunkX,
                                chunkZ,
                                chunk
                        );
                    }

                } catch (Throwable error) {

                    RegionSystem.LOGGER.debug(
                            "Region chunk preload failed {} {}",
                            chunkX,
                            chunkZ,
                            error
                    );
                }

                budget--;
            }
        }

        if (region.isComplete()) {

            region.state(
                    RegionState.READY
            );

        } else {

            region.state(
                    RegionState.ACTIVE
            );
        }
    }

    private void evict() {

        if (cache.size()
                <= cache.maximum()) {

            return;
        }

        Set<RegionPos>
                protectedRegions =
                new HashSet<>();

        for (ServerLevel level :
                server.getAllLevels()) {

            for (ServerPlayer player :
                    level.players()) {

                int chunkX =
                        player.blockPosition()
                                .getX() >> 4;

                int chunkZ =
                        player.blockPosition()
                                .getZ() >> 4;

                RegionPos center =
                        RegionPos.fromChunk(
                                chunkX,
                                chunkZ
                        );

                for (int dz =
                        -REGION_RADIUS;
                     dz <= REGION_RADIUS;
                     dz++) {

                    for (int dx =
                            -REGION_RADIUS;
                         dx <= REGION_RADIUS;
                         dx++) {

                        protectedRegions.add(
                                new RegionPos(
                                        center.x() + dx,
                                        center.z() + dz
                                )
                        );
                    }
                }
            }
        }

        while (cache.size()
                > cache.maximum()) {

            Region old =
                    cache.oldestOutside(
                            protectedRegions
                    );

            if (old == null) {
                break;
            }

            old.state(
                    RegionState.EVICTING
            );

            cache.remove(
                    old.pos()
            );
        }
    }

    public RegionCache cache() {
        return cache;
    }
}
