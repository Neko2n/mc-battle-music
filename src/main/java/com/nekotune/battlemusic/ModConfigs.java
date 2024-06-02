package com.nekotune.battlemusic;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class ModConfigs
{
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Double> VOLUME;
    public static final ForgeConfigSpec.ConfigValue<Boolean> LINKED_TO_MUSIC;
    public static final ForgeConfigSpec.ConfigValue<Double> HEALTH_PITCH_AMOUNT;
    public static final ForgeConfigSpec.ConfigValue<Double> DRAGON_PITCH_AMOUNT;
    public static final ForgeConfigSpec.ConfigValue<Integer> HEALTH_PITCH_THRESH;
    public static final ForgeConfigSpec.ConfigValue<Boolean> HEALTH_PITCH_PERCENT;
    public static final ForgeConfigSpec.ConfigValue<Double> FADE_TIME;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ENTITIES_SONGS;
    public static final ForgeConfigSpec.ConfigValue<String> DEFAULT_SONG;

    static {
        BUILDER.push(BattleMusic.MOD_ID + " configs");

        VOLUME = BUILDER.comment("Volume of battle music, set to zero to mute")
                        .defineInRange("volume", 1D, 0D, 5D);
        LINKED_TO_MUSIC = BUILDER.comment("\nWhether battle music's volume is affected by the music slider")
                        .define("linked_to_music_volume", true);
        HEALTH_PITCH_AMOUNT = BUILDER.comment("\nHow much the battle music changes in pitch when at low health"
                        +"\n    > Values below zero shift the pitch down, above zero shift the pitch up"
                        +"\n    > Set to zero to disable")
                        .defineInRange("health_pitch_amount", 0.05D, -0.5D, 1D);
        DRAGON_PITCH_AMOUNT = BUILDER.comment("\nHow much the battle music changes in pitch during the second phase of the Ender Dragon fight"
                        +"\n    > Values below zero shift the pitch down, above zero shift the pitch up"
                        +"\n    > Set to zero to disable")
                .defineInRange("dragon_pitch_amount", 0.05D, -0.5D, 1D);
        HEALTH_PITCH_THRESH = BUILDER.comment("\nAt what HP should the battle music shift in pitch")
                        .defineInRange("health_pitch_thresh", 6, 1, Integer.MAX_VALUE);
        HEALTH_PITCH_PERCENT = BUILDER.comment("Is the health threshold a percentage value?"
                        +"\n    > If it is, the above value must be between 1 and 100")
                .define("health_pitch_percent", false);
        FADE_TIME = BUILDER.comment("\nHow many seconds songs take to fade in and out")
                .defineInRange("fade_time", 1D, 0D, 10D);
        ENTITIES_SONGS = BUILDER.comment("\nEntites and their respective songs, write in entity;song;priority format"
                        +"\n    > entity = the entity's ID, eg. \"minecraft:pig\""
                        +"\n    > song = the song's ID, eg. \"battlemusic:mini1\""
                        +"\n    > priority = what precedence this song takes over other entity's music, eg. a priority of 1 will fade out and play over a priority of 0.")
                .defineListAllowEmpty(Collections.singletonList("entities_songs"), () -> Arrays.asList(
                        "minecraft:elder_guardian;battlemusic:mini1;0",
                        "minecraft:wither;battlemusic:wither_storm;2",
                        "minecraft:ender_dragon;battlemusic:enderman;5",
                        "alexsmobs:void_worm;battlemusic:ghast;4",
                        "alexsmobs:warped_mosco;battlemusic:mini1;0",
                        "aether:slider;battlemusic:mini2;2",
                        "aether:valkyrie;battlemusic:mini1;0",
                        "aether:sun_god;battlemusic:summit;3",
                        "aether:valkyrie_queen;battlemusic:mini2;2",
                        "cataclysm:ender_golem;battlemusic:mini2;0",
                        "cataclysm:ignited_revenant;battlemusic:mini2;0",
                        "cataclysm:ender_guardian;battlemusic:porcus_humungous;4",
                        "cataclysm:netherite_monstrosity;battlemusic:redstone_monstrosity;4",
                        "cataclysm:ignis;battlemusic:wildfire;4",
                        "cataclysm:the_harbinger;battlemusic:ancient;4",
                        "cataclysm:the_leviathan;battlemusic:ancient_guardian;4",
                        "cataclysm:the_prowler;battlemusic:mini1;0",
                        "cataclysm:amethyst_crab;battlemusic:mini2;0",
                        "cataclysm:coralssus;battlemusic:mini1;0",
                        "cataclysm:ancient_remnant;battlemusic:ascension;3",
                        "cataclysm:deepling_brute;battlemusic:mini1;0",
                        "mowziesmobs:ferrous_wroughtnaut;mowziesmobs:music.ferrous_wroughtnaut_theme;1",
                        "mowziesmobs:umvuthi;mowziesmobs:music.umvuthi_theme;2",
                        "mowziesmobs:frostmaw;mowziesmobs:music.frostmaw_theme;2",
                        "twilightforest:naga;battlemusic:arena2;2",
                        "twilightforest:lich;battlemusic:boss;3",
                        "twilightforest:hydra;battlemusic:arena1;3",
                        "twilightforest:minoshroom;battlemusic:mini1;0",
                        "twilightforest:alpha_yeti;battlemusic:mini2;1",
                        "twilightforest:phantom_knight;battlemusic:mini2;1",
                        "twilightforest:ur_ghast;battlemusic:metaluna;3",
                        "twilightforest:snow_queen;battlemusic:ascension;3",
                        "mutant_mobs:mutant_zombie;battlemusic:mini1;0",
                        "mutant_mobs:mutant_skeleton;battlemusic:mini1;0",
                        "mutant_mobs:mutant_creeper;battlemusic:mini2;0",
                        "mutant_mobs:mutant_enderman;battlemusic:shattered;1",
                        "mutant_more:mutant_blaze;battlemusic:mini2;1",
                        "mutant_more:mutant_shulker;battlemusic:mini2;1",
                        "mutant_more:mutant_wither_skeleton;battlemusic:mini1;0",
                        "rottencreatures:dead_beard;battlemusic:mini1;0",
                        "rottencreatures:immortal;battlemusic:mini1;0"
                ), a -> true);

        DEFAULT_SONG = BUILDER.comment("\nA generic battle song to play for any other undefined hostile entity"
                        +"\n    > Leave blank for no default battle music")
                .define("default_song", "");

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
