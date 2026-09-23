package com.denonexus.mgshaders.client.budget;

/**
 * Orçamento adaptativo por frame. Ajusta sozinho conforme o FPS.
 * Thread-safe por volatile + interval check.
 */
public final class FrameBudget {

    // ── tetos ajustáveis ──
    public static volatile int lightDrainsPerSecond   = 30;
    public static volatile int entityCullDistance     = 64;

    // ── estado interno ──
    private static volatile double lastFps = 60.0;
    private static long lastAdjustNs = 0L;
    private static final long ADJUST_INTERVAL_NS = 1_000_000_000L;

    // fila de luz: só deixa drenar N vezes/seg
    private static long lastLightDrainNs = 0L;

    private FrameBudget() {}

    public static void reportFps(double fps) {
        if (!Double.isFinite(fps) || fps <= 0) return;
        lastFps = fps;
        long now = System.nanoTime();
        if (now - lastAdjustNs < ADJUST_INTERVAL_NS) return;
        lastAdjustNs = now;
        adjust();
    }

    private static void adjust() {
        if (lastFps < 30.0) {
            lightDrainsPerSecond = Math.max(15, lightDrainsPerSecond - 5);
            entityCullDistance   = Math.max(24, entityCullDistance - 8);
        } else if (lastFps > 55.0) {
            lightDrainsPerSecond = Math.min(60, lightDrainsPerSecond + 5);
            entityCullDistance   = Math.min(96, entityCullDistance + 8);
        }
    }

    /** Retorna true se já pode drenar de novo. Chamado pela mixin de luz. */
    public static boolean canDrainLight() {
        long now = System.nanoTime();
        long interval = 1_000_000_000L / Math.max(1, lightDrainsPerSecond);
        if (now - lastLightDrainNs < interval) return false;
        lastLightDrainNs = now;
        return true;
    }

    public static double lastFps() { return lastFps; }

    public static String snapshot() {
        return String.format("L=%d/s E=%d (%.1f fps)",
                lightDrainsPerSecond, entityCullDistance, lastFps);
    }
}
