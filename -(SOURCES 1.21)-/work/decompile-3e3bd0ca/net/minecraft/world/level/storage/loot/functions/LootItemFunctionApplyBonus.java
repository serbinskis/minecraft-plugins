package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootItemFunctionApplyBonus extends LootItemFunctionConditional {

    private static final Map<MinecraftKey, LootItemFunctionApplyBonus.c> FORMULAS = (Map) Stream.of(LootItemFunctionApplyBonus.a.TYPE, LootItemFunctionApplyBonus.d.TYPE, LootItemFunctionApplyBonus.e.TYPE).collect(Collectors.toMap(LootItemFunctionApplyBonus.c::id, Function.identity()));
    private static final Codec<LootItemFunctionApplyBonus.c> FORMULA_TYPE_CODEC = MinecraftKey.CODEC.comapFlatMap((minecraftkey) -> {
        LootItemFunctionApplyBonus.c lootitemfunctionapplybonus_c = (LootItemFunctionApplyBonus.c) LootItemFunctionApplyBonus.FORMULAS.get(minecraftkey);

        return lootitemfunctionapplybonus_c != null ? DataResult.success(lootitemfunctionapplybonus_c) : DataResult.error(() -> {
            return "No formula type with id: '" + String.valueOf(minecraftkey) + "'";
        });
    }, LootItemFunctionApplyBonus.c::id);
    private static final MapCodec<LootItemFunctionApplyBonus.b> FORMULA_CODEC = ExtraCodecs.dispatchOptionalValue("formula", "parameters", LootItemFunctionApplyBonus.FORMULA_TYPE_CODEC, LootItemFunctionApplyBonus.b::getType, LootItemFunctionApplyBonus.c::codec);
    public static final MapCodec<LootItemFunctionApplyBonus> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(instance.group(Enchantment.CODEC.fieldOf("enchantment").forGetter((lootitemfunctionapplybonus) -> {
            return lootitemfunctionapplybonus.enchantment;
        }), LootItemFunctionApplyBonus.FORMULA_CODEC.forGetter((lootitemfunctionapplybonus) -> {
            return lootitemfunctionapplybonus.formula;
        }))).apply(instance, LootItemFunctionApplyBonus::new);
    });
    private final Holder<Enchantment> enchantment;
    private final LootItemFunctionApplyBonus.b formula;

    private LootItemFunctionApplyBonus(List<LootItemCondition> list, Holder<Enchantment> holder, LootItemFunctionApplyBonus.b lootitemfunctionapplybonus_b) {
        super(list);
        this.enchantment = holder;
        this.formula = lootitemfunctionapplybonus_b;
    }

    @Override
    public LootItemFunctionType<LootItemFunctionApplyBonus> getType() {
        return LootItemFunctions.APPLY_BONUS;
    }

    @Override
    public Set<LootContextParameter<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParameters.TOOL);
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        ItemStack itemstack1 = (ItemStack) loottableinfo.getParamOrNull(LootContextParameters.TOOL);

        if (itemstack1 != null) {
            int i = EnchantmentManager.getItemEnchantmentLevel(this.enchantment, itemstack1);
            int j = this.formula.calculateNewCount(loottableinfo.getRandom(), itemstack.getCount(), i);

            itemstack.setCount(j);
        }

        return itemstack;
    }

    public static LootItemFunctionConditional.a<?> addBonusBinomialDistributionCount(Holder<Enchantment> holder, float f, int i) {
        return simpleBuilder((list) -> {
            return new LootItemFunctionApplyBonus(list, holder, new LootItemFunctionApplyBonus.a(i, f));
        });
    }

    public static LootItemFunctionConditional.a<?> addOreBonusCount(Holder<Enchantment> holder) {
        return simpleBuilder((list) -> {
            return new LootItemFunctionApplyBonus(list, holder, new LootItemFunctionApplyBonus.d());
        });
    }

    public static LootItemFunctionConditional.a<?> addUniformBonusCount(Holder<Enchantment> holder) {
        return simpleBuilder((list) -> {
            return new LootItemFunctionApplyBonus(list, holder, new LootItemFunctionApplyBonus.e(1));
        });
    }

    public static LootItemFunctionConditional.a<?> addUniformBonusCount(Holder<Enchantment> holder, int i) {
        return simpleBuilder((list) -> {
            return new LootItemFunctionApplyBonus(list, holder, new LootItemFunctionApplyBonus.e(i));
        });
    }

    private interface b {

        int calculateNewCount(RandomSource randomsource, int i, int j);

        LootItemFunctionApplyBonus.c getType();
    }

    private static record e(int bonusMultiplier) implements LootItemFunctionApplyBonus.b {

        public static final Codec<LootItemFunctionApplyBonus.e> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(Codec.INT.fieldOf("bonusMultiplier").forGetter(LootItemFunctionApplyBonus.e::bonusMultiplier)).apply(instance, LootItemFunctionApplyBonus.e::new);
        });
        public static final LootItemFunctionApplyBonus.c TYPE = new LootItemFunctionApplyBonus.c(MinecraftKey.withDefaultNamespace("uniform_bonus_count"), LootItemFunctionApplyBonus.e.CODEC);

        @Override
        public int calculateNewCount(RandomSource randomsource, int i, int j) {
            return i + randomsource.nextInt(this.bonusMultiplier * j + 1);
        }

        @Override
        public LootItemFunctionApplyBonus.c getType() {
            return LootItemFunctionApplyBonus.e.TYPE;
        }
    }

    private static record d() implements LootItemFunctionApplyBonus.b {

        public static final Codec<LootItemFunctionApplyBonus.d> CODEC = Codec.unit(LootItemFunctionApplyBonus.d::new);
        public static final LootItemFunctionApplyBonus.c TYPE = new LootItemFunctionApplyBonus.c(MinecraftKey.withDefaultNamespace("ore_drops"), LootItemFunctionApplyBonus.d.CODEC);

        @Override
        public int calculateNewCount(RandomSource randomsource, int i, int j) {
            if (j > 0) {
                int k = randomsource.nextInt(j + 2) - 1;

                if (k < 0) {
                    k = 0;
                }

                return i * (k + 1);
            } else {
                return i;
            }
        }

        @Override
        public LootItemFunctionApplyBonus.c getType() {
            return LootItemFunctionApplyBonus.d.TYPE;
        }
    }

    private static record a(int extraRounds, float probability) implements LootItemFunctionApplyBonus.b {

        private static final Codec<LootItemFunctionApplyBonus.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(Codec.INT.fieldOf("extra").forGetter(LootItemFunctionApplyBonus.a::extraRounds), Codec.FLOAT.fieldOf("probability").forGetter(LootItemFunctionApplyBonus.a::probability)).apply(instance, LootItemFunctionApplyBonus.a::new);
        });
        public static final LootItemFunctionApplyBonus.c TYPE = new LootItemFunctionApplyBonus.c(MinecraftKey.withDefaultNamespace("binomial_with_bonus_count"), LootItemFunctionApplyBonus.a.CODEC);

        @Override
        public int calculateNewCount(RandomSource randomsource, int i, int j) {
            for (int k = 0; k < j + this.extraRounds; ++k) {
                if (randomsource.nextFloat() < this.probability) {
                    ++i;
                }
            }

            return i;
        }

        @Override
        public LootItemFunctionApplyBonus.c getType() {
            return LootItemFunctionApplyBonus.a.TYPE;
        }
    }

    private static record c(MinecraftKey id, Codec<? extends LootItemFunctionApplyBonus.b> codec) {

    }
}
