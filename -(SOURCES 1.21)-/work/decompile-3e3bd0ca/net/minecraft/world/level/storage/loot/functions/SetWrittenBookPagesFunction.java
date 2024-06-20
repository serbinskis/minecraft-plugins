package net.minecraft.world.level.storage.loot.functions;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetWrittenBookPagesFunction extends LootItemFunctionConditional {

    public static final Codec<IChatBaseComponent> PAGE_CODEC = ComponentSerialization.CODEC.validate((ichatbasecomponent) -> {
        return WrittenBookContent.CONTENT_CODEC.encodeStart(JavaOps.INSTANCE, ichatbasecomponent).map((object) -> {
            return ichatbasecomponent;
        });
    });
    public static final MapCodec<SetWrittenBookPagesFunction> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(instance.group(WrittenBookContent.pagesCodec(SetWrittenBookPagesFunction.PAGE_CODEC).fieldOf("pages").forGetter((setwrittenbookpagesfunction) -> {
            return setwrittenbookpagesfunction.pages;
        }), ListOperation.UNLIMITED_CODEC.forGetter((setwrittenbookpagesfunction) -> {
            return setwrittenbookpagesfunction.pageOperation;
        }))).apply(instance, SetWrittenBookPagesFunction::new);
    });
    private final List<Filterable<IChatBaseComponent>> pages;
    private final ListOperation pageOperation;

    protected SetWrittenBookPagesFunction(List<LootItemCondition> list, List<Filterable<IChatBaseComponent>> list1, ListOperation listoperation) {
        super(list);
        this.pages = list1;
        this.pageOperation = listoperation;
    }

    @Override
    protected ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        itemstack.update(DataComponents.WRITTEN_BOOK_CONTENT, WrittenBookContent.EMPTY, this::apply);
        return itemstack;
    }

    @VisibleForTesting
    public WrittenBookContent apply(WrittenBookContent writtenbookcontent) {
        List<Filterable<IChatBaseComponent>> list = this.pageOperation.apply(writtenbookcontent.pages(), this.pages);

        return writtenbookcontent.withReplacedPages(list);
    }

    @Override
    public LootItemFunctionType<SetWrittenBookPagesFunction> getType() {
        return LootItemFunctions.SET_WRITTEN_BOOK_PAGES;
    }
}
