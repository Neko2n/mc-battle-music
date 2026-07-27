package com.nekotune.battlemusic;

import net.neoforged.api.distmarker.OnlyIn;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;

@OnlyIn(Dist.CLIENT)
public class BattleMusicInstance extends AbstractTickableSoundInstance {
    public Mob entity;
    public SoundEvent soundEvent;
    public int priority;
    public float fadeLength;
    public boolean fadeOut = false;

    public BattleMusicInstance(BattleMusic.EntitySoundData soundData, Mob entity) {
        super(soundData.soundEvent, SoundSource.MASTER, SoundInstance.createUnseededRandom());
        this.volume = (BMConfig.FADE_TIME.get().floatValue() == 0) ? BattleMusic.getVolume() : 0.001f;
        this.looping = true;
        this.relative = true;

        this.soundEvent = soundData.soundEvent;
        this.priority = soundData.priority;
        this.entity = entity;
        this.fadeLength = BMConfig.FADE_TIME.get().floatValue();
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }
    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void destroy() {
        this.stop();
        Minecraft.getInstance().getSoundManager().stop(this);
        this.fadeLength = 0;
        if (BattleMusic.playing == this) {
            BattleMusic.playing = null;
        }
    }

    public void fade(float seconds) {
        this.fadeOut = true;
        if (seconds == 0) {
            this.destroy();
        } else {
            this.fadeLength = seconds;
        }
    }

    private boolean shouldFade() {
        if (BattleMusic.QUEUED_TO_PLAY.contains(this.entity)) return false;
        for (Mob candidate : BattleMusic.QUEUED_TO_PLAY) {
            final var entityType = candidate.getType();
            if (entityType == this.entity.getType()) {
                this.entity = candidate;
                return false;
            }
            final var soundData = BattleMusic.getEntitySoundData().get(entityType);
            if (soundData.soundEvent.getLocation().compareTo(this.getLocation()) == 0
                    && soundData.priority >= this.priority) {
                this.entity = candidate;
                return false;
            }
        };
        return true;
    }

    public void tick() {
        if (this.isStopped()) return;

        // Remove all non-battlemusic music
        Minecraft.getInstance().getSoundManager().stop(null, SoundSource.MUSIC);

        // Fade
        if (this.fadeLength > 0f) {
            if (!this.fadeOut) {
                this.volume += (BattleMusic.getVolume()) / (this.fadeLength *20);
                if (this.volume >= BattleMusic.getVolume()) {
                    this.volume = BattleMusic.getVolume();
                    this.fadeLength = 0f;
                }
            } else {
                if (shouldFade()) {
                    this.volume -= (BattleMusic.getVolume()) / (this.fadeLength * 20);
                    if (this.volume <= 0) {
                        this.destroy();
                    }
                } else {
                    this.fadeOut = false;
                }
                return;
            }
        } else if (shouldFade()) {
            this.fade(Math.max(BMConfig.FADE_TIME.get().floatValue(), 2f));
        } else {
            this.volume = BattleMusic.getVolume();
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        // Pitch up music when at low health (unless fighting the warden)
        float pitch = 1f;
        boolean belowHpThreshold;
        if (BMConfig.HEALTH_PITCH_PERCENT.get()) {
            belowHpThreshold = (player.getHealth()/player.getMaxHealth())*100 <= BMConfig.HEALTH_PITCH_THRESH.get();
        } else {
            belowHpThreshold = player.getHealth() <= BMConfig.HEALTH_PITCH_THRESH.get();
        }
        if (belowHpThreshold && !(this.entity instanceof Warden)) {
            pitch += BMConfig.HEALTH_PITCH_AMOUNT.get();
        }

        // Pitch up music during second phase of dragon and wither fights
        if (BMConfig.PHASE2_PITCH_AMOUNT.get() != 0) {
            if (this.entity instanceof EnderDragon) {
                List<EndCrystal> list = this.entity.level().getEntitiesOfClass(EndCrystal.class, AABB.ofSize(new Vec3(0, 64, 0), 128, 128, 128));
                if (list.isEmpty()) {
                    pitch += BMConfig.PHASE2_PITCH_AMOUNT.get();
                }
            } else if (this.entity instanceof WitherBoss && ((WitherBoss) this.entity).isPowered()) {
                pitch += BMConfig.PHASE2_PITCH_AMOUNT.get();
            }
        }

        // Apply pitch changes
        this.pitch = pitch;
    }
}
