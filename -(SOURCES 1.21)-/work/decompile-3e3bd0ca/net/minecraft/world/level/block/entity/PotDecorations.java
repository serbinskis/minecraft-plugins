package net.minecraft.world.level.block.entity;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public record PotDecorations(Optional<Item> back, Optional<Item> left, Optional<Item> right, Optional<Item> front) {

    public static final PotDecorations EMPTY = new PotDecorations(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    public static final Codec<PotDecorations> CODEC = BuiltInRegistries.ITEM.byNameCodec().sizeLimitedListOf(4).xmap(PotDecorations::new, PotDecorations::ordered);
    public static final StreamCodec<RegistryFriendlyByteBuf, PotDecorations> STREAM_CODEC = ByteBufCodecs.registry(Registries.ITEM).apply(ByteBufCodecs.list(4)).map(PotDecorations::new, PotDecorations::ordered);

    private PotDecorations(List<Item> list) {
        this(getItem(list, 0), getItem(list, 1), getItem(list, 2), getItem(list, 3));
    }

    public PotDecorations(Item item, Item item1, Item item2, Item item3) {
        this(List.of(item, item1, item2, item3));
    }

    private static Optional<Item> getItem(List<Item> list, int i) {
        if (i >= list.size()) {
            return Optional.empty();
        } else {
            Item item = (Item) list.get(i);

            return item == Items.BRICK ? Optional.empty() : Optional.of(item);
        }
    }

    public NBTTagCompound save(NBTTagCompound nbttagcompound) {
        if (this.equals(PotDecorations.EMPTY)) {
            return nbttagcompound;
        } else {
            nbttagcompound.put("sherds", (NBTBase) PotDecorations.CODEC.encodeStart(DynamicOpsNBT.INSTANCE, this).getOrThrow());
            return nbttagcompound;
        }
    }

    public List<Item> ordered() {
        return Stream.of(this.back, this.left, this.right, this.front).map((optional) -> {
            return (Item) optional.orElse(Items.BRICK);
        }).toList();
    }

    public static PotDecorations load(@Nullable NBTTagCompound nbttagcompound) {
        return nbttagcompound != null && nbttagcompound.contains("sherds") ? (PotDecorations) PotDecorations.CODEC.parse(DynamicOpsNBT.INSTANCE, nbttagcompound.get("sherds")).result().orElse(PotDecorations.EMPTY) : PotDecorations.EMPTY;
    }
}
