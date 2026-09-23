package com.denonexus.mgshaders.client.profile;

public final class FpsTracker {
    private static final StatsWindow W = new StatsWindow(300);
    private static long lastNs = 0L;
    private static int frames = 0;
    private static volatile int lastFps = 0;

    private FpsTracker() {}

    public static void onFrame() {
        long now = System.nanoTime();
        frames++;
        if (lastNs == 0L) { lastNs = now; return; }
        long dt = now - lastNs;
        if (dt >= 1_000_000_000L) {
            int fps = (int) Math.round(frames * 1_000_000_000.0 / dt);
            lastFps = fps;
            W.push(fps);
            frames = 0;
            lastNs = now;
        }
    }

    public static int current() { return lastFps; }
    public static int min()     { return (int) Math.round(W.min()); }
    public static int max()     { return (int) Math.round(W.max()); }
    public static int avg()     { return (int) Math.round(W.avg()); }
    public static void reset()  { W.reset(); lastFps = 0; frames = 0; lastNs = 0; }
}
