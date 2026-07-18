package com.nekotune.battlemusic.compat;

import net.minecraft.world.entity.Mob;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.Dist;

import com.aetherteam.nitrogen.entity.BossMob;

@OnlyIn(Dist.CLIENT)
public abstract class AetherCompat {
    public final static String ACTIVE_BOSS_TAG = "active_boss_aether";

    public static boolean isAetherBoss(final Mob mob) {
        return mob instanceof BossMob;
    }
}
