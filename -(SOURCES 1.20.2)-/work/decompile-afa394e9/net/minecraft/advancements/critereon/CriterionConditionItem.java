package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemEnchantedBook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionRegistry;
import net.minecraft.world.item.alchemy.PotionUtil;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.level.IMaterial;

public record CriterionConditionItem(Optional<TagKey<Item>> tag, Optional<HolderSet<Item>> items, CriterionConditionValue.IntegerRange count, CriterionConditionValue.IntegerRange durability, List<CriterionConditionEnchantments> enchantments, List<CriterionConditionEnchantments> storedEnchantments, Optional<Holder<PotionRegistry>> potion, Optional<CriterionConditionNBT> nbt) {

    private static final Codec<HolderSet<Item>> ITEMS_CODEC = BuiltInRegistries.ITEM.holderByNameCodec().listOf().xmap(HolderSet::direct, (holderset) -> {
        return holderset.stream().toList();
    });
    public static final Codec<CriterionConditionItem> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(ExtraCodecs.strictOptionalField(TagKey.codec(Registries.ITEM), "tag").forGetter(CriterionConditionItem::tag), ExtraCodecs.strictOptionalField(CriterionConditionItem.ITEMS_CODEC, "items").forGetter(CriterionConditionItem::items), ExtraCodecs.strictOptionalField(CriterionConditionValue.IntegerRange.CODEC, "count", CriterionConditionValue.IntegerRange.ANY).forGetter(CriterionConditionItem::count), ExtraCodecs.strictOptionalField(CriterionConditionValue.IntegerRange.CODEC, "durability", CriterionConditionValue.IntegerRange.ANY).forGetter(CriterionConditionItem::durability), ExtraCodecs.strictOptionalField(CriterionConditionEnchantments.CODEC.listOf(), "enchantments", List.of()).forGetter(CriterionConditionItem::enchantments), ExtraCodecs.strictOptionalField(CriterionConditionEnchantments.CODEC.listOf(), "stored_enchantments", List.of()).forGetter(CriterionConditionItem::storedEnchantments), ExtraCodecs.strictOptionalField(BuiltInRegistries.POTION.holderByNameCodec(), "potion").forGetter(CriterionConditionItem::potion), ExtraCodecs.strictOptionalField(CriterionConditionNBT.CODEC, "nbt").forGetter(CriterionConditionItem::nbt)).apply(instance, CriterionConditionItem::new);
    });

    public boolean matches(ItemStack itemstack) {
        if (this.tag.isPresent() && !itemstack.is((TagKey) this.tag.get())) {
            return false;
        } else if (this.items.isPresent() && !itemstack.is((HolderSet) this.items.get())) {
            return false;
        } else if (!this.count.matches(itemstack.getCount())) {
            return false;
        } else if (!this.durability.isAny() && !itemstack.isDamageableItem()) {
            return false;
        } else if (!this.durability.matches(itemstack.getMaxDamage() - itemstack.getDamageValue())) {
            return false;
        } else if (this.nbt.isPresent() && !((CriterionConditionNBT) this.nbt.get()).matches(itemstack)) {
            return false;
        } else {
            Map map;
            Iterator iterator;
            CriterionConditionEnchantments criterionconditionenchantments;

            if (!this.enchantments.isEmpty()) {
                map = EnchantmentManager.deserializeEnchantments(itemstack.getEnchantmentTags());
                iterator = this.enchantments.iterator();

                while (iterator.hasNext()) {
                    criterionconditionenchantments = (CriterionConditionEnchantments) iterator.next();
                    if (!criterionconditionenchantments.containedIn(map)) {
                        return false;
                    }
                }
            }

            if (!this.storedEnchantments.isEmpty()) {
                map = EnchantmentManager.deserializeEnchantments(ItemEnchantedBook.getEnchantments(itemstack));
                iterator = this.storedEnchantments.iterator();

                while (iterator.hasNext()) {
                    criterionconditionenchantments = (CriterionConditionEnchantments) iterator.next();
                    if (!criterionconditionenchantments.containedIn(map)) {
                        return false;
                    }
                }
            }

            return !this.potion.isPresent() || ((Holder) this.potion.get()).value() == PotionUtil.getPotion(itemstack);
        }
    }

    public static Optional<CriterionConditionItem> fromJson(@Nullable JsonElement jsonelement) {
        return jsonelement != null && !jsonelement.isJsonNull() ? Optional.of((CriterionConditionItem) SystemUtils.getOrThrow(CriterionConditionItem.CODEC.parse(JsonOps.INSTANCE, jsonelement), JsonParseException::new)) : Optional.empty();
    }

    public JsonElement serializeToJson() {
        return (JsonElement) SystemUtils.getOrThrow(CriterionConditionItem.CODEC.encodeStart(JsonOps.INSTANCE, this), IllegalStateException::new);
    }

    public static JsonElement serializeToJsonArray(List<CriterionConditionItem> list) {
        return (JsonElement) SystemUtils.getOrThrow(CriterionConditionItem.CODEC.listOf().encodeStart(JsonOps.INSTANCE, list), IllegalStateException::new);
    }

    public static List<CriterionConditionItem> fromJsonArray(@Nullable JsonElement jsonelement) {
        return jsonelement != null && !jsonelement.isJsonNull() ? (List) SystemUtils.getOrThrow(CriterionConditionItem.CODEC.listOf().parse(JsonOps.INSTANCE, jsonelement), JsonParseException::new) : List.of();
    }

    public static class a {

        private final Builder<CriterionConditionEnchantments> enchantments = ImmutableList.builder();
        private final Builder<CriterionConditionEnchantments> storedEnchantments = ImmutableList.builder();
        private Optional<HolderSet<Item>> items = Optional.empty();
        private Optional<TagKey<Item>> tag = Optional.empty();
        private CriterionConditionValue.IntegerRange count;
        private CriterionConditionValue.IntegerRange durability;
        private Optional<Holder<PotionRegistry>> potion;
        private Optional<CriterionConditionNBT> nbt;

        private a() {
            this.count = CriterionConditionValue.IntegerRange.ANY;
            this.durability = CriterionConditionValue.IntegerRange.ANY;
            this.potion = Optional.empty();
            this.nbt = Optional.empty();
        }

        public static CriterionConditionItem.a item() {
            return new CriterionConditionItem.a();
        }

        public CriterionConditionItem.a of(IMaterial... aimaterial) {
            this.items = Optional.of(HolderSet.direct((imaterial) -> {
                return imaterial.asItem().builtInRegistryHolder();
            }, (Object[]) aimaterial));
            return this;
        }

        public CriterionConditionItem.a of(TagKey<Item> tagkey) {
            this.tag = Optional.of(tagkey);
            return this;
        }

        public CriterionConditionItem.a withCount(CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange) {
            this.count = criterionconditionvalue_integerrange;
            return this;
        }

        public CriterionConditionItem.a hasDurability(CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange) {
            this.durability = criterionconditionvalue_integerrange;
            return this;
        }

        public CriterionConditionItem.a isPotion(PotionRegistry potionregistry) {
            this.potion = Optional.of(potionregistry.builtInRegistryHolder());
            return this;
        }

        public CriterionConditionItem.a hasNbt(NBTTagCompound nbttagcompound) {
            this.nbt = Optional.of(new CriterionConditionNBT(nbttagcompound));
            return this;
        }

        public CriterionConditionItem.a hasEnchantment(CriterionConditionEnchantments criterionconditionenchantments) {
            this.enchantments.add(criterionconditionenchantments);
            return this;
        }

        public CriterionConditionItem.a hasStoredEnchantment(CriterionConditionEnchantments criterionconditionenchantments) {
            this.storedEnchantments.add(criterionconditionenchantments);
            return this;
        }

        public CriterionConditionItem build() {
            List<CriterionConditionEnchantments> list = this.enchantments.build();
            List<CriterionConditionEnchantments> list1 = this.storedEnchantments.build();

            return new CriterionConditionItem(this.tag, this.items, this.count, this.durability, list, list1, this.potion, this.nbt);
        }
    }
}
