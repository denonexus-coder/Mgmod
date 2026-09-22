package com.denonexus.mgshaders.client.profile;

import com.denonexus.mgshaders.MGShaders;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Mede as fases entre chunk loaded e meshing done.
 *
 * Correlaciona por SectionPos — cada chunk tem seu registro.
 * Dumpa no shutdown em /sdcard/MG/mgshaders_pipeline.json
 */
public final class PipelineProfiler {

    private static final Path OUT =
            Paths.get("/sdcard/MG/mgshaders_pipeline.json");

    // Mapa SectionPos (long) -> timestamps de cada fase
    private static final java.util.Map<Long, ChunkPhases> chunks =
            new java.util.concurrent.ConcurrentHashMap<>();

    private static long maxQueueDelayNs = 0;
    private static long maxLightDelayNs = 0;

    public static class ChunkPhases {
        public long loadedNs = 0;      // quando o chunk chegou do disco
        public long lightReadyNs = 0;  // quando o light engine terminou
        public long compileStartNs = 0; // quando SectionCompiler.compile comecou
        public long compileEndNs = 0;   // quando terminou
        public long uploadedNs = 0;     // quando uploadAllPendingUploads processou

        public boolean hasLoad()    { return loadedNs > 0; }
        public boolean hasLight()   { return lightReadyNs > 0; }
        public boolean hasCompile() { return compileEndNs > 0; }
        public boolean hasUpload()  { return uploadedNs > 0; }
    }

    private PipelineProfiler() {}

    public static ChunkPhases get(long posKey) {
        return chunks.computeIfAbsent(posKey, k -> new ChunkPhases());
    }

    public static void flush() {
        if (chunks.isEmpty()) return;

        long now = System.nanoTime();

        List<Long> loadToLight = new ArrayList<>();
        List<Long> lightToCompile = new ArrayList<>();
        List<Long> compileDuration = new ArrayList<>();
        List<Long> compileToUpload = new ArrayList<>();

        for (ChunkPhases p : chunks.values()) {
            if (p.hasLight() && p.hasLoad() && p.loadedNs < p.lightReadyNs)
                loadToLight.add(p.lightReadyNs - p.loadedNs);
            if (p.hasCompile() && p.hasLight() && p.lightReadyNs < p.compileStartNs)
                lightToCompile.add(p.compileStartNs - p.lightReadyNs);
            if (p.hasCompile() && p.compileEndNs > p.compileStartNs)
                compileDuration.add(p.compileEndNs - p.compileStartNs);
            if (p.hasUpload() && p.hasCompile() && p.compileEndNs < p.uploadedNs)
                compileToUpload.add(p.uploadedNs - p.compileEndNs);
        }

        StringBuilder sb = new StringBuilder(2048);
        sb.append("{\n");
        sb.append("  \"chunks_tracked\": ").append(chunks.size()).append(",\n");
        sb.append("  \"load_to_light_ms\":   ").append(json(loadToLight)).append(",\n");
        sb.append("  \"light_to_compile_ms\": ").append(json(lightToCompile)).append(",\n");
        sb.append("  \"compile_duration_ms\": ").append(json(compileDuration)).append(",\n");
        sb.append("  \"compile_to_upload_ms\": ").append(json(compileToUpload)).append("\n");
        sb.append("}\n");

        try {
            Files.createDirectories(OUT.getParent());
            Files.writeString(OUT, sb.toString(), StandardCharsets.UTF_8);
            MGShaders.LOGGER.info(
                "[MGShaders] pipeline ({}) — tracked {} chunks",
                "flush", chunks.size());
        } catch (IOException e) {
            MGShaders.LOGGER.warn("[MGShaders] pipeline write failed: {}", e.getMessage());
        }
    }

    private static String json(List<Long> src) {
        if (src.isEmpty()) return "{\"count\":0}";
        long[] a = new long[src.size()];
        for (int i = 0; i < a.length; i++) a[i] = src.get(i);
        Arrays.sort(a);
        long sum = 0;
        for (long v : a) sum += v;
        return String.format(
            "{\"count\":%d,\"avg\":%.1f,\"p50\":%.1f,\"p95\":%.1f,\"max\":%.1f}",
            a.length,
            sum / (double)a.length / 1_000_000.0,
            a[a.length / 2] / 1_000_000.0,
            a[Math.min((int)(a.length * 0.95), a.length - 1)] / 1_000_000.0,
            a[a.length - 1] / 1_000_000.0);
    }
}
