package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.network.Filterable;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetBookCoverFunction extends LootItemFunctionConditional {

    public static final MapCodec<SetBookCoverFunction> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(instance.group(Filterable.codec(Codec.string(0, 32)).optionalFieldOf("title").forGetter((setbookcoverfunction) -> {
            return setbookcoverfunction.title;
        }), Codec.STRING.optionalFieldOf("author").forGetter((setbookcoverfunction) -> {
            return setbookcoverfunction.author;
        }), ExtraCodecs.intRange(0, 3).optionalFieldOf("generation").forGetter((setbookcoverfunction) -> {
            return setbookcoverfunction.generation;
        }))).apply(instance, SetBookCoverFunction::new);
    });
    private final Optional<String> author;
    private final Optional<Filterable<String>> title;
    private final Optional<Integer> generation;

    public SetBookCoverFunction(List<LootItemCondition> list, Optional<Filterable<String>> optional, Optional<String> optional1, Optional<Integer> optional2) {
        super(list);
        this.author = optional1;
        this.title = optional;
        this.generation = optional2;
    }

    @Override
    protected ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        itemstack.update(DataComponents.WRITTEN_BOOK_CONTENT, WrittenBookContent.EMPTY, this::apply);
        return itemstack;
    }

    private WrittenBookContent apply(WrittenBookContent writtenbookcontent) {
        Optional optional = this.title;

        Objects.requireNonNull(writtenbookcontent);
        Filterable filterable = (Filterable) optional.orElseGet(writtenbookcontent::title);
        Optional optional1 = this.author;

        Objects.requireNonNull(writtenbookcontent);
        String s = (String) optional1.orElseGet(writtenbookcontent::author);
        Optional optional2 = this.generation;

        Objects.requireNonNull(writtenbookcontent);
        return new WrittenBookContent(filterable, s, (Integer) optional2.orElseGet(writtenbookcontent::generation), writtenbookcontent.pages(), writtenbookcontent.resolved());
    }

    @Override
    public LootItemFunctionType<SetBookCoverFunction> getType() {
        return LootItemFunctions.SET_BOOK_COVER;
    }
}
