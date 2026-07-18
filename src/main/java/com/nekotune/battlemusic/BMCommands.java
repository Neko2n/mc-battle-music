package com.nekotune.battlemusic;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class BMCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(BattleMusic.MOD_ID)
            .then(Commands.literal("volume")
                .then(Commands.literal("set")
                    .then(Commands.argument("volume", FloatArgumentType.floatArg(0f, 5f))
                        .executes(BMCommands::setVolume)))
                .then(Commands.literal("get")
                    .executes(BMCommands::getVolume))
                .then(Commands.literal("separate")
                    .then(Commands.argument("separate", BoolArgumentType.bool())
                        .executes(BMCommands::separateVolume))))
            .then(Commands.literal("reload")
                .executes(BMCommands::reload))
            .then(Commands.literal("debug")
                .executes(BMCommands::debug))
            .then(Commands.literal("set")
                .then(Commands.argument("entity", ResourceLocationArgument.id()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                    .executes((ctx) -> BMCommands.set(ctx, false, false))
                    .then(Commands.argument("song", ResourceLocationArgument.id()).suggests(SuggestionProviders.AVAILABLE_SOUNDS)
                        .executes((ctx) -> BMCommands.set(ctx, true, false))
                    .then(Commands.argument("priority", IntegerArgumentType.integer())
                        .executes((ctx) -> BMCommands.set(ctx, true, true))))));
        dispatcher.register(builder);
    }

    private static int setVolume(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        float volume = context.getArgument("volume", float.class);
        BMConfig.VOLUME.set((double) volume);
        BMConfig.VOLUME.save();
        BattleMusic.setVolume(volume);
        source.sendSuccess(() -> Component.literal("Set battle music volume to " + volume), true);
        return 1;
    }

    private static int getVolume(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Component.literal("Battle music volume is currently set to " + BMConfig.VOLUME.get()), true);
        return 1;
    }

    private static int separateVolume(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        boolean separate = context.getArgument("separate", boolean.class);
        BMConfig.LINKED_TO_MUSIC.set(separate);
        BMConfig.LINKED_TO_MUSIC.save();
        source.sendSuccess(() -> Component.literal((separate) ? "Battle music volume is now separate from music volume" : "Battle music volume is now linked to music volume"), true);
        return 1;
    }

    private static int reload(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Component.literal("Reloaded battle music"), true);
        BattleMusic.reload();
        return 1;
    }

    private static int debug(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        final boolean isPlaying = BattleMusic.playing != null;
        BattleMusic.LOGGER.debug("BATTLE MUSIC DEBUG INFO\n"
                        + "  - Is sound playing? {}\n"
                        + "  - Entity playing music: {}\n"
                        + "  - Sound being played: {}\n"
                        + "  - Volume: {}\n"
                        + "  - Pitch: {}\n"
                        + "  - Fading out? {}\n"
                        + "  - Priority: {}",
                isPlaying ? Minecraft.getInstance().getSoundManager().isActive(BattleMusic.playing) : "false",
                isPlaying ? BattleMusic.playing.entity.getName().getString() : "NULL",
                isPlaying ? BattleMusic.playing.soundEvent.getLocation() : "NULL",
                isPlaying ? BattleMusic.playing.getVolume() : "0",
                isPlaying ? BattleMusic.playing.getPitch() : "1",
                isPlaying && BattleMusic.playing.fadeOut,
                isPlaying ? BattleMusic.playing.priority : "0");
        source.sendSuccess(() -> Component.literal("Logged debug info to logs/debug.log"), true);
        return 1;
    }

    private static int set(CommandContext<CommandSourceStack> context, boolean songDef, boolean priorityDef) {
        CommandSourceStack source = context.getSource();
        ResourceLocation entity = context.getArgument("entity", ResourceLocation.class);
        List<String> data = new ArrayList<>(BMConfig.ENTITIES_SONGS.get());
        int priority = 0;
        boolean changes = false;

        // Remove old data
        for (int i = 0; i < data.size(); i++) {
            String item = data.get(i);
            if (item.substring(0, item.indexOf(';')).equalsIgnoreCase(entity.toString())) {
                priority = Integer.parseInt(item.substring(item.lastIndexOf(';')+1));
                data.remove(i);
                changes = true;
                i--;
            }
        }

        // Set new data
        String success;
        if (songDef) {
            ResourceLocation song = context.getArgument("song", ResourceLocation.class);
            if (priorityDef) {
                priority = context.getArgument("priority", int.class);
            }
            data.add(entity.toString() + ';' + song.toString() + ';' + priority);
            BMConfig.ENTITIES_SONGS.set(data);
            BMConfig.ENTITIES_SONGS.save();
            success = "Set battle music for entity " + entity.getPath() + " to sound " + song + " with priority " + priority;
        } else if (changes) {
            BMConfig.ENTITIES_SONGS.set(data);
            BMConfig.ENTITIES_SONGS.save();
            success = "Removed battle music from entity " + entity.getPath();
        } else {
            source.sendFailure(Component.literal("No battle music set for entity " + entity.getPath()));
            return 1;
        }
        source.sendSuccess(() -> Component.literal(success), true);
        BattleMusic.reload();
        return 1;
    }
}
