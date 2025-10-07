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
    private int shockFlashTicks = 0;

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

        if (shockFlashTicks > 0) {
            shockFlashTicks--;
        }

        if (suppressionLevel > 0) {
            applySquadShake(mc.player, suppressionLevel, maxLevel);
        }
    }

    public void addSuppressionImpact(int baseImpact, double distance, double velocity) {
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

        int flashBoost = 3 + (int)(distanceFactor * 5);
        accumulateSuppression(impact, true, flashBoost);
    }

    public void addProjectileImpact(double distance, double velocity) {
        double maxDist = Config.NEAR_IMPACT_MAX_RANGE.get();
        if (distance > maxDist) return;

        double distanceFactor = Math.max(0.2, 1.0 - (distance / maxDist));
        double speedFactor = 1.0 + Math.min(1.5, velocity * 3.2);

        int baseImpact = Config.NEAR_IMPACT_BASE_IMPACT.get();
        int impact = (int)(baseImpact * distanceFactor * speedFactor);
        int flashBoost = 8 + (int)(distanceFactor * 10);

        accumulateSuppression(impact, true, flashBoost);
    }

    public void addExplosionShock(double distance, float volume) {
        double maxDist = Config.EXPLOSION_MAX_RANGE.get();
        if (distance > maxDist) return;

        double distanceFactor = Math.max(0.0, 1.0 - (distance / maxDist));
        double shaped = Math.pow(distanceFactor, 1.35);
        double volumeFactor = 0.85 + Math.min(1.5, volume) * 0.45;

        int baseImpact = Config.EXPLOSION_BASE_IMPACT.get();
        int impact = (int)(baseImpact * shaped * volumeFactor);
        int flashBoost = 12 + (int)(distanceFactor * 14);

        accumulateSuppression(impact, false, flashBoost);
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

    private void accumulateSuppression(int impact, boolean trackBurst, int flashBoost) {
        if (impact <= 0) return;
        int maxLevel = Config.MAX_SUPPRESSION_LEVEL.get();
        targetLevel = Math.min(maxLevel, targetLevel + impact);

        if (trackBurst) {
            recentHitCount++;
            hitCounterTicks = 40;
        }

        if (flashBoost > 0) {
            shockFlashTicks = Math.min(80, shockFlashTicks + flashBoost);
        }
    }

    public int getSuppressionLevel() { return suppressionLevel; }

    public float getSuppressionRatio() {
        int maxLevel = Config.MAX_SUPPRESSION_LEVEL.get();
        if (maxLevel <= 0) return 0.0f;
        return Math.min(1.0f, suppressionLevel / (float)maxLevel);
    }

    public float getShockFlashIntensity() {
        return Math.min(1.0f, shockFlashTicks / 40.0f);
    }
}
