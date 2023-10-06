package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.EnumColor;
import net.minecraft.world.item.ItemBlock;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.EnumBannerPatternType;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetBannerPatternFunction extends LootItemFunctionConditional {

    private static final Codec<Pair<Holder<EnumBannerPatternType>, EnumColor>> PATTERN_CODEC = Codec.mapPair(BuiltInRegistries.BANNER_PATTERN.holderByNameCodec().fieldOf("pattern"), EnumColor.CODEC.fieldOf("color")).codec();
    public static final Codec<SetBannerPatternFunction> CODEC = RecordCodecBuilder.create((instance) -> {
        return commonFields(instance).and(instance.group(SetBannerPatternFunction.PATTERN_CODEC.listOf().fieldOf("patterns").forGetter((setbannerpatternfunction) -> {
            return setbannerpatternfunction.patterns;
        }), Codec.BOOL.fieldOf("append").forGetter((setbannerpatternfunction) -> {
            return setbannerpatternfunction.append;
        }))).apply(instance, SetBannerPatternFunction::new);
    });
    private final List<Pair<Holder<EnumBannerPatternType>, EnumColor>> patterns;
    private final boolean append;

    SetBannerPatternFunction(List<LootItemCondition> list, List<Pair<Holder<EnumBannerPatternType>, EnumColor>> list1, boolean flag) {
        super(list);
        this.patterns = list1;
        this.append = flag;
    }

    @Override
    protected ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        NBTTagCompound nbttagcompound = ItemBlock.getBlockEntityData(itemstack);

        if (nbttagcompound == null) {
            nbttagcompound = new NBTTagCompound();
        }

        EnumBannerPatternType.a enumbannerpatterntype_a = new EnumBannerPatternType.a();
        List list = this.patterns;

        Objects.requireNonNull(enumbannerpatterntype_a);
        list.forEach(enumbannerpatterntype_a::addPattern);
        NBTTagList nbttaglist = enumbannerpatterntype_a.toListTag();
        NBTTagList nbttaglist1;

        if (this.append) {
            nbttaglist1 = nbttagcompound.getList("Patterns", 10).copy();
            nbttaglist1.addAll(nbttaglist);
        } else {
            nbttaglist1 = nbttaglist;
        }

        nbttagcompound.put("Patterns", nbttaglist1);
        ItemBlock.setBlockEntityData(itemstack, TileEntityTypes.BANNER, nbttagcompound);
        return itemstack;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_BANNER_PATTERN;
    }

    public static SetBannerPatternFunction.a setBannerPattern(boolean flag) {
        return new SetBannerPatternFunction.a(flag);
    }

    public static class a extends LootItemFunctionConditional.a<SetBannerPatternFunction.a> {

        private final Builder<Pair<Holder<EnumBannerPatternType>, EnumColor>> patterns = ImmutableList.builder();
        private final boolean append;

        a(boolean flag) {
            this.append = flag;
        }

        @Override
        protected SetBannerPatternFunction.a getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new SetBannerPatternFunction(this.getConditions(), this.patterns.build(), this.append);
        }

        public SetBannerPatternFunction.a addPattern(ResourceKey<EnumBannerPatternType> resourcekey, EnumColor enumcolor) {
            return this.addPattern((Holder) BuiltInRegistries.BANNER_PATTERN.getHolderOrThrow(resourcekey), enumcolor);
        }

        public SetBannerPatternFunction.a addPattern(Holder<EnumBannerPatternType> holder, EnumColor enumcolor) {
            this.patterns.add(Pair.of(holder, enumcolor));
            return this;
        }
    }
}
