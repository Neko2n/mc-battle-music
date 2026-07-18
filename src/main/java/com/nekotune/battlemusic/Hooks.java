package com.nekotune.battlemusic;

import java.util.List;

import com.nekotune.battlemusic.BattleMusic.EntitySoundData;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = BattleMusic.MOD_ID)
public class Hooks {
    // Register commands
    @SubscribeEvent
    public static void onCommandRegister(final RegisterClientCommandsEvent event) {
        BMCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(final PlayerEvent.PlayerChangedDimensionEvent event) {
        if (BattleMusic.playing != null) {
            BattleMusic.playing.destroy();
        }
        BattleMusic.QUEUED_TO_PLAY.clear();
    }

    // Update queued entities
    @SubscribeEvent
    public static void onEntityTick(final EntityTickEvent.Post event) {
        final Entity entity = event.getEntity();
        if (!(entity instanceof final LivingEntity living)) return;
        final LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || living.level() != player.level())
            return;

        if (living instanceof Mob) {
            if (BattleMusic.shouldPlayMusic((Mob) living, true)) {
                if (!BattleMusic.QUEUED_TO_PLAY.contains(living)) {
                    BattleMusic.QUEUED_TO_PLAY.add((Mob) living);
                }
            }
        }
    }

    // Update battle music
    @SubscribeEvent
    public static void onLevelTick(final LevelTickEvent.Post event) {
        final LocalPlayer player = Minecraft.getInstance().player;
        if (player == null)
            return;
        
        EntitySoundData soundData = null;
        Mob entity = null;
        for (final Mob e : List.copyOf(BattleMusic.QUEUED_TO_PLAY)) {

            // Remove and skip invalid entities
            if (!BattleMusic.shouldPlayMusic(e, false)) {
                BattleMusic.QUEUED_TO_PLAY.remove(e);
                continue;
            }

            final EntitySoundData sd = BattleMusic.getEntitySoundData().get(e.getType());

            if (BattleMusic.playing != null) {
                // Ensure this music has higher priority
                if (BattleMusic.playing.priority >= sd.priority) {
                    continue;
                }
                // If the music is already playing at a lower priority, just change the priority
                // and entity
                if (BattleMusic.playing.soundEvent.getLocation().equals(sd.soundEvent.getLocation())) {
                    BattleMusic.playing.priority = sd.priority;
                    BattleMusic.playing.entity = e;
                    continue;
                }
            }

            // Only overwrite if priority is higher and music is different
            if (soundData != null) {
                if (soundData.priority >= sd.priority) {
                    continue;
                }
                if (soundData.soundEvent.getLocation().equals(sd.soundEvent.getLocation())) {
                    soundData = sd;
                    continue;
                }
            }

            soundData = sd;
            entity = e;
        }

        // Play battle music
        final SoundManager sounds = Minecraft.getInstance().getSoundManager();
        if (soundData != null && BattleMusic.shouldPlayMusic(entity, true)) {
            if (BattleMusic.playing != null) {
                BattleMusic.playing.destroy();
            }
            BattleMusic.playing = new BattleMusicInstance(soundData, entity);
            sounds.queueTickingSound(BattleMusic.playing);
        }
    }
}
