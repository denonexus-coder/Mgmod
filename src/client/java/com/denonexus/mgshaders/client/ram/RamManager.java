package com.denonexus.mgshaders.client.ram;

import com.denonexus.mgshaders.MGShaders;
import com.denonexus.mgshaders.client.profile.ChunkProfiler;
import com.denonexus.mgshaders.client.profile.PipelineProfiler;

/**
 * Gestor de RAM não-bloqueante.
 *
 * REGRAS (não violar):
 *   1. NUNCA chamar System.gc() nem Runtime.gc() — pausa stop-the-world
 *      de 150-400 ms no GE8320 + Cortex-A55, e ainda pior quando há swap ativo.
 *   2. Só encolher estruturas que nós controlamos (ChunkProfiler, PipelineProfiler).
 *   3. Reportar RSS/PSS/Swap reais do /proc — não estimativas.
 *   4. Se o heap Java passar de 85%, apenas logar. Quem decide GC é o ART.
 */
public final class RamManager {

    private static final long SAMPLE_INTERVAL_NS = 5_000_000_000L;      // 5 s
    private static final long CLEAN_INTERVAL_NS  = 30_000_000_000L;     // 30 s
    private static final long HEAP_WARN_PCT      = 85L;

    // ── estado exposto pro HUD ──
    private static volatile long rssKb       = 0;
    private static volatile long swapKb      = 0;
    private static volatile long javaUsedKb  = 0;
    private static volatile long javaMaxKb   = 0;
    private static volatile long javaFreedKb = 0;      // acumulado desta sessão

    // ── internos ──
    private static long lastSampleNs = 0;
    private static long lastCleanNs  = 0;
    private static long peakUsedKb   = 0;
    private static long cleanRuns    = 0;

    private RamManager() {}

    public static void tick() {
        long now = System.nanoTime();

        if (now - lastSampleNs >= SAMPLE_INTERVAL_NS) {
            lastSampleNs = now;
            sample();
        }
        if (now - lastCleanNs >= CLEAN_INTERVAL_NS) {
            lastCleanNs = now;
            clean();
        }
    }

    private static void sample() {
        RamSnapshot s = RamSnapshot.read();
        rssKb  = s.rssKb;
        swapKb = s.swapKb;

        Runtime rt = Runtime.getRuntime();
        long used  = rt.totalMemory() - rt.freeMemory();
        long max   = rt.maxMemory();
        javaUsedKb = used / 1024;
        javaMaxKb  = max  / 1024;

        if (javaUsedKb > peakUsedKb) peakUsedKb = javaUsedKb;

        // Guarda-leak dos mapas que controlamos
        LeakGuard.sample("chunkProfiler",  ChunkProfiler.estimatedBytes());
        LeakGuard.sample("pipelineProfiler", PipelineProfiler.estimatedBytes());

        long usedPct = javaMaxKb > 0 ? (javaUsedKb * 100 / javaMaxKb) : 0;
        if (usedPct >= HEAP_WARN_PCT) {
            MGShaders.LOGGER.warn(
                "[MGShaders] ram: heap {}/{} MB ({}%) rss={} MB swap={} MB",
                javaUsedKb / 1024, javaMaxKb / 1024, usedPct,
                rssKb / 1024, swapKb / 1024);
        }
    }

    private static void clean() {
        long beforeKb = javaUsedKb;

        // encolhe só o que é nosso
        ChunkProfiler.trim();
        PipelineProfiler.trim();

        // amostra nova (sem forçar GC)
        RamSnapshot s = RamSnapshot.read();
        rssKb  = s.rssKb;
        swapKb = s.swapKb;

        Runtime rt = Runtime.getRuntime();
        long usedAfterKb = (rt.totalMemory() - rt.freeMemory()) / 1024;
        javaUsedKb = usedAfterKb;
        if (javaUsedKb > peakUsedKb) peakUsedKb = javaUsedKb;

        long freed = Math.max(0, beforeKb - usedAfterKb);
        javaFreedKb += freed;
        cleanRuns++;

        // Só loga quando liberou algo significativo (>256 KB)
        if (freed > 256) {
            MGShaders.LOGGER.info(
                "[MGShaders] ram clean #{} — freed {} KB, heap {}/{} MB, rss {} MB, swap {} MB",
                cleanRuns, freed, javaUsedKb / 1024, javaMaxKb / 1024,
                rssKb / 1024, swapKb / 1024);
        }
    }

    public static long rssKb()       { return rssKb; }
    public static long swapKb()      { return swapKb; }
    public static long javaUsedKb()  { return javaUsedKb; }
    public static long javaMaxKb()   { return javaMaxKb; }
    public static long javaFreedKb() { return javaFreedKb; }
    public static long peakUsedKb()  { return peakUsedKb; }

    /** Formata "213M/550M" */
    public static String heapShort() {
        return (javaUsedKb / 1024) + "M/" + (javaMaxKb / 1024) + "M";
    }

    /** Formata "+14M" (liberado desde início da sessão) */
    public static String freedShort() {
        long kb = javaFreedKb;
        if (kb < 1024) return kb + "K";
        return (kb / 1024) + "M";
    }
}
