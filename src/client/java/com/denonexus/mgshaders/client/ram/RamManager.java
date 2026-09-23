package com.denonexus.mgshaders.client.ram;

import com.denonexus.mgshaders.MGShaders;
import com.denonexus.mgshaders.client.profile.ChunkProfiler;
import com.denonexus.mgshaders.client.profile.PipelineProfiler;

/**
 * Gestor de RAM não-bloqueante v3.1.
 *
 * REGRAS:
 *   1. NUNCA System.gc()
 *   2. Só encolher estruturas próprias (ChunkProfiler, PipelineProfiler)
 *   3. Reportar RSS/PSS/Swap reais de /proc
 *   4. Se heap > 80%, apenas logar
 *
 * v3.1: freed = peakRss - currentRss (o "quanto caiu do pico", não acumulativo).
 */
public final class RamManager {

    private static final long SAMPLE_NS = 5_000_000_000L;
    private static final long CLEAN_NS  = 30_000_000_000L;
    private static final long HEAP_WARN_PCT = 80L;
    private static final long CG_WARN_PCT   = 85L;

    // estado exposto pro HUD
    private static volatile long rssKb, pssKb, swapKb;
    private static volatile long javaUsedKb, javaMaxKb;
    private static volatile long peakRssKb;

    // internos
    private static long lastSampleNs, lastCleanNs, cleanRuns;
    private static long rssAtCleanStart;

    // caps adaptativos
    private static volatile int capChunk = 4096;
    private static volatile int capPipeline = 8192;

    private RamManager() {}

    public static void tick() {
        long now = System.nanoTime();
        if (now - lastSampleNs >= SAMPLE_NS) { lastSampleNs = now; sample(); }
        if (now - lastCleanNs  >= CLEAN_NS)  { lastCleanNs  = now; clean();  }
    }

    private static void sample() {
        RamSnapshot s = RamSnapshot.read();
        rssKb  = s.rssKb;
        pssKb  = s.hasPss() ? s.pssKb : s.rssKb;
        swapKb = s.swapKb;
        if (rssKb > peakRssKb) peakRssKb = rssKb;

        Runtime rt = Runtime.getRuntime();
        javaUsedKb = (rt.totalMemory() - rt.freeMemory()) / 1024;
        javaMaxKb  = rt.maxMemory() / 1024;

        adaptCaps();
        LeakGuard.sample("chunkProfiler",    ChunkProfiler.estimatedBytes());
        LeakGuard.sample("pipelineProfiler", PipelineProfiler.estimatedBytes());

        long usedPct = javaMaxKb > 0 ? (javaUsedKb * 100 / javaMaxKb) : 0;
        if (usedPct >= HEAP_WARN_PCT) {
            MGShaders.LOGGER.warn(
                "[MGShaders] heap {}% ({}/{} MB) — {}",
                usedPct, javaUsedKb / 1024, javaMaxKb / 1024, s.shortSummary());
        }
        if (s.hasCgroup()) {
            long cgPct = s.cgroupUsageKb * 100 / s.cgroupLimitKb;
            if (cgPct >= CG_WARN_PCT) {
                MGShaders.LOGGER.warn(
                    "[MGShaders] cgroup {}% ({} / {} MB) — {}",
                    cgPct, s.cgroupUsageKb / 1024, s.cgroupLimitKb / 1024, s.shortSummary());
            }
        }
    }

    private static void clean() {
        long rssBefore = rssKb > 0 ? rssKb : RamSnapshot.read().rssKb;
        long pssBefore = pssKb > 0 ? pssKb : rssBefore;

        ChunkProfiler.setCap(capChunk);
        PipelineProfiler.setCap(capPipeline);
        ChunkProfiler.trim();
        PipelineProfiler.trim();

        cleanRuns++;
        long freed = Math.max(0L, peakRssKb - rssBefore);

        // loga só nas duas primeiras vezes ou quando o delta for grande
        long delta = rssAtCleanStart > 0 ? Math.abs(rssAtCleanStart - rssBefore) : 0;
        if (delta > 512 || cleanRuns <= 2) {
            MGShaders.LOGGER.info(
                "[MGShaders] ram clean #{} — rss={}M peak={}M (freed={}M), caps={}/{}, pss={}M",
                cleanRuns, rssBefore / 1024, peakRssKb / 1024, freed / 1024,
                capChunk, capPipeline, pssBefore / 1024);
        }
        rssAtCleanStart = rssBefore;
    }

    private static void adaptCaps() {
        if (javaMaxKb == 0) return;
        long pct = javaUsedKb * 100 / javaMaxKb;
        if      (pct >= 80) { capChunk = 1024; capPipeline = 2048;  }
        else if (pct >= 60) { capChunk = 2048; capPipeline = 4096;  }
        else if (pct >= 40) { capChunk = 4096; capPipeline = 8192;  }
        else                { capChunk = 8192; capPipeline = 16384; }
    }

    // ── API pro HUD ──
    public static long rssKb()       { return rssKb; }
    public static long pssKb()       { return pssKb; }
    public static long swapKb()      { return swapKb; }
    public static long javaUsedKb()  { return javaUsedKb; }
    public static long javaMaxKb()   { return javaMaxKb; }
    public static long peakRssKb()   { return peakRssKb; }

    /** Freed = quanto o RSS já caiu do pico desde o boot. */
    public static long javaFreedKb() {
        return Math.max(0L, peakRssKb - rssKb);
    }

    public static String heapShort() { return (javaUsedKb / 1024) + "M/" + (javaMaxKb / 1024) + "M"; }

    public static String freedShort() {
        long kb = javaFreedKb();
        if (kb < 1024) return kb + "K";
        return (kb / 1024) + "M";
    }

    public static int capChunk()    { return capChunk; }
    public static int capPipeline() { return capPipeline; }

    public static void reset() {
        peakRssKb = 0; cleanRuns = 0; rssAtCleanStart = 0;
        LeakGuard.reset();
    }
}
