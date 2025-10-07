package ru.liko.suppressionmod;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.liko.suppressionmod.client.ClientConfigState;

@Mod.EventBusSubscriber(modid = SuppressionMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SuppressionOverlay {

    private static final ResourceLocation OVERLAY_TEXTURE =
            new ResourceLocation(SuppressionMod.MOD_ID, "textures/gui/suppression_vignette.png");

    private static BulletSuppressionHandler handler;
    private static float smoothedOverlayAlpha = 0.0f;
    private static float smoothedTintAlpha = 0.0f;

    public static void setHandler(BulletSuppressionHandler h) {
        handler = h;
        smoothedOverlayAlpha = 0.0f;
        smoothedTintAlpha = 0.0f;
    }

    @SubscribeEvent
    public static void onOverlay(RenderGuiOverlayEvent.Post event) {
        if (handler == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getWindow() == null) return;

        float suppression = handler.getSuppressionRatio();
        float flash = handler.getShockFlashIntensity();

        float maxAlpha = (float)ClientConfigState.vignetteMaxAlpha();
        float tintScale = (float)ClientConfigState.uiTintStrength();
        float vignetteScale = (float)ClientConfigState.uiVignetteStrength();
        float flashScale = (float)ClientConfigState.uiFlashStrength();
        float fadeIn = Mth.clamp((float)ClientConfigState.uiFadeInSpeed(), 0.01f, 1.0f);
        float fadeOut = Mth.clamp((float)ClientConfigState.uiFadeOutSpeed(), 0.01f, 1.0f);

        float baseTarget = Math.min(1.0f, suppression * maxAlpha * 0.7f) * vignetteScale;
        float flashTarget = Math.min(1.0f, flash * 0.65f) * flashScale;

        float overlayTarget = Math.min(1.0f, baseTarget * 0.5f + flashTarget * 0.8f);
        float tintTarget = Math.min(1.0f, (baseTarget * 0.3f + flashTarget * 0.2f) * tintScale);

        float overlayLerp = overlayTarget > smoothedOverlayAlpha ? fadeIn : fadeOut;
        float tintLerp = tintTarget > smoothedTintAlpha ? fadeIn : fadeOut;
        smoothedOverlayAlpha = Mth.lerp(overlayLerp, smoothedOverlayAlpha, overlayTarget);
        smoothedTintAlpha = Mth.lerp(tintLerp, smoothedTintAlpha, tintTarget);

        if (smoothedOverlayAlpha <= 0.01f && smoothedTintAlpha <= 0.01f) return;

        GuiGraphics graphics = event.getGuiGraphics();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        if (smoothedTintAlpha > 0.01f) {
            int tint = FastColor.ARGB32.color((int)(smoothedTintAlpha * 90.0f), 32, 28, 26);
            graphics.fill(0, 0, width, height, tint);
        }

        if (smoothedOverlayAlpha > 0.01f) {
            RenderSystem.enableBlend();
            graphics.setColor(1.0f, 1.0f, 1.0f, Math.min(1.0f, smoothedOverlayAlpha));
            graphics.blit(OVERLAY_TEXTURE, 0, 0, width, height, 0, 0, 256, 256, 256, 256);
            graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.disableBlend();
        }
    }
}
