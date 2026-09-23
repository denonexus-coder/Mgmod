package com.denonexus.mgshaders.client.profile;

import com.denonexus.mgshaders.MGShaders;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Profiler de compilacao de chunks.
 *
 * Mede tempos em nanosegundos. Agrega. Dumpa JSON em
 * /sdcard/MG/mgshaders_chunk_profile.json a cada 30s e no shutdown.
 *
 * Thread-safe: mixins rodam em threads de trabalho paralelas.
 * Nao otimiza nada. Apenas mede.
 */
public final class ChunkProfiler {

    private static final DateTimeFormatter ISO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                             .withZone(ZoneOffset.UTC);

    private static final long DUMP_INTERVAL_NS = 30L * 1_000_000_000L;

    private static final Path OUT =
            Paths.get("/sdcard/MG/mgshaders_chunk_profile.json");

    private static final List<Long> queueWaitNs = new ArrayList<>();
    private static final List<Long> compileNs   = new ArrayList<>();
    private static final List<Long> totalNs     = new ArrayList<>();
    private static final List<Long> uploadNs    = new ArrayList<>();

    private static final AtomicLong chunksCompiled = new AtomicLong(0);
    private static final AtomicLong chunksFailed   = new AtomicLong(0);
    private static final AtomicLong lastDumpNs     = new AtomicLong(System.nanoTime());
    private static final long sessionStartNs       = System.nanoTime();
    private static final long sessionStartMs       = System.currentTimeMillis();

    private static final ThreadLocal<Long> T_UPLOAD = new ThreadLocal<>();

    public static void markUploadStart() {
        T_UPLOAD.set(System.nanoTime());
    }

    public static long markUploadEnd() {
        Long start = T_UPLOAD.get();
        if (start == null) return -1L;
        T_UPLOAD.remove();
        return System.nanoTime() - start;
    }

    public static void recordUpload(long ns) {
        if (ns <= 0) return;
        synchronized (ChunkProfiler.class) {
            uploadNs.add(ns);
        }
    }

    // Rastreia threads únicas que compilam
    private static final java.util.Set<Long> activeThreads =
            java.util.Collections.synchronizedSet(new java.util.HashSet<>());
    private static final java.util.Set<String> uniqueThreadNames =
            java.util.Collections.synchronizedSet(new java.util.HashSet<>());

    // Outliers (chunks que levam > threshold)
    private static final long OUTLIER_THRESHOLD_NS = 500_000_000L; // 500ms
    private static final java.util.List<String> outliers =
            java.util.Collections.synchronizedList(new java.util.ArrayList<>());

    public static void recordThread() {
        activeThreads.add(Thread.currentThread().threadId());
        uniqueThreadNames.add(Thread.currentThread().getName());
    }

    public static void recordOutlier(String pos, long ns) {
        if (ns < OUTLIER_THRESHOLD_NS) return;
        outliers.add(String.format("%s took %.0fms", pos, ns / 1_000_000.0));
        // Limita a 50 outliers na lista
        if (outliers.size() > 50) outliers.remove(0);
    }

    private ChunkProfiler() {}

    public static void record(long queueWait, long compile, long total, boolean success) {
        if (success) chunksCompiled.incrementAndGet();
        else chunksFailed.incrementAndGet();

        synchronized (ChunkProfiler.class) {
            if (queueWait >= 0) queueWaitNs.add(queueWait);
            if (compile   >= 0) compileNs.add(compile);
            if (total     >= 0) totalNs.add(total);
        }

        long now = System.nanoTime();
        long last = lastDumpNs.get();
        if (now - last > DUMP_INTERVAL_NS && lastDumpNs.compareAndSet(last, now)) {
            dump("periodic");
        }
    }

    public static void flush() {
        dump("flush");
    }

    private static long[] snapshot(List<Long> src) {
        synchronized (ChunkProfiler.class) {
            long[] out = new long[src.size()];
            for (int i = 0; i < src.size(); i++) out[i] = src.get(i);
            return out;
        }
    }

    private static Stats compute(long[] samples) {
        if (samples.length == 0) return new Stats(0, 0, 0, 0, 0, 0, 0);
        long[] sorted = samples.clone();
        Arrays.sort(sorted);
        long sum = 0;
        for (long v : sorted) sum += v;
        return new Stats(
            sorted.length,
            sum / sorted.length,
            sorted[0],
            sorted[sorted.length - 1],
            sorted[(int)(sorted.length * 0.50)],
            sorted[Math.min((int)(sorted.length * 0.95), sorted.length - 1)],
            sorted[Math.min((int)(sorted.length * 0.99), sorted.length - 1)]
        );
    }

    private record Stats(long count, long avg, long min, long max,
                         long p50, long p95, long p99) {
        long ms(long ns) { return ns / 1_000_000L; }
    }

    private static void dump(String reason) {
        Stats sQ = compute(snapshot(queueWaitNs));
        Stats sC = compute(snapshot(compileNs));
        Stats sT = compute(snapshot(totalNs));
        Stats sU = compute(snapshot(uploadNs));

        long sessionDurNs = System.nanoTime() - sessionStartNs;
        double sessionSec = sessionDurNs / 1_000_000_000.0;
        long compiled = chunksCompiled.get();
        long failed = chunksFailed.get();
        double rate = sessionSec > 0 ? compiled / sessionSec : 0.0;

        StringBuilder sb = new StringBuilder(2048);
        sb.append("{\n");
        sb.append("  \"session_id\": ").append(sessionStartMs).append(",\n");
        sb.append("  \"reason\": \"").append(reason).append("\",\n");
        sb.append("  \"dump_at\": \"").append(ISO.format(Instant.now())).append("\",\n");
        sb.append("  \"session_start\": \"")
          .append(ISO.format(Instant.ofEpochMilli(sessionStartMs))).append("\",\n");
        sb.append("  \"session_duration_sec\": ")
          .append(String.format("%.2f", sessionSec)).append(",\n");
        sb.append("  \"counters\": {\n");
        sb.append("    \"chunks_compiled\": ").append(compiled).append(",\n");
        sb.append("    \"chunks_failed\": ").append(failed).append(",\n");
        sb.append("    \"rate_chunks_per_sec\": ")
          .append(String.format("%.3f", rate)).append("\n");
        sb.append("  },\n");
        sb.append("  \"queue_wait_ms\": ").append(json(sQ)).append(",\n");
        sb.append("  \"compile_ms\":    ").append(json(sC)).append(",\n");
        sb.append("  \"total_ms\":      ").append(json(sT)).append(",\n");
        sb.append("  \"upload_ms\":     ").append(json(sU)).append(",\n");
        sb.append("  \"threads\": {\n");
        sb.append("    \"unique_count\": ").append(uniqueThreadNames.size()).append(",\n");
        sb.append("    \"names\": [");
        boolean first = true;
        for (String n : uniqueThreadNames) {
            if (!first) sb.append(",");
            sb.append("\"").append(n).append("\"");
            first = false;
        }
        sb.append("]\n");
        sb.append("  },\n");
        sb.append("  \"outliers\": [");
        first = true;
        for (String o : outliers) {
            if (!first) sb.append(",");
            sb.append("\"").append(o).append("\"");
            first = false;
        }
        sb.append("]\n");
        sb.append("}\n");

        try {
            Files.createDirectories(OUT.getParent());
            Files.writeString(OUT, sb.toString(), StandardCharsets.UTF_8);
            MGShaders.LOGGER.info(
                "[MGShaders] chunk profile ({}) — {} chunks, {} c/s",
                reason, compiled, String.format("%.2f", rate));
            MGShaders.LOGGER.info("[MGShaders]   compile_ms  avg={} p95={} max={}",
                sC.ms(sC.avg()), sC.ms(sC.p95()), sC.ms(sC.max()));
            MGShaders.LOGGER.info("[MGShaders]   total_ms    avg={} p95={} max={}",
                sT.ms(sT.avg()), sT.ms(sT.p95()), sT.ms(sT.max()));
            MGShaders.LOGGER.info("[MGShaders]   upload_ms   avg={} p95={} max={}",
                sU.ms(sU.avg()), sU.ms(sU.p95()), sU.ms(sU.max()));
        } catch (IOException e) {
            MGShaders.LOGGER.warn("[MGShaders] profile write failed: {}", e.getMessage());
        }
    }

    private static String json(Stats s) {
        return String.format(
            "{\"count\":%d,\"avg\":%.2f,\"min\":%.2f,\"max\":%.2f," +
            "\"p50\":%.2f,\"p95\":%.2f,\"p99\":%.2f}",
            s.count(),
            (double)s.ms(s.avg()), (double)s.ms(s.min()), (double)s.ms(s.max()),
            (double)s.ms(s.p50()), (double)s.ms(s.p95()), (double)s.ms(s.p99()));
    }

    /** Limita o tamanho das listas — chamado periodicamente pelo RamCleaner. */
    public static void trim() {
        final int MAX = 4096;
        synchronized (ChunkProfiler.class) {
            if (queueWaitNs.size() > MAX) queueWaitNs.subList(0, queueWaitNs.size() - MAX).clear();
            if (compileNs.size()   > MAX) compileNs.subList(0, compileNs.size() - MAX).clear();
            if (totalNs.size()     > MAX) totalNs.subList(0, totalNs.size() - MAX).clear();
            if (uploadNs.size()    > MAX) uploadNs.subList(0, uploadNs.size() - MAX).clear();
        }
        if (outliers.size() > 50) {
            synchronized (outliers) {
                while (outliers.size() > 50) outliers.remove(0);
            }
        }
    }
}
