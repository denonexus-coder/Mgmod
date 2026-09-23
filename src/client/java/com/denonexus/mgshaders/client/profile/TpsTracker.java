package com.denonexus.mgshaders.client.profile;

public final class TpsTracker {
    private static final int MAX_TPS = 20;
    private static final StatsWindow W = new StatsWindow(300);
    private static long lastNs = 0L;
    private static int ticks = 0;
    private static volatile int lastTps = MAX_TPS;

    private TpsTracker() {}

    public static void onServerTick() {
        long now = System.nanoTime();
        ticks++;
        if (lastNs == 0L) { lastNs = now; return; }
        long dt = now - lastNs;
        if (dt >= 1_000_000_000L) {
            int tps = (int) Math.round(ticks * 1_000_000_000.0 / dt);
            if (tps > MAX_TPS) tps = MAX_TPS;
            lastTps = tps;
            W.push(tps);
            ticks = 0;
            lastNs = now;
        }
    }

    public static int current() { return lastTps; }
    public static int min()     { return (int) Math.round(W.min()); }
    public static int max()     { return (int) Math.round(W.max()); }
    public static int avg()     { return (int) Math.round(W.avg()); }
    public static void reset()  { W.reset(); lastTps = MAX_TPS; ticks = 0; lastNs = 0; }
}
