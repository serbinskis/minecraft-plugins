package net.minecraft.world.effect;

import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.raid.Raid;

class BadOmenMobEffect extends MobEffectList {

    protected BadOmenMobEffect(MobEffectInfo mobeffectinfo, int i) {
        super(mobeffectinfo, i);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int i, int j) {
        return true;
    }

    @Override
    public boolean applyEffectTick(EntityLiving entityliving, int i) {
        if (entityliving instanceof EntityPlayer entityplayer) {
            if (!entityplayer.isSpectator()) {
                WorldServer worldserver = entityplayer.serverLevel();

                if (worldserver.getDifficulty() != EnumDifficulty.PEACEFUL && worldserver.isVillage(entityplayer.blockPosition())) {
                    Raid raid = worldserver.getRaidAt(entityplayer.blockPosition());

                    if (raid == null || raid.getRaidOmenLevel() < raid.getMaxRaidOmenLevel()) {
                        entityplayer.addEffect(new MobEffect(MobEffects.RAID_OMEN, 600, i));
                        entityplayer.setRaidOmenPosition(entityplayer.blockPosition());
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
