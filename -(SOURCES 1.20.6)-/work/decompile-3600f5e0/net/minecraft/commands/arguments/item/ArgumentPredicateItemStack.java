package net.minecraft.commands.arguments.item;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ArgumentPredicateItemStack {

    private static final Dynamic2CommandExceptionType ERROR_STACK_TOO_BIG = new Dynamic2CommandExceptionType((object, object1) -> {
        return IChatBaseComponent.translatableEscape("arguments.item.overstacked", object, object1);
    });
    private final Holder<Item> item;
    private final DataComponentMap components;

    public ArgumentPredicateItemStack(Holder<Item> holder, DataComponentMap datacomponentmap) {
        this.item = holder;
        this.components = datacomponentmap;
    }

    public Item getItem() {
        return (Item) this.item.value();
    }

    public ItemStack createItemStack(int i, boolean flag) throws CommandSyntaxException {
        ItemStack itemstack = new ItemStack(this.item, i);

        itemstack.applyComponents(this.components);
        if (flag && i > itemstack.getMaxStackSize()) {
            throw ArgumentPredicateItemStack.ERROR_STACK_TOO_BIG.create(this.getItemName(), itemstack.getMaxStackSize());
        } else {
            return itemstack;
        }
    }

    public String serialize(HolderLookup.a holderlookup_a) {
        StringBuilder stringbuilder = new StringBuilder(this.getItemName());
        String s = this.serializeComponents(holderlookup_a);

        if (!s.isEmpty()) {
            stringbuilder.append('[');
            stringbuilder.append(s);
            stringbuilder.append(']');
        }

        return stringbuilder.toString();
    }

    private String serializeComponents(HolderLookup.a holderlookup_a) {
        DynamicOps<NBTBase> dynamicops = holderlookup_a.createSerializationContext(DynamicOpsNBT.INSTANCE);

        return (String) this.components.stream().flatMap((typeddatacomponent) -> {
            DataComponentType<?> datacomponenttype = typeddatacomponent.type();
            MinecraftKey minecraftkey = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(datacomponenttype);
            Optional<NBTBase> optional = typeddatacomponent.encodeValue(dynamicops).result();

            if (minecraftkey != null && !optional.isEmpty()) {
                String s = minecraftkey.toString();

                return Stream.of(s + "=" + String.valueOf(optional.get()));
            } else {
                return Stream.empty();
            }
        }).collect(Collectors.joining(String.valueOf(',')));
    }

    private String getItemName() {
        return this.item.unwrapKey().map(ResourceKey::location).orElseGet(() -> {
            return "unknown[" + String.valueOf(this.item) + "]";
        }).toString();
    }
}
