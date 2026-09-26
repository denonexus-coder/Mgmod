package com.denonexus.mgshaders.client.profile;

import com.denonexus.mgshaders.MGShaders;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public final class ServerBottleneckProfiler {

    private static final Path ROOT =
            Path.of("/sdcard/MG/mgshaders_server_profiler");

    private static final long WARN_NS = 50_000_000L;
    private static final long HIGH_NS = 100_000_000L;
    private static final long CRITICAL_NS = 150_000_000L;
    private static final long SEVERE_NS = 200_000_000L;

    private static final int MAX_SAMPLES = 4096;
    private static final int MAX_SLOW_ENTRIES = 128;

    private static final AtomicLong TICK_COUNTER =
            new AtomicLong();

    private static final AtomicReference<TickData> CURRENT =
            new AtomicReference<>();

    private static final ThreadLocal<Long> SERVER_START =
            new ThreadLocal<>();

    private static final ThreadLocal<Long> CHILDREN_START =
            new ThreadLocal<>();

    private static final ThreadLocal<Long> CONNECTION_START =
            new ThreadLocal<>();

    private static final ThreadLocal<Long> WORLD_TICK_START =
            new ThreadLocal<>();

    private static final ThreadLocal<Long> CHUNK_CACHE_START =
            new ThreadLocal<>();

    private static final ThreadLocal<Long> DISTANCE_START =
            new ThreadLocal<>();

    private static final ThreadLocal<Long> BLOCK_ENTITIES_START =
            new ThreadLocal<>();

    private static final ThreadLocal<Long> ENTITY_START =
            new ThreadLocal<>();

    private static final ThreadLocal<Long> SPAWNER_START =
            new ThreadLocal<>();

    private static final ThreadLocal<Long> BLOCK_EVENTS_START =
            new ThreadLocal<>();

    private static final ThreadLocal<Long> CHUNK_TICK_START =
            new ThreadLocal<>();

    private static final List<Long> tickTimes =
            new ArrayList<>();

    private static final List<TickData> slowTicks =
            new ArrayList<>();

    private static final Map<String, PhaseStats> phases =
            new ConcurrentHashMap<>();

    private static final Map<String, SlowEntry> slowChunks =
            new ConcurrentHashMap<>();

    private static final Map<String, SlowEntry> slowEntities =
            new ConcurrentHashMap<>();

    private static volatile long lastDumpNs =
            System.nanoTime();

    private static final long DUMP_INTERVAL_NS =
            30_000_000_000L;

    private ServerBottleneckProfiler() {}

    public static void beginServerTick() {
        long now = System.nanoTime();
        SERVER_START.set(now);

        TickData t = new TickData(
                TICK_COUNTER.incrementAndGet(),
                now
        );

        CURRENT.set(t);
    }

    public static void endServerTick() {
        Long start = SERVER_START.get();

        if (start == null) {
            return;
        }

        SERVER_START.remove();

        long elapsed = System.nanoTime() - start;

        TickData t = CURRENT.get();

        if (t == null) {
            return;
        }

        t.totalNs = elapsed;

        synchronized (tickTimes) {
            tickTimes.add(elapsed);

            if (tickTimes.size() > MAX_SAMPLES) {
                tickTimes.remove(0);
            }
        }

        if (elapsed >= WARN_NS) {
            synchronized (slowTicks) {
                slowTicks.add(t);

                if (slowTicks.size() > MAX_SLOW_ENTRIES) {
                    slowTicks.remove(0);
                }
            }
        }

        long now = System.nanoTime();

        if (now - lastDumpNs >= DUMP_INTERVAL_NS) {
            lastDumpNs = now;
            dump("periodic");
        }

        CURRENT.compareAndSet(t, null);
    }

    public static void beginChildren() {
        CHILDREN_START.set(System.nanoTime());
    }

    public static void endChildren() {
        Long s = CHILDREN_START.get();

        if (s == null) return;

        CHILDREN_START.remove();
        record("server.tick_children", System.nanoTime() - s);
    }

    public static void beginConnection() {
        CONNECTION_START.set(System.nanoTime());
    }

    public static void endConnection() {
        Long s = CONNECTION_START.get();

        if (s == null) return;

        CONNECTION_START.remove();
        record("server.connection", System.nanoTime() - s);
    }

    public static void beginWorldTick() {
        WORLD_TICK_START.set(System.nanoTime());
    }

    public static void endWorldTick(String world) {
        Long s = WORLD_TICK_START.get();

        if (s == null) return;

        WORLD_TICK_START.remove();

        long ns = System.nanoTime() - s;

        record("world.tick", ns);
        record("world:" + world, ns);
    }

    public static void beginChunkCache() {
        CHUNK_CACHE_START.set(System.nanoTime());
    }

    public static void endChunkCache() {
        Long s = CHUNK_CACHE_START.get();

        if (s == null) return;

        CHUNK_CACHE_START.remove();
        record("chunks.server_chunk_cache", System.nanoTime() - s);
    }

    public static void beginDistanceManager() {
        DISTANCE_START.set(System.nanoTime());
    }

    public static void endDistanceManager() {
        Long s = DISTANCE_START.get();

        if (s == null) return;

        DISTANCE_START.remove();
        record("chunks.distance_manager", System.nanoTime() - s);
    }

    public static void beginBlockEntities() {
        BLOCK_ENTITIES_START.set(System.nanoTime());
    }

    public static void endBlockEntities() {
        Long s = BLOCK_ENTITIES_START.get();

        if (s == null) return;

        BLOCK_ENTITIES_START.remove();

        TickData t = CURRENT.get();

        if (t != null) {
            t.blockEntityCalls++;
        }

        record("world.block_entities", System.nanoTime() - s);
    }

    public static void beginEntity() {
        ENTITY_START.set(System.nanoTime());
    }

    public static void endEntity(String entityType) {
        Long s = ENTITY_START.get();

        if (s == null) return;

        ENTITY_START.remove();

        long ns = System.nanoTime() - s;

        TickData t = CURRENT.get();

        if (t != null) {
            t.entityCalls++;
        }

        record("world.entities", ns);

        if (ns >= 1_000_000L) {
            recordSlow(
                    slowEntities,
                    entityType,
                    ns
            );
        }
    }

    public static void beginSpawners() {
        SPAWNER_START.set(System.nanoTime());
    }

    public static void endSpawners() {
        Long s = SPAWNER_START.get();

        if (s == null) return;

        SPAWNER_START.remove();
        record("world.spawners", System.nanoTime() - s);
    }

    public static void beginBlockEvents() {
        BLOCK_EVENTS_START.set(System.nanoTime());
    }

    public static void endBlockEvents() {
        Long s = BLOCK_EVENTS_START.get();

        if (s == null) return;

        BLOCK_EVENTS_START.remove();
        record("world.block_events", System.nanoTime() - s);
    }

    public static void beginChunkTick() {
        CHUNK_TICK_START.set(System.nanoTime());
    }

    public static void endChunkTick(String position) {
        Long s = CHUNK_TICK_START.get();

        if (s == null) return;

        CHUNK_TICK_START.remove();

        long ns = System.nanoTime() - s;

        TickData t = CURRENT.get();

        if (t != null) {
            t.chunkCalls++;
        }

        record("chunks.tick_chunk", ns);

        if (ns >= 2_000_000L) {
            recordSlow(
                    slowChunks,
                    position,
                    ns
            );
        }
    }

    private static void record(
            String phase,
            long ns
    ) {
        if (ns < 0) return;

        PhaseStats stats =
                phases.computeIfAbsent(
                        phase,
                        k -> new PhaseStats()
                );

        stats.count.incrementAndGet();
        stats.total.addAndGet(ns);

        updateMax(stats, ns);

        TickData t = CURRENT.get();

        if (t != null) {
            t.phaseTotals.merge(
                    phase,
                    ns,
                    Long::sum
            );
        }
    }

    private static void updateMax(
            PhaseStats stats,
            long value
    ) {
        long old;

        do {
            old = stats.max.get();

            if (value <= old) {
                return;
            }

        } while (!stats.max.compareAndSet(old, value));
    }

    private static void recordSlow(
            Map<String, SlowEntry> map,
            String key,
            long ns
    ) {
        map.compute(
                key,
                (k, old) -> {
                    if (old == null) {
                        return new SlowEntry(
                                k,
                                ns,
                                1
                        );
                    }

                    old.count++;
                    old.totalNs += ns;

                    if (ns > old.maxNs) {
                        old.maxNs = ns;
                    }

                    return old;
                }
        );

        if (map.size() > MAX_SLOW_ENTRIES * 4) {
            trimSlow(map);
        }
    }

    private static void trimSlow(
            Map<String, SlowEntry> map
    ) {
        List<SlowEntry> list =
                new ArrayList<>(map.values());

        list.sort(
                Comparator.comparingLong(
                        SlowEntry::maxNs
                ).reversed()
        );

        for (int i = MAX_SLOW_ENTRIES; i < list.size(); i++) {
            map.remove(list.get(i).key);
        }
    }

    public static void flush() {
        dump("flush");
    }

    private static void dump(
            String reason
    ) {
        try {
            Files.createDirectories(ROOT);

            writeTicks();
            writePhases();
            writeSlowChunks();
            writeSlowEntities();
            writeSummary(reason);

        } catch (Throwable t) {
            MGShaders.LOGGER.warn(
                    "[MGShaders] server profiler dump failed",
                    t
            );
        }
    }

    private static void writeTicks()
            throws IOException {

        List<Long> samples;

        synchronized (tickTimes) {
            samples = new ArrayList<>(tickTimes);
        }

        samples.sort(Long::compare);

        StringBuilder out =
                new StringBuilder();

        out.append(
                "tick,total_ms,classification\n"
        );

        long base =
                TICK_COUNTER.get()
                - samples.size();

        for (int i = 0; i < samples.size(); i++) {

            long ns = samples.get(i);

            out.append(base + i + 1)
                    .append(',')
                    .append(ms(ns))
                    .append(',')
                    .append(classification(ns))
                    .append('\n');
        }

        Files.writeString(
                ROOT.resolve("ticks.csv"),
                out.toString(),
                StandardCharsets.UTF_8
        );
    }

    private static void writePhases()
            throws IOException {

        StringBuilder out =
                new StringBuilder();

        out.append(
                "phase,count,total_ms,avg_ms,max_ms\n"
        );

        phases.entrySet()
                .stream()
                .sorted(
                        Comparator.comparingLong(
                                e -> -e.getValue().total.get()
                        )
                )
                .forEach(e -> {

                    PhaseStats s =
                            e.getValue();

                    long count =
                            s.count.get();

                    long total =
                            s.total.get();

                    out.append(e.getKey())
                            .append(',')
                            .append(count)
                            .append(',')
                            .append(ms(total))
                            .append(',')
                            .append(
                                    count == 0
                                    ? 0
                                    : ms(total / count)
                            )
                            .append(',')
                            .append(ms(s.max.get()))
                            .append('\n');
                });

        Files.writeString(
                ROOT.resolve("phases.csv"),
                out.toString(),
                StandardCharsets.UTF_8
        );
    }

    private static void writeSlowChunks()
            throws IOException {

        writeSlowMap(
                ROOT.resolve("chunks.csv"),
                slowChunks
        );
    }

    private static void writeSlowEntities()
            throws IOException {

        writeSlowMap(
                ROOT.resolve("entities.csv"),
                slowEntities
        );
    }

    private static void writeSlowMap(
            Path path,
            Map<String, SlowEntry> map
    ) throws IOException {

        List<SlowEntry> list =
                new ArrayList<>(map.values());

        list.sort(
                Comparator.comparingLong(
                        SlowEntry::maxNs
                ).reversed()
        );

        StringBuilder out =
                new StringBuilder();

        out.append(
                "key,count,total_ms,avg_ms,max_ms\n"
        );

        for (SlowEntry e : list) {

            out.append(csv(e.key))
                    .append(',')
                    .append(e.count)
                    .append(',')
                    .append(ms(e.totalNs))
                    .append(',')
                    .append(
                            e.count == 0
                            ? 0
                            : ms(e.totalNs / e.count)
                    )
                    .append(',')
                    .append(ms(e.maxNs))
                    .append('\n');
        }

        Files.writeString(
                path,
                out.toString(),
                StandardCharsets.UTF_8
        );
    }

    private static void writeSummary(
            String reason
    ) throws IOException {

        List<Long> samples;

        synchronized (tickTimes) {
            samples = new ArrayList<>(tickTimes);
        }

        samples.sort(Long::compare);

        long avg = average(samples);
        long p50 = percentile(samples, 0.50);
        long p95 = percentile(samples, 0.95);
        long p99 = percentile(samples, 0.99);
        long max = samples.isEmpty()
                ? 0
                : samples.get(samples.size() - 1);

        int over50 = countOver(samples, WARN_NS);
        int over100 = countOver(samples, HIGH_NS);
        int over150 = countOver(samples, CRITICAL_NS);
        int over200 = countOver(samples, SEVERE_NS);

        StringBuilder out =
                new StringBuilder();

        out.append("{\n");
        out.append("  \"reason\":\"")
                .append(csv(reason))
                .append("\",\n");

        out.append("  \"ticks\":")
                .append(samples.size())
                .append(",\n");

        out.append("  \"tick_ms\":{\n");
        out.append("    \"avg\":")
                .append(ms(avg))
                .append(",\n");
        out.append("    \"p50\":")
                .append(ms(p50))
                .append(",\n");
        out.append("    \"p95\":")
                .append(ms(p95))
                .append(",\n");
        out.append("    \"p99\":")
                .append(ms(p99))
                .append(",\n");
        out.append("    \"max\":")
                .append(ms(max))
                .append("\n");
        out.append("  },\n");

        out.append("  \"thresholds\":{\n");
        out.append("    \"over_50ms\":")
                .append(over50)
                .append(",\n");
        out.append("    \"over_100ms\":")
                .append(over100)
                .append(",\n");
        out.append("    \"over_150ms\":")
                .append(over150)
                .append(",\n");
        out.append("    \"over_200ms\":")
                .append(over200)
                .append("\n");
        out.append("  },\n");

        out.append("  \"largest_measured_phases\":[\n");

        List<Map.Entry<String, PhaseStats>> list =
                new ArrayList<>(phases.entrySet());

        list.sort(
                Comparator.comparingLong(
                        e -> -e.getValue().total.get()
                )
        );

        int limit =
                Math.min(12, list.size());

        for (int i = 0; i < limit; i++) {

            var e = list.get(i);

            if (i > 0) out.append(',');

            out.append("    {\"phase\":\"")
                    .append(csv(e.getKey()))
                    .append("\",\"total_ms\":")
                    .append(ms(e.getValue().total.get()))
                    .append(",\"max_ms\":")
                    .append(ms(e.getValue().max.get()))
                    .append('}');
            out.append('\n');
        }

        out.append("  ],\n");

        out.append("  \"diagnosis\":\"")
                .append(diagnosis(
                        p95,
                        max
                ))
                .append("\"\n");

        out.append("}\n");

        Files.writeString(
                ROOT.resolve("summary.json"),
                out.toString(),
                StandardCharsets.UTF_8
        );

        MGShaders.LOGGER.info(
                "[MGShaders] SERVER PROFILER: avg={}ms p95={}ms p99={}ms max={}ms",
                ms(avg),
                ms(p95),
                ms(p99),
                ms(max)
        );
    }

    private static String diagnosis(
            long p95,
            long max
    ) {
        if (max >= SEVERE_NS) {
            return "ticks_above_200ms_detected";
        }

        if (max >= CRITICAL_NS) {
            return "ticks_above_150ms_detected";
        }

        if (p95 >= HIGH_NS) {
            return "p95_above_100ms";
        }

        if (p95 >= WARN_NS) {
            return "p95_above_50ms";
        }

        return "no_major_tick_spike_in_current_window";
    }

    private static String classification(
            long ns
    ) {
        if (ns >= SEVERE_NS) return "SEVERE";
        if (ns >= CRITICAL_NS) return "CRITICAL";
        if (ns >= HIGH_NS) return "HIGH";
        if (ns >= WARN_NS) return "WARNING";
        return "NORMAL";
    }

    private static int countOver(
            List<Long> list,
            long threshold
    ) {
        int n = 0;

        for (long v : list) {
            if (v > threshold) {
                n++;
            }
        }

        return n;
    }

    private static long average(
            List<Long> list
    ) {
        if (list.isEmpty()) return 0;

        long sum = 0;

        for (long v : list) {
            sum += v;
        }

        return sum / list.size();
    }

    private static long percentile(
            List<Long> list,
            double p
    ) {
        if (list.isEmpty()) return 0;

        int index =
                Math.min(
                        list.size() - 1,
                        (int) Math.ceil(
                                p * list.size()
                        ) - 1
                );

        return list.get(index);
    }

    private static double ms(long ns) {
        return ns / 1_000_000.0;
    }

    private static String csv(
            String s
    ) {
        return s
                .replace("\\", "\\\\")
                .replace("\"", "\"\"");
    }

    public static String classificationFor(long ns) {
        return classification(ns);
    }

    private static final class PhaseStats {

        final AtomicLong count =
                new AtomicLong();

        final AtomicLong total =
                new AtomicLong();

        final AtomicLong max =
                new AtomicLong();
    }

    private static final class SlowEntry {

        final String key;

        long count;

        long totalNs;

        long maxNs;

        SlowEntry(
                String key,
                long ns,
                long count
        ) {
            this.key = key;
            this.totalNs = ns;
            this.maxNs = ns;
            this.count = count;
        }

        long maxNs() {
            return maxNs;
        }
    }

    private static final class TickData {

        final long tick;

        final long startNs;

        long totalNs;

        long chunkCalls;

        long entityCalls;

        long blockEntityCalls;

        final Map<String, Long> phaseTotals =
                new ConcurrentHashMap<>();

        TickData(
                long tick,
                long startNs
        ) {
            this.tick = tick;
            this.startNs = startNs;
        }
    }
}
