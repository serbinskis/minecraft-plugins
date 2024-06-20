package net.minecraft.world.item;

import java.util.List;
import net.minecraft.EnumChatFormat;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.entity.EnumBannerPatternType;

public class ItemBannerPattern extends Item {

    private final TagKey<EnumBannerPatternType> bannerPattern;

    public ItemBannerPattern(TagKey<EnumBannerPatternType> tagkey, Item.Info item_info) {
        super(item_info);
        this.bannerPattern = tagkey;
    }

    public TagKey<EnumBannerPatternType> getBannerPattern() {
        return this.bannerPattern;
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.b item_b, List<IChatBaseComponent> list, TooltipFlag tooltipflag) {
        list.add(this.getDisplayName().withStyle(EnumChatFormat.GRAY));
    }

    public IChatMutableComponent getDisplayName() {
        return IChatBaseComponent.translatable(this.getDescriptionId() + ".desc");
    }
}
