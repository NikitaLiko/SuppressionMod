package ru.liko.suppressionmod;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class BulletSuppressionHandler {

    private final Random rnd = new Random();

    private int suppressionLevel = 0;
    private int targetLevel = 0;
    private int recentHitCount = 0;
    private int hitCounterTicks = 0;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.isPaused() || mc.player == null || mc.level == null) return;

        int maxLevel = Config.MAX_SUPPRESSION_LEVEL.get();
        int recovery = Config.RECOVERY_RATE.get();
        int targetDecay = Config.TARGET_DECAY_RATE.get();

        if (suppressionLevel < targetLevel) {
            int ramp = Math.max(4, (targetLevel - suppressionLevel) / 5);
            suppressionLevel = Math.min(targetLevel, suppressionLevel + ramp);
        } else if (suppressionLevel > 0) {
            suppressionLevel = Math.max(0, suppressionLevel - recovery);
        }

        if (targetLevel > 0) {
            targetLevel = Math.max(0, targetLevel - targetDecay);
        }

        if (hitCounterTicks > 0) {
            hitCounterTicks--;
            if (hitCounterTicks == 0) {
                recentHitCount = 0;
            }
        }

        if (suppressionLevel > 0) {
            applySquadShake(mc.player, suppressionLevel, maxLevel);
        }
    }

    public void addSuppressionImpact(int baseImpact, double distance, double velocity) {
        int maxLevel = Config.MAX_SUPPRESSION_LEVEL.get();

        double maxDist = Config.MAX_DETECTION_RANGE.get();
        double distanceFactor = Math.max(0.35, 1.0 - (distance / maxDist));

        double velocityFactor = 1.0;
        if (Config.ENABLE_VELOCITY_SCALING.get()) {
            velocityFactor = Math.min(1.8, 0.9 + velocity * 2.0);
        }

        int impact = (int)(baseImpact * distanceFactor * velocityFactor);

        if (Config.ENABLE_NONLINEAR_ACCUMULATION.get() && recentHitCount >= 2) {
            double multiplier = Config.ACCUMULATION_MULTIPLIER.get();
            impact = (int)(impact * Math.pow(multiplier, Math.min(recentHitCount - 1, 4)));
        }

        targetLevel = Math.min(maxLevel, targetLevel + impact);
        recentHitCount++;
        hitCounterTicks = 40;
    }

    private void applySquadShake(Player p, int level, int maxLevel) {
        float t = Math.min(level / (float)maxLevel, 1.0f);
        float baseIntensity = (float)(t * Config.SHAKE_INTENSITY.get());

        float stress = (float)Math.pow(t, 1.2);
        float intensity = baseIntensity * (0.7f + 0.3f * stress);

        float pitch = (rnd.nextFloat() - 0.5f) * intensity * 20.0f;
        float yaw   = (rnd.nextFloat() - 0.5f) * intensity * 20.0f;

        p.setXRot(p.getXRot() + pitch);
        p.setYRot(p.getYRot() + yaw);
    }

    public int getSuppressionLevel() { return suppressionLevel; }
}
