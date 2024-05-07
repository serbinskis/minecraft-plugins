package net.minecraft.world.item;

import java.util.List;
import java.util.Objects;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.item.component.FireworkExplosion;

public class ItemFireworksCharge extends Item {

    public ItemFireworksCharge(Item.Info item_info) {
        super(item_info);
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.b item_b, List<IChatBaseComponent> list, TooltipFlag tooltipflag) {
        FireworkExplosion fireworkexplosion = (FireworkExplosion) itemstack.get(DataComponents.FIREWORK_EXPLOSION);

        if (fireworkexplosion != null) {
            Objects.requireNonNull(list);
            fireworkexplosion.addToTooltip(item_b, list::add, tooltipflag);
        }

    }
}
