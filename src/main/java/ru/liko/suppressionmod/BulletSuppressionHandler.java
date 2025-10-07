package ru.liko.suppressionmod;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import ru.liko.suppressionmod.client.ClientConfigState;

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

        int maxLevel = ClientConfigState.maxSuppressionLevel();
        int recovery = ClientConfigState.recoveryRate();
        int targetDecay = ClientConfigState.targetDecayRate();

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
        double maxDist = ClientConfigState.maxDetectionRange();
        double distanceFactor = Math.max(0.35, 1.0 - (distance / maxDist));

        double velocityFactor = 1.0;
        if (ClientConfigState.enableVelocityScaling()) {
            velocityFactor = Math.min(1.8, 0.9 + velocity * 2.0);
        }

        int impact = (int)(baseImpact * distanceFactor * velocityFactor);

        if (ClientConfigState.enableNonlinearAccumulation() && recentHitCount >= 2) {
            double multiplier = ClientConfigState.accumulationMultiplier();
            impact = (int)(impact * Math.pow(multiplier, Math.min(recentHitCount - 1, 4)));
        }

        int flashBoost = 2 + (int)(distanceFactor * 4);
        accumulateSuppression(impact, true, flashBoost);
    }

    public void addProjectileImpact(double distance, double velocity) {
        double maxDist = ClientConfigState.nearImpactMaxRange();
        if (distance > maxDist) return;

        double distanceFactor = Math.max(0.2, 1.0 - (distance / maxDist));
        double speedFactor = 1.0 + Math.min(1.5, velocity * 3.2);

        int baseImpact = ClientConfigState.nearImpactBaseImpact();
        int impact = (int)(baseImpact * distanceFactor * speedFactor);
        int flashBoost = 6 + (int)(distanceFactor * 8);

        accumulateSuppression(impact, true, flashBoost);
    }

    public void addExplosionShock(double distance, float volume) {
        double maxDist = ClientConfigState.explosionMaxRange();
        if (distance > maxDist) return;

        double distanceFactor = Math.max(0.0, 1.0 - (distance / maxDist));
        double shaped = Math.pow(distanceFactor, 1.35);
        double volumeFactor = 0.85 + Math.min(1.5, volume) * 0.45;

        int baseImpact = ClientConfigState.explosionBaseImpact();
        int impact = (int)(baseImpact * shaped * volumeFactor);
        int flashBoost = 10 + (int)(distanceFactor * 12);

        accumulateSuppression(impact, false, flashBoost);
    }

    private void applySquadShake(Player p, int level, int maxLevel) {
        float t = Math.min(level / (float)maxLevel, 1.0f);
        float baseIntensity = (float)(t * ClientConfigState.shakeIntensity());

        float stress = (float)Math.pow(t, 1.2);
        float intensity = baseIntensity * (0.7f + 0.3f * stress);

        float pitch = (rnd.nextFloat() - 0.5f) * intensity * 20.0f;
        float yaw   = (rnd.nextFloat() - 0.5f) * intensity * 20.0f;

        p.setXRot(p.getXRot() + pitch);
        p.setYRot(p.getYRot() + yaw);
    }

    private void accumulateSuppression(int impact, boolean trackBurst, int flashBoost) {
        if (impact <= 0) return;
        int maxLevel = ClientConfigState.maxSuppressionLevel();
        targetLevel = Math.min(maxLevel, targetLevel + impact);

        if (trackBurst) {
            recentHitCount++;
            hitCounterTicks = 40;
        }

        if (flashBoost > 0) {
            shockFlashTicks = Math.min(60, shockFlashTicks + flashBoost);
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
