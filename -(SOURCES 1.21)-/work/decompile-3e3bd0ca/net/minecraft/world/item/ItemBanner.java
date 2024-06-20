package net.minecraft.world.item;

import java.util.List;
import net.minecraft.EnumChatFormat;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockBannerAbstract;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.apache.commons.lang3.Validate;

public class ItemBanner extends ItemBlockWallable {

    public ItemBanner(Block block, Block block1, Item.Info item_info) {
        super(block, block1, item_info, EnumDirection.DOWN);
        Validate.isInstanceOf(BlockBannerAbstract.class, block);
        Validate.isInstanceOf(BlockBannerAbstract.class, block1);
    }

    public static void appendHoverTextFromBannerBlockEntityTag(ItemStack itemstack, List<IChatBaseComponent> list) {
        BannerPatternLayers bannerpatternlayers = (BannerPatternLayers) itemstack.get(DataComponents.BANNER_PATTERNS);

        if (bannerpatternlayers != null) {
            for (int i = 0; i < Math.min(bannerpatternlayers.layers().size(), 6); ++i) {
                BannerPatternLayers.b bannerpatternlayers_b = (BannerPatternLayers.b) bannerpatternlayers.layers().get(i);

                list.add(bannerpatternlayers_b.description().withStyle(EnumChatFormat.GRAY));
            }

        }
    }

    public EnumColor getColor() {
        return ((BlockBannerAbstract) this.getBlock()).getColor();
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.b item_b, List<IChatBaseComponent> list, TooltipFlag tooltipflag) {
        appendHoverTextFromBannerBlockEntityTag(itemstack, list);
    }
}
