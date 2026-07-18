package com.nekotune.battlemusic;

import java.util.function.Supplier;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class BMSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, BattleMusic.MOD_ID);

    public static final Supplier<SoundEvent> ENDERMAN = registerSoundEvent("enderman");
    public static final Supplier<SoundEvent> NECROMANCER = registerSoundEvent("necromancer");
    public static final Supplier<SoundEvent> WITHER_STORM = registerSoundEvent("wither_storm");
    public static final Supplier<SoundEvent> MINI1 = registerSoundEvent("mini1");
    public static final Supplier<SoundEvent> MINI2 = registerSoundEvent("mini2");
    public static final Supplier<SoundEvent> CAULDRON = registerSoundEvent("cauldron");
    public static final Supplier<SoundEvent> ANCIENT = registerSoundEvent("ancient");
    public static final Supplier<SoundEvent> ANCIENT_GUARDIAN = registerSoundEvent("ancient_guardian");
    public static final Supplier<SoundEvent> ARENA1 = registerSoundEvent("arena1");
    public static final Supplier<SoundEvent> ARENA2 = registerSoundEvent("arena2");
    public static final Supplier<SoundEvent> ASCENSION = registerSoundEvent("ascension");
    public static final Supplier<SoundEvent> BOSS = registerSoundEvent("boss");
    public static final Supplier<SoundEvent> BROKEN_HEART_OF_ENDER = registerSoundEvent("broken_heart_of_ender");
    public static final Supplier<SoundEvent> GHAST = registerSoundEvent("ghast");
    public static final Supplier<SoundEvent> KERMETIC = registerSoundEvent("kermetic");
    public static final Supplier<SoundEvent> MENTA_MENARDI = registerSoundEvent("menta_menardi");
    public static final Supplier<SoundEvent> METALUNA = registerSoundEvent("metaluna");
    public static final Supplier<SoundEvent> PORCUS_HUMUNGOUS = registerSoundEvent("porcus_humungous");
    public static final Supplier<SoundEvent> REDSTONE_MONSTROSITY = registerSoundEvent("redstone_monstrosity");
    public static final Supplier<SoundEvent> SHATTERED = registerSoundEvent("shattered");
    public static final Supplier<SoundEvent> SUMMIT = registerSoundEvent("summit");
    public static final Supplier<SoundEvent> WILDFIRE = registerSoundEvent("wildfire");

    private static Supplier<SoundEvent> registerSoundEvent(String name) {
        ResourceLocation id = ResourceLocation.tryBuild(BattleMusic.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> {
            assert id != null;
            return SoundEvent.createVariableRangeEvent(id);
        });
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
