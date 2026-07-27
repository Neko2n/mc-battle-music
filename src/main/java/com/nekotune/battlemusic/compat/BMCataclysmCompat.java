package com.nekotune.battlemusic.compat;

import com.github.L_Ender.cataclysm.config.CMClientConfig;
import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.IABoss_monster;
import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Ancient_Remnant.Ancient_Remnant_Entity;

import net.minecraft.world.entity.Mob;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModList;

@OnlyIn(value = Dist.CLIENT)
public class BMCataclysmCompat {
    public static boolean validate(final Mob mob) {
        if (!ModList.get().isLoaded("cataclysm"))
            return true;
        if (mob instanceof final Ancient_Remnant_Entity ancientRemnant) {
            if (ancientRemnant.isSleep())
                return false;
        }
        return true;
    }
    public static boolean hasExistingMusic(final Mob mob) {
        if (!ModList.get().isLoaded("cataclysm"))
            return false;
        if (mob instanceof final IABoss_monster boss) {
            if (boss.getBossMusic() != null && CMClientConfig.BossMusic)
                return true;
        }
        return false;
    }
}
