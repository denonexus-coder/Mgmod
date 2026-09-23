package com.denonexus.mgshaders.client.hud;

import com.denonexus.mgshaders.MGShaders;
import com.denonexus.mgshaders.client.budget.FrameBudget;
import com.denonexus.mgshaders.client.profile.FpsTracker;
import com.denonexus.mgshaders.client.profile.RenderProfiler;
import com.denonexus.mgshaders.client.profile.TpsTracker;
import com.denonexus.mgshaders.client.ram.RamManager;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;

/**
 * HUD compacto — 2 linhas, canto superior esquerdo, estilo "f3 minimal".
 *
 *   linha 1: FPS 42(31-45)  TPS 20  RAM 213M/550M  +14M
 *   linha 2: Ck 2.1  Li 0.8  En 0.4  Bl 0.6  Sd 0.2  ms
 */
public final class MgHudOverlay {

    private static volatile boolean enabled = true;

    private static final int BG     = 0x90000000;
    private static final int BORDER = 0x8000FF88;
    private static final int FG     = 0xFFE0E0E0;
    private static final int HDR    = 0xFF7FE7FF;
    private static final int RAM_H  = 0xFFFFC24B;
    private static final int RAM_OK = 0xFFB0FFB0;

    private static final int PAD    = 2;
    private static final int LINE_H = 10;

    private MgHudOverlay() {}

    public static void register() {
        HudElementRegistry.addLast(
            Identifier.fromNamespaceAndPath(MGShaders.MOD_ID, "stats"),
            MgHudOverlay::render
        );
        MGShaders.LOGGER.info("[{}] HUD registered", MGShaders.MOD_NAME);
    }

    public static void toggle() { enabled = !enabled; }
    public static boolean isEnabled() { return enabled; }

    private static void render(GuiGraphics ctx, DeltaTracker dt) {
        FpsTracker.onFrame();
        FrameBudget.reportFps(FpsTracker.current());
        if (!enabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null || mc.options.hideGui) return;

        Font font = mc.font;

        String line1 = String.format("FPS %d(%d-%d)  TPS %d  RAM %s  +%s",
                FpsTracker.current(), FpsTracker.min(), FpsTracker.max(),
                TpsTracker.current(),
                RamManager.heapShort(),
                RamManager.freedShort());

        String line2 = String.format("Ck %.1f  Li %.1f  En %.1f  Bl %.1f  Sd %.1f  ms",
                RenderProfiler.CHUNK_RENDER.avg(),
                RenderProfiler.LIGHT.avg(),
                RenderProfiler.ENTITIES.avg(),
                RenderProfiler.BLOCKS.avg(),
                RenderProfiler.SHADER_COMPILE.avg());

        int w1 = font.width(line1);
        int w2 = font.width(line2);
        int boxW = Math.max(w1, w2) + PAD * 2 + 2;
        int boxH = 2 * LINE_H + PAD * 2;
        int x = 3, y = 3;

        ctx.fill(x - 1, y - 1, x + boxW + 1, y + boxH + 1, BORDER);
        ctx.fill(x, y, x + boxW, y + boxH, BG);

        // Cores condicionais de RAM
        long usedPct = RamManager.javaMaxKb() > 0
                ? RamManager.javaUsedKb() * 100 / RamManager.javaMaxKb() : 0;
        int ramColor = usedPct >= 80 ? RAM_H : RAM_OK;

        // linha 1 — FPS/TPS/RAM
        int tx = x + PAD;
        int ty = y + PAD;

        // Desenha segmentado para colorir RAM diferente
        ctx.drawString(font, "FPS " + FpsTracker.current(), tx, ty, HDR);
        int cur = tx + font.width("FPS " + FpsTracker.current());
        String minmax = String.format("(%d-%d)", FpsTracker.min(), FpsTracker.max());
        ctx.drawString(font, minmax, cur, ty, FG);
        cur += font.width(minmax) + 6;

        ctx.drawString(font, "TPS " + TpsTracker.current(), cur, ty, HDR);
        cur += font.width("TPS " + TpsTracker.current()) + 6;

        ctx.drawString(font, "RAM " + RamManager.heapShort(), cur, ty, ramColor);
        cur += font.width("RAM " + RamManager.heapShort()) + 6;

        ctx.drawString(font, "+" + RamManager.freedShort(), cur, ty, FG);

        // linha 2
        ctx.drawString(font, line2, x + PAD, ty + LINE_H, FG);
    }
}
