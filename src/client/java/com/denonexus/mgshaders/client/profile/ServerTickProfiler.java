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

public final class ServerTickProfiler {

    private static final Path OUT =
            Paths.get("/sdcard/MG/mgshaders_server_tick.json");

    private static final List<Long> tickNs = new ArrayList<>();
    private static long tickCount = 0;
    private static long lastDumpNs = System.nanoTime();
    private static final long DUMP_INTERVAL_NS = 30L * 1_000_000_000L;

    private ServerTickProfiler() {}

    public static void record(long ns) {
        tickCount++;
        synchronized (ServerTickProfiler.class) {
            tickNs.add(ns);
            if (tickNs.size() > 5000) tickNs.remove(0);
        }
        long now = System.nanoTime();
        if (now - lastDumpNs > DUMP_INTERVAL_NS) {
            lastDumpNs = now;
            dump("periodic");
        }
    }

    public static void flush() { dump("flush"); }

    private static void dump(String reason) {
        long[] samples;
        synchronized (ServerTickProfiler.class) {
            samples = new long[tickNs.size()];
            for (int i = 0; i < tickNs.size(); i++) samples[i] = tickNs.get(i);
        }
        if (samples.length == 0) return;

        long[] sorted = samples.clone();
        Arrays.sort(sorted);
        long sum = 0;
        for (long v : sorted) sum += v;

        long avg = sum / sorted.length;
        long p50 = sorted[(int)(sorted.length * 0.50)];
        long p95 = sorted[Math.min((int)(sorted.length * 0.95), sorted.length - 1)];
        long p99 = sorted[Math.min((int)(sorted.length * 0.99), sorted.length - 1)];
        long max = sorted[sorted.length - 1];

        int over50 = 0, over100 = 0, over250 = 0;
        for (long v : sorted) {
            if (v > 50_000_000L) over50++;
            if (v > 100_000_000L) over100++;
            if (v > 250_000_000L) over250++;
        }

        double avgMs = avg / 1_000_000.0;
        double tps = avgMs > 0 ? 1000.0 / avgMs : 0;
        double worstTps = max > 0 ? 1000.0 / (max / 1_000_000.0) : 0;

        StringBuilder sb = new StringBuilder(1024);
        sb.append("{\n");
        sb.append("  \"reason\": \"").append(reason).append("\",\n");
        sb.append("  \"tick_count\": ").append(tickCount).append(",\n");
        sb.append("  \"samples\": ").append(sorted.length).append(",\n");
        sb.append("  \"tick_ms\": {\n");
        sb.append("    \"avg\": ").append(String.format("%.1f", avg / 1_000_000.0)).append(",\n");
        sb.append("    \"p50\": ").append(String.format("%.1f", p50 / 1_000_000.0)).append(",\n");
        sb.append("    \"p95\": ").append(String.format("%.1f", p95 / 1_000_000.0)).append(",\n");
        sb.append("    \"p99\": ").append(String.format("%.1f", p99 / 1_000_000.0)).append(",\n");
        sb.append("    \"max\": ").append(String.format("%.1f", max / 1_000_000.0)).append("\n");
        sb.append("  },\n");
        sb.append("  \"effective_tps\": ").append(String.format("%.1f", tps)).append(",\n");
        sb.append("  \"worst_tps\": ").append(String.format("%.1f", worstTps)).append(",\n");
        sb.append("  \"over_budget\": {\n");
        sb.append("    \"over_50ms\": ").append(over50).append(",\n");
        sb.append("    \"over_100ms\": ").append(over100).append(",\n");
        sb.append("    \"over_250ms\": ").append(over250).append("\n");
        sb.append("  }\n");
        sb.append("}\n");

        try {
            Files.createDirectories(OUT.getParent());
            Files.writeString(OUT, sb.toString(), StandardCharsets.UTF_8);
            MGShaders.LOGGER.info(
                "[MGShaders] server tick ({}) — TPS={} p50={}ms p95={}ms max={}ms",
                reason, String.format("%.1f", tps),
                p50 / 1_000_000, p95 / 1_000_000, max / 1_000_000);
        } catch (IOException e) {
            MGShaders.LOGGER.warn("[MGShaders] tick profile write failed: {}", e.getMessage());
        }
    }
}
