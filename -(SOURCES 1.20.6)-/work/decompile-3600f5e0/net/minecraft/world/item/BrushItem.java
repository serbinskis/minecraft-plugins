package net.minecraft.world.item;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.particles.ParticleParamBlock;
import net.minecraft.core.particles.Particles;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.EnumMainHand;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.projectile.ProjectileHelper;
import net.minecraft.world.item.context.ItemActionContext;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.EnumRenderType;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;

public class BrushItem extends Item {

    public static final int ANIMATION_DURATION = 10;
    private static final int USE_DURATION = 200;

    public BrushItem(Item.Info item_info) {
        super(item_info);
    }

    @Override
    public EnumInteractionResult useOn(ItemActionContext itemactioncontext) {
        EntityHuman entityhuman = itemactioncontext.getPlayer();

        if (entityhuman != null && this.calculateHitResult(entityhuman).getType() == MovingObjectPosition.EnumMovingObjectType.BLOCK) {
            entityhuman.startUsingItem(itemactioncontext.getHand());
        }

        return EnumInteractionResult.CONSUME;
    }

    @Override
    public EnumAnimation getUseAnimation(ItemStack itemstack) {
        return EnumAnimation.BRUSH;
    }

    @Override
    public int getUseDuration(ItemStack itemstack) {
        return 200;
    }

    @Override
    public void onUseTick(World world, EntityLiving entityliving, ItemStack itemstack, int i) {
        if (i >= 0 && entityliving instanceof EntityHuman entityhuman) {
            MovingObjectPosition movingobjectposition = this.calculateHitResult(entityhuman);

            if (movingobjectposition instanceof MovingObjectPositionBlock movingobjectpositionblock) {
                if (movingobjectposition.getType() == MovingObjectPosition.EnumMovingObjectType.BLOCK) {
                    int j = this.getUseDuration(itemstack) - i + 1;
                    boolean flag = j % 10 == 5;

                    if (flag) {
                        BlockPosition blockposition = movingobjectpositionblock.getBlockPos();
                        IBlockData iblockdata = world.getBlockState(blockposition);
                        EnumMainHand enummainhand = entityliving.getUsedItemHand() == EnumHand.MAIN_HAND ? entityhuman.getMainArm() : entityhuman.getMainArm().getOpposite();

                        if (iblockdata.shouldSpawnTerrainParticles() && iblockdata.getRenderShape() != EnumRenderType.INVISIBLE) {
                            this.spawnDustParticles(world, movingobjectpositionblock, iblockdata, entityliving.getViewVector(0.0F), enummainhand);
                        }

                        Block block = iblockdata.getBlock();
                        SoundEffect soundeffect;

                        if (block instanceof BrushableBlock) {
                            BrushableBlock brushableblock = (BrushableBlock) block;

                            soundeffect = brushableblock.getBrushSound();
                        } else {
                            soundeffect = SoundEffects.BRUSH_GENERIC;
                        }

                        world.playSound(entityhuman, blockposition, soundeffect, SoundCategory.BLOCKS);
                        if (!world.isClientSide()) {
                            TileEntity tileentity = world.getBlockEntity(blockposition);

                            if (tileentity instanceof BrushableBlockEntity) {
                                BrushableBlockEntity brushableblockentity = (BrushableBlockEntity) tileentity;
                                boolean flag1 = brushableblockentity.brush(world.getGameTime(), entityhuman, movingobjectpositionblock.getDirection());

                                if (flag1) {
                                    EnumItemSlot enumitemslot = itemstack.equals(entityhuman.getItemBySlot(EnumItemSlot.OFFHAND)) ? EnumItemSlot.OFFHAND : EnumItemSlot.MAINHAND;

                                    itemstack.hurtAndBreak(1, entityliving, enumitemslot);
                                }
                            }
                        }
                    }

                    return;
                }
            }

            entityliving.releaseUsingItem();
        } else {
            entityliving.releaseUsingItem();
        }
    }

    private MovingObjectPosition calculateHitResult(EntityHuman entityhuman) {
        return ProjectileHelper.getHitResultOnViewVector(entityhuman, (entity) -> {
            return !entity.isSpectator() && entity.isPickable();
        }, entityhuman.blockInteractionRange());
    }

    private void spawnDustParticles(World world, MovingObjectPositionBlock movingobjectpositionblock, IBlockData iblockdata, Vec3D vec3d, EnumMainHand enummainhand) {
        double d0 = 3.0D;
        int i = enummainhand == EnumMainHand.RIGHT ? 1 : -1;
        int j = world.getRandom().nextInt(7, 12);
        ParticleParamBlock particleparamblock = new ParticleParamBlock(Particles.BLOCK, iblockdata);
        EnumDirection enumdirection = movingobjectpositionblock.getDirection();
        BrushItem.a brushitem_a = BrushItem.a.fromDirection(vec3d, enumdirection);
        Vec3D vec3d1 = movingobjectpositionblock.getLocation();

        for (int k = 0; k < j; ++k) {
            world.addParticle(particleparamblock, vec3d1.x - (double) (enumdirection == EnumDirection.WEST ? 1.0E-6F : 0.0F), vec3d1.y, vec3d1.z - (double) (enumdirection == EnumDirection.NORTH ? 1.0E-6F : 0.0F), brushitem_a.xd() * (double) i * 3.0D * world.getRandom().nextDouble(), 0.0D, brushitem_a.zd() * (double) i * 3.0D * world.getRandom().nextDouble());
        }

    }

    private static record a(double xd, double yd, double zd) {

        private static final double ALONG_SIDE_DELTA = 1.0D;
        private static final double OUT_FROM_SIDE_DELTA = 0.1D;

        public static BrushItem.a fromDirection(Vec3D vec3d, EnumDirection enumdirection) {
            double d0 = 0.0D;
            BrushItem.a brushitem_a;

            switch (enumdirection) {
                case DOWN:
                case UP:
                    brushitem_a = new BrushItem.a(vec3d.z(), 0.0D, -vec3d.x());
                    break;
                case NORTH:
                    brushitem_a = new BrushItem.a(1.0D, 0.0D, -0.1D);
                    break;
                case SOUTH:
                    brushitem_a = new BrushItem.a(-1.0D, 0.0D, 0.1D);
                    break;
                case WEST:
                    brushitem_a = new BrushItem.a(-0.1D, 0.0D, -1.0D);
                    break;
                case EAST:
                    brushitem_a = new BrushItem.a(0.1D, 0.0D, 1.0D);
                    break;
                default:
                    throw new MatchException((String) null, (Throwable) null);
            }

            return brushitem_a;
        }
    }
}
