package net.minecraft.world.item.enchantment;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.EnumChatFormat;
import net.minecraft.SystemUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class Enchantment implements FeatureElement {

    private final Enchantment.b definition;
    @Nullable
    protected String descriptionId;
    private final Holder.c<Enchantment> builtInRegistryHolder;

    public static Enchantment.a constantCost(int i) {
        return new Enchantment.a(i, 0);
    }

    public static Enchantment.a dynamicCost(int i, int j) {
        return new Enchantment.a(i, j);
    }

    public static Enchantment.b definition(TagKey<Item> tagkey, TagKey<Item> tagkey1, int i, int j, Enchantment.a enchantment_a, Enchantment.a enchantment_a1, int k, EnumItemSlot... aenumitemslot) {
        return new Enchantment.b(tagkey, Optional.of(tagkey1), i, j, enchantment_a, enchantment_a1, k, FeatureFlags.DEFAULT_FLAGS, aenumitemslot);
    }

    public static Enchantment.b definition(TagKey<Item> tagkey, int i, int j, Enchantment.a enchantment_a, Enchantment.a enchantment_a1, int k, EnumItemSlot... aenumitemslot) {
        return new Enchantment.b(tagkey, Optional.empty(), i, j, enchantment_a, enchantment_a1, k, FeatureFlags.DEFAULT_FLAGS, aenumitemslot);
    }

    public static Enchantment.b definition(TagKey<Item> tagkey, int i, int j, Enchantment.a enchantment_a, Enchantment.a enchantment_a1, int k, FeatureFlagSet featureflagset, EnumItemSlot... aenumitemslot) {
        return new Enchantment.b(tagkey, Optional.empty(), i, j, enchantment_a, enchantment_a1, k, featureflagset, aenumitemslot);
    }

    @Nullable
    public static Enchantment byId(int i) {
        return (Enchantment) BuiltInRegistries.ENCHANTMENT.byId(i);
    }

    public Enchantment(Enchantment.b enchantment_b) {
        this.builtInRegistryHolder = BuiltInRegistries.ENCHANTMENT.createIntrusiveHolder(this);
        this.definition = enchantment_b;
    }

    public Map<EnumItemSlot, ItemStack> getSlotItems(EntityLiving entityliving) {
        Map<EnumItemSlot, ItemStack> map = Maps.newEnumMap(EnumItemSlot.class);
        EnumItemSlot[] aenumitemslot = this.definition.slots();
        int i = aenumitemslot.length;

        for (int j = 0; j < i; ++j) {
            EnumItemSlot enumitemslot = aenumitemslot[j];
            ItemStack itemstack = entityliving.getItemBySlot(enumitemslot);

            if (!itemstack.isEmpty()) {
                map.put(enumitemslot, itemstack);
            }
        }

        return map;
    }

    public final TagKey<Item> getSupportedItems() {
        return this.definition.supportedItems();
    }

    public final boolean isPrimaryItem(ItemStack itemstack) {
        return this.definition.primaryItems.isEmpty() || itemstack.is((TagKey) this.definition.primaryItems.get());
    }

    public final int getWeight() {
        return this.definition.weight();
    }

    public final int getAnvilCost() {
        return this.definition.anvilCost();
    }

    public final int getMinLevel() {
        return 1;
    }

    public final int getMaxLevel() {
        return this.definition.maxLevel();
    }

    public final int getMinCost(int i) {
        return this.definition.minCost().calculate(i);
    }

    public final int getMaxCost(int i) {
        return this.definition.maxCost().calculate(i);
    }

    public int getDamageProtection(int i, DamageSource damagesource) {
        return 0;
    }

    public float getDamageBonus(int i, @Nullable EntityTypes<?> entitytypes) {
        return 0.0F;
    }

    public final boolean isCompatibleWith(Enchantment enchantment) {
        return this.checkCompatibility(enchantment) && enchantment.checkCompatibility(this);
    }

    protected boolean checkCompatibility(Enchantment enchantment) {
        return this != enchantment;
    }

    protected String getOrCreateDescriptionId() {
        if (this.descriptionId == null) {
            this.descriptionId = SystemUtils.makeDescriptionId("enchantment", BuiltInRegistries.ENCHANTMENT.getKey(this));
        }

        return this.descriptionId;
    }

    public String getDescriptionId() {
        return this.getOrCreateDescriptionId();
    }

    public IChatBaseComponent getFullname(int i) {
        IChatMutableComponent ichatmutablecomponent = IChatBaseComponent.translatable(this.getDescriptionId());

        if (this.isCurse()) {
            ichatmutablecomponent.withStyle(EnumChatFormat.RED);
        } else {
            ichatmutablecomponent.withStyle(EnumChatFormat.GRAY);
        }

        if (i != 1 || this.getMaxLevel() != 1) {
            ichatmutablecomponent.append(CommonComponents.SPACE).append((IChatBaseComponent) IChatBaseComponent.translatable("enchantment.level." + i));
        }

        return ichatmutablecomponent;
    }

    public boolean canEnchant(ItemStack itemstack) {
        return itemstack.getItem().builtInRegistryHolder().is(this.definition.supportedItems());
    }

    public void doPostAttack(EntityLiving entityliving, Entity entity, int i) {}

    public void doPostHurt(EntityLiving entityliving, Entity entity, int i) {}

    public void doPostItemStackHurt(EntityLiving entityliving, Entity entity, int i) {}

    public boolean isTreasureOnly() {
        return false;
    }

    public boolean isCurse() {
        return false;
    }

    public boolean isTradeable() {
        return true;
    }

    public boolean isDiscoverable() {
        return true;
    }

    /** @deprecated */
    @Deprecated
    public Holder.c<Enchantment> builtInRegistryHolder() {
        return this.builtInRegistryHolder;
    }

    @Override
    public FeatureFlagSet requiredFeatures() {
        return this.definition.requiredFeatures();
    }

    public static record a(int base, int perLevel) {

        public int calculate(int i) {
            return this.base + this.perLevel * (i - 1);
        }
    }

    public static record b(TagKey<Item> supportedItems, Optional<TagKey<Item>> primaryItems, int weight, int maxLevel, Enchantment.a minCost, Enchantment.a maxCost, int anvilCost, FeatureFlagSet requiredFeatures, EnumItemSlot[] slots) {

    }
}
