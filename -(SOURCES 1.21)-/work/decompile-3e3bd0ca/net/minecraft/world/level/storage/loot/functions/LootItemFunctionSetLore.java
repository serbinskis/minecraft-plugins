package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootItemFunctionSetLore extends LootItemFunctionConditional {

    public static final MapCodec<LootItemFunctionSetLore> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(instance.group(ComponentSerialization.CODEC.sizeLimitedListOf(256).fieldOf("lore").forGetter((lootitemfunctionsetlore) -> {
            return lootitemfunctionsetlore.lore;
        }), ListOperation.codec(256).forGetter((lootitemfunctionsetlore) -> {
            return lootitemfunctionsetlore.mode;
        }), LootTableInfo.EntityTarget.CODEC.optionalFieldOf("entity").forGetter((lootitemfunctionsetlore) -> {
            return lootitemfunctionsetlore.resolutionContext;
        }))).apply(instance, LootItemFunctionSetLore::new);
    });
    private final List<IChatBaseComponent> lore;
    private final ListOperation mode;
    private final Optional<LootTableInfo.EntityTarget> resolutionContext;

    public LootItemFunctionSetLore(List<LootItemCondition> list, List<IChatBaseComponent> list1, ListOperation listoperation, Optional<LootTableInfo.EntityTarget> optional) {
        super(list);
        this.lore = List.copyOf(list1);
        this.mode = listoperation;
        this.resolutionContext = optional;
    }

    @Override
    public LootItemFunctionType<LootItemFunctionSetLore> getType() {
        return LootItemFunctions.SET_LORE;
    }

    @Override
    public Set<LootContextParameter<?>> getReferencedContextParams() {
        return (Set) this.resolutionContext.map((loottableinfo_entitytarget) -> {
            return Set.of(loottableinfo_entitytarget.getParam());
        }).orElseGet(Set::of);
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        itemstack.update(DataComponents.LORE, ItemLore.EMPTY, (itemlore) -> {
            return new ItemLore(this.updateLore(itemlore, loottableinfo));
        });
        return itemstack;
    }

    private List<IChatBaseComponent> updateLore(@Nullable ItemLore itemlore, LootTableInfo loottableinfo) {
        if (itemlore == null && this.lore.isEmpty()) {
            return List.of();
        } else {
            UnaryOperator<IChatBaseComponent> unaryoperator = LootItemFunctionSetName.createResolver(loottableinfo, (LootTableInfo.EntityTarget) this.resolutionContext.orElse((Object) null));
            List<IChatBaseComponent> list = this.lore.stream().map(unaryoperator).toList();
            List<IChatBaseComponent> list1 = this.mode.apply(itemlore.lines(), list, 256);

            return list1;
        }
    }

    public static LootItemFunctionSetLore.a setLore() {
        return new LootItemFunctionSetLore.a();
    }

    public static class a extends LootItemFunctionConditional.a<LootItemFunctionSetLore.a> {

        private Optional<LootTableInfo.EntityTarget> resolutionContext = Optional.empty();
        private final Builder<IChatBaseComponent> lore = ImmutableList.builder();
        private ListOperation mode;

        public a() {
            this.mode = ListOperation.a.INSTANCE;
        }

        public LootItemFunctionSetLore.a setMode(ListOperation listoperation) {
            this.mode = listoperation;
            return this;
        }

        public LootItemFunctionSetLore.a setResolutionContext(LootTableInfo.EntityTarget loottableinfo_entitytarget) {
            this.resolutionContext = Optional.of(loottableinfo_entitytarget);
            return this;
        }

        public LootItemFunctionSetLore.a addLine(IChatBaseComponent ichatbasecomponent) {
            this.lore.add(ichatbasecomponent);
            return this;
        }

        @Override
        protected LootItemFunctionSetLore.a getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new LootItemFunctionSetLore(this.getConditions(), this.lore.build(), this.mode, this.resolutionContext);
        }
    }
}
