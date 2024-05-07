package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.EnumChatFormat;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.stats.StatisticList;
import net.minecraft.util.UtilColor;
import net.minecraft.world.EnumHand;
import net.minecraft.world.InteractionResultWrapper;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.World;

public class ItemWrittenBook extends Item {

    public ItemWrittenBook(Item.Info item_info) {
        super(item_info);
    }

    @Override
    public IChatBaseComponent getName(ItemStack itemstack) {
        WrittenBookContent writtenbookcontent = (WrittenBookContent) itemstack.get(DataComponents.WRITTEN_BOOK_CONTENT);

        if (writtenbookcontent != null) {
            String s = (String) writtenbookcontent.title().raw();

            if (!UtilColor.isBlank(s)) {
                return IChatBaseComponent.literal(s);
            }
        }

        return super.getName(itemstack);
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.b item_b, List<IChatBaseComponent> list, TooltipFlag tooltipflag) {
        WrittenBookContent writtenbookcontent = (WrittenBookContent) itemstack.get(DataComponents.WRITTEN_BOOK_CONTENT);

        if (writtenbookcontent != null) {
            if (!UtilColor.isBlank(writtenbookcontent.author())) {
                list.add(IChatBaseComponent.translatable("book.byAuthor", writtenbookcontent.author()).withStyle(EnumChatFormat.GRAY));
            }

            list.add(IChatBaseComponent.translatable("book.generation." + writtenbookcontent.generation()).withStyle(EnumChatFormat.GRAY));
        }

    }

    @Override
    public InteractionResultWrapper<ItemStack> use(World world, EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);

        entityhuman.openItemGui(itemstack, enumhand);
        entityhuman.awardStat(StatisticList.ITEM_USED.get(this));
        return InteractionResultWrapper.sidedSuccess(itemstack, world.isClientSide());
    }

    public static boolean resolveBookComponents(ItemStack itemstack, CommandListenerWrapper commandlistenerwrapper, @Nullable EntityHuman entityhuman) {
        WrittenBookContent writtenbookcontent = (WrittenBookContent) itemstack.get(DataComponents.WRITTEN_BOOK_CONTENT);

        if (writtenbookcontent != null && !writtenbookcontent.resolved()) {
            WrittenBookContent writtenbookcontent1 = writtenbookcontent.resolve(commandlistenerwrapper, entityhuman);

            if (writtenbookcontent1 != null) {
                itemstack.set(DataComponents.WRITTEN_BOOK_CONTENT, writtenbookcontent1);
                return true;
            }

            itemstack.set(DataComponents.WRITTEN_BOOK_CONTENT, writtenbookcontent.markResolved());
        }

        return false;
    }
}
