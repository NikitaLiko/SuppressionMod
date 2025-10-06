package ru.liko.suppressionmod;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.DoubleValue MAX_DETECTION_RANGE;
    public static final ForgeConfigSpec.IntValue MAX_SUPPRESSION_LEVEL;
    public static final ForgeConfigSpec.IntValue SINGLE_BULLET_IMPACT;
    public static final ForgeConfigSpec.DoubleValue ACCUMULATION_MULTIPLIER;
    public static final ForgeConfigSpec.IntValue RECOVERY_RATE;
    public static final ForgeConfigSpec.IntValue TARGET_DECAY_RATE;
    public static final ForgeConfigSpec.DoubleValue SHAKE_INTENSITY;
    public static final ForgeConfigSpec.DoubleValue VIGNETTE_MAX_ALPHA;
    public static final ForgeConfigSpec.BooleanValue ENABLE_VELOCITY_SCALING;
    public static final ForgeConfigSpec.BooleanValue ENABLE_NONLINEAR_ACCUMULATION;

    static {
        BUILDER.push("Suppression Mod - Server Configuration");
        BUILDER.comment("=== Настройки подавления для сервера ===");
        BUILDER.comment("Эти настройки синхронизируются со всеми клиентами автоматически");

        BUILDER.push("Detection");
        MAX_DETECTION_RANGE = BUILDER
                .comment("Максимальная дистанция обнаружения пуль (блоки)")
                .defineInRange("maxDetectionRange", 16.0, 4.0, 64.0);
        BUILDER.pop();

        BUILDER.push("Intensity");
        MAX_SUPPRESSION_LEVEL = BUILDER
                .comment("Максимальный уровень подавления (очки)")
                .defineInRange("maxSuppressionLevel", 100, 20, 300);

        SINGLE_BULLET_IMPACT = BUILDER
                .comment("Базовое воздействие одной пули")
                .defineInRange("singleBulletImpact", 20, 5, 50);

        ACCUMULATION_MULTIPLIER = BUILDER
                .comment("Множитель накопления при серии")
                .defineInRange("accumulationMultiplier", 1.4, 1.0, 2.5);
        BUILDER.pop();

        BUILDER.push("Recovery");
        RECOVERY_RATE = BUILDER
                .comment("Скорость спада текущего уровня (очки/тик)")
                .defineInRange("recoveryRate", 1, 1, 5);

        TARGET_DECAY_RATE = BUILDER
                .comment("Скорость спада целевого уровня (очки/тик)")
                .defineInRange("targetDecayRate", 2, 1, 10);
        BUILDER.pop();

        BUILDER.push("Visual");
        SHAKE_INTENSITY = BUILDER
                .comment("Интенсивность тряски камеры (0.0-1.0)")
                .defineInRange("shakeIntensity", 0.25, 0.0, 1.0);

        VIGNETTE_MAX_ALPHA = BUILDER
                .comment("Максимальная прозрачность виньетки (0.0-1.0)")
                .defineInRange("vignetteMaxAlpha", 0.75, 0.0, 1.0);
        BUILDER.pop();

        BUILDER.push("Advanced");
        ENABLE_VELOCITY_SCALING = BUILDER
                .comment("Учитывать скорость пули (быстрые пули = сильнее эффект)")
                .define("enableVelocityScaling", true);

        ENABLE_NONLINEAR_ACCUMULATION = BUILDER
                .comment("Нелинейное накопление (3+ пули подряд усиливают эффект)")
                .define("enableNonlinearAccumulation", true);
        BUILDER.pop();

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
