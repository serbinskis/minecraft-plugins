package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.MinecraftKey;
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
    static final Codec<LootItemFunctionApplyBonus.c> FORMULA_TYPE_CODEC = MinecraftKey.CODEC.comapFlatMap((minecraftkey) -> {
        LootItemFunctionApplyBonus.c lootitemfunctionapplybonus_c = (LootItemFunctionApplyBonus.c) LootItemFunctionApplyBonus.FORMULAS.get(minecraftkey);

        return lootitemfunctionapplybonus_c != null ? DataResult.success(lootitemfunctionapplybonus_c) : DataResult.error(() -> {
            return "No formula type with id: '" + minecraftkey + "'";
        });
    }, LootItemFunctionApplyBonus.c::id);
    private static final MapCodec<LootItemFunctionApplyBonus.b> FORMULA_CODEC = new MapCodec<LootItemFunctionApplyBonus.b>() {
        private static final String TYPE_KEY = "formula";
        private static final String VALUE_KEY = "parameters";

        public <T> Stream<T> keys(DynamicOps<T> dynamicops) {
            return Stream.of(dynamicops.createString("formula"), dynamicops.createString("parameters"));
        }

        public <T> DataResult<LootItemFunctionApplyBonus.b> decode(DynamicOps<T> dynamicops, MapLike<T> maplike) {
            T t0 = maplike.get("formula");

            return t0 == null ? DataResult.error(() -> {
                return "Missing type for formula in: " + maplike;
            }) : LootItemFunctionApplyBonus.FORMULA_TYPE_CODEC.decode(dynamicops, t0).flatMap((pair) -> {
                Object object = maplike.get("parameters");

                Objects.requireNonNull(dynamicops);
                T t1 = Objects.requireNonNullElseGet(object, dynamicops::emptyMap);

                return ((LootItemFunctionApplyBonus.c) pair.getFirst()).codec().decode(dynamicops, t1).map(Pair::getFirst);
            });
        }

        public <T> RecordBuilder<T> encode(LootItemFunctionApplyBonus.b lootitemfunctionapplybonus_b, DynamicOps<T> dynamicops, RecordBuilder<T> recordbuilder) {
            LootItemFunctionApplyBonus.c lootitemfunctionapplybonus_c = lootitemfunctionapplybonus_b.getType();

            recordbuilder.add("formula", LootItemFunctionApplyBonus.FORMULA_TYPE_CODEC.encodeStart(dynamicops, lootitemfunctionapplybonus_c));
            DataResult<T> dataresult = this.encode(lootitemfunctionapplybonus_c.codec(), lootitemfunctionapplybonus_b, dynamicops);

            if (dataresult.result().isEmpty() || !Objects.equals(dataresult.result().get(), dynamicops.emptyMap())) {
                recordbuilder.add("parameters", dataresult);
            }

            return recordbuilder;
        }

        private <T, F extends LootItemFunctionApplyBonus.b> DataResult<T> encode(Codec<F> codec, LootItemFunctionApplyBonus.b lootitemfunctionapplybonus_b, DynamicOps<T> dynamicops) {
            return codec.encodeStart(dynamicops, lootitemfunctionapplybonus_b);
        }
    };
    public static final Codec<LootItemFunctionApplyBonus> CODEC = RecordCodecBuilder.create((instance) -> {
        return commonFields(instance).and(instance.group(BuiltInRegistries.ENCHANTMENT.holderByNameCodec().fieldOf("enchantment").forGetter((lootitemfunctionapplybonus) -> {
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
    public LootItemFunctionType getType() {
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
            int i = EnchantmentManager.getItemEnchantmentLevel((Enchantment) this.enchantment.value(), itemstack1);
            int j = this.formula.calculateNewCount(loottableinfo.getRandom(), itemstack.getCount(), i);

            itemstack.setCount(j);
        }

        return itemstack;
    }

    public static LootItemFunctionConditional.a<?> addBonusBinomialDistributionCount(Enchantment enchantment, float f, int i) {
        return simpleBuilder((list) -> {
            return new LootItemFunctionApplyBonus(list, enchantment.builtInRegistryHolder(), new LootItemFunctionApplyBonus.a(i, f));
        });
    }

    public static LootItemFunctionConditional.a<?> addOreBonusCount(Enchantment enchantment) {
        return simpleBuilder((list) -> {
            return new LootItemFunctionApplyBonus(list, enchantment.builtInRegistryHolder(), new LootItemFunctionApplyBonus.d());
        });
    }

    public static LootItemFunctionConditional.a<?> addUniformBonusCount(Enchantment enchantment) {
        return simpleBuilder((list) -> {
            return new LootItemFunctionApplyBonus(list, enchantment.builtInRegistryHolder(), new LootItemFunctionApplyBonus.e(1));
        });
    }

    public static LootItemFunctionConditional.a<?> addUniformBonusCount(Enchantment enchantment, int i) {
        return simpleBuilder((list) -> {
            return new LootItemFunctionApplyBonus(list, enchantment.builtInRegistryHolder(), new LootItemFunctionApplyBonus.e(i));
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
        public static final LootItemFunctionApplyBonus.c TYPE = new LootItemFunctionApplyBonus.c(new MinecraftKey("uniform_bonus_count"), LootItemFunctionApplyBonus.e.CODEC);

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
        public static final LootItemFunctionApplyBonus.c TYPE = new LootItemFunctionApplyBonus.c(new MinecraftKey("ore_drops"), LootItemFunctionApplyBonus.d.CODEC);

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
        public static final LootItemFunctionApplyBonus.c TYPE = new LootItemFunctionApplyBonus.c(new MinecraftKey("binomial_with_bonus_count"), LootItemFunctionApplyBonus.a.CODEC);

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
