package com.denonexus.mgshaders.client.hud;

import com.denonexus.mgshaders.MGShaders;
import com.denonexus.mgshaders.client.budget.FrameBudget;
import com.denonexus.mgshaders.client.profile.FpsTracker;
import com.denonexus.mgshaders.client.profile.RenderProfiler;
import com.denonexus.mgshaders.client.profile.StatsWindow;
import com.denonexus.mgshaders.client.profile.TpsTracker;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;

public final class MgHudOverlay {

    private static volatile boolean enabled = true;

    private static final int BG     = 0xC0101010;
    private static final int BORDER = 0xFF00FF88;
    private static final int FG     = 0xFFFFFFFF;
    private static final int HEADER = 0xFF7FE7FF;

    private static final int PAD    = 4;
    private static final int LINE_H = 10;

    private MgHudOverlay() {}

    public static void register() {
        HudElementRegistry.addLast(
            Identifier.fromNamespaceAndPath(MGShaders.MOD_ID, "stats"),
            MgHudOverlay::render
        );
        MGShaders.LOGGER.info("[{}] HUD registered", MGShaders.MOD_NAME);
    }

    public static void setEnabled(boolean v) { enabled = v; }
    public static boolean isEnabled()        { return enabled; }
    public static void toggle() {
        enabled = !enabled;
        MGShaders.LOGGER.info("[{}] HUD enabled={}", MGShaders.MOD_NAME, enabled);
    }

    private static void render(GuiGraphics ctx, DeltaTracker tick) {
        FpsTracker.onFrame();
        FrameBudget.reportFps(FpsTracker.current());

        if (!enabled) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null || mc.options.hideGui) return;

        Font font = mc.font;
        String[] lines = buildLines();

        int textW = 0;
        for (String s : lines) textW = Math.max(textW, font.width(s));

        int boxW = textW + PAD * 2 + 2;
        int boxH = lines.length * LINE_H + PAD * 2;
        int x = 4, y = 4;

        ctx.fill(x - 1, y - 1, x + boxW + 1, y + boxH + 1, BORDER);
        ctx.fill(x, y, x + boxW, y + boxH, BG);

        int ty = y + PAD;
        for (String s : lines) {
            int color = (s.startsWith("FPS") || s.startsWith("TPS")) ? HEADER : FG;
            ctx.drawString(font, s, x + PAD, ty, color);
            ty += LINE_H;
        }
    }

    private static String[] buildLines() {
        return new String[] {
            rate("FPS", FpsTracker.current(), FpsTracker.min(), FpsTracker.avg(), FpsTracker.max()),
            rate("TPS", TpsTracker.current(), TpsTracker.min(), TpsTracker.avg(), TpsTracker.max()),
            ms("Chunk load",     RenderProfiler.CHUNK_LOAD),
            ms("Chunk render",   RenderProfiler.CHUNK_RENDER),
            ms("Light",          RenderProfiler.LIGHT),
            ms("Entities",       RenderProfiler.ENTITIES),
            ms("Blocks",         RenderProfiler.BLOCKS),
            ms("Shader compile", RenderProfiler.SHADER_COMPILE),
        };
    }

    private static String rate(String label, int cur, int min, int avg, int max) {
        return String.format("%-14s cur%4d  min%4d  avg%4d  max%4d",
                label, cur, min, avg, max);
    }

    private static String ms(String label, StatsWindow w) {
        return String.format("%-14s cur%7.2f  min%7.2f  avg%7.2f  max%7.2f",
                label, w.current(), w.min(), w.avg(), w.max());
    }
}
