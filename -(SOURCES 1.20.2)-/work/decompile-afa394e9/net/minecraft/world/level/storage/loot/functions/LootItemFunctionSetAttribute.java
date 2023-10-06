package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.SystemUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.ai.attributes.AttributeBase;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class LootItemFunctionSetAttribute extends LootItemFunctionConditional {

    public static final Codec<LootItemFunctionSetAttribute> CODEC = RecordCodecBuilder.create((instance) -> {
        return commonFields(instance).and(ExtraCodecs.nonEmptyList(LootItemFunctionSetAttribute.b.CODEC.listOf()).fieldOf("modifiers").forGetter((lootitemfunctionsetattribute) -> {
            return lootitemfunctionsetattribute.modifiers;
        })).apply(instance, LootItemFunctionSetAttribute::new);
    });
    private final List<LootItemFunctionSetAttribute.b> modifiers;

    LootItemFunctionSetAttribute(List<LootItemCondition> list, List<LootItemFunctionSetAttribute.b> list1) {
        super(list);
        this.modifiers = List.copyOf(list1);
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_ATTRIBUTES;
    }

    @Override
    public Set<LootContextParameter<?>> getReferencedContextParams() {
        return (Set) this.modifiers.stream().flatMap((lootitemfunctionsetattribute_b) -> {
            return lootitemfunctionsetattribute_b.amount.getReferencedContextParams().stream();
        }).collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        RandomSource randomsource = loottableinfo.getRandom();
        Iterator iterator = this.modifiers.iterator();

        while (iterator.hasNext()) {
            LootItemFunctionSetAttribute.b lootitemfunctionsetattribute_b = (LootItemFunctionSetAttribute.b) iterator.next();
            UUID uuid = (UUID) lootitemfunctionsetattribute_b.id.orElseGet(UUID::randomUUID);
            EnumItemSlot enumitemslot = (EnumItemSlot) SystemUtils.getRandom(lootitemfunctionsetattribute_b.slots, randomsource);

            itemstack.addAttributeModifier((AttributeBase) lootitemfunctionsetattribute_b.attribute.value(), new AttributeModifier(uuid, lootitemfunctionsetattribute_b.name, (double) lootitemfunctionsetattribute_b.amount.getFloat(loottableinfo), lootitemfunctionsetattribute_b.operation), enumitemslot);
        }

        return itemstack;
    }

    public static LootItemFunctionSetAttribute.c modifier(String s, Holder<AttributeBase> holder, AttributeModifier.Operation attributemodifier_operation, NumberProvider numberprovider) {
        return new LootItemFunctionSetAttribute.c(s, holder, attributemodifier_operation, numberprovider);
    }

    public static LootItemFunctionSetAttribute.a setAttributes() {
        return new LootItemFunctionSetAttribute.a();
    }

    private static record b(String name, Holder<AttributeBase> attribute, AttributeModifier.Operation operation, NumberProvider amount, List<EnumItemSlot> slots, Optional<UUID> id) {

        private static final Codec<List<EnumItemSlot>> SLOTS_CODEC = ExtraCodecs.nonEmptyList(Codec.either(EnumItemSlot.CODEC, EnumItemSlot.CODEC.listOf()).xmap((either) -> {
            return (List) either.map(List::of, Function.identity());
        }, (list) -> {
            return list.size() == 1 ? Either.left((EnumItemSlot) list.get(0)) : Either.right(list);
        }));
        public static final Codec<LootItemFunctionSetAttribute.b> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(Codec.STRING.fieldOf("name").forGetter(LootItemFunctionSetAttribute.b::name), BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("attribute").forGetter(LootItemFunctionSetAttribute.b::attribute), AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(LootItemFunctionSetAttribute.b::operation), NumberProviders.CODEC.fieldOf("amount").forGetter(LootItemFunctionSetAttribute.b::amount), LootItemFunctionSetAttribute.b.SLOTS_CODEC.fieldOf("slot").forGetter(LootItemFunctionSetAttribute.b::slots), ExtraCodecs.strictOptionalField(UUIDUtil.STRING_CODEC, "id").forGetter(LootItemFunctionSetAttribute.b::id)).apply(instance, LootItemFunctionSetAttribute.b::new);
        });
    }

    public static class c {

        private final String name;
        private final Holder<AttributeBase> attribute;
        private final AttributeModifier.Operation operation;
        private final NumberProvider amount;
        private Optional<UUID> id = Optional.empty();
        private final Set<EnumItemSlot> slots = EnumSet.noneOf(EnumItemSlot.class);

        public c(String s, Holder<AttributeBase> holder, AttributeModifier.Operation attributemodifier_operation, NumberProvider numberprovider) {
            this.name = s;
            this.attribute = holder;
            this.operation = attributemodifier_operation;
            this.amount = numberprovider;
        }

        public LootItemFunctionSetAttribute.c forSlot(EnumItemSlot enumitemslot) {
            this.slots.add(enumitemslot);
            return this;
        }

        public LootItemFunctionSetAttribute.c withUuid(UUID uuid) {
            this.id = Optional.of(uuid);
            return this;
        }

        public LootItemFunctionSetAttribute.b build() {
            return new LootItemFunctionSetAttribute.b(this.name, this.attribute, this.operation, this.amount, List.copyOf(this.slots), this.id);
        }
    }

    public static class a extends LootItemFunctionConditional.a<LootItemFunctionSetAttribute.a> {

        private final List<LootItemFunctionSetAttribute.b> modifiers = Lists.newArrayList();

        public a() {}

        @Override
        protected LootItemFunctionSetAttribute.a getThis() {
            return this;
        }

        public LootItemFunctionSetAttribute.a withModifier(LootItemFunctionSetAttribute.c lootitemfunctionsetattribute_c) {
            this.modifiers.add(lootitemfunctionsetattribute_c.build());
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new LootItemFunctionSetAttribute(this.getConditions(), this.modifiers);
        }
    }
}
