package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.SystemUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffectList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemSuspiciousStew;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.SuspiciousEffectHolder;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class LootItemFunctionSetStewEffect extends LootItemFunctionConditional {

    private static final Codec<List<LootItemFunctionSetStewEffect.b>> EFFECTS_LIST = ExtraCodecs.validate(LootItemFunctionSetStewEffect.b.CODEC.listOf(), (list) -> {
        Set<Holder<MobEffectList>> set = new ObjectOpenHashSet();
        Iterator iterator = list.iterator();

        LootItemFunctionSetStewEffect.b lootitemfunctionsetsteweffect_b;

        do {
            if (!iterator.hasNext()) {
                return DataResult.success(list);
            }

            lootitemfunctionsetsteweffect_b = (LootItemFunctionSetStewEffect.b) iterator.next();
        } while (set.add(lootitemfunctionsetsteweffect_b.effect()));

        return DataResult.error(() -> {
            return "Encountered duplicate mob effect: '" + lootitemfunctionsetsteweffect_b.effect() + "'";
        });
    });
    public static final Codec<LootItemFunctionSetStewEffect> CODEC = RecordCodecBuilder.create((instance) -> {
        return commonFields(instance).and(ExtraCodecs.strictOptionalField(LootItemFunctionSetStewEffect.EFFECTS_LIST, "effects", List.of()).forGetter((lootitemfunctionsetsteweffect) -> {
            return lootitemfunctionsetsteweffect.effects;
        })).apply(instance, LootItemFunctionSetStewEffect::new);
    });
    private final List<LootItemFunctionSetStewEffect.b> effects;

    LootItemFunctionSetStewEffect(List<LootItemCondition> list, List<LootItemFunctionSetStewEffect.b> list1) {
        super(list);
        this.effects = list1;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_STEW_EFFECT;
    }

    @Override
    public Set<LootContextParameter<?>> getReferencedContextParams() {
        return (Set) this.effects.stream().flatMap((lootitemfunctionsetsteweffect_b) -> {
            return lootitemfunctionsetsteweffect_b.duration().getReferencedContextParams().stream();
        }).collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        if (itemstack.is(Items.SUSPICIOUS_STEW) && !this.effects.isEmpty()) {
            LootItemFunctionSetStewEffect.b lootitemfunctionsetsteweffect_b = (LootItemFunctionSetStewEffect.b) SystemUtils.getRandom(this.effects, loottableinfo.getRandom());
            MobEffectList mobeffectlist = (MobEffectList) lootitemfunctionsetsteweffect_b.effect().value();
            int i = lootitemfunctionsetsteweffect_b.duration().getInt(loottableinfo);

            if (!mobeffectlist.isInstantenous()) {
                i *= 20;
            }

            ItemSuspiciousStew.appendMobEffects(itemstack, List.of(new SuspiciousEffectHolder.a(mobeffectlist, i)));
            return itemstack;
        } else {
            return itemstack;
        }
    }

    public static LootItemFunctionSetStewEffect.a stewEffect() {
        return new LootItemFunctionSetStewEffect.a();
    }

    private static record b(Holder<MobEffectList> effect, NumberProvider duration) {

        public static final Codec<LootItemFunctionSetStewEffect.b> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(BuiltInRegistries.MOB_EFFECT.holderByNameCodec().fieldOf("type").forGetter(LootItemFunctionSetStewEffect.b::effect), NumberProviders.CODEC.fieldOf("duration").forGetter(LootItemFunctionSetStewEffect.b::duration)).apply(instance, LootItemFunctionSetStewEffect.b::new);
        });
    }

    public static class a extends LootItemFunctionConditional.a<LootItemFunctionSetStewEffect.a> {

        private final Builder<LootItemFunctionSetStewEffect.b> effects = ImmutableList.builder();

        public a() {}

        @Override
        protected LootItemFunctionSetStewEffect.a getThis() {
            return this;
        }

        public LootItemFunctionSetStewEffect.a withEffect(MobEffectList mobeffectlist, NumberProvider numberprovider) {
            this.effects.add(new LootItemFunctionSetStewEffect.b(mobeffectlist.builtInRegistryHolder(), numberprovider));
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new LootItemFunctionSetStewEffect(this.getConditions(), this.effects.build());
        }
    }
}
