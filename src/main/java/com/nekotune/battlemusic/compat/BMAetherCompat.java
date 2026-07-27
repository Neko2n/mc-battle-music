package com.nekotune.battlemusic.compat;

import net.minecraft.world.entity.Mob;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModList;
import net.neoforged.api.distmarker.Dist;

import com.aetherteam.aether.AetherConfig;
import com.aetherteam.aether.entity.AetherBossMob;

@OnlyIn(Dist.CLIENT)
public abstract class BMAetherCompat {
    public static boolean hasExistingMusic(final Mob mob) {
        if (!ModList.get().isLoaded("aether"))
            return false;
        if (mob instanceof final AetherBossMob boss) {
            if (boss.getBossMusic() != null && !AetherConfig.CLIENT.disable_aether_boss_music.get())
                return true;
        }
        return false;
    }
}
