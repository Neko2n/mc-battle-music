package com.nekotune.battlemusic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import java.util.List;

import static com.nekotune.battlemusic.BattleMusic.validEntities;

@OnlyIn(Dist.CLIENT)
public class BattleMusicInstance extends SimpleSoundInstance
{
    // Fields
    public Mob entity;
    public SoundEvent soundEvent;
    public int priority;
    public float fadeLength;
    public boolean fadeOut = false;

    // Constructors
    public BattleMusicInstance(BattleMusic.EntitySoundData soundData, Mob entity) {
        super(soundData.soundEvent.getLocation(), SoundSource.MASTER,
                (ModConfigs.FADE_TIME.get().floatValue() == 0) ? BattleMusic.getVolume() : 0.0f,
                1.0F, SoundInstance.createUnseededRandom(), true,
                0, SoundInstance.Attenuation.NONE, 0.0D, 0.0D, 0.0D, true);
        this.soundEvent = soundData.soundEvent;
        this.priority = soundData.priority;
        this.entity = entity;
        this.fadeLength = ModConfigs.FADE_TIME.get().floatValue();
    }

    // Methods
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

    public void stop() {
        Minecraft.getInstance().getSoundManager().stop(this);
        this.fadeLength = 0;
        BattleMusic.playing = null;
    }

    public void fade(float seconds) {
        this.fadeOut = true;
        if (seconds == 0) {
            this.stop();
        } else {
            this.fadeLength = seconds;
        }
    }

    public void tick() {
        if (!Minecraft.getInstance().getSoundManager().isActive(this)) return;

        // Fade
        if (this.fadeLength > 0f) {
            if (!this.fadeOut) {
                this.volume += (BattleMusic.getVolume()) / (this.fadeLength *20);
                if (this.volume >= BattleMusic.getVolume()) {
                    this.volume = BattleMusic.getVolume();
                    this.fadeLength = 0f;
                }
            } else {
                if (validEntities.contains(this.entity)) {
                    this.fadeOut = false;
                } else {
                    this.volume -= (BattleMusic.getVolume()) / (this.fadeLength * 20);
                    if (this.volume <= 0) {
                        this.stop();
                    }
                }
                return;
            }
        } else if (!validEntities.contains(this.entity)) {
            this.fade(Math.max(ModConfigs.FADE_TIME.get().floatValue(), 2f));
        } else {
            this.volume = BattleMusic.getVolume();
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        // Pitch up music when at low health (unless fighting the warden)
        float pitch = 1f;
        boolean belowHpThreshold;
        if (ModConfigs.HEALTH_PITCH_PERCENT.get()) {
            belowHpThreshold = (player.getHealth()/player.getMaxHealth())*100 <= ModConfigs.HEALTH_PITCH_THRESH.get();
        } else {
            belowHpThreshold = player.getHealth() <= ModConfigs.HEALTH_PITCH_THRESH.get();
        }
        if (belowHpThreshold && !(this.entity instanceof Warden)) {
            pitch += ModConfigs.HEALTH_PITCH_AMOUNT.get();
        }

        // Pitch up music during second phase of dragon and wither fights
        if (ModConfigs.PHASE2_PITCH_AMOUNT.get() != 0) {
            if (this.entity instanceof EnderDragon) {
                List<EndCrystal> list = this.entity.level().getEntitiesOfClass(EndCrystal.class, AABB.ofSize(new Vec3(0, 64, 0), 128, 128, 128));
                if (list.isEmpty()) {
                    pitch += ModConfigs.PHASE2_PITCH_AMOUNT.get();
                }
            } else if (this.entity instanceof WitherBoss && ((WitherBoss) this.entity).isPowered()) {
                pitch += ModConfigs.PHASE2_PITCH_AMOUNT.get();
            }
        }

        // Apply pitch changes
        this.pitch = pitch;
    }
}
