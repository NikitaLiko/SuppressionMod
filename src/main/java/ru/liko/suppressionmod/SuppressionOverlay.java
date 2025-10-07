package ru.liko.suppressionmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FastColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = SuppressionMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SuppressionOverlay {

    private static BulletSuppressionHandler handler;
    private static final Random NOISE = new Random();

    public static void setHandler(BulletSuppressionHandler h) { handler = h; }

    @SubscribeEvent
    public static void onOverlay(RenderGuiOverlayEvent.Post e) {
        if (handler == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getWindow() == null) return;

        float suppression = handler.getSuppressionRatio();
        float flash = handler.getShockFlashIntensity();
        if (suppression <= 0.001f && flash <= 0.001f) return;

        GuiGraphics g = e.getGuiGraphics();
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();

        float maxAlpha = Config.VIGNETTE_MAX_ALPHA.get().floatValue();
        float tintScale = Config.UI_TINT_STRENGTH.get().floatValue();
        float vignetteScale = Config.UI_VIGNETTE_STRENGTH.get().floatValue();
        float flashScale = Config.UI_FLASH_STRENGTH.get().floatValue();
        float grainScale = Config.UI_GRAIN_STRENGTH.get().floatValue();

        float baseIntensity = Math.min(1.0f, (float)Math.pow(suppression, 1.05) * maxAlpha) * vignetteScale;
        float flashIntensity = Math.min(1.0f, flash * 0.55f) * flashScale;
        float tintIntensity = Math.min(1.0f, baseIntensity * tintScale);
        long gameTime = mc.level != null ? mc.level.getGameTime() : 0L;
        float partial = e.getPartialTick();
        float animTime = (gameTime + partial) / 20.0f;

        drawColorGrade(g, w, h, tintIntensity, flashIntensity);
        drawSoftVignette(g, w, h, baseIntensity, flashIntensity);
        drawDirectionalHaze(g, w, h, baseIntensity, flashIntensity, animTime);
        drawFineGrain(g, w, h, baseIntensity, flashIntensity, gameTime, grainScale);
    }

    private static void drawColorGrade(GuiGraphics g, int w, int h, float intensity, float flash) {
        float alpha = Math.min(1.0f, 0.22f * intensity + 0.12f * flash);
        if (alpha <= 0.01f) return;
        int color = FastColor.ARGB32.color((int)(alpha * 255.0f), 38, 33, 30);
        g.fill(0, 0, w, h, color);
    }

    private static void drawSoftVignette(GuiGraphics g, int w, int h, float intensity, float flash) {
        float edgeStrength = Math.min(1.0f, intensity * 0.65f + flash * 0.28f);
        if (edgeStrength <= 0.01f) return;

        int steps = 36;
        int vw = (int)(w * 0.28f);
        int vh = (int)(h * 0.32f);

        for (int i = 0; i < steps; i++) {
            float step = (float)i / steps;
            float falloff = 1.0f - step;
            float shaped = (float)Math.pow(falloff, 1.9);
            float alpha = edgeStrength * shaped;
            int a = (int)(Math.min(alpha, 1.0f) * 120.0f);
            if (a <= 1) continue;

            int tone = 12 + (int)(falloff * 16);
            int color = FastColor.ARGB32.color(a, tone, tone, tone);

            int y1 = (vh * i) / steps;
            int y2 = (vh * (i + 1)) / steps;
            g.fill(0, y1, w, y2, color);
            g.fill(0, h - y2, w, h - y1, color);

            int x1 = (vw * i) / steps;
            int x2 = (vw * (i + 1)) / steps;
            g.fill(x1, 0, x2, h, color);
            g.fill(w - x2, 0, w - x1, h, color);
        }
    }

    private static void drawDirectionalHaze(GuiGraphics g, int w, int h, float intensity, float flash, float time) {
        float base = Math.min(1.0f, intensity * 0.35f + flash * 0.22f);
        if (base <= 0.01f) return;

        float breathing = 0.95f + 0.05f * (float)Math.sin(time * 2.9f);
        int topAlpha = (int)(base * breathing * 90.0f);
        int bottomAlpha = (int)(base * 0.7f * 110.0f);
        int toneTop = 62;
        int toneBottom = 48;

        g.fillGradient(0, 0, w, (int)(h * 0.18f), FastColor.ARGB32.color(topAlpha, toneTop, toneTop - 6, toneTop - 10), 0);
        g.fillGradient(0, h - (int)(h * 0.22f), w, h, 0, FastColor.ARGB32.color(bottomAlpha, toneBottom, toneBottom - 10, toneBottom - 16));
    }

    private static void drawFineGrain(GuiGraphics g, int w, int h, float intensity, float flash, long gameTime, float grainScale) {
        float grainBase = Math.max(intensity * 0.14f, flash * 0.18f) * Math.max(0.0f, grainScale);
        if (grainBase <= 0.01f) return;

        NOISE.setSeed(gameTime * 341873128712L + w * 31L + h * 17L);
        int clusters = 6 + (w * h) / 16000;

        for (int i = 0; i < clusters; i++) {
            int x = NOISE.nextInt(w);
            int y = NOISE.nextInt(h);
            int width = 1;
            float variance = 0.45f + NOISE.nextFloat() * 0.4f;
            int alpha = (int)(Math.min(1.0f, grainBase * variance) * 38.0f);
            if (alpha <= 0) continue;
            int shade = 124 + NOISE.nextInt(20);
            int color = FastColor.ARGB32.color(alpha, shade, shade - 8, shade - 10);
            g.fill(x, y, Math.min(w, x + width), Math.min(h, y + 1), color);
        }
    }
}
