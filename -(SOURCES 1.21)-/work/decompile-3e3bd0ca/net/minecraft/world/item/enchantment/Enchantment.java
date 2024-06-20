package net.minecraft.world.item.enchantment;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.EnumChatFormat;
import net.minecraft.SystemUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ChatComponentUtils;
import net.minecraft.network.chat.ChatModifier;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.effects.DamageImmunity;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentLocationBasedEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameterSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3D;
import org.apache.commons.lang3.mutable.MutableFloat;

public record Enchantment(IChatBaseComponent description, Enchantment.c definition, HolderSet<Enchantment> exclusiveSet, DataComponentMap effects) {

    public static final int MAX_LEVEL = 255;
    public static final Codec<Enchantment> DIRECT_CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(ComponentSerialization.CODEC.fieldOf("description").forGetter(Enchantment::description), Enchantment.c.CODEC.forGetter(Enchantment::definition), RegistryCodecs.homogeneousList(Registries.ENCHANTMENT).optionalFieldOf("exclusive_set", HolderSet.direct()).forGetter(Enchantment::exclusiveSet), EnchantmentEffectComponents.CODEC.optionalFieldOf("effects", DataComponentMap.EMPTY).forGetter(Enchantment::effects)).apply(instance, Enchantment::new);
    });
    public static final Codec<Holder<Enchantment>> CODEC = RegistryFixedCodec.create(Registries.ENCHANTMENT);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<Enchantment>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.ENCHANTMENT);

    public static Enchantment.b constantCost(int i) {
        return new Enchantment.b(i, 0);
    }

    public static Enchantment.b dynamicCost(int i, int j) {
        return new Enchantment.b(i, j);
    }

    public static Enchantment.c definition(HolderSet<Item> holderset, HolderSet<Item> holderset1, int i, int j, Enchantment.b enchantment_b, Enchantment.b enchantment_b1, int k, EquipmentSlotGroup... aequipmentslotgroup) {
        return new Enchantment.c(holderset, Optional.of(holderset1), i, j, enchantment_b, enchantment_b1, k, List.of(aequipmentslotgroup));
    }

    public static Enchantment.c definition(HolderSet<Item> holderset, int i, int j, Enchantment.b enchantment_b, Enchantment.b enchantment_b1, int k, EquipmentSlotGroup... aequipmentslotgroup) {
        return new Enchantment.c(holderset, Optional.empty(), i, j, enchantment_b, enchantment_b1, k, List.of(aequipmentslotgroup));
    }

    public Map<EnumItemSlot, ItemStack> getSlotItems(EntityLiving entityliving) {
        Map<EnumItemSlot, ItemStack> map = Maps.newEnumMap(EnumItemSlot.class);
        EnumItemSlot[] aenumitemslot = EnumItemSlot.values();
        int i = aenumitemslot.length;

        for (int j = 0; j < i; ++j) {
            EnumItemSlot enumitemslot = aenumitemslot[j];

            if (this.matchingSlot(enumitemslot)) {
                ItemStack itemstack = entityliving.getItemBySlot(enumitemslot);

                if (!itemstack.isEmpty()) {
                    map.put(enumitemslot, itemstack);
                }
            }
        }

        return map;
    }

    public HolderSet<Item> getSupportedItems() {
        return this.definition.supportedItems();
    }

    public boolean matchingSlot(EnumItemSlot enumitemslot) {
        return this.definition.slots().stream().anyMatch((equipmentslotgroup) -> {
            return equipmentslotgroup.test(enumitemslot);
        });
    }

    public boolean isPrimaryItem(ItemStack itemstack) {
        return this.isSupportedItem(itemstack) && (this.definition.primaryItems.isEmpty() || itemstack.is((HolderSet) this.definition.primaryItems.get()));
    }

    public boolean isSupportedItem(ItemStack itemstack) {
        return itemstack.is(this.definition.supportedItems);
    }

    public int getWeight() {
        return this.definition.weight();
    }

    public int getAnvilCost() {
        return this.definition.anvilCost();
    }

    public int getMinLevel() {
        return 1;
    }

    public int getMaxLevel() {
        return this.definition.maxLevel();
    }

    public int getMinCost(int i) {
        return this.definition.minCost().calculate(i);
    }

    public int getMaxCost(int i) {
        return this.definition.maxCost().calculate(i);
    }

    public String toString() {
        return "Enchantment " + this.description.getString();
    }

    public static boolean areCompatible(Holder<Enchantment> holder, Holder<Enchantment> holder1) {
        return !holder.equals(holder1) && !((Enchantment) holder.value()).exclusiveSet.contains(holder1) && !((Enchantment) holder1.value()).exclusiveSet.contains(holder);
    }

    public static IChatBaseComponent getFullname(Holder<Enchantment> holder, int i) {
        IChatMutableComponent ichatmutablecomponent = ((Enchantment) holder.value()).description.copy();

        if (holder.is(EnchantmentTags.CURSE)) {
            ChatComponentUtils.mergeStyles(ichatmutablecomponent, ChatModifier.EMPTY.withColor(EnumChatFormat.RED));
        } else {
            ChatComponentUtils.mergeStyles(ichatmutablecomponent, ChatModifier.EMPTY.withColor(EnumChatFormat.GRAY));
        }

        if (i != 1 || ((Enchantment) holder.value()).getMaxLevel() != 1) {
            ichatmutablecomponent.append(CommonComponents.SPACE).append((IChatBaseComponent) IChatBaseComponent.translatable("enchantment.level." + i));
        }

        return ichatmutablecomponent;
    }

    public boolean canEnchant(ItemStack itemstack) {
        return this.definition.supportedItems().contains(itemstack.getItemHolder());
    }

    public <T> List<T> getEffects(DataComponentType<List<T>> datacomponenttype) {
        return (List) this.effects.getOrDefault(datacomponenttype, List.of());
    }

    public boolean isImmuneToDamage(WorldServer worldserver, int i, Entity entity, DamageSource damagesource) {
        LootTableInfo loottableinfo = damageContext(worldserver, i, entity, damagesource);
        Iterator iterator = this.getEffects(EnchantmentEffectComponents.DAMAGE_IMMUNITY).iterator();

        ConditionalEffect conditionaleffect;

        do {
            if (!iterator.hasNext()) {
                return false;
            }

            conditionaleffect = (ConditionalEffect) iterator.next();
        } while (!conditionaleffect.matches(loottableinfo));

        return true;
    }

    public void modifyDamageProtection(WorldServer worldserver, int i, ItemStack itemstack, Entity entity, DamageSource damagesource, MutableFloat mutablefloat) {
        LootTableInfo loottableinfo = damageContext(worldserver, i, entity, damagesource);
        Iterator iterator = this.getEffects(EnchantmentEffectComponents.DAMAGE_PROTECTION).iterator();

        while (iterator.hasNext()) {
            ConditionalEffect<EnchantmentValueEffect> conditionaleffect = (ConditionalEffect) iterator.next();

            if (conditionaleffect.matches(loottableinfo)) {
                mutablefloat.setValue(((EnchantmentValueEffect) conditionaleffect.effect()).process(i, entity.getRandom(), mutablefloat.floatValue()));
            }
        }

    }

    public void modifyDurabilityChange(WorldServer worldserver, int i, ItemStack itemstack, MutableFloat mutablefloat) {
        this.modifyItemFilteredCount(EnchantmentEffectComponents.ITEM_DAMAGE, worldserver, i, itemstack, mutablefloat);
    }

    public void modifyAmmoCount(WorldServer worldserver, int i, ItemStack itemstack, MutableFloat mutablefloat) {
        this.modifyItemFilteredCount(EnchantmentEffectComponents.AMMO_USE, worldserver, i, itemstack, mutablefloat);
    }

    public void modifyPiercingCount(WorldServer worldserver, int i, ItemStack itemstack, MutableFloat mutablefloat) {
        this.modifyItemFilteredCount(EnchantmentEffectComponents.PROJECTILE_PIERCING, worldserver, i, itemstack, mutablefloat);
    }

    public void modifyBlockExperience(WorldServer worldserver, int i, ItemStack itemstack, MutableFloat mutablefloat) {
        this.modifyItemFilteredCount(EnchantmentEffectComponents.BLOCK_EXPERIENCE, worldserver, i, itemstack, mutablefloat);
    }

    public void modifyMobExperience(WorldServer worldserver, int i, ItemStack itemstack, Entity entity, MutableFloat mutablefloat) {
        this.modifyEntityFilteredValue(EnchantmentEffectComponents.MOB_EXPERIENCE, worldserver, i, itemstack, entity, mutablefloat);
    }

    public void modifyDurabilityToRepairFromXp(WorldServer worldserver, int i, ItemStack itemstack, MutableFloat mutablefloat) {
        this.modifyItemFilteredCount(EnchantmentEffectComponents.REPAIR_WITH_XP, worldserver, i, itemstack, mutablefloat);
    }

    public void modifyTridentReturnToOwnerAcceleration(WorldServer worldserver, int i, ItemStack itemstack, Entity entity, MutableFloat mutablefloat) {
        this.modifyEntityFilteredValue(EnchantmentEffectComponents.TRIDENT_RETURN_ACCELERATION, worldserver, i, itemstack, entity, mutablefloat);
    }

    public void modifyTridentSpinAttackStrength(RandomSource randomsource, int i, MutableFloat mutablefloat) {
        this.modifyUnfilteredValue(EnchantmentEffectComponents.TRIDENT_SPIN_ATTACK_STRENGTH, randomsource, i, mutablefloat);
    }

    public void modifyFishingTimeReduction(WorldServer worldserver, int i, ItemStack itemstack, Entity entity, MutableFloat mutablefloat) {
        this.modifyEntityFilteredValue(EnchantmentEffectComponents.FISHING_TIME_REDUCTION, worldserver, i, itemstack, entity, mutablefloat);
    }

    public void modifyFishingLuckBonus(WorldServer worldserver, int i, ItemStack itemstack, Entity entity, MutableFloat mutablefloat) {
        this.modifyEntityFilteredValue(EnchantmentEffectComponents.FISHING_LUCK_BONUS, worldserver, i, itemstack, entity, mutablefloat);
    }

    public void modifyDamage(WorldServer worldserver, int i, ItemStack itemstack, Entity entity, DamageSource damagesource, MutableFloat mutablefloat) {
        this.modifyDamageFilteredValue(EnchantmentEffectComponents.DAMAGE, worldserver, i, itemstack, entity, damagesource, mutablefloat);
    }

    public void modifyFallBasedDamage(WorldServer worldserver, int i, ItemStack itemstack, Entity entity, DamageSource damagesource, MutableFloat mutablefloat) {
        this.modifyDamageFilteredValue(EnchantmentEffectComponents.SMASH_DAMAGE_PER_FALLEN_BLOCK, worldserver, i, itemstack, entity, damagesource, mutablefloat);
    }

    public void modifyKnockback(WorldServer worldserver, int i, ItemStack itemstack, Entity entity, DamageSource damagesource, MutableFloat mutablefloat) {
        this.modifyDamageFilteredValue(EnchantmentEffectComponents.KNOCKBACK, worldserver, i, itemstack, entity, damagesource, mutablefloat);
    }

    public void modifyArmorEffectivness(WorldServer worldserver, int i, ItemStack itemstack, Entity entity, DamageSource damagesource, MutableFloat mutablefloat) {
        this.modifyDamageFilteredValue(EnchantmentEffectComponents.ARMOR_EFFECTIVENESS, worldserver, i, itemstack, entity, damagesource, mutablefloat);
    }

    public static void doPostAttack(TargetedConditionalEffect<EnchantmentEntityEffect> targetedconditionaleffect, WorldServer worldserver, int i, EnchantedItemInUse enchantediteminuse, Entity entity, DamageSource damagesource) {
        if (targetedconditionaleffect.matches(damageContext(worldserver, i, entity, damagesource))) {
            Entity entity1;

            switch (targetedconditionaleffect.affected()) {
                case ATTACKER:
                    entity1 = damagesource.getEntity();
                    break;
                case DAMAGING_ENTITY:
                    entity1 = damagesource.getDirectEntity();
                    break;
                case VICTIM:
                    entity1 = entity;
                    break;
                default:
                    throw new MatchException((String) null, (Throwable) null);
            }

            Entity entity2 = entity1;

            if (entity2 != null) {
                ((EnchantmentEntityEffect) targetedconditionaleffect.effect()).apply(worldserver, i, enchantediteminuse, entity2, entity2.position());
            }
        }

    }

    public void doPostAttack(WorldServer worldserver, int i, EnchantedItemInUse enchantediteminuse, EnchantmentTarget enchantmenttarget, Entity entity, DamageSource damagesource) {
        Iterator iterator = this.getEffects(EnchantmentEffectComponents.POST_ATTACK).iterator();

        while (iterator.hasNext()) {
            TargetedConditionalEffect<EnchantmentEntityEffect> targetedconditionaleffect = (TargetedConditionalEffect) iterator.next();

            if (enchantmenttarget == targetedconditionaleffect.enchanted()) {
                doPostAttack(targetedconditionaleffect, worldserver, i, enchantediteminuse, entity, damagesource);
            }
        }

    }

    public void modifyProjectileCount(WorldServer worldserver, int i, ItemStack itemstack, Entity entity, MutableFloat mutablefloat) {
        this.modifyEntityFilteredValue(EnchantmentEffectComponents.PROJECTILE_COUNT, worldserver, i, itemstack, entity, mutablefloat);
    }

    public void modifyProjectileSpread(WorldServer worldserver, int i, ItemStack itemstack, Entity entity, MutableFloat mutablefloat) {
        this.modifyEntityFilteredValue(EnchantmentEffectComponents.PROJECTILE_SPREAD, worldserver, i, itemstack, entity, mutablefloat);
    }

    public void modifyCrossbowChargeTime(RandomSource randomsource, int i, MutableFloat mutablefloat) {
        this.modifyUnfilteredValue(EnchantmentEffectComponents.CROSSBOW_CHARGE_TIME, randomsource, i, mutablefloat);
    }

    public void modifyUnfilteredValue(DataComponentType<EnchantmentValueEffect> datacomponenttype, RandomSource randomsource, int i, MutableFloat mutablefloat) {
        EnchantmentValueEffect enchantmentvalueeffect = (EnchantmentValueEffect) this.effects.get(datacomponenttype);

        if (enchantmentvalueeffect != null) {
            mutablefloat.setValue(enchantmentvalueeffect.process(i, randomsource, mutablefloat.floatValue()));
        }

    }

    public void tick(WorldServer worldserver, int i, EnchantedItemInUse enchantediteminuse, Entity entity) {
        applyEffects(this.getEffects(EnchantmentEffectComponents.TICK), entityContext(worldserver, i, entity, entity.position()), (enchantmententityeffect) -> {
            enchantmententityeffect.apply(worldserver, i, enchantediteminuse, entity, entity.position());
        });
    }

    public void onProjectileSpawned(WorldServer worldserver, int i, EnchantedItemInUse enchantediteminuse, Entity entity) {
        applyEffects(this.getEffects(EnchantmentEffectComponents.PROJECTILE_SPAWNED), entityContext(worldserver, i, entity, entity.position()), (enchantmententityeffect) -> {
            enchantmententityeffect.apply(worldserver, i, enchantediteminuse, entity, entity.position());
        });
    }

    public void onHitBlock(WorldServer worldserver, int i, EnchantedItemInUse enchantediteminuse, Entity entity, Vec3D vec3d, IBlockData iblockdata) {
        applyEffects(this.getEffects(EnchantmentEffectComponents.HIT_BLOCK), blockHitContext(worldserver, i, entity, vec3d, iblockdata), (enchantmententityeffect) -> {
            enchantmententityeffect.apply(worldserver, i, enchantediteminuse, entity, vec3d);
        });
    }

    private void modifyItemFilteredCount(DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> datacomponenttype, WorldServer worldserver, int i, ItemStack itemstack, MutableFloat mutablefloat) {
        applyEffects(this.getEffects(datacomponenttype), itemContext(worldserver, i, itemstack), (enchantmentvalueeffect) -> {
            mutablefloat.setValue(enchantmentvalueeffect.process(i, worldserver.getRandom(), mutablefloat.getValue()));
        });
    }

    private void modifyEntityFilteredValue(DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> datacomponenttype, WorldServer worldserver, int i, ItemStack itemstack, Entity entity, MutableFloat mutablefloat) {
        applyEffects(this.getEffects(datacomponenttype), entityContext(worldserver, i, entity, entity.position()), (enchantmentvalueeffect) -> {
            mutablefloat.setValue(enchantmentvalueeffect.process(i, entity.getRandom(), mutablefloat.floatValue()));
        });
    }

    private void modifyDamageFilteredValue(DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> datacomponenttype, WorldServer worldserver, int i, ItemStack itemstack, Entity entity, DamageSource damagesource, MutableFloat mutablefloat) {
        applyEffects(this.getEffects(datacomponenttype), damageContext(worldserver, i, entity, damagesource), (enchantmentvalueeffect) -> {
            mutablefloat.setValue(enchantmentvalueeffect.process(i, entity.getRandom(), mutablefloat.floatValue()));
        });
    }

    public static LootTableInfo damageContext(WorldServer worldserver, int i, Entity entity, DamageSource damagesource) {
        LootParams lootparams = (new LootParams.a(worldserver)).withParameter(LootContextParameters.THIS_ENTITY, entity).withParameter(LootContextParameters.ENCHANTMENT_LEVEL, i).withParameter(LootContextParameters.ORIGIN, entity.position()).withParameter(LootContextParameters.DAMAGE_SOURCE, damagesource).withOptionalParameter(LootContextParameters.ATTACKING_ENTITY, damagesource.getEntity()).withOptionalParameter(LootContextParameters.DIRECT_ATTACKING_ENTITY, damagesource.getDirectEntity()).create(LootContextParameterSets.ENCHANTED_DAMAGE);

        return (new LootTableInfo.Builder(lootparams)).create(Optional.empty());
    }

    private static LootTableInfo itemContext(WorldServer worldserver, int i, ItemStack itemstack) {
        LootParams lootparams = (new LootParams.a(worldserver)).withParameter(LootContextParameters.TOOL, itemstack).withParameter(LootContextParameters.ENCHANTMENT_LEVEL, i).create(LootContextParameterSets.ENCHANTED_ITEM);

        return (new LootTableInfo.Builder(lootparams)).create(Optional.empty());
    }

    private static LootTableInfo locationContext(WorldServer worldserver, int i, Entity entity, boolean flag) {
        LootParams lootparams = (new LootParams.a(worldserver)).withParameter(LootContextParameters.THIS_ENTITY, entity).withParameter(LootContextParameters.ENCHANTMENT_LEVEL, i).withParameter(LootContextParameters.ORIGIN, entity.position()).withParameter(LootContextParameters.ENCHANTMENT_ACTIVE, flag).create(LootContextParameterSets.ENCHANTED_LOCATION);

        return (new LootTableInfo.Builder(lootparams)).create(Optional.empty());
    }

    private static LootTableInfo entityContext(WorldServer worldserver, int i, Entity entity, Vec3D vec3d) {
        LootParams lootparams = (new LootParams.a(worldserver)).withParameter(LootContextParameters.THIS_ENTITY, entity).withParameter(LootContextParameters.ENCHANTMENT_LEVEL, i).withParameter(LootContextParameters.ORIGIN, vec3d).create(LootContextParameterSets.ENCHANTED_ENTITY);

        return (new LootTableInfo.Builder(lootparams)).create(Optional.empty());
    }

    private static LootTableInfo blockHitContext(WorldServer worldserver, int i, Entity entity, Vec3D vec3d, IBlockData iblockdata) {
        LootParams lootparams = (new LootParams.a(worldserver)).withParameter(LootContextParameters.THIS_ENTITY, entity).withParameter(LootContextParameters.ENCHANTMENT_LEVEL, i).withParameter(LootContextParameters.ORIGIN, vec3d).withParameter(LootContextParameters.BLOCK_STATE, iblockdata).create(LootContextParameterSets.HIT_BLOCK);

        return (new LootTableInfo.Builder(lootparams)).create(Optional.empty());
    }

    private static <T> void applyEffects(List<ConditionalEffect<T>> list, LootTableInfo loottableinfo, Consumer<T> consumer) {
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            ConditionalEffect<T> conditionaleffect = (ConditionalEffect) iterator.next();

            if (conditionaleffect.matches(loottableinfo)) {
                consumer.accept(conditionaleffect.effect());
            }
        }

    }

    public void runLocationChangedEffects(WorldServer worldserver, int i, EnchantedItemInUse enchantediteminuse, EntityLiving entityliving) {
        if (enchantediteminuse.inSlot() != null && !this.matchingSlot(enchantediteminuse.inSlot())) {
            Set<EnchantmentLocationBasedEffect> set = (Set) entityliving.activeLocationDependentEnchantments().remove(this);

            if (set != null) {
                set.forEach((enchantmentlocationbasedeffect) -> {
                    enchantmentlocationbasedeffect.onDeactivated(enchantediteminuse, entityliving, entityliving.position(), i);
                });
            }

        } else {
            Set<EnchantmentLocationBasedEffect> set1 = (Set) entityliving.activeLocationDependentEnchantments().get(this);
            Iterator iterator = this.getEffects(EnchantmentEffectComponents.LOCATION_CHANGED).iterator();

            while (iterator.hasNext()) {
                ConditionalEffect<EnchantmentLocationBasedEffect> conditionaleffect = (ConditionalEffect) iterator.next();
                EnchantmentLocationBasedEffect enchantmentlocationbasedeffect = (EnchantmentLocationBasedEffect) conditionaleffect.effect();
                boolean flag = set1 != null && ((Set) set1).contains(enchantmentlocationbasedeffect);

                if (conditionaleffect.matches(locationContext(worldserver, i, entityliving, flag))) {
                    if (!flag) {
                        if (set1 == null) {
                            set1 = new ObjectArraySet();
                            entityliving.activeLocationDependentEnchantments().put(this, set1);
                        }

                        ((Set) set1).add(enchantmentlocationbasedeffect);
                    }

                    enchantmentlocationbasedeffect.onChangedBlock(worldserver, i, enchantediteminuse, entityliving, entityliving.position(), !flag);
                } else if (set1 != null && ((Set) set1).remove(enchantmentlocationbasedeffect)) {
                    enchantmentlocationbasedeffect.onDeactivated(enchantediteminuse, entityliving, entityliving.position(), i);
                }
            }

            if (set1 != null && ((Set) set1).isEmpty()) {
                entityliving.activeLocationDependentEnchantments().remove(this);
            }

        }
    }

    public void stopLocationBasedEffects(int i, EnchantedItemInUse enchantediteminuse, EntityLiving entityliving) {
        Set<EnchantmentLocationBasedEffect> set = (Set) entityliving.activeLocationDependentEnchantments().remove(this);

        if (set != null) {
            Iterator iterator = set.iterator();

            while (iterator.hasNext()) {
                EnchantmentLocationBasedEffect enchantmentlocationbasedeffect = (EnchantmentLocationBasedEffect) iterator.next();

                enchantmentlocationbasedeffect.onDeactivated(enchantediteminuse, entityliving, entityliving.position(), i);
            }

        }
    }

    public static Enchantment.a enchantment(Enchantment.c enchantment_c) {
        return new Enchantment.a(enchantment_c);
    }

    public static record c(HolderSet<Item> supportedItems, Optional<HolderSet<Item>> primaryItems, int weight, int maxLevel, Enchantment.b minCost, Enchantment.b maxCost, int anvilCost, List<EquipmentSlotGroup> slots) {

        public static final MapCodec<Enchantment.c> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("supported_items").forGetter(Enchantment.c::supportedItems), RegistryCodecs.homogeneousList(Registries.ITEM).optionalFieldOf("primary_items").forGetter(Enchantment.c::primaryItems), ExtraCodecs.intRange(1, 1024).fieldOf("weight").forGetter(Enchantment.c::weight), ExtraCodecs.intRange(1, 255).fieldOf("max_level").forGetter(Enchantment.c::maxLevel), Enchantment.b.CODEC.fieldOf("min_cost").forGetter(Enchantment.c::minCost), Enchantment.b.CODEC.fieldOf("max_cost").forGetter(Enchantment.c::maxCost), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("anvil_cost").forGetter(Enchantment.c::anvilCost), EquipmentSlotGroup.CODEC.listOf().fieldOf("slots").forGetter(Enchantment.c::slots)).apply(instance, Enchantment.c::new);
        });
    }

    public static record b(int base, int perLevelAboveFirst) {

        public static final Codec<Enchantment.b> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(Codec.INT.fieldOf("base").forGetter(Enchantment.b::base), Codec.INT.fieldOf("per_level_above_first").forGetter(Enchantment.b::perLevelAboveFirst)).apply(instance, Enchantment.b::new);
        });

        public int calculate(int i) {
            return this.base + this.perLevelAboveFirst * (i - 1);
        }
    }

    public static class a {

        private final Enchantment.c definition;
        private HolderSet<Enchantment> exclusiveSet = HolderSet.direct();
        private final Map<DataComponentType<?>, List<?>> effectLists = new HashMap();
        private final DataComponentMap.a effectMapBuilder = DataComponentMap.builder();

        public a(Enchantment.c enchantment_c) {
            this.definition = enchantment_c;
        }

        public Enchantment.a exclusiveWith(HolderSet<Enchantment> holderset) {
            this.exclusiveSet = holderset;
            return this;
        }

        public <E> Enchantment.a withEffect(DataComponentType<List<ConditionalEffect<E>>> datacomponenttype, E e0, LootItemCondition.a lootitemcondition_a) {
            this.getEffectsList(datacomponenttype).add(new ConditionalEffect<>(e0, Optional.of(lootitemcondition_a.build())));
            return this;
        }

        public <E> Enchantment.a withEffect(DataComponentType<List<ConditionalEffect<E>>> datacomponenttype, E e0) {
            this.getEffectsList(datacomponenttype).add(new ConditionalEffect<>(e0, Optional.empty()));
            return this;
        }

        public <E> Enchantment.a withEffect(DataComponentType<List<TargetedConditionalEffect<E>>> datacomponenttype, EnchantmentTarget enchantmenttarget, EnchantmentTarget enchantmenttarget1, E e0, LootItemCondition.a lootitemcondition_a) {
            this.getEffectsList(datacomponenttype).add(new TargetedConditionalEffect<>(enchantmenttarget, enchantmenttarget1, e0, Optional.of(lootitemcondition_a.build())));
            return this;
        }

        public <E> Enchantment.a withEffect(DataComponentType<List<TargetedConditionalEffect<E>>> datacomponenttype, EnchantmentTarget enchantmenttarget, EnchantmentTarget enchantmenttarget1, E e0) {
            this.getEffectsList(datacomponenttype).add(new TargetedConditionalEffect<>(enchantmenttarget, enchantmenttarget1, e0, Optional.empty()));
            return this;
        }

        public Enchantment.a withEffect(DataComponentType<List<EnchantmentAttributeEffect>> datacomponenttype, EnchantmentAttributeEffect enchantmentattributeeffect) {
            this.getEffectsList(datacomponenttype).add(enchantmentattributeeffect);
            return this;
        }

        public <E> Enchantment.a withSpecialEffect(DataComponentType<E> datacomponenttype, E e0) {
            this.effectMapBuilder.set(datacomponenttype, e0);
            return this;
        }

        public Enchantment.a withEffect(DataComponentType<Unit> datacomponenttype) {
            this.effectMapBuilder.set(datacomponenttype, Unit.INSTANCE);
            return this;
        }

        private <E> List<E> getEffectsList(DataComponentType<List<E>> datacomponenttype) {
            return (List) this.effectLists.computeIfAbsent(datacomponenttype, (datacomponenttype1) -> {
                ArrayList<E> arraylist = new ArrayList();

                this.effectMapBuilder.set(datacomponenttype, arraylist);
                return arraylist;
            });
        }

        public Enchantment build(MinecraftKey minecraftkey) {
            return new Enchantment(IChatBaseComponent.translatable(SystemUtils.makeDescriptionId("enchantment", minecraftkey)), this.definition, this.exclusiveSet, this.effectMapBuilder.build());
        }
    }
}
