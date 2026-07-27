package com.nekotune.battlemusic;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.nekotune.battlemusic.compat.BMCataclysmCompat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mod(BattleMusic.MOD_ID)
public class BattleMusic {
    public static final String MOD_ID = "battlemusic";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String CONFIG_FILE = MOD_ID + ".toml";
    public static final float VOLUME_REDUCTION = 2f;
    public static final double MAX_SONG_RANGE = 256D;
    public static BattleMusicInstance playing = null;
    private static float volume = 1f;
    public static final Set<Mob> QUEUED_ENTITIES = new HashSet<>();

    public BattleMusic() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModSounds.register(modEventBus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT,
                ModConfigs.SPEC, CONFIG_FILE);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        updateEntitySoundData();
        setVolume(ModConfigs.VOLUME.get().floatValue());
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

    private static final HashMap<EntityType<?>, EntitySoundData> ENTITY_SOUND_DATA = new HashMap<>();

    public static void updateEntitySoundData() {
        ENTITY_SOUND_DATA.clear();
        List<? extends String> entityDataStrings = ModConfigs.ENTITIES_SONGS.get();
        final String ERROR_MSG = "Error loading entity music data from battlemusic config: ";
        for (String entityDataString : entityDataStrings) {
            EntityType<?> entityType = null;
            SoundEvent soundEvent = null;

            String entityString = entityDataString.substring(0, entityDataString.indexOf(';'));
            DataResult<ResourceLocation> weakEntityResource = ResourceLocation.read(entityString);
            if (weakEntityResource.result().isPresent()) {
                ResourceLocation resource = weakEntityResource.get().left().get();
                entityType = ForgeRegistries.ENTITY_TYPES.getValue(resource);
            }
            if (entityType == null || entityType == EntityType.PIG) {
                LOGGER.warn(ERROR_MSG + "Skipping invalid entity ID \"{}\" (You can ignore this warning)",
                        entityString);
                continue;
            }

            String soundString = entityDataString.substring(entityDataString.indexOf(';') + 1,
                    entityDataString.lastIndexOf(';'));
            DataResult<ResourceLocation> weakSoundResource = ResourceLocation.read(soundString);
            if (weakSoundResource.result().isPresent()) {
                ResourceLocation resource = weakSoundResource.get().left().get();
                soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(resource);
            }
            if (soundEvent == null) {
                LOGGER.error(ERROR_MSG + "Invalid sound ID \"{}\" in line \"{}\", skipping", soundString,
                        entityDataString);
                continue;
            }

            int priority = 0;
            String priorityString = entityDataString.substring(entityDataString.lastIndexOf(';') + 1,
                    entityDataString.lastIndexOf(';') + 2);
            try {
                priority = Integer.parseInt(priorityString);
            } catch (Exception e) {
                LOGGER.error(ERROR_MSG + "Invalid priority \"{}\" in line \"{}\", defaulting to 0", priorityString,
                        entityDataString);
            }

            LOGGER.debug("Added battle music {} to {} with priority {}", soundEvent.getLocation(), entityType,
                    priority);
            ENTITY_SOUND_DATA.put(entityType, new EntitySoundData(soundEvent, priority));
        }

        String defaultSongString = ModConfigs.DEFAULT_SONG.get();
        if (!defaultSongString.isEmpty()) {
            SoundEvent defaultSong = null;
            DataResult<ResourceLocation> weakDefaultSongResource = ResourceLocation.read(defaultSongString);
            if (weakDefaultSongResource.result().isPresent()) {
                ResourceLocation resource = weakDefaultSongResource.get().left().get();
                defaultSong = ForgeRegistries.SOUND_EVENTS.getValue(resource);
            }
            if (defaultSong == null) {
                LOGGER.error(ERROR_MSG + "Invalid default song sound ID \"{}\"", defaultSongString);
            } else {
                final SoundEvent ds = defaultSong;
                ForgeRegistries.ENTITY_TYPES.forEach((final EntityType<?> entityType) -> {
                    if (entityType.getCategory() != MobCategory.MONSTER)
                        return;
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
        volume = newVolume / VOLUME_REDUCTION;
    }

    public static float getVolume() {
        if (ModConfigs.LINKED_TO_MUSIC.get()) {
            return volume * Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MUSIC);
        }
        return volume;
    }

    public static boolean shouldPlayMusic(Mob mob, boolean toStart) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.isDeadOrDying())
            return false;
        if (mob == null)
            return false;
        if (ENTITY_SOUND_DATA.get(mob.getType()) == null)
            return false;
        if (ModList.get().isLoaded("cataclysm")) {
            if (!BMCataclysmCompat.validate(mob))
                return false;
        }
        if (mob instanceof final NeutralMob neutralMob
                && !neutralMob.isAngryAt(player))
            return false;
        if (mob.isDeadOrDying()
                || mob.isNoAi()
                || mob.isSilent()
                || !(mob.level().dimensionType().equals(player.level().dimensionType()))
                || mob.isSleeping()
                || mob.isAlliedTo(player.self()))
            return false;
        AttributeInstance frAttribute = mob.getAttribute(Attributes.FOLLOW_RANGE);
        double followRange = (frAttribute != null) ? frAttribute.getValue() : MAX_SONG_RANGE;
        if (mob instanceof EnderDragon) {
            followRange = 300; // Because the ender dragon is special
        }
        if (toStart && (!player.hasLineOfSight(mob) || !mob.hasLineOfSight(player)))
            return false;
        return mob.canAttack(player,
                TargetingConditions.forCombat().range(followRange).ignoreLineOfSight().ignoreInvisibilityTesting());
    }

    public static void reload() {
        updateEntitySoundData();
        QUEUED_ENTITIES.clear();
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = BattleMusic.MOD_ID)
    public static abstract class ForgeEvents {
        // Register commands
        @SubscribeEvent
        public static void onCommandRegister(RegisterClientCommandsEvent event) {
            ModCommands.register(event.getDispatcher());
        }

        @SubscribeEvent
        public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
            if (playing != null) {
                playing.destroy();
            }
            QUEUED_ENTITIES.clear();
        }

        // Update valid entities
        @SubscribeEvent
        public static void onLivingTick(LivingEvent.LivingTickEvent event) {
            LivingEntity entity = event.getEntity();
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null || entity.level() != player.level())
                return;

            if (entity instanceof Mob) {
                if (shouldPlayMusic((Mob) entity, true)) {
                    if (!QUEUED_ENTITIES.contains(entity)) {
                        QUEUED_ENTITIES.add((Mob) entity);
                    }
                }
            }
        }

        // Update battle music
        @SubscribeEvent
        public static void onLevelTick(final LevelTickEvent event) {
            if (event.phase != TickEvent.Phase.END)
                return;
            final LocalPlayer player = Minecraft.getInstance().player;
            if (player == null)
                return;

            EntitySoundData soundData = null;
            Mob entity = null;
            for (final Mob e : List.copyOf(QUEUED_ENTITIES)) {

                // Remove and skip invalid entities
                if (!BattleMusic.shouldPlayMusic(e, false)) {
                    QUEUED_ENTITIES.remove(e);
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

                // Handle mod overrides
                boolean hasExistingMusic = BMCataclysmCompat.hasExistingMusic(entity);

                if (!hasExistingMusic) {
                    if (BattleMusic.playing != null) {
                        BattleMusic.playing.destroy();
                    }
                    BattleMusic.playing = new BattleMusicInstance(soundData, entity);
                    sounds.queueTickingSound(BattleMusic.playing);
                }
            }
        }
    }
}
