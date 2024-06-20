package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WritableBookContent;

public record ItemWritableBookPredicate(Optional<CollectionPredicate<Filterable<String>, ItemWritableBookPredicate.a>> pages) implements SingleComponentItemPredicate<WritableBookContent> {

    public static final Codec<ItemWritableBookPredicate> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(CollectionPredicate.codec(ItemWritableBookPredicate.a.CODEC).optionalFieldOf("pages").forGetter(ItemWritableBookPredicate::pages)).apply(instance, ItemWritableBookPredicate::new);
    });

    @Override
    public DataComponentType<WritableBookContent> componentType() {
        return DataComponents.WRITABLE_BOOK_CONTENT;
    }

    public boolean matches(ItemStack itemstack, WritableBookContent writablebookcontent) {
        return !this.pages.isPresent() || ((CollectionPredicate) this.pages.get()).test((Iterable) writablebookcontent.pages());
    }

    public static record a(String contents) implements Predicate<Filterable<String>> {

        public static final Codec<ItemWritableBookPredicate.a> CODEC = Codec.STRING.xmap(ItemWritableBookPredicate.a::new, ItemWritableBookPredicate.a::contents);

        public boolean test(Filterable<String> filterable) {
            return ((String) filterable.raw()).equals(this.contents);
        }
    }
}
