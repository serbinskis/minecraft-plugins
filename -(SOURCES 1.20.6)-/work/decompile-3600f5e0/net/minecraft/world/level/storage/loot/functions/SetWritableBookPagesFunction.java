package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetWritableBookPagesFunction extends LootItemFunctionConditional {

    public static final MapCodec<SetWritableBookPagesFunction> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(instance.group(WritableBookContent.PAGES_CODEC.fieldOf("pages").forGetter((setwritablebookpagesfunction) -> {
            return setwritablebookpagesfunction.pages;
        }), ListOperation.codec(100).forGetter((setwritablebookpagesfunction) -> {
            return setwritablebookpagesfunction.pageOperation;
        }))).apply(instance, SetWritableBookPagesFunction::new);
    });
    private final List<Filterable<String>> pages;
    private final ListOperation pageOperation;

    protected SetWritableBookPagesFunction(List<LootItemCondition> list, List<Filterable<String>> list1, ListOperation listoperation) {
        super(list);
        this.pages = list1;
        this.pageOperation = listoperation;
    }

    @Override
    protected ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        itemstack.update(DataComponents.WRITABLE_BOOK_CONTENT, WritableBookContent.EMPTY, this::apply);
        return itemstack;
    }

    public WritableBookContent apply(WritableBookContent writablebookcontent) {
        List<Filterable<String>> list = this.pageOperation.apply(writablebookcontent.pages(), this.pages, 100);

        return writablebookcontent.withReplacedPages(list);
    }

    @Override
    public LootItemFunctionType<SetWritableBookPagesFunction> getType() {
        return LootItemFunctions.SET_WRITABLE_BOOK_PAGES;
    }
}
