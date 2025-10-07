package ru.liko.suppressionmod.client;

import ru.liko.suppressionmod.Config;

public final class ClientConfigState {

    private static double maxDetectionRange;
    private static int maxSuppressionLevel;
    private static int singleBulletImpact;
    private static double accumulationMultiplier;
    private static int recoveryRate;
    private static int targetDecayRate;
    private static double shakeIntensity;
    private static double vignetteMaxAlpha;
    private static boolean enableVelocityScaling;
    private static boolean enableNonlinearAccumulation;
    private static int explosionBaseImpact;
    private static double explosionMaxRange;
    private static int nearImpactBaseImpact;
    private static double nearImpactMaxRange;
    private static double uiTintStrength;
    private static double uiVignetteStrength;
    private static double uiFlashStrength;
    private static double uiFadeInSpeed;
    private static double uiFadeOutSpeed;

    static {
        reloadFromLocalConfig();
    }

    private ClientConfigState() {}

    public static void reloadFromLocalConfig() {
        maxDetectionRange = Config.MAX_DETECTION_RANGE.get();
        maxSuppressionLevel = Config.MAX_SUPPRESSION_LEVEL.get();
        singleBulletImpact = Config.SINGLE_BULLET_IMPACT.get();
        accumulationMultiplier = Config.ACCUMULATION_MULTIPLIER.get();
        recoveryRate = Config.RECOVERY_RATE.get();
        targetDecayRate = Config.TARGET_DECAY_RATE.get();
        shakeIntensity = Config.SHAKE_INTENSITY.get();
        vignetteMaxAlpha = Config.VIGNETTE_MAX_ALPHA.get();
        enableVelocityScaling = Config.ENABLE_VELOCITY_SCALING.get();
        enableNonlinearAccumulation = Config.ENABLE_NONLINEAR_ACCUMULATION.get();
        explosionBaseImpact = Config.EXPLOSION_BASE_IMPACT.get();
        explosionMaxRange = Config.EXPLOSION_MAX_RANGE.get();
        nearImpactBaseImpact = Config.NEAR_IMPACT_BASE_IMPACT.get();
        nearImpactMaxRange = Config.NEAR_IMPACT_MAX_RANGE.get();
        uiTintStrength = Config.UI_TINT_STRENGTH.get();
        uiVignetteStrength = Config.UI_VIGNETTE_STRENGTH.get();
        uiFlashStrength = Config.UI_FLASH_STRENGTH.get();
        uiFadeInSpeed = Config.UI_FADE_IN_SPEED.get();
        uiFadeOutSpeed = Config.UI_FADE_OUT_SPEED.get();
    }

    public static void applyServerValues(double maxDetectionRange,
                                         int maxSuppressionLevel,
                                         int singleBulletImpact,
                                         double accumulationMultiplier,
                                         int recoveryRate,
                                         int targetDecayRate,
                                         double shakeIntensity,
                                         double vignetteMaxAlpha,
                                         boolean enableVelocityScaling,
                                         boolean enableNonlinearAccumulation,
                                         int explosionBaseImpact,
                                         double explosionMaxRange,
                                         int nearImpactBaseImpact,
                                         double nearImpactMaxRange,
                                         double uiTintStrength,
                                         double uiVignetteStrength,
                                         double uiFlashStrength,
                                         double uiFadeInSpeed,
                                         double uiFadeOutSpeed) {
        ClientConfigState.maxDetectionRange = maxDetectionRange;
        ClientConfigState.maxSuppressionLevel = maxSuppressionLevel;
        ClientConfigState.singleBulletImpact = singleBulletImpact;
        ClientConfigState.accumulationMultiplier = accumulationMultiplier;
        ClientConfigState.recoveryRate = recoveryRate;
        ClientConfigState.targetDecayRate = targetDecayRate;
        ClientConfigState.shakeIntensity = shakeIntensity;
        ClientConfigState.vignetteMaxAlpha = vignetteMaxAlpha;
        ClientConfigState.enableVelocityScaling = enableVelocityScaling;
        ClientConfigState.enableNonlinearAccumulation = enableNonlinearAccumulation;
        ClientConfigState.explosionBaseImpact = explosionBaseImpact;
        ClientConfigState.explosionMaxRange = explosionMaxRange;
        ClientConfigState.nearImpactBaseImpact = nearImpactBaseImpact;
        ClientConfigState.nearImpactMaxRange = nearImpactMaxRange;
        ClientConfigState.uiTintStrength = uiTintStrength;
        ClientConfigState.uiVignetteStrength = uiVignetteStrength;
        ClientConfigState.uiFlashStrength = uiFlashStrength;
        ClientConfigState.uiFadeInSpeed = uiFadeInSpeed;
        ClientConfigState.uiFadeOutSpeed = uiFadeOutSpeed;
    }

    public static double maxDetectionRange() { return maxDetectionRange; }
    public static int maxSuppressionLevel() { return maxSuppressionLevel; }
    public static int singleBulletImpact() { return singleBulletImpact; }
    public static double accumulationMultiplier() { return accumulationMultiplier; }
    public static int recoveryRate() { return recoveryRate; }
    public static int targetDecayRate() { return targetDecayRate; }
    public static double shakeIntensity() { return shakeIntensity; }
    public static double vignetteMaxAlpha() { return vignetteMaxAlpha; }
    public static boolean enableVelocityScaling() { return enableVelocityScaling; }
    public static boolean enableNonlinearAccumulation() { return enableNonlinearAccumulation; }
    public static int explosionBaseImpact() { return explosionBaseImpact; }
    public static double explosionMaxRange() { return explosionMaxRange; }
    public static int nearImpactBaseImpact() { return nearImpactBaseImpact; }
    public static double nearImpactMaxRange() { return nearImpactMaxRange; }
    public static double uiTintStrength() { return uiTintStrength; }
    public static double uiVignetteStrength() { return uiVignetteStrength; }
    public static double uiFlashStrength() { return uiFlashStrength; }
    public static double uiFadeInSpeed() { return uiFadeInSpeed; }
    public static double uiFadeOutSpeed() { return uiFadeOutSpeed; }
}
