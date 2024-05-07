package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WrittenBookContent;

public record ItemWrittenBookPredicate(Optional<CollectionPredicate<Filterable<IChatBaseComponent>, ItemWrittenBookPredicate.a>> pages, Optional<String> author, Optional<String> title, CriterionConditionValue.IntegerRange generation, Optional<Boolean> resolved) implements SingleComponentItemPredicate<WrittenBookContent> {

    public static final Codec<ItemWrittenBookPredicate> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(CollectionPredicate.codec(ItemWrittenBookPredicate.a.CODEC).optionalFieldOf("pages").forGetter(ItemWrittenBookPredicate::pages), Codec.STRING.optionalFieldOf("author").forGetter(ItemWrittenBookPredicate::author), Codec.STRING.optionalFieldOf("title").forGetter(ItemWrittenBookPredicate::title), CriterionConditionValue.IntegerRange.CODEC.optionalFieldOf("generation", CriterionConditionValue.IntegerRange.ANY).forGetter(ItemWrittenBookPredicate::generation), Codec.BOOL.optionalFieldOf("resolved").forGetter(ItemWrittenBookPredicate::resolved)).apply(instance, ItemWrittenBookPredicate::new);
    });

    @Override
    public DataComponentType<WrittenBookContent> componentType() {
        return DataComponents.WRITTEN_BOOK_CONTENT;
    }

    public boolean matches(ItemStack itemstack, WrittenBookContent writtenbookcontent) {
        return this.author.isPresent() && !((String) this.author.get()).equals(writtenbookcontent.author()) ? false : (this.title.isPresent() && !((String) this.title.get()).equals(writtenbookcontent.title().raw()) ? false : (!this.generation.matches(writtenbookcontent.generation()) ? false : (this.resolved.isPresent() && (Boolean) this.resolved.get() != writtenbookcontent.resolved() ? false : !this.pages.isPresent() || ((CollectionPredicate) this.pages.get()).test((Iterable) writtenbookcontent.pages()))));
    }

    public static record a(IChatBaseComponent contents) implements Predicate<Filterable<IChatBaseComponent>> {

        public static final Codec<ItemWrittenBookPredicate.a> CODEC = ComponentSerialization.CODEC.xmap(ItemWrittenBookPredicate.a::new, ItemWrittenBookPredicate.a::contents);

        public boolean test(Filterable<IChatBaseComponent> filterable) {
            return ((IChatBaseComponent) filterable.raw()).equals(this.contents);
        }
    }
}
