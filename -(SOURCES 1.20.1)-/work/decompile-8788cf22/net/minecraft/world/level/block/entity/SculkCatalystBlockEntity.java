package net.minecraft.world.level.block.entity;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.particles.Particles;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.SculkCatalystBlock;
import net.minecraft.world.level.block.SculkSpreader;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.Vec3D;

public class SculkCatalystBlockEntity extends TileEntity implements GameEventListener.b<SculkCatalystBlockEntity.a> {

    private final SculkCatalystBlockEntity.a catalystListener;

    public SculkCatalystBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        super(TileEntityTypes.SCULK_CATALYST, blockposition, iblockdata);
        this.catalystListener = new SculkCatalystBlockEntity.a(iblockdata, new BlockPositionSource(blockposition));
    }

    public static void serverTick(World world, BlockPosition blockposition, IBlockData iblockdata, SculkCatalystBlockEntity sculkcatalystblockentity) {
        sculkcatalystblockentity.catalystListener.getSculkSpreader().updateCursors(world, blockposition, world.getRandom(), true);
    }

    @Override
    public void load(NBTTagCompound nbttagcompound) {
        this.catalystListener.sculkSpreader.load(nbttagcompound);
    }

    @Override
    protected void saveAdditional(NBTTagCompound nbttagcompound) {
        this.catalystListener.sculkSpreader.save(nbttagcompound);
        super.saveAdditional(nbttagcompound);
    }

    @Override
    public SculkCatalystBlockEntity.a getListener() {
        return this.catalystListener;
    }

    public static class a implements GameEventListener {

        public static final int PULSE_TICKS = 8;
        final SculkSpreader sculkSpreader;
        private final IBlockData blockState;
        private final PositionSource positionSource;

        public a(IBlockData iblockdata, PositionSource positionsource) {
            this.blockState = iblockdata;
            this.positionSource = positionsource;
            this.sculkSpreader = SculkSpreader.createLevelSpreader();
        }

        @Override
        public PositionSource getListenerSource() {
            return this.positionSource;
        }

        @Override
        public int getListenerRadius() {
            return 8;
        }

        @Override
        public GameEventListener.a getDeliveryMode() {
            return GameEventListener.a.BY_DISTANCE;
        }

        @Override
        public boolean handleGameEvent(WorldServer worldserver, GameEvent gameevent, GameEvent.a gameevent_a, Vec3D vec3d) {
            if (gameevent == GameEvent.ENTITY_DIE) {
                Entity entity = gameevent_a.sourceEntity();

                if (entity instanceof EntityLiving) {
                    EntityLiving entityliving = (EntityLiving) entity;

                    if (!entityliving.wasExperienceConsumed()) {
                        int i = entityliving.getExperienceReward();

                        if (entityliving.shouldDropExperience() && i > 0) {
                            this.sculkSpreader.addCursors(BlockPosition.containing(vec3d.relative(EnumDirection.UP, 0.5D)), i);
                            this.tryAwardItSpreadsAdvancement(worldserver, entityliving);
                        }

                        entityliving.skipDropExperience();
                        this.positionSource.getPosition(worldserver).ifPresent((vec3d1) -> {
                            this.bloom(worldserver, BlockPosition.containing(vec3d1), this.blockState, worldserver.getRandom());
                        });
                    }

                    return true;
                }
            }

            return false;
        }

        @VisibleForTesting
        public SculkSpreader getSculkSpreader() {
            return this.sculkSpreader;
        }

        private void bloom(WorldServer worldserver, BlockPosition blockposition, IBlockData iblockdata, RandomSource randomsource) {
            worldserver.setBlock(blockposition, (IBlockData) iblockdata.setValue(SculkCatalystBlock.PULSE, true), 3);
            worldserver.scheduleTick(blockposition, iblockdata.getBlock(), 8);
            worldserver.sendParticles(Particles.SCULK_SOUL, (double) blockposition.getX() + 0.5D, (double) blockposition.getY() + 1.15D, (double) blockposition.getZ() + 0.5D, 2, 0.2D, 0.0D, 0.2D, 0.0D);
            worldserver.playSound((EntityHuman) null, blockposition, SoundEffects.SCULK_CATALYST_BLOOM, SoundCategory.BLOCKS, 2.0F, 0.6F + randomsource.nextFloat() * 0.4F);
        }

        private void tryAwardItSpreadsAdvancement(World world, EntityLiving entityliving) {
            EntityLiving entityliving1 = entityliving.getLastHurtByMob();

            if (entityliving1 instanceof EntityPlayer) {
                EntityPlayer entityplayer = (EntityPlayer) entityliving1;
                DamageSource damagesource = entityliving.getLastDamageSource() == null ? world.damageSources().playerAttack(entityplayer) : entityliving.getLastDamageSource();

                CriterionTriggers.KILL_MOB_NEAR_SCULK_CATALYST.trigger(entityplayer, entityliving, damagesource);
            }

        }
    }
}
