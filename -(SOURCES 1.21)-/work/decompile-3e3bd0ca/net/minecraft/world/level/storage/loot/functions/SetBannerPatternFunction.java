package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.EnumColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.EnumBannerPatternType;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetBannerPatternFunction extends LootItemFunctionConditional {

    public static final MapCodec<SetBannerPatternFunction> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(instance.group(BannerPatternLayers.CODEC.fieldOf("patterns").forGetter((setbannerpatternfunction) -> {
            return setbannerpatternfunction.patterns;
        }), Codec.BOOL.fieldOf("append").forGetter((setbannerpatternfunction) -> {
            return setbannerpatternfunction.append;
        }))).apply(instance, SetBannerPatternFunction::new);
    });
    private final BannerPatternLayers patterns;
    private final boolean append;

    SetBannerPatternFunction(List<LootItemCondition> list, BannerPatternLayers bannerpatternlayers, boolean flag) {
        super(list);
        this.patterns = bannerpatternlayers;
        this.append = flag;
    }

    @Override
    protected ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        if (this.append) {
            itemstack.update(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY, this.patterns, (bannerpatternlayers, bannerpatternlayers1) -> {
                return (new BannerPatternLayers.a()).addAll(bannerpatternlayers).addAll(bannerpatternlayers1).build();
            });
        } else {
            itemstack.set(DataComponents.BANNER_PATTERNS, this.patterns);
        }

        return itemstack;
    }

    @Override
    public LootItemFunctionType<SetBannerPatternFunction> getType() {
        return LootItemFunctions.SET_BANNER_PATTERN;
    }

    public static SetBannerPatternFunction.a setBannerPattern(boolean flag) {
        return new SetBannerPatternFunction.a(flag);
    }

    public static class a extends LootItemFunctionConditional.a<SetBannerPatternFunction.a> {

        private final BannerPatternLayers.a patterns = new BannerPatternLayers.a();
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

        public SetBannerPatternFunction.a addPattern(Holder<EnumBannerPatternType> holder, EnumColor enumcolor) {
            this.patterns.add(holder, enumcolor);
            return this;
        }
    }
}
