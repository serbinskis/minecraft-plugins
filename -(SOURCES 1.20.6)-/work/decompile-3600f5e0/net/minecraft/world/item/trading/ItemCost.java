package net.minecraft.world.item.trading;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.UnaryOperator;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.IMaterial;

public record ItemCost(Holder<Item> item, int count, DataComponentPredicate components, ItemStack itemStack) {

    public static final Codec<ItemCost> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(ItemStack.ITEM_NON_AIR_CODEC.fieldOf("id").forGetter(ItemCost::item), ExtraCodecs.POSITIVE_INT.fieldOf("count").orElse(1).forGetter(ItemCost::count), DataComponentPredicate.CODEC.optionalFieldOf("components", DataComponentPredicate.EMPTY).forGetter(ItemCost::components)).apply(instance, ItemCost::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemCost> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.holderRegistry(Registries.ITEM), ItemCost::item, ByteBufCodecs.VAR_INT, ItemCost::count, DataComponentPredicate.STREAM_CODEC, ItemCost::components, ItemCost::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, Optional<ItemCost>> OPTIONAL_STREAM_CODEC = ItemCost.STREAM_CODEC.apply(ByteBufCodecs::optional);

    public ItemCost(IMaterial imaterial) {
        this(imaterial, 1);
    }

    public ItemCost(IMaterial imaterial, int i) {
        this(imaterial.asItem().builtInRegistryHolder(), i, DataComponentPredicate.EMPTY);
    }

    public ItemCost(Holder<Item> holder, int i, DataComponentPredicate datacomponentpredicate) {
        this(holder, i, datacomponentpredicate, createStack(holder, i, datacomponentpredicate));
    }

    public ItemCost withComponents(UnaryOperator<DataComponentPredicate.a> unaryoperator) {
        return new ItemCost(this.item, this.count, ((DataComponentPredicate.a) unaryoperator.apply(DataComponentPredicate.builder())).build());
    }

    private static ItemStack createStack(Holder<Item> holder, int i, DataComponentPredicate datacomponentpredicate) {
        return new ItemStack(holder, i, datacomponentpredicate.asPatch());
    }

    public boolean test(ItemStack itemstack) {
        return itemstack.is(this.item) && this.components.test((DataComponentHolder) itemstack);
    }
}
