package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootItemFunctionSetLore extends LootItemFunctionConditional {

    public static final Codec<LootItemFunctionSetLore> CODEC = RecordCodecBuilder.create((instance) -> {
        return commonFields(instance).and(instance.group(Codec.BOOL.fieldOf("replace").orElse(false).forGetter((lootitemfunctionsetlore) -> {
            return lootitemfunctionsetlore.replace;
        }), ExtraCodecs.COMPONENT.listOf().fieldOf("lore").forGetter((lootitemfunctionsetlore) -> {
            return lootitemfunctionsetlore.lore;
        }), ExtraCodecs.strictOptionalField(LootTableInfo.EntityTarget.CODEC, "entity").forGetter((lootitemfunctionsetlore) -> {
            return lootitemfunctionsetlore.resolutionContext;
        }))).apply(instance, LootItemFunctionSetLore::new);
    });
    private final boolean replace;
    private final List<IChatBaseComponent> lore;
    private final Optional<LootTableInfo.EntityTarget> resolutionContext;

    public LootItemFunctionSetLore(List<LootItemCondition> list, boolean flag, List<IChatBaseComponent> list1, Optional<LootTableInfo.EntityTarget> optional) {
        super(list);
        this.replace = flag;
        this.lore = List.copyOf(list1);
        this.resolutionContext = optional;
    }

    @Override
    public LootItemFunctionType getType() {
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
        NBTTagList nbttaglist = this.getLoreTag(itemstack, !this.lore.isEmpty());

        if (nbttaglist != null) {
            if (this.replace) {
                nbttaglist.clear();
            }

            UnaryOperator<IChatBaseComponent> unaryoperator = LootItemFunctionSetName.createResolver(loottableinfo, (LootTableInfo.EntityTarget) this.resolutionContext.orElse((Object) null));
            Stream stream = this.lore.stream().map(unaryoperator).map(IChatBaseComponent.ChatSerializer::toJson).map(NBTTagString::valueOf);

            Objects.requireNonNull(nbttaglist);
            stream.forEach(nbttaglist::add);
        }

        return itemstack;
    }

    @Nullable
    private NBTTagList getLoreTag(ItemStack itemstack, boolean flag) {
        NBTTagCompound nbttagcompound;

        if (itemstack.hasTag()) {
            nbttagcompound = itemstack.getTag();
        } else {
            if (!flag) {
                return null;
            }

            nbttagcompound = new NBTTagCompound();
            itemstack.setTag(nbttagcompound);
        }

        NBTTagCompound nbttagcompound1;

        if (nbttagcompound.contains("display", 10)) {
            nbttagcompound1 = nbttagcompound.getCompound("display");
        } else {
            if (!flag) {
                return null;
            }

            nbttagcompound1 = new NBTTagCompound();
            nbttagcompound.put("display", nbttagcompound1);
        }

        if (nbttagcompound1.contains("Lore", 9)) {
            return nbttagcompound1.getList("Lore", 8);
        } else if (flag) {
            NBTTagList nbttaglist = new NBTTagList();

            nbttagcompound1.put("Lore", nbttaglist);
            return nbttaglist;
        } else {
            return null;
        }
    }

    public static LootItemFunctionSetLore.a setLore() {
        return new LootItemFunctionSetLore.a();
    }

    public static class a extends LootItemFunctionConditional.a<LootItemFunctionSetLore.a> {

        private boolean replace;
        private Optional<LootTableInfo.EntityTarget> resolutionContext = Optional.empty();
        private final Builder<IChatBaseComponent> lore = ImmutableList.builder();

        public a() {}

        public LootItemFunctionSetLore.a setReplace(boolean flag) {
            this.replace = flag;
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
            return new LootItemFunctionSetLore(this.getConditions(), this.replace, this.lore.build(), this.resolutionContext);
        }
    }
}
