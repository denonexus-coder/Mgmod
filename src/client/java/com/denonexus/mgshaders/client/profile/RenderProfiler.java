package com.denonexus.mgshaders.client.profile;

public final class RenderProfiler {

    public static final StatsWindow CHUNK_LOAD     = new StatsWindow(120);
    public static final StatsWindow CHUNK_RENDER   = new StatsWindow(120);
    public static final StatsWindow LIGHT          = new StatsWindow(120);
    public static final StatsWindow ENTITIES       = new StatsWindow(120);
    public static final StatsWindow BLOCKS         = new StatsWindow(120);
    public static final StatsWindow SHADER_COMPILE = new StatsWindow(60);

    private static final ThreadLocal<Long> T0 = new ThreadLocal<>();

    private RenderProfiler() {}

    public static void begin() { T0.set(System.nanoTime()); }

    public static void end(StatsWindow w) {
        Long t = T0.get();
        if (t == null) return;
        T0.remove();
        w.push((System.nanoTime() - t) / 1_000_000.0);
    }

    public static void push(StatsWindow w, long ns) {
        if (ns >= 0) w.push(ns / 1_000_000.0);
    }

    public static void shaderCompile(long ns) {
        if (ns > 0) SHADER_COMPILE.push(ns / 1_000_000.0);
    }
}
