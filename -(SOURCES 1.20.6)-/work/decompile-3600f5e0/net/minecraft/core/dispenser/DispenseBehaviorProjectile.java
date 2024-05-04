package net.minecraft.core.dispenser;

import net.minecraft.core.EnumDirection;
import net.minecraft.core.IPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.projectile.IProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.block.BlockDispenser;

public class DispenseBehaviorProjectile extends DispenseBehaviorItem {

    private final ProjectileItem projectileItem;
    private final ProjectileItem.a dispenseConfig;

    public DispenseBehaviorProjectile(Item item) {
        if (item instanceof ProjectileItem projectileitem) {
            this.projectileItem = projectileitem;
            this.dispenseConfig = projectileitem.createDispenseConfig();
        } else {
            String s = String.valueOf(item);

            throw new IllegalArgumentException(s + " not instance of " + ProjectileItem.class.getSimpleName());
        }
    }

    @Override
    public ItemStack execute(SourceBlock sourceblock, ItemStack itemstack) {
        WorldServer worldserver = sourceblock.level();
        EnumDirection enumdirection = (EnumDirection) sourceblock.state().getValue(BlockDispenser.FACING);
        IPosition iposition = this.dispenseConfig.positionFunction().getDispensePosition(sourceblock, enumdirection);
        IProjectile iprojectile = this.projectileItem.asProjectile(worldserver, iposition, itemstack, enumdirection);

        this.projectileItem.shoot(iprojectile, (double) enumdirection.getStepX(), (double) enumdirection.getStepY(), (double) enumdirection.getStepZ(), this.dispenseConfig.power(), this.dispenseConfig.uncertainty());
        worldserver.addFreshEntity(iprojectile);
        itemstack.shrink(1);
        return itemstack;
    }

    @Override
    protected void playSound(SourceBlock sourceblock) {
        sourceblock.level().levelEvent(this.dispenseConfig.overrideDispenseEvent().orElse(1002), sourceblock.pos(), 0);
    }
}
