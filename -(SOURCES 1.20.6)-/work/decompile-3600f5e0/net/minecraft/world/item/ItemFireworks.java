package net.minecraft.world.item;

import java.util.List;
import java.util.Objects;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.IPosition;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.dispenser.SourceBlock;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.stats.StatisticList;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.InteractionResultWrapper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.projectile.EntityFireworks;
import net.minecraft.world.entity.projectile.IProjectile;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.context.ItemActionContext;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.Vec3D;

public class ItemFireworks extends Item implements ProjectileItem {

    public static final byte[] CRAFTABLE_DURATIONS = new byte[]{1, 2, 3};
    public static final double ROCKET_PLACEMENT_OFFSET = 0.15D;

    public ItemFireworks(Item.Info item_info) {
        super(item_info);
    }

    @Override
    public EnumInteractionResult useOn(ItemActionContext itemactioncontext) {
        World world = itemactioncontext.getLevel();

        if (!world.isClientSide) {
            ItemStack itemstack = itemactioncontext.getItemInHand();
            Vec3D vec3d = itemactioncontext.getClickLocation();
            EnumDirection enumdirection = itemactioncontext.getClickedFace();
            EntityFireworks entityfireworks = new EntityFireworks(world, itemactioncontext.getPlayer(), vec3d.x + (double) enumdirection.getStepX() * 0.15D, vec3d.y + (double) enumdirection.getStepY() * 0.15D, vec3d.z + (double) enumdirection.getStepZ() * 0.15D, itemstack);

            world.addFreshEntity(entityfireworks);
            itemstack.shrink(1);
        }

        return EnumInteractionResult.sidedSuccess(world.isClientSide);
    }

    @Override
    public InteractionResultWrapper<ItemStack> use(World world, EntityHuman entityhuman, EnumHand enumhand) {
        if (entityhuman.isFallFlying()) {
            ItemStack itemstack = entityhuman.getItemInHand(enumhand);

            if (!world.isClientSide) {
                EntityFireworks entityfireworks = new EntityFireworks(world, itemstack, entityhuman);

                world.addFreshEntity(entityfireworks);
                itemstack.consume(1, entityhuman);
                entityhuman.awardStat(StatisticList.ITEM_USED.get(this));
            }

            return InteractionResultWrapper.sidedSuccess(entityhuman.getItemInHand(enumhand), world.isClientSide());
        } else {
            return InteractionResultWrapper.pass(entityhuman.getItemInHand(enumhand));
        }
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.b item_b, List<IChatBaseComponent> list, TooltipFlag tooltipflag) {
        Fireworks fireworks = (Fireworks) itemstack.get(DataComponents.FIREWORKS);

        if (fireworks != null) {
            Objects.requireNonNull(list);
            fireworks.addToTooltip(item_b, list::add, tooltipflag);
        }

    }

    @Override
    public IProjectile asProjectile(World world, IPosition iposition, ItemStack itemstack, EnumDirection enumdirection) {
        return new EntityFireworks(world, itemstack.copyWithCount(1), iposition.x(), iposition.y(), iposition.z(), true);
    }

    @Override
    public ProjectileItem.a createDispenseConfig() {
        return ProjectileItem.a.builder().positionFunction(ItemFireworks::getEntityPokingOutOfBlockPos).uncertainty(1.0F).power(0.5F).overrideDispenseEvent(1004).build();
    }

    private static Vec3D getEntityPokingOutOfBlockPos(SourceBlock sourceblock, EnumDirection enumdirection) {
        return sourceblock.center().add((double) enumdirection.getStepX() * (0.5000099999997474D - (double) EntityTypes.FIREWORK_ROCKET.getWidth() / 2.0D), (double) enumdirection.getStepY() * (0.5000099999997474D - (double) EntityTypes.FIREWORK_ROCKET.getHeight() / 2.0D) - (double) EntityTypes.FIREWORK_ROCKET.getHeight() / 2.0D, (double) enumdirection.getStepZ() * (0.5000099999997474D - (double) EntityTypes.FIREWORK_ROCKET.getWidth() / 2.0D));
    }
}
