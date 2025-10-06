package ru.liko.suppressionmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SuppressionMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SuppressionOverlay {

    private static BulletSuppressionHandler handler;

    public static void setHandler(BulletSuppressionHandler h) { handler = h; }

    @SubscribeEvent
    public static void onOverlay(RenderGuiOverlayEvent.Post e) {
        if (handler == null) return;
        int level = handler.getSuppressionLevel();
        if (level <= 0) return;

        Minecraft mc = Minecraft.getInstance();
        GuiGraphics g = e.getGuiGraphics();
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();

        int maxLevel = Config.MAX_SUPPRESSION_LEVEL.get();
        float maxAlpha = Config.VIGNETTE_MAX_ALPHA.get().floatValue();

        float t = Math.min(level / (float)maxLevel, 1.0f);
        float intensity = (float)Math.pow(t, 1.4) * maxAlpha;

        int screenDarken = ((int)(intensity * 0.35f * 255) << 24) | 0x000000;
        g.fill(0, 0, w, h, screenDarken);

        drawVignette(g, w, h, intensity);
    }

    private static void drawVignette(GuiGraphics g, int w, int h, float intensity) {
        int vw = (int)(w * 0.35f);
        int vh = (int)(h * 0.35f);

        int steps = 30;

        for (int i = 0; i < steps; i++) {
            float ratio = 1.0f - ((float)i / steps);
            float alpha = intensity * 0.6f * (float)Math.pow(ratio, 2.0);
            int color = ((int)(alpha * 255) << 24) | 0x000000;

            int y = (vh * i) / steps;
            int nextY = (vh * (i + 1)) / steps;
            g.fill(0, y, w, nextY, color);
        }

        for (int i = 0; i < steps; i++) {
            float ratio = 1.0f - ((float)i / steps);
            float alpha = intensity * 0.6f * (float)Math.pow(ratio, 2.0);
            int color = ((int)(alpha * 255) << 24) | 0x000000;

            int y = h - (vh * (i + 1)) / steps;
            int nextY = h - (vh * i) / steps;
            g.fill(0, y, w, nextY, color);
        }

        for (int i = 0; i < steps; i++) {
            float ratio = 1.0f - ((float)i / steps);
            float alpha = intensity * 0.6f * (float)Math.pow(ratio, 2.0);
            int color = ((int)(alpha * 255) << 24) | 0x000000;

            int x = (vw * i) / steps;
            int nextX = (vw * (i + 1)) / steps;
            g.fill(x, 0, nextX, h, color);
        }

        for (int i = 0; i < steps; i++) {
            float ratio = 1.0f - ((float)i / steps);
            float alpha = intensity * 0.6f * (float)Math.pow(ratio, 2.0);
            int color = ((int)(alpha * 255) << 24) | 0x000000;

            int x = w - (vw * (i + 1)) / steps;
            int nextX = w - (vw * i) / steps;
            g.fill(x, 0, nextX, h, color);
        }
    }
}
