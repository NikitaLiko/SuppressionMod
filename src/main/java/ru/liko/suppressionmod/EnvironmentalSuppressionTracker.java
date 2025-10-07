package ru.liko.suppressionmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import ru.liko.suppressionmod.client.ClientConfigState;

import java.util.HashSet;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class EnvironmentalSuppressionTracker {

    private static final Set<ResourceLocation> EXPLOSION_SOUNDS = new HashSet<>();

    static {
        EXPLOSION_SOUNDS.add(new ResourceLocation("minecraft", "entity.generic.explode"));
        EXPLOSION_SOUNDS.add(new ResourceLocation("minecraft", "entity.creeper.explode"));
        EXPLOSION_SOUNDS.add(new ResourceLocation("minecraft", "entity.dragon_fireball.explode"));
        EXPLOSION_SOUNDS.add(new ResourceLocation("minecraft", "entity.firework_rocket.large_blast"));
    }

    private final BulletSuppressionHandler suppression;

    public EnvironmentalSuppressionTracker(BulletSuppressionHandler suppression) {
        this.suppression = suppression;
    }

    @SubscribeEvent
    public void onPlaySound(PlaySoundEvent event) {
        SoundInstance sound = event.getSound();
        if (sound == null) return;

        ResourceLocation id = sound.getLocation();
        if (!isExplosionSound(id)) return;
        if (sound.isRelative()) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (mc == null || player == null || mc.level == null) return;

        double sx = sound.getX();
        double sy = sound.getY();
        double sz = sound.getZ();
        double distance = Math.sqrt(player.distanceToSqr(sx, sy, sz));
        double maxRange = ClientConfigState.explosionMaxRange();
        if (distance > maxRange) return;

        float volume = sound.getVolume();
        suppression.addExplosionShock(distance, volume);
    }

    private static boolean isExplosionSound(ResourceLocation id) {
        if (id == null) return false;
        if (EXPLOSION_SOUNDS.contains(id)) return true;
        String path = id.getPath();
        return path.contains("explode") || path.contains("explosion");
    }
}
