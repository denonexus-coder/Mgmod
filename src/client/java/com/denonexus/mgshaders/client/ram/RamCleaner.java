package com.denonexus.mgshaders.client.ram;

import com.denonexus.mgshaders.MGShaders;
import com.denonexus.mgshaders.client.profile.ChunkProfiler;
import com.denonexus.mgshaders.client.profile.PipelineProfiler;

/**
 * Limpeza periódica não-bloqueante.
 *
 * NÃO chama System.gc() — causaria pausa stop-the-world visível como stutter.
 * Em vez disso:
 *   - encolhe listas internas dos profilers (ChunkProfiler, PipelineProfiler)
 *   - apenas reporta pressão de heap pra log
 *
 * Roda no client tick via ClientTickEvents (main thread), mas o trabalho é
 * O(1) exceto quando os limites são excedidos.
 */
public final class RamCleaner {

    private static final long INTERVAL_NS = 30_000_000_000L;   // 30 s
    private static final long HEAP_WARN_RATIO_PCT = 85L;       // % do heap máx

    private static long lastNs = 0L;
    private static volatile long runs = 0L;
    private static volatile long lastFreedBytes = 0L;
    private static volatile long lastUsedBefore = 0L;
    private static volatile long lastUsedAfter  = 0L;

    private RamCleaner() {}

    public static void tick() {
        long now = System.nanoTime();
        if (now - lastNs < INTERVAL_NS) return;
        lastNs = now;
        clean();
    }

    private static void clean() {
        Runtime rt = Runtime.getRuntime();
        long usedBefore = rt.totalMemory() - rt.freeMemory();

        // 1. Encolhe listas ilimitadas do ChunkProfiler
        ChunkProfiler.trim();

        // 2. Encolhe o mapa do PipelineProfiler
        PipelineProfiler.trim();

        // 3. Relatório (não força GC)
        long usedAfter = rt.totalMemory() - rt.freeMemory();
        long max = rt.maxMemory();

        lastUsedBefore = usedBefore;
        lastUsedAfter  = usedAfter;
        lastFreedBytes = Math.max(0L, usedBefore - usedAfter);
        runs++;

        long usedPct = max > 0 ? (usedAfter * 100L / max) : 0L;
        if (usedPct >= HEAP_WARN_RATIO_PCT) {
            MGShaders.LOGGER.warn(
                "[MGShaders] ram clean #{} — heap {} MB / {} MB ({}%) — pressao alta",
                runs,
                usedAfter  / (1024 * 1024),
                max        / (1024 * 1024),
                usedPct);
        } else {
            MGShaders.LOGGER.info(
                "[MGShaders] ram clean #{} — heap {} MB / {} MB ({}%), freed\u2248{} KB",
                runs,
                usedAfter  / (1024 * 1024),
                max        / (1024 * 1024),
                usedPct,
                lastFreedBytes / 1024);
        }
    }

    public static long runs()           { return runs; }
    public static long lastFreedBytes() { return lastFreedBytes; }
    public static long lastUsedBefore() { return lastUsedBefore; }
    public static long lastUsedAfter()  { return lastUsedAfter; }
}
