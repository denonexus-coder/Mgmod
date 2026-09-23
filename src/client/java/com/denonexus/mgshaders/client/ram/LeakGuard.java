package com.denonexus.mgshaders.client.ram;

import com.denonexus.mgshaders.MGShaders;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Detecta vazamentos por "tag" monitorando crescimento monotônico.
 * NÃO força GC. Apenas observa e reporta.
 */
public final class LeakGuard {

    private static final long GROWTH_THRESHOLD_BYTES = 8L * 1024 * 1024;   // +8 MB
    private static final long WARN_EVERY_NS = 60_000_000_000L;              // 60 s

    private static final class Entry {
        long baseline;
        long peak;
        long lastWarnNs;
        int  monotonicSamples;
    }

    private static final Map<String, Entry> MAP = new ConcurrentHashMap<>();

    private LeakGuard() {}

    /** Registra tamanho atual do recurso 'tag'. Baseline no primeiro registro. */
    public static void sample(String tag, long bytes) {
        Entry e = MAP.computeIfAbsent(tag, k -> {
            Entry ne = new Entry();
            ne.baseline = bytes;
            ne.peak = bytes;
            return ne;
        });

        if (bytes > e.peak) e.peak = bytes;

        if (bytes > e.baseline + GROWTH_THRESHOLD_BYTES) {
            e.monotonicSamples++;
            long now = System.nanoTime();
            if (now - e.lastWarnNs > WARN_EVERY_NS) {
                e.lastWarnNs = now;
                MGShaders.LOGGER.warn(
                    "[MGShaders] possível vazamento '{}' — base {} KB, atual {} KB, pico {} KB, amostras={}",
                    tag,
                    e.baseline / 1024,
                    bytes / 1024,
                    e.peak / 1024,
                    e.monotonicSamples);
            }
        }
    }

    public static String report() {
        StringBuilder sb = new StringBuilder();
        for (var en : MAP.entrySet()) {
            Entry e = en.getValue();
            sb.append(en.getKey()).append('=')
              .append((e.peak - e.baseline) / 1024).append("KB; ");
        }
        return sb.toString();
    }

    public static void reset() { MAP.clear(); }
}
