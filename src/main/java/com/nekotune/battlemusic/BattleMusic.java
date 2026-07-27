package com.nekotune.battlemusic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.nekotune.battlemusic.compat.AetherCompat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Enemy;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(BattleMusic.MOD_ID)
public class BattleMusic {
    public static final String MOD_ID = "battlemusic";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final float VOLUME_REDUCTION = 2f;
    public static final double MAX_SONG_RANGE = 256D;
    public static final Set<Mob> QUEUED_TO_PLAY = new HashSet<>();
    public static BattleMusicInstance playing = null;
    private static float volume = 1f;

    public BattleMusic(IEventBus modEventBus, ModContainer modContainer) {
        // Register setup events
        modEventBus.addListener(this::onClientSetup);

        BMSounds.register(modEventBus);

        // Register our mod's BMConfigpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.CLIENT, BMConfig.SPEC);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        updateEntitySoundData();
        setVolume(BMConfig.VOLUME.get().floatValue());
    }

    // Static hashmap of what entities play what sounds
    public static class EntitySoundData {
        public SoundEvent soundEvent;
        public int priority;

        public EntitySoundData(SoundEvent soundEvent, int priority) {
            this.soundEvent = soundEvent;
            this.priority = priority;
        }
    }
    protected static final HashMap<EntityType<?>, EntitySoundData> ENTITY_SOUND_DATA = new HashMap<>();
    public static void updateEntitySoundData() {
        ENTITY_SOUND_DATA.clear();
        List<? extends String> entityDataStrings = BMConfig.ENTITIES_SONGS.get();
        final String ERROR_MSG = "Error loading entity music data from battlemusic config: ";
        for (String entityDataString : entityDataStrings) {
            EntityType<?> entityType = null;
            SoundEvent soundEvent = null;

            String entityString = entityDataString.substring(0, entityDataString.indexOf(';'));
            DataResult<ResourceLocation> weakEntityResource = ResourceLocation.read(entityString);
            if (weakEntityResource.result().isPresent()) {
                ResourceLocation resource = weakEntityResource.getOrThrow();
                entityType = BuiltInRegistries.ENTITY_TYPE.get(resource);
            }
            if (entityType == null || entityType == EntityType.PIG) {
                LOGGER.warn(ERROR_MSG + "Skipping invalid entity ID \"{}\" (You can ignore this warning)", entityString);
                continue;
            }

            String soundString = entityDataString.substring(entityDataString.indexOf(';') + 1, entityDataString.lastIndexOf(';'));
            DataResult<ResourceLocation> weakSoundResource = ResourceLocation.read(soundString);
            if (weakSoundResource.result().isPresent()) {
                ResourceLocation resource = weakSoundResource.getOrThrow();
                soundEvent = BuiltInRegistries.SOUND_EVENT.get(resource);
            }
            if (soundEvent == null) {
                LOGGER.error(ERROR_MSG + "Invalid sound ID \"{}\" in line \"{}\", skipping", soundString, entityDataString);
                continue;
            }

            int priority = 0;
            String priorityString = entityDataString.substring(entityDataString.lastIndexOf(';') + 1, entityDataString.lastIndexOf(';') + 2);
            try {
                priority = Integer.parseInt(priorityString);
            } catch(Exception e) {
                LOGGER.error(ERROR_MSG + "Invalid priority \"{}\" in line \"{}\", defaulting to 0", priorityString, entityDataString);
            }

            LOGGER.debug("Added battle music {} to {} with priority {}", soundEvent.getLocation(), entityType, priority);
            ENTITY_SOUND_DATA.put(entityType, new EntitySoundData(soundEvent, priority));
        }

        String defaultSongString = BMConfig.DEFAULT_SONG.get();
        if (!defaultSongString.isEmpty()) {
            SoundEvent defaultSong = null;
            DataResult<ResourceLocation> weakDefaultSongResource = ResourceLocation.read(defaultSongString);
            if (weakDefaultSongResource.result().isPresent()) {
                ResourceLocation resource = weakDefaultSongResource.getOrThrow();
                defaultSong = BuiltInRegistries.SOUND_EVENT.get(resource);
            }
            if (defaultSong == null) {
                LOGGER.error(ERROR_MSG + "Invalid default song sound ID \"{}\"", defaultSongString);
            } else {
                final SoundEvent ds = defaultSong;
                BuiltInRegistries.ENTITY_TYPE.stream().forEach(entityType -> {
                    ENTITY_SOUND_DATA.putIfAbsent(entityType, new EntitySoundData(ds, Integer.MIN_VALUE));
                });
            }
        }
        if (playing != null) {
            playing.destroy();
        }
        LOGGER.debug("[BATTLE MUSIC] Updated entity sound data");
    }
    public static HashMap<EntityType<?>, EntitySoundData> getEntitySoundData() {
        HashMap<EntityType<?>, EntitySoundData> clone = new HashMap<>();
        for (EntityType<?> key : ENTITY_SOUND_DATA.keySet()) {
            clone.put(key, ENTITY_SOUND_DATA.get(key));
        }
        return clone;
    }

    public static void setVolume(float newVolume) {
        newVolume /= VOLUME_REDUCTION;
        if (playing != null) {
            playing.setVolume(playing.getVolume() * (newVolume / volume));
            volume = newVolume;
            if (playing.getVolume() > volume) {
                playing.setVolume(volume);
            }
        } else volume = newVolume;
    }

    public static float getVolume() {
        if (BMConfig.LINKED_TO_MUSIC.get()) {
            return volume * Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MUSIC);
        }
        return volume;
    }

    public static boolean shouldPlayMusic(Mob mob, boolean toStart) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.isDeadOrDying()) return false;
        if (mob == null || mob.isDeadOrDying()) return false;
        if (ModList.get().isLoaded("aether")) {
            if (AetherCompat.isAetherBoss(mob)) return false;
        }
        if (ENTITY_SOUND_DATA.get(mob.getType()) == null) return false;
        final boolean neutralOrEnemy = 
                (mob instanceof final NeutralMob neutralMob && neutralMob.isAngryAt(player)) 
                || (mob instanceof Enemy);
        if (!(neutralOrEnemy)) return false;
        if (!(mob.level().dimensionType().equals(player.level().dimensionType()))) return false;
        if (mob.isSleeping() || mob.isNoAi()) return false;
        if (mob.isAlliedTo(player.self())) return false;
        AttributeInstance frAttribute = mob.getAttribute(Attributes.FOLLOW_RANGE);
        double followRange = (frAttribute != null) ? frAttribute.getValue() : MAX_SONG_RANGE;
        if (mob instanceof EnderDragon) {
            followRange = 300; // Because the ender dragon is special
        }
        if (toStart && (!player.hasLineOfSight(mob) || !mob.hasLineOfSight(player))) return false;
        return mob.canAttack(player, TargetingConditions.forCombat().range(followRange).ignoreLineOfSight().ignoreInvisibilityTesting());
    }

    public static void reload() {
        updateEntitySoundData();
        QUEUED_TO_PLAY.clear();
    }
}
