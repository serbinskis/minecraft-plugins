package net.minecraft.world.item;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.IPosition;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.RandomSource;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.projectile.EntitySmallFireball;
import net.minecraft.world.entity.projectile.IProjectile;
import net.minecraft.world.item.context.ItemActionContext;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.BlockCampfire;
import net.minecraft.world.level.block.BlockDispenser;
import net.minecraft.world.level.block.BlockFireAbstract;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3D;

public class ItemFireball extends Item implements ProjectileItem {

    public ItemFireball(Item.Info item_info) {
        super(item_info);
    }

    @Override
    public EnumInteractionResult useOn(ItemActionContext itemactioncontext) {
        World world = itemactioncontext.getLevel();
        BlockPosition blockposition = itemactioncontext.getClickedPos();
        IBlockData iblockdata = world.getBlockState(blockposition);
        boolean flag = false;

        if (!BlockCampfire.canLight(iblockdata) && !CandleBlock.canLight(iblockdata) && !CandleCakeBlock.canLight(iblockdata)) {
            blockposition = blockposition.relative(itemactioncontext.getClickedFace());
            if (BlockFireAbstract.canBePlacedAt(world, blockposition, itemactioncontext.getHorizontalDirection())) {
                // CraftBukkit start - fire BlockIgniteEvent
                if (org.bukkit.craftbukkit.event.CraftEventFactory.callBlockIgniteEvent(world, blockposition, org.bukkit.event.block.BlockIgniteEvent.IgniteCause.FIREBALL, itemactioncontext.getPlayer()).isCancelled()) {
                    if (!itemactioncontext.getPlayer().getAbilities().instabuild) {
                        itemactioncontext.getItemInHand().shrink(1);
                    }
                    return EnumInteractionResult.PASS;
                }
                // CraftBukkit end
                this.playSound(world, blockposition);
                world.setBlockAndUpdate(blockposition, BlockFireAbstract.getState(world, blockposition));
                world.gameEvent((Entity) itemactioncontext.getPlayer(), (Holder) GameEvent.BLOCK_PLACE, blockposition);
                flag = true;
            }
        } else {
            // CraftBukkit start - fire BlockIgniteEvent
            if (org.bukkit.craftbukkit.event.CraftEventFactory.callBlockIgniteEvent(world, blockposition, org.bukkit.event.block.BlockIgniteEvent.IgniteCause.FIREBALL, itemactioncontext.getPlayer()).isCancelled()) {
                if (!itemactioncontext.getPlayer().getAbilities().instabuild) {
                    itemactioncontext.getItemInHand().shrink(1);
                }
                return EnumInteractionResult.PASS;
            }
            // CraftBukkit end
            this.playSound(world, blockposition);
            world.setBlockAndUpdate(blockposition, (IBlockData) iblockdata.setValue(BlockProperties.LIT, true));
            world.gameEvent((Entity) itemactioncontext.getPlayer(), (Holder) GameEvent.BLOCK_CHANGE, blockposition);
            flag = true;
        }

        if (flag) {
            itemactioncontext.getItemInHand().shrink(1);
            return EnumInteractionResult.sidedSuccess(world.isClientSide);
        } else {
            return EnumInteractionResult.FAIL;
        }
    }

    private void playSound(World world, BlockPosition blockposition) {
        RandomSource randomsource = world.getRandom();

        world.playSound((EntityHuman) null, blockposition, SoundEffects.FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0F, (randomsource.nextFloat() - randomsource.nextFloat()) * 0.2F + 1.0F);
    }

    @Override
    public IProjectile asProjectile(World world, IPosition iposition, ItemStack itemstack, EnumDirection enumdirection) {
        RandomSource randomsource = world.getRandom();
        double d0 = randomsource.triangle((double) enumdirection.getStepX(), 0.11485000000000001D);
        double d1 = randomsource.triangle((double) enumdirection.getStepY(), 0.11485000000000001D);
        double d2 = randomsource.triangle((double) enumdirection.getStepZ(), 0.11485000000000001D);
        EntitySmallFireball entitysmallfireball = new EntitySmallFireball(world, iposition.x(), iposition.y(), iposition.z(), d0, d1, d2);

        entitysmallfireball.setItem(itemstack);
        return entitysmallfireball;
    }

    @Override
    public void shoot(IProjectile iprojectile, double d0, double d1, double d2, float f, float f1) {}

    @Override
    public ProjectileItem.a createDispenseConfig() {
        return ProjectileItem.a.builder().positionFunction((sourceblock, enumdirection) -> {
            return BlockDispenser.getDispensePosition(sourceblock, 1.0D, Vec3D.ZERO);
        }).uncertainty(6.6666665F).power(1.0F).overrideDispenseEvent(1018).build();
    }
}
