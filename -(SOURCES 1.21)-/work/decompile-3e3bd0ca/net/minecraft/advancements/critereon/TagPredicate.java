package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public record TagPredicate<T>(TagKey<T> tag, boolean expected) {

    public static <T> Codec<TagPredicate<T>> codec(ResourceKey<? extends IRegistry<T>> resourcekey) {
        return RecordCodecBuilder.create((instance) -> {
            return instance.group(TagKey.codec(resourcekey).fieldOf("id").forGetter(TagPredicate::tag), Codec.BOOL.fieldOf("expected").forGetter(TagPredicate::expected)).apply(instance, TagPredicate::new);
        });
    }

    public static <T> TagPredicate<T> is(TagKey<T> tagkey) {
        return new TagPredicate<>(tagkey, true);
    }

    public static <T> TagPredicate<T> isNot(TagKey<T> tagkey) {
        return new TagPredicate<>(tagkey, false);
    }

    public boolean matches(Holder<T> holder) {
        return holder.is(this.tag) == this.expected;
    }
}
