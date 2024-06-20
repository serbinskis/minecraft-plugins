package net.minecraft.world.effect;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.particles.ParticleParam;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityLiving;

class RaidOmenMobEffect extends MobEffectList {

    protected RaidOmenMobEffect(MobEffectInfo mobeffectinfo, int i, ParticleParam particleparam) {
        super(mobeffectinfo, i, particleparam);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int i, int j) {
        return i == 1;
    }

    @Override
    public boolean applyEffectTick(EntityLiving entityliving, int i) {
        if (entityliving instanceof EntityPlayer entityplayer) {
            if (!entityliving.isSpectator()) {
                WorldServer worldserver = entityplayer.serverLevel();
                BlockPosition blockposition = entityplayer.getRaidOmenPosition();

                if (blockposition != null) {
                    worldserver.getRaids().createOrExtendRaid(entityplayer, blockposition);
                    entityplayer.clearRaidOmenPosition();
                    return false;
                }
            }
        }

        return true;
    }
}
