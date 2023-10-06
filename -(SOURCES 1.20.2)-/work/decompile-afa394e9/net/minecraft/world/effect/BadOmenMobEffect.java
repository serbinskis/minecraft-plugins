package net.minecraft.world.effect;

import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.entity.EntityLiving;

class BadOmenMobEffect extends MobEffectList {

    protected BadOmenMobEffect(MobEffectInfo mobeffectinfo, int i) {
        super(mobeffectinfo, i);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int i, int j) {
        return true;
    }

    @Override
    public void applyEffectTick(EntityLiving entityliving, int i) {
        super.applyEffectTick(entityliving, i);
        if (entityliving instanceof EntityPlayer) {
            EntityPlayer entityplayer = (EntityPlayer) entityliving;

            if (!entityliving.isSpectator()) {
                WorldServer worldserver = entityplayer.serverLevel();

                if (worldserver.getDifficulty() == EnumDifficulty.PEACEFUL) {
                    return;
                }

                if (worldserver.isVillage(entityliving.blockPosition())) {
                    worldserver.getRaids().createOrExtendRaid(entityplayer);
                }
            }
        }

    }
}
