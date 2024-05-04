package net.minecraft.world.item;

import net.minecraft.core.EnumDirection;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.TileEntitySkull;

public class ItemSkullPlayer extends ItemBlockWallable {

    public ItemSkullPlayer(Block block, Block block1, Item.Info item_info) {
        super(block, block1, item_info, EnumDirection.DOWN);
    }

    @Override
    public IChatBaseComponent getName(ItemStack itemstack) {
        ResolvableProfile resolvableprofile = (ResolvableProfile) itemstack.get(DataComponents.PROFILE);

        return (IChatBaseComponent) (resolvableprofile != null && resolvableprofile.name().isPresent() ? IChatBaseComponent.translatable(this.getDescriptionId() + ".named", resolvableprofile.name().get()) : super.getName(itemstack));
    }

    @Override
    public void verifyComponentsAfterLoad(ItemStack itemstack) {
        ResolvableProfile resolvableprofile = (ResolvableProfile) itemstack.get(DataComponents.PROFILE);

        if (resolvableprofile != null && !resolvableprofile.isResolved()) {
            resolvableprofile.resolve().thenAcceptAsync((resolvableprofile1) -> {
                itemstack.set(DataComponents.PROFILE, resolvableprofile1);
            }, TileEntitySkull.CHECKED_MAIN_THREAD_EXECUTOR);
        }

    }
}
