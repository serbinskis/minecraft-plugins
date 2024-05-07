package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.Particles;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.stats.StatisticList;
import net.minecraft.tags.TagsFluid;
import net.minecraft.world.EnumHand;
import net.minecraft.world.InteractionResultWrapper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.RayTrace;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.IFluidContainer;
import net.minecraft.world.level.block.IFluidSource;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.level.material.FluidTypeFlowing;
import net.minecraft.world.level.material.FluidTypes;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.MovingObjectPositionBlock;

// CraftBukkit start
import net.minecraft.network.protocol.game.PacketPlayOutBlockChange;
import net.minecraft.server.level.WorldServer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.DummyGeneratorAccess;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
// CraftBukkit end

public class ItemBucket extends Item implements DispensibleContainerItem {

    public final FluidType content;

    public ItemBucket(FluidType fluidtype, Item.Info item_info) {
        super(item_info);
        this.content = fluidtype;
    }

    @Override
    public InteractionResultWrapper<ItemStack> use(World world, EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);
        MovingObjectPositionBlock movingobjectpositionblock = getPlayerPOVHitResult(world, entityhuman, this.content == FluidTypes.EMPTY ? RayTrace.FluidCollisionOption.SOURCE_ONLY : RayTrace.FluidCollisionOption.NONE);

        if (movingobjectpositionblock.getType() == MovingObjectPosition.EnumMovingObjectType.MISS) {
            return InteractionResultWrapper.pass(itemstack);
        } else if (movingobjectpositionblock.getType() != MovingObjectPosition.EnumMovingObjectType.BLOCK) {
            return InteractionResultWrapper.pass(itemstack);
        } else {
            BlockPosition blockposition = movingobjectpositionblock.getBlockPos();
            EnumDirection enumdirection = movingobjectpositionblock.getDirection();
            BlockPosition blockposition1 = blockposition.relative(enumdirection);

            if (world.mayInteract(entityhuman, blockposition) && entityhuman.mayUseItemAt(blockposition1, enumdirection, itemstack)) {
                IBlockData iblockdata;

                if (this.content == FluidTypes.EMPTY) {
                    iblockdata = world.getBlockState(blockposition);
                    Block block = iblockdata.getBlock();

                    if (block instanceof IFluidSource) {
                        IFluidSource ifluidsource = (IFluidSource) block;
                        // CraftBukkit start
                        ItemStack dummyFluid = ifluidsource.pickupBlock(entityhuman, DummyGeneratorAccess.INSTANCE, blockposition, iblockdata);
                        if (dummyFluid.isEmpty()) return InteractionResultWrapper.fail(itemstack); // Don't fire event if the bucket won't be filled.
                        PlayerBucketFillEvent event = CraftEventFactory.callPlayerBucketFillEvent((WorldServer) world, entityhuman, blockposition, blockposition, movingobjectpositionblock.getDirection(), itemstack, dummyFluid.getItem(), enumhand);

                        if (event.isCancelled()) {
                            ((EntityPlayer) entityhuman).connection.send(new PacketPlayOutBlockChange(world, blockposition)); // SPIGOT-5163 (see PlayerInteractManager)
                            ((EntityPlayer) entityhuman).getBukkitEntity().updateInventory(); // SPIGOT-4541
                            return InteractionResultWrapper.fail(itemstack);
                        }
                        // CraftBukkit end
                        ItemStack itemstack1 = ifluidsource.pickupBlock(entityhuman, world, blockposition, iblockdata);

                        if (!itemstack1.isEmpty()) {
                            entityhuman.awardStat(StatisticList.ITEM_USED.get(this));
                            ifluidsource.getPickupSound().ifPresent((soundeffect) -> {
                                entityhuman.playSound(soundeffect, 1.0F, 1.0F);
                            });
                            world.gameEvent((Entity) entityhuman, (Holder) GameEvent.FLUID_PICKUP, blockposition);
                            ItemStack itemstack2 = ItemLiquidUtil.createFilledResult(itemstack, entityhuman, CraftItemStack.asNMSCopy(event.getItemStack())); // CraftBukkit

                            if (!world.isClientSide) {
                                CriterionTriggers.FILLED_BUCKET.trigger((EntityPlayer) entityhuman, itemstack1);
                            }

                            return InteractionResultWrapper.sidedSuccess(itemstack2, world.isClientSide());
                        }
                    }

                    return InteractionResultWrapper.fail(itemstack);
                } else {
                    iblockdata = world.getBlockState(blockposition);
                    BlockPosition blockposition2 = iblockdata.getBlock() instanceof IFluidContainer && this.content == FluidTypes.WATER ? blockposition : blockposition1;

                    if (this.emptyContents(entityhuman, world, blockposition2, movingobjectpositionblock, movingobjectpositionblock.getDirection(), blockposition, itemstack, enumhand)) { // CraftBukkit
                        this.checkExtraContent(entityhuman, world, itemstack, blockposition2);
                        if (entityhuman instanceof EntityPlayer) {
                            CriterionTriggers.PLACED_BLOCK.trigger((EntityPlayer) entityhuman, blockposition2, itemstack);
                        }

                        entityhuman.awardStat(StatisticList.ITEM_USED.get(this));
                        return InteractionResultWrapper.sidedSuccess(getEmptySuccessItem(itemstack, entityhuman), world.isClientSide());
                    } else {
                        return InteractionResultWrapper.fail(itemstack);
                    }
                }
            } else {
                return InteractionResultWrapper.fail(itemstack);
            }
        }
    }

    public static ItemStack getEmptySuccessItem(ItemStack itemstack, EntityHuman entityhuman) {
        return !entityhuman.hasInfiniteMaterials() ? new ItemStack(Items.BUCKET) : itemstack;
    }

    @Override
    public void checkExtraContent(@Nullable EntityHuman entityhuman, World world, ItemStack itemstack, BlockPosition blockposition) {}

    @Override
    public boolean emptyContents(@Nullable EntityHuman entityhuman, World world, BlockPosition blockposition, @Nullable MovingObjectPositionBlock movingobjectpositionblock) {
        // CraftBukkit start
        return emptyContents(entityhuman, world, blockposition, movingobjectpositionblock, null, null, null, EnumHand.MAIN_HAND);
    }

    public boolean emptyContents(EntityHuman entityhuman, World world, BlockPosition blockposition, @Nullable MovingObjectPositionBlock movingobjectpositionblock, EnumDirection enumdirection, BlockPosition clicked, ItemStack itemstack, EnumHand enumhand) {
        // CraftBukkit end
        FluidType fluidtype = this.content;

        if (!(fluidtype instanceof FluidTypeFlowing fluidtypeflowing)) {
            return false;
        } else {
            IBlockData iblockdata;
            Block block;
            boolean flag;
            IFluidContainer ifluidcontainer;
            boolean flag1;
            label70:
            {
                iblockdata = world.getBlockState(blockposition);
                block = iblockdata.getBlock();
                flag = iblockdata.canBeReplaced(this.content);
                if (!iblockdata.isAir() && !flag) {
                    label67:
                    {
                        if (block instanceof IFluidContainer) {
                            ifluidcontainer = (IFluidContainer) block;
                            if (ifluidcontainer.canPlaceLiquid(entityhuman, world, blockposition, iblockdata, this.content)) {
                                break label67;
                            }
                        }

                        flag1 = false;
                        break label70;
                    }
                }

                flag1 = true;
            }

            boolean flag2 = flag1;

            // CraftBukkit start
            if (flag2 && entityhuman != null) {
                PlayerBucketEmptyEvent event = CraftEventFactory.callPlayerBucketEmptyEvent((WorldServer) world, entityhuman, blockposition, clicked, enumdirection, itemstack, enumhand);
                if (event.isCancelled()) {
                    ((EntityPlayer) entityhuman).connection.send(new PacketPlayOutBlockChange(world, blockposition)); // SPIGOT-4238: needed when looking through entity
                    ((EntityPlayer) entityhuman).getBukkitEntity().updateInventory(); // SPIGOT-4541
                    return false;
                }
            }
            // CraftBukkit end
            if (!flag2) {
                return movingobjectpositionblock != null && this.emptyContents(entityhuman, world, movingobjectpositionblock.getBlockPos().relative(movingobjectpositionblock.getDirection()), (MovingObjectPositionBlock) null, enumdirection, clicked, itemstack, enumhand); // CraftBukkit
            } else if (world.dimensionType().ultraWarm() && this.content.is(TagsFluid.WATER)) {
                int i = blockposition.getX();
                int j = blockposition.getY();
                int k = blockposition.getZ();

                world.playSound(entityhuman, blockposition, SoundEffects.FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);

                for (int l = 0; l < 8; ++l) {
                    world.addParticle(Particles.LARGE_SMOKE, (double) i + Math.random(), (double) j + Math.random(), (double) k + Math.random(), 0.0D, 0.0D, 0.0D);
                }

                return true;
            } else {
                if (block instanceof IFluidContainer) {
                    ifluidcontainer = (IFluidContainer) block;
                    if (this.content == FluidTypes.WATER) {
                        ifluidcontainer.placeLiquid(world, blockposition, iblockdata, fluidtypeflowing.getSource(false));
                        this.playEmptySound(entityhuman, world, blockposition);
                        return true;
                    }
                }

                if (!world.isClientSide && flag && !iblockdata.liquid()) {
                    world.destroyBlock(blockposition, true);
                }

                if (!world.setBlock(blockposition, this.content.defaultFluidState().createLegacyBlock(), 11) && !iblockdata.getFluidState().isSource()) {
                    return false;
                } else {
                    this.playEmptySound(entityhuman, world, blockposition);
                    return true;
                }
            }
        }
    }

    protected void playEmptySound(@Nullable EntityHuman entityhuman, GeneratorAccess generatoraccess, BlockPosition blockposition) {
        SoundEffect soundeffect = this.content.is(TagsFluid.LAVA) ? SoundEffects.BUCKET_EMPTY_LAVA : SoundEffects.BUCKET_EMPTY;

        generatoraccess.playSound(entityhuman, blockposition, soundeffect, SoundCategory.BLOCKS, 1.0F, 1.0F);
        generatoraccess.gameEvent((Entity) entityhuman, (Holder) GameEvent.FLUID_PLACE, blockposition);
    }
}
