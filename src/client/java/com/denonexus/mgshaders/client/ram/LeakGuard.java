package com.denonexus.mgshaders.client.ram;

import com.denonexus.mgshaders.MGShaders;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Detecta crescimento MONOTÔNICO por tag.
 * Só alerta após N amostras consecutivas de crescimento E crescimento > 8 MB.
 * Reset ao encolher. Cooldown de 120s entre avisos. Sem System.gc().
 */
public final class LeakGuard {

    private static final long GROWTH_THRESHOLD = 8L * 1024 * 1024;
    private static final int  MIN_SAMPLES      = 6;
    private static final long WARN_COOLDOWN_NS = 120_000_000_000L;

    private static final class Entry {
        long baseline, peak, lastValue, lastWarnNs;
        int  consecutiveGrowth, warnings;
    }

    private static final Map<String, Entry> MAP = new ConcurrentHashMap<>();

    private LeakGuard() {}

    public static void sample(String tag, long bytes) {
        Entry e = MAP.computeIfAbsent(tag, k -> {
            Entry ne = new Entry();
            ne.baseline = ne.peak = ne.lastValue = bytes;
            return ne;
        });

        if      (bytes < e.lastValue) e.consecutiveGrowth = 0;
        else if (bytes > e.lastValue) e.consecutiveGrowth++;

        e.lastValue = bytes;
        if (bytes > e.peak) e.peak = bytes;

        long growth = bytes - e.baseline;
        boolean leak = growth >= GROWTH_THRESHOLD && e.consecutiveGrowth >= MIN_SAMPLES;

        if (leak) {
            long now = System.nanoTime();
            if (now - e.lastWarnNs > WARN_COOLDOWN_NS) {
                e.lastWarnNs = now;
                e.warnings++;
                MGShaders.LOGGER.warn(
                    "[MGShaders] leak? '{}' +{} KB (cresc monotônico {}x, avisos={})",
                    tag, growth / 1024, e.consecutiveGrowth, e.warnings);
            }
        }
    }

    public static void reset() { MAP.clear(); }
}
