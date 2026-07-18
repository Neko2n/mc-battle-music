package com.nekotune.battlemusic;

import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

public class BMConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.ConfigValue<Double> VOLUME;
    public static final ModConfigSpec.ConfigValue<Boolean> LINKED_TO_MUSIC;
    public static final ModConfigSpec.ConfigValue<Double> HEALTH_PITCH_AMOUNT;
    public static final ModConfigSpec.ConfigValue<Double> PHASE2_PITCH_AMOUNT;
    public static final ModConfigSpec.ConfigValue<Integer> HEALTH_PITCH_THRESH;
    public static final ModConfigSpec.ConfigValue<Boolean> HEALTH_PITCH_PERCENT;
    public static final ModConfigSpec.ConfigValue<Double> FADE_TIME;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ENTITIES_SONGS;
    public static final ModConfigSpec.ConfigValue<String> DEFAULT_SONG;

    static {
        BUILDER.push(BattleMusic.MOD_ID + " configs");

        VOLUME = BUILDER.comment("Volume of battle music, set to zero to mute")
                .defineInRange("volume", 1D, 0D, 5D);
        LINKED_TO_MUSIC = BUILDER.comment("\nWhether battle music's volume is affected by the music slider")
                .define("linked_to_music_volume", true);
        HEALTH_PITCH_AMOUNT = BUILDER.comment("""

                How much the battle music changes in pitch when at low health   \s
                    > Values below zero shift the pitch down, above zero shift the pitch up   \s
                    > Set to zero to disable  \s""")
                .defineInRange("health_pitch_amount", 0.05D, -0.5D, 1D);
        PHASE2_PITCH_AMOUNT = BUILDER.comment("""

                                How much the battle music changes in pitch during the second phases of the Ender Dragon and Wither fights   \s
                                    > Values below zero shift the pitch down, above zero shift the pitch up \s
                                    > Set to zero to disable    \s""")
                .defineInRange("phase2_pitch_amount", 0.05D, -0.5D, 1D);
        HEALTH_PITCH_THRESH = BUILDER.comment("\nAt what HP should the battle music shift in pitch")
                .defineInRange("health_pitch_thresh", 6, 1, Integer.MAX_VALUE);
        HEALTH_PITCH_PERCENT = BUILDER.comment("""

                Is the health threshold a percentage value?   \s
                    > If it is, the above value must be between 1 and 100   \s""")
                .define("health_pitch_percent", false);
        FADE_TIME = BUILDER.comment("\nHow many seconds songs take to fade in and out")
                .defineInRange("fade_time", 1D, 0D, 10D);
        ENTITIES_SONGS = BUILDER.comment("""

                    Entites and their respective songs, write in entity;song;priority format      \s
                        > entity = the entity's ID, eg. "minecraft:pig"      \s
                        > song = the song's ID, eg. "battlemusic:mini1"      \s
                        > priority = what precedence this song takes over other entity's music, eg. a priority of 1 will fade out and play over a priority of 0.     \s""")
                .defineListAllowEmpty(List.of("entities_songs"),
                        () -> List.of(
                            "minecraft:elder_guardian;battlemusic:mini1;0",
                            "minecraft:warden;battlemusic:necromancer;1",
                            "minecraft:wither;battlemusic:wither_storm;2",
                            "minecraft:ender_dragon;battlemusic:enderman;5",
                            "queen_bee:queen_bee;battlemusic:mini1;0",
                            "irons_spellbooks:dead_king;battlemusic:boss;2",
                            "irons_spellbooks:citadel_keeper;battlemusic:mini1;0",
                            "irons_spellbooks:archevoker;battlemusic:mini1;0",
                            "dawnera:tyrannosaurus;battlemusic:mini1;0",
                            "upgrade_aquatic:great_thrasher;battlemusic:mini1;0",
                            "born_in_chaos_v1:dire_hound_leader;battlemusic:mini1;0",
                            "born_in_chaos_v1:supreme_bonescaller;battlemusic:mini1;0",
                            "born_in_chaos_v1:lord_pumpkinhead;battlemusic:mini2;0",
                            "alexscaves:luxtructosaurus;battlemusic:porcus_humungous;5",
                            "alexscaves:tremorzilla;battlemusic:metaluna;2",
                            "alexscaves:magnetron;battlemusic:mini2;0",
                            "alexscaves:forsaken;battlemusic:mini2;1",
                            "alexscaves:brainiac;battlemusic:mini1;0",
                            "alexsmobs:void_worm;battlemusic:ghast;4",
                            "alexsmobs:warped_mosco;battlemusic:mini1;0",
                            "endergetic:brood_eetle;battlemusic:cauldron;2",
                            "aether:slider;battlemusic:mini2;2",
                            "aether:valkyrie;battlemusic:mini1;0",
                            "aether:sun_spirit;battlemusic:summit;3",
                            "aether:valkyrie_queen;battlemusic:mini2;2",
                            "cataclysm:ender_golem;battlemusic:mini2;0",
                            "cataclysm:ignited_revenant;battlemusic:mini2;0",
                            "cataclysm:ignited_berserker;battlemusic:mini2;0",
                            "cataclysm:ender_guardian;battlemusic:menta_menardi;4",
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
                            "bosses_of_mass_destruction:void_blossom;battlemusic:arena2;1",
                            "bosses_of_mass_destruction:lich;battlemusic:boss;2",
                            "bosses_of_mass_destruction:gauntlet;battlemusic:kermetic;3",
                            "bosses_of_mass_destruction:obsidilith;battlemusic:broken_heart_of_ender;3",
                            "twilightforest:naga;battlemusic:arena2;2",
                            "twilightforest:lich;battlemusic:boss;3",
                            "twilightforest:hydra;battlemusic:arena1;3",
                            "twilightforest:minoshroom;battlemusic:mini1;0",
                            "twilightforest:alpha_yeti;battlemusic:mini2;1",
                            "twilightforest:phantom_knight;battlemusic:mini2;1",
                            "twilightforest:ur_ghast;battlemusic:metaluna;3",
                            "twilightforest:snow_queen;battlemusic:ascension;3",
                            "mutantmonsters:mutant_zombie;battlemusic:mini1;0",
                            "mutantmonsters:mutant_skeleton;battlemusic:mini1;0",
                            "mutantmonsters:mutant_creeper;battlemusic:mini2;0",
                            "mutantmonsters:mutant_enderman;battlemusic:shattered;1"),
                        () -> "",
                        a -> true);

        DEFAULT_SONG = BUILDER.comment("""

                A generic battle song to play for any other undefined hostile entity     \s
                    > Leave blank for no default battle music      \s""")
                .define("default_song", "");

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
