package com.denonexus.mgshaders.region.server;

import com.denonexus.mgshaders.region.Region;
import com.denonexus.mgshaders.region.RegionCache;
import com.denonexus.mgshaders.region.RegionPos;
import com.denonexus.mgshaders.region.RegionState;
import com.denonexus.mgshaders.region.RegionSystem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import java.util.*;

public final class RegionServerManager {

    /*
     * 1 Region = 4x4 chunks = 16 chunks.
     *
     * We deliberately keep the active window small:
     * current Region + predicted next Region.
     *
     * This avoids turning the tablet RAM into a huge chunk cache.
     */
    public static final int REGION_SIZE = 4;
    public static final int CHUNKS_PER_REGION = 16;

    /*
     * Region radius 0 means exactly the current 4x4 Region.
     * The predictor adds the next Region separately.
     */
    public static final int REGION_RADIUS = 0;

    /*
     * Logical RAM Region cache.
     */
    public static final int CACHE_SIZE = 24;

    /*
     * Maximum new FULL chunk futures scheduled per server pass.
     */
    public static final int CHUNKS_PER_TICK = 2;

    /*
     * Radius 0 means exactly one chunk per ticket.
     *
     * The ticket is non-persistent and therefore does not pollute
     * level.dat forced chunks.
     */
    public static final int TICKET_RADIUS = 0;

    /*
     * Predict roughly half a second ahead.
     */
    private static final int LOOKAHEAD_TICKS = 10;

    /*
     * No timeout: the ticket remains until we explicitly remove it.
     */
    private static final TicketType<ChunkPos> REGION_TICKET =
            TicketType.create(
                    "mgshaders_region",
                    Comparator.comparingLong(ChunkPos::toLong)
            );

    private final MinecraftServer server;

    private final Map<ServerLevel, RegionCache> caches =
            new IdentityHashMap<>();

    private final Map<ServerLevel, Set<Long>> pinned =
            new IdentityHashMap<>();

    private final Map<UUID, Motion> motion =
            new HashMap<>();

    private boolean running;
    private long tick;

    private static final class Motion {
        double x;
        double z;

        double vx;
        double vz;

        boolean initialized;
    }

    private record Request(
            int x,
            int z,
            double score
    ) {}

    public RegionServerManager(
            MinecraftServer server
    ) {
        this.server = server;
    }

    public void start() {
        running = true;

        RegionSystem.LOGGER.info(
                "RegionSystem: 4x4 Region scheduler ONLINE"
        );

        RegionSystem.LOGGER.info(
                "RegionSystem: vanilla ticket + ChunkMap pipeline ONLINE"
        );

        RegionSystem.LOGGER.info(
                "RegionSystem: predictive preload ONLINE"
        );
    }

    public void stop() {
        running = false;

        for (Map.Entry<ServerLevel, Set<Long>> entry :
                pinned.entrySet()) {

            ServerChunkCache source =
                    entry.getKey().getChunkSource();

            for (long key : entry.getValue()) {
                source.removeTicketWithRadius(
                        REGION_TICKET,
                        new ChunkPos(key),
                        TICKET_RADIUS
                );
            }
        }

        pinned.clear();

        for (RegionCache cache : caches.values()) {
            cache.clear();
        }

        caches.clear();
        motion.clear();

        RegionSystem.LOGGER.info(
                "RegionSystem: stopped and vanilla tickets released"
        );
    }

    public void onPlayerJoin(
            ServerPlayer player
    ) {
        motion.remove(player.getUUID());
    }

    public void onPlayerLeave(
            ServerPlayer player
    ) {
        motion.remove(player.getUUID());
    }

    public void tick() {

        if (!running) {
            return;
        }

        tick++;

        /*
         * Run every second server tick.
         * This reduces scheduler overhead while retaining prediction.
         */
        if ((tick & 1L) != 0L) {
            return;
        }

        for (ServerLevel level :
                server.getAllLevels()) {

            RegionCache cache =
                    caches.computeIfAbsent(
                            level,
                            k -> new RegionCache(CACHE_SIZE)
                    );

            Set<Long> wanted =
                    Collections.newSetFromMap(
                            new HashMap<>()
                    );

            List<Request> requests =
                    new ArrayList<>();

            int budget = CHUNKS_PER_TICK;

            for (ServerPlayer player :
                    level.players()) {

                Motion m =
                        motion.computeIfAbsent(
                                player.getUUID(),
                                k -> new Motion()
                        );

                updateMotion(
                        player,
                        m
                );

                ChunkPos currentChunk =
                        player.chunkPosition();

                ChunkPos predictedChunk =
                        predictedChunk(
                                player,
                                m
                        );

                RegionPos currentRegion =
                        RegionPos.fromChunk(
                                currentChunk.x,
                                currentChunk.z
                        );

                RegionPos predictedRegion =
                        RegionPos.fromChunk(
                                predictedChunk.x,
                                predictedChunk.z
                        );

                /*
                 * Current Region.
                 */
                scheduleRegion(
                        cache,
                        currentRegion,
                        player,
                        currentChunk,
                        wanted,
                        requests
                );

                /*
                 * Predicted Region.
                 */
                if (!predictedRegion.equals(
                        currentRegion
                )) {
                    scheduleRegion(
                            cache,
                            predictedRegion,
                            player,
                            currentChunk,
                            wanted,
                            requests
                    );
                }
            }

            /*
             * Real vanilla DistanceManager ticket path.
             */
            updateTickets(
                    level,
                    wanted
            );

            /*
             * Closest / forward chunks first.
             */
            requests.sort(
                    Comparator.comparingDouble(
                            Request::score
                    )
            );

            for (Request request :
                    requests) {

                if (budget <= 0) {
                    break;
                }

                long key =
                        ChunkPos.asLong(
                                request.x,
                                request.z
                        );

                if (cacheContains(
                        level,
                        key
                )) {
                    continue;
                }

                budget--;

                ServerChunkCache source =
                        level.getChunkSource();

                /*
                 * IMPORTANT:
                 *
                 * Do NOT call getChunk(...FULL,true)
                 * synchronously here.
                 *
                 * getChunkFuture() lets vanilla schedule
                 * generation/loading through ChunkMap.
                 */
                source.getChunkFuture(
                        request.x,
                        request.z,
                        ChunkStatus.FULL,
                        true
                ).thenAccept(result -> {

                    if (result == null ||
                            !result.isSuccess()) {
                        return;
                    }

                    ChunkAccess chunk =
                            result.orElse(null);

                    if (chunk == null) {
                        return;
                    }

                    RegionPos regionPos =
                            RegionPos.fromChunk(
                                    request.x,
                                    request.z
                            );

                    cache.getOrCreate(
                            regionPos
                    ).put(
                            request.x,
                            request.z,
                            chunk
                    );
                });
            }

            evict(
                    level,
                    wanted
            );
        }
    }

    private void scheduleRegion(
            RegionCache cache,
            RegionPos region,
            ServerPlayer player,
            ChunkPos playerChunk,
            Set<Long> wanted,
            List<Request> requests
    ) {

        Region cached =
                cache.getOrCreate(region);

        cached.touch(tick);

        if (cached.state() ==
                RegionState.EMPTY ||
                cached.state() ==
                RegionState.ACTIVE) {

            cached.state(
                    RegionState.PRELOADING
            );
        }

        for (int z = 0;
             z < REGION_SIZE;
             z++) {

            for (int x = 0;
                 x < REGION_SIZE;
                 x++) {

                int chunkX =
                        region.minChunkX() + x;

                int chunkZ =
                        region.minChunkZ() + z;

                long key =
                        ChunkPos.asLong(
                                chunkX,
                                chunkZ
                        );

                wanted.add(key);

                int distance =
                        Math.abs(
                                chunkX -
                                        playerChunk.x
                        )
                        +
                        Math.abs(
                                chunkZ -
                                        playerChunk.z
                        );

                double forward =
                        aheadScore(
                                player,
                                chunkX,
                                chunkZ
                        );

                /*
                 * Lower score = higher priority.
                 *
                 * Distance matters, but chunks in the
                 * movement direction are promoted.
                 */
                double score =
                        distance -
                                forward * 4.0;

                requests.add(
                        new Request(
                                chunkX,
                                chunkZ,
                                score
                        )
                );
            }
        }
    }

    private void updateTickets(
            ServerLevel level,
            Set<Long> wanted
    ) {

        Set<Long> old =
                pinned.computeIfAbsent(
                        level,
                        k -> new HashSet<>()
                );

        /*
         * Add real vanilla loading tickets.
         */
        for (long key : wanted) {

            if (old.add(key)) {

                level.getChunkSource()
                        .addTicketWithRadius(
                                REGION_TICKET,
                                new ChunkPos(key),
                                TICKET_RADIUS
                        );
            }
        }

        /*
         * Remove tickets that are no longer
         * in the current/predicted window.
         *
         * Vanilla then handles normal lifecycle,
         * saving and unloading.
         */
        Iterator<Long> iterator =
                old.iterator();

        while (iterator.hasNext()) {

            long key =
                    iterator.next();

            if (!wanted.contains(key)) {

                level.getChunkSource()
                        .removeTicketWithRadius(
                                REGION_TICKET,
                                new ChunkPos(key),
                                TICKET_RADIUS
                        );

                iterator.remove();
            }
        }
    }

    private boolean cacheContains(
            ServerLevel level,
            long key
    ) {

        RegionCache cache =
                caches.get(level);

        if (cache == null) {
            return false;
        }

        ChunkPos pos =
                new ChunkPos(key);

        Region region =
                cache.get(
                        RegionPos.fromChunk(
                                pos.x,
                                pos.z
                        )
                );

        return region != null &&
                region.get(
                        pos.x,
                        pos.z
                ) != null;
    }

    private void evict(
            ServerLevel level,
            Set<Long> wanted
    ) {

        RegionCache cache =
                caches.get(level);

        if (cache == null) {
            return;
        }

        Set<RegionPos> protectedRegions =
                new HashSet<>();

        for (long key : wanted) {

            ChunkPos pos =
                    new ChunkPos(key);

            protectedRegions.add(
                    RegionPos.fromChunk(
                            pos.x,
                            pos.z
                    )
            );
        }

        while (cache.size() >
                CACHE_SIZE) {

            Region oldest =
                    cache.oldestOutside(
                            protectedRegions
                    );

            if (oldest == null) {
                break;
            }

            oldest.state(
                    RegionState.EVICTING
            );

            cache.remove(
                    oldest.pos()
            );
        }
    }

    private static void updateMotion(
            ServerPlayer player,
            Motion motion
    ) {

        double x =
                player.getX();

        double z =
                player.getZ();

        if (!motion.initialized) {

            motion.x = x;
            motion.z = z;

            motion.vx =
                    player.getDeltaMovement().x;

            motion.vz =
                    player.getDeltaMovement().z;

            motion.initialized = true;

            return;
        }

        double measuredVx =
                x - motion.x;

        double measuredVz =
                z - motion.z;

        /*
         * Exponential smoothing.
         *
         * This prevents a single tick of jitter
         * from redirecting the preload queue.
         */
        motion.vx =
                motion.vx * 0.65 +
                        measuredVx * 0.35;

        motion.vz =
                motion.vz * 0.65 +
                        measuredVz * 0.35;

        motion.x = x;
        motion.z = z;
    }

    private static ChunkPos predictedChunk(
            ServerPlayer player,
            Motion motion
    ) {

        double speed =
                Math.hypot(
                        motion.vx,
                        motion.vz
                );

        double x =
                player.getX()
                        +
                        motion.vx *
                                LOOKAHEAD_TICKS;

        double z =
                player.getZ()
                        +
                        motion.vz *
                                LOOKAHEAD_TICKS;

        /*
         * Look direction is used as an additional
         * forward bias.
         */
        double yaw =
                Math.toRadians(
                        player.getYRot()
                );

        double lookX =
                -Math.sin(yaw);

        double lookZ =
                Math.cos(yaw);

        double lookBoost =
                Math.min(
                        8.0,
                        speed * 3.0
                );

        x += lookX * lookBoost;
        z += lookZ * lookBoost;

        return new ChunkPos(
                ((int)Math.floor(x)) >> 4,
                ((int)Math.floor(z)) >> 4
        );
    }

    private static double aheadScore(
            ServerPlayer player,
            int chunkX,
            int chunkZ
    ) {

        double dx =
                (chunkX + 0.5) * 16.0
                        - player.getX();

        double dz =
                (chunkZ + 0.5) * 16.0
                        - player.getZ();

        double length =
                Math.hypot(
                        dx,
                        dz
                );

        if (length < 0.000001) {
            return 1.0;
        }

        double yaw =
                Math.toRadians(
                        player.getYRot()
                );

        double lookX =
                -Math.sin(yaw);

        double lookZ =
                Math.cos(yaw);

        return (
                dx * lookX +
                        dz * lookZ
        ) / length;
    }

    public RegionCache cache(
            ServerLevel level
    ) {

        return caches.computeIfAbsent(
                level,
                k -> new RegionCache(
                        CACHE_SIZE
                )
        );
    }

    public RegionCache cache() {

        return caches.values()
                .stream()
                .findFirst()
                .orElse(null);
    }
}
