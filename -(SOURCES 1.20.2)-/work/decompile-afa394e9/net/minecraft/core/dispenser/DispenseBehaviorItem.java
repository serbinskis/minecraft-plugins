package net.minecraft.core.dispenser;

import net.minecraft.core.EnumDirection;
import net.minecraft.core.IPosition;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.BlockDispenser;

public class DispenseBehaviorItem implements IDispenseBehavior {

    public DispenseBehaviorItem() {}

    @Override
    public final ItemStack dispense(SourceBlock sourceblock, ItemStack itemstack) {
        ItemStack itemstack1 = this.execute(sourceblock, itemstack);

        this.playSound(sourceblock);
        this.playAnimation(sourceblock, (EnumDirection) sourceblock.state().getValue(BlockDispenser.FACING));
        return itemstack1;
    }

    protected ItemStack execute(SourceBlock sourceblock, ItemStack itemstack) {
        EnumDirection enumdirection = (EnumDirection) sourceblock.state().getValue(BlockDispenser.FACING);
        IPosition iposition = BlockDispenser.getDispensePosition(sourceblock);
        ItemStack itemstack1 = itemstack.split(1);

        spawnItem(sourceblock.level(), itemstack1, 6, enumdirection, iposition);
        return itemstack;
    }

    public static void spawnItem(World world, ItemStack itemstack, int i, EnumDirection enumdirection, IPosition iposition) {
        double d0 = iposition.x();
        double d1 = iposition.y();
        double d2 = iposition.z();

        if (enumdirection.getAxis() == EnumDirection.EnumAxis.Y) {
            d1 -= 0.125D;
        } else {
            d1 -= 0.15625D;
        }

        EntityItem entityitem = new EntityItem(world, d0, d1, d2, itemstack);
        double d3 = world.random.nextDouble() * 0.1D + 0.2D;

        entityitem.setDeltaMovement(world.random.triangle((double) enumdirection.getStepX() * d3, 0.0172275D * (double) i), world.random.triangle(0.2D, 0.0172275D * (double) i), world.random.triangle((double) enumdirection.getStepZ() * d3, 0.0172275D * (double) i));
        world.addFreshEntity(entityitem);
    }

    protected void playSound(SourceBlock sourceblock) {
        sourceblock.level().levelEvent(1000, sourceblock.pos(), 0);
    }

    protected void playAnimation(SourceBlock sourceblock, EnumDirection enumdirection) {
        sourceblock.level().levelEvent(2000, sourceblock.pos(), enumdirection.get3DDataValue());
    }
}
