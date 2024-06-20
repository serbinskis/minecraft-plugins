package net.minecraft.world.item.enchantment;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom2;
import net.minecraft.world.DifficultyDamageScaler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeBase;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.projectile.EntityArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.item.enchantment.providers.EnchantmentProvider;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.phys.Vec3D;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableObject;

public class EnchantmentManager {

    public EnchantmentManager() {}

    public static int getItemEnchantmentLevel(Holder<Enchantment> holder, ItemStack itemstack) {
        ItemEnchantments itemenchantments = (ItemEnchantments) itemstack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

        return itemenchantments.getLevel(holder);
    }

    public static ItemEnchantments updateEnchantments(ItemStack itemstack, Consumer<ItemEnchantments.a> consumer) {
        DataComponentType<ItemEnchantments> datacomponenttype = getComponentType(itemstack);
        ItemEnchantments itemenchantments = (ItemEnchantments) itemstack.get(datacomponenttype);

        if (itemenchantments == null) {
            return ItemEnchantments.EMPTY;
        } else {
            ItemEnchantments.a itemenchantments_a = new ItemEnchantments.a(itemenchantments);

            consumer.accept(itemenchantments_a);
            ItemEnchantments itemenchantments1 = itemenchantments_a.toImmutable();

            itemstack.set(datacomponenttype, itemenchantments1);
            return itemenchantments1;
        }
    }

    public static boolean canStoreEnchantments(ItemStack itemstack) {
        return itemstack.has(getComponentType(itemstack));
    }

    public static void setEnchantments(ItemStack itemstack, ItemEnchantments itemenchantments) {
        itemstack.set(getComponentType(itemstack), itemenchantments);
    }

    public static ItemEnchantments getEnchantmentsForCrafting(ItemStack itemstack) {
        return (ItemEnchantments) itemstack.getOrDefault(getComponentType(itemstack), ItemEnchantments.EMPTY);
    }

    private static DataComponentType<ItemEnchantments> getComponentType(ItemStack itemstack) {
        return itemstack.is(Items.ENCHANTED_BOOK) ? DataComponents.STORED_ENCHANTMENTS : DataComponents.ENCHANTMENTS;
    }

    public static boolean hasAnyEnchantments(ItemStack itemstack) {
        return !((ItemEnchantments) itemstack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY)).isEmpty() || !((ItemEnchantments) itemstack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY)).isEmpty();
    }

    public static int processDurabilityChange(WorldServer worldserver, ItemStack itemstack, int i) {
        MutableFloat mutablefloat = new MutableFloat((float) i);

        runIterationOnItem(itemstack, (holder, j) -> {
            ((Enchantment) holder.value()).modifyDurabilityChange(worldserver, j, itemstack, mutablefloat);
        });
        return mutablefloat.intValue();
    }

    public static int processAmmoUse(WorldServer worldserver, ItemStack itemstack, ItemStack itemstack1, int i) {
        MutableFloat mutablefloat = new MutableFloat((float) i);

        runIterationOnItem(itemstack, (holder, j) -> {
            ((Enchantment) holder.value()).modifyAmmoCount(worldserver, j, itemstack1, mutablefloat);
        });
        return mutablefloat.intValue();
    }

    public static int processBlockExperience(WorldServer worldserver, ItemStack itemstack, int i) {
        MutableFloat mutablefloat = new MutableFloat((float) i);

        runIterationOnItem(itemstack, (holder, j) -> {
            ((Enchantment) holder.value()).modifyBlockExperience(worldserver, j, itemstack, mutablefloat);
        });
        return mutablefloat.intValue();
    }

    public static int processMobExperience(WorldServer worldserver, @Nullable Entity entity, Entity entity1, int i) {
        if (entity instanceof EntityLiving entityliving) {
            MutableFloat mutablefloat = new MutableFloat((float) i);

            runIterationOnEquipment(entityliving, (holder, j, enchantediteminuse) -> {
                ((Enchantment) holder.value()).modifyMobExperience(worldserver, j, enchantediteminuse.itemStack(), entity1, mutablefloat);
            });
            return mutablefloat.intValue();
        } else {
            return i;
        }
    }

    private static void runIterationOnItem(ItemStack itemstack, EnchantmentManager.b enchantmentmanager_b) {
        ItemEnchantments itemenchantments = (ItemEnchantments) itemstack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        Iterator iterator = itemenchantments.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<Holder<Enchantment>> entry = (Entry) iterator.next();

            enchantmentmanager_b.accept((Holder) entry.getKey(), entry.getIntValue());
        }

    }

    private static void runIterationOnItem(ItemStack itemstack, EnumItemSlot enumitemslot, EntityLiving entityliving, EnchantmentManager.a enchantmentmanager_a) {
        if (!itemstack.isEmpty()) {
            ItemEnchantments itemenchantments = (ItemEnchantments) itemstack.get(DataComponents.ENCHANTMENTS);

            if (itemenchantments != null && !itemenchantments.isEmpty()) {
                EnchantedItemInUse enchantediteminuse = new EnchantedItemInUse(itemstack, enumitemslot, entityliving);
                Iterator iterator = itemenchantments.entrySet().iterator();

                while (iterator.hasNext()) {
                    Entry<Holder<Enchantment>> entry = (Entry) iterator.next();
                    Holder<Enchantment> holder = (Holder) entry.getKey();

                    if (((Enchantment) holder.value()).matchingSlot(enumitemslot)) {
                        enchantmentmanager_a.accept(holder, entry.getIntValue(), enchantediteminuse);
                    }
                }

            }
        }
    }

    private static void runIterationOnEquipment(EntityLiving entityliving, EnchantmentManager.a enchantmentmanager_a) {
        EnumItemSlot[] aenumitemslot = EnumItemSlot.values();
        int i = aenumitemslot.length;

        for (int j = 0; j < i; ++j) {
            EnumItemSlot enumitemslot = aenumitemslot[j];

            runIterationOnItem(entityliving.getItemBySlot(enumitemslot), enumitemslot, entityliving, enchantmentmanager_a);
        }

    }

    public static boolean isImmuneToDamage(WorldServer worldserver, EntityLiving entityliving, DamageSource damagesource) {
        MutableBoolean mutableboolean = new MutableBoolean();

        runIterationOnEquipment(entityliving, (holder, i, enchantediteminuse) -> {
            mutableboolean.setValue(mutableboolean.isTrue() || ((Enchantment) holder.value()).isImmuneToDamage(worldserver, i, entityliving, damagesource));
        });
        return mutableboolean.isTrue();
    }

    public static float getDamageProtection(WorldServer worldserver, EntityLiving entityliving, DamageSource damagesource) {
        MutableFloat mutablefloat = new MutableFloat(0.0F);

        runIterationOnEquipment(entityliving, (holder, i, enchantediteminuse) -> {
            ((Enchantment) holder.value()).modifyDamageProtection(worldserver, i, enchantediteminuse.itemStack(), entityliving, damagesource, mutablefloat);
        });
        return mutablefloat.floatValue();
    }

    public static float modifyDamage(WorldServer worldserver, ItemStack itemstack, Entity entity, DamageSource damagesource, float f) {
        MutableFloat mutablefloat = new MutableFloat(f);

        runIterationOnItem(itemstack, (holder, i) -> {
            ((Enchantment) holder.value()).modifyDamage(worldserver, i, itemstack, entity, damagesource, mutablefloat);
        });
        return mutablefloat.floatValue();
    }

    public static float modifyFallBasedDamage(WorldServer worldserver, ItemStack itemstack, Entity entity, DamageSource damagesource, float f) {
        MutableFloat mutablefloat = new MutableFloat(f);

        runIterationOnItem(itemstack, (holder, i) -> {
            ((Enchantment) holder.value()).modifyFallBasedDamage(worldserver, i, itemstack, entity, damagesource, mutablefloat);
        });
        return mutablefloat.floatValue();
    }

    public static float modifyArmorEffectiveness(WorldServer worldserver, ItemStack itemstack, Entity entity, DamageSource damagesource, float f) {
        MutableFloat mutablefloat = new MutableFloat(f);

        runIterationOnItem(itemstack, (holder, i) -> {
            ((Enchantment) holder.value()).modifyArmorEffectivness(worldserver, i, itemstack, entity, damagesource, mutablefloat);
        });
        return mutablefloat.floatValue();
    }

    public static float modifyKnockback(WorldServer worldserver, ItemStack itemstack, Entity entity, DamageSource damagesource, float f) {
        MutableFloat mutablefloat = new MutableFloat(f);

        runIterationOnItem(itemstack, (holder, i) -> {
            ((Enchantment) holder.value()).modifyKnockback(worldserver, i, itemstack, entity, damagesource, mutablefloat);
        });
        return mutablefloat.floatValue();
    }

    public static void doPostAttackEffects(WorldServer worldserver, Entity entity, DamageSource damagesource) {
        Entity entity1 = damagesource.getEntity();

        if (entity1 instanceof EntityLiving entityliving) {
            doPostAttackEffectsWithItemSource(worldserver, entity, damagesource, entityliving.getWeaponItem());
        } else {
            doPostAttackEffectsWithItemSource(worldserver, entity, damagesource, (ItemStack) null);
        }

    }

    public static void doPostAttackEffectsWithItemSource(WorldServer worldserver, Entity entity, DamageSource damagesource, @Nullable ItemStack itemstack) {
        if (entity instanceof EntityLiving entityliving) {
            runIterationOnEquipment(entityliving, (holder, i, enchantediteminuse) -> {
                ((Enchantment) holder.value()).doPostAttack(worldserver, i, enchantediteminuse, EnchantmentTarget.VICTIM, entity, damagesource);
            });
        }

        if (itemstack != null) {
            Entity entity1 = damagesource.getEntity();

            if (entity1 instanceof EntityLiving) {
                entityliving = (EntityLiving) entity1;
                runIterationOnItem(itemstack, EnumItemSlot.MAINHAND, entityliving, (holder, i, enchantediteminuse) -> {
                    ((Enchantment) holder.value()).doPostAttack(worldserver, i, enchantediteminuse, EnchantmentTarget.ATTACKER, entity, damagesource);
                });
            }
        }

    }

    public static void runLocationChangedEffects(WorldServer worldserver, EntityLiving entityliving) {
        runIterationOnEquipment(entityliving, (holder, i, enchantediteminuse) -> {
            ((Enchantment) holder.value()).runLocationChangedEffects(worldserver, i, enchantediteminuse, entityliving);
        });
    }

    public static void runLocationChangedEffects(WorldServer worldserver, ItemStack itemstack, EntityLiving entityliving, EnumItemSlot enumitemslot) {
        runIterationOnItem(itemstack, enumitemslot, entityliving, (holder, i, enchantediteminuse) -> {
            ((Enchantment) holder.value()).runLocationChangedEffects(worldserver, i, enchantediteminuse, entityliving);
        });
    }

    public static void stopLocationBasedEffects(EntityLiving entityliving) {
        runIterationOnEquipment(entityliving, (holder, i, enchantediteminuse) -> {
            ((Enchantment) holder.value()).stopLocationBasedEffects(i, enchantediteminuse, entityliving);
        });
    }

    public static void stopLocationBasedEffects(ItemStack itemstack, EntityLiving entityliving, EnumItemSlot enumitemslot) {
        runIterationOnItem(itemstack, enumitemslot, entityliving, (holder, i, enchantediteminuse) -> {
            ((Enchantment) holder.value()).stopLocationBasedEffects(i, enchantediteminuse, entityliving);
        });
    }

    public static void tickEffects(WorldServer worldserver, EntityLiving entityliving) {
        runIterationOnEquipment(entityliving, (holder, i, enchantediteminuse) -> {
            ((Enchantment) holder.value()).tick(worldserver, i, enchantediteminuse, entityliving);
        });
    }

    public static int getEnchantmentLevel(Holder<Enchantment> holder, EntityLiving entityliving) {
        Iterable<ItemStack> iterable = ((Enchantment) holder.value()).getSlotItems(entityliving).values();
        int i = 0;
        Iterator iterator = iterable.iterator();

        while (iterator.hasNext()) {
            ItemStack itemstack = (ItemStack) iterator.next();
            int j = getItemEnchantmentLevel(holder, itemstack);

            if (j > i) {
                i = j;
            }
        }

        return i;
    }

    public static int processProjectileCount(WorldServer worldserver, ItemStack itemstack, Entity entity, int i) {
        MutableFloat mutablefloat = new MutableFloat((float) i);

        runIterationOnItem(itemstack, (holder, j) -> {
            ((Enchantment) holder.value()).modifyProjectileCount(worldserver, j, itemstack, entity, mutablefloat);
        });
        return Math.max(0, mutablefloat.intValue());
    }

    public static float processProjectileSpread(WorldServer worldserver, ItemStack itemstack, Entity entity, float f) {
        MutableFloat mutablefloat = new MutableFloat(f);

        runIterationOnItem(itemstack, (holder, i) -> {
            ((Enchantment) holder.value()).modifyProjectileSpread(worldserver, i, itemstack, entity, mutablefloat);
        });
        return Math.max(0.0F, mutablefloat.floatValue());
    }

    public static int getPiercingCount(WorldServer worldserver, ItemStack itemstack, ItemStack itemstack1) {
        MutableFloat mutablefloat = new MutableFloat(0.0F);

        runIterationOnItem(itemstack, (holder, i) -> {
            ((Enchantment) holder.value()).modifyPiercingCount(worldserver, i, itemstack1, mutablefloat);
        });
        return Math.max(0, mutablefloat.intValue());
    }

    public static void onProjectileSpawned(WorldServer worldserver, ItemStack itemstack, EntityArrow entityarrow, Consumer<Item> consumer) {
        Entity entity = entityarrow.getOwner();
        EntityLiving entityliving;

        if (entity instanceof EntityLiving entityliving1) {
            entityliving = entityliving1;
        } else {
            entityliving = null;
        }

        EntityLiving entityliving2 = entityliving;
        EnchantedItemInUse enchantediteminuse = new EnchantedItemInUse(itemstack, (EnumItemSlot) null, entityliving2, consumer);

        runIterationOnItem(itemstack, (holder, i) -> {
            ((Enchantment) holder.value()).onProjectileSpawned(worldserver, i, enchantediteminuse, entityarrow);
        });
    }

    public static void onHitBlock(WorldServer worldserver, ItemStack itemstack, @Nullable EntityLiving entityliving, Entity entity, @Nullable EnumItemSlot enumitemslot, Vec3D vec3d, IBlockData iblockdata, Consumer<Item> consumer) {
        EnchantedItemInUse enchantediteminuse = new EnchantedItemInUse(itemstack, enumitemslot, entityliving, consumer);

        runIterationOnItem(itemstack, (holder, i) -> {
            ((Enchantment) holder.value()).onHitBlock(worldserver, i, enchantediteminuse, entity, vec3d, iblockdata);
        });
    }

    public static int modifyDurabilityToRepairFromXp(WorldServer worldserver, ItemStack itemstack, int i) {
        MutableFloat mutablefloat = new MutableFloat((float) i);

        runIterationOnItem(itemstack, (holder, j) -> {
            ((Enchantment) holder.value()).modifyDurabilityToRepairFromXp(worldserver, j, itemstack, mutablefloat);
        });
        return Math.max(0, mutablefloat.intValue());
    }

    public static float processEquipmentDropChance(WorldServer worldserver, EntityLiving entityliving, DamageSource damagesource, float f) {
        MutableFloat mutablefloat = new MutableFloat(f);
        RandomSource randomsource = entityliving.getRandom();

        runIterationOnEquipment(entityliving, (holder, i, enchantediteminuse) -> {
            LootTableInfo loottableinfo = Enchantment.damageContext(worldserver, i, entityliving, damagesource);

            ((Enchantment) holder.value()).getEffects(EnchantmentEffectComponents.EQUIPMENT_DROPS).forEach((targetedconditionaleffect) -> {
                if (targetedconditionaleffect.enchanted() == EnchantmentTarget.VICTIM && targetedconditionaleffect.affected() == EnchantmentTarget.VICTIM && targetedconditionaleffect.matches(loottableinfo)) {
                    mutablefloat.setValue(((EnchantmentValueEffect) targetedconditionaleffect.effect()).process(i, randomsource, mutablefloat.floatValue()));
                }

            });
        });
        Entity entity = damagesource.getEntity();

        if (entity instanceof EntityLiving entityliving1) {
            runIterationOnEquipment(entityliving1, (holder, i, enchantediteminuse) -> {
                LootTableInfo loottableinfo = Enchantment.damageContext(worldserver, i, entityliving, damagesource);

                ((Enchantment) holder.value()).getEffects(EnchantmentEffectComponents.EQUIPMENT_DROPS).forEach((targetedconditionaleffect) -> {
                    if (targetedconditionaleffect.enchanted() == EnchantmentTarget.ATTACKER && targetedconditionaleffect.affected() == EnchantmentTarget.VICTIM && targetedconditionaleffect.matches(loottableinfo)) {
                        mutablefloat.setValue(((EnchantmentValueEffect) targetedconditionaleffect.effect()).process(i, randomsource, mutablefloat.floatValue()));
                    }

                });
            });
        }

        return mutablefloat.floatValue();
    }

    public static void forEachModifier(ItemStack itemstack, EquipmentSlotGroup equipmentslotgroup, BiConsumer<Holder<AttributeBase>, AttributeModifier> biconsumer) {
        runIterationOnItem(itemstack, (holder, i) -> {
            ((Enchantment) holder.value()).getEffects(EnchantmentEffectComponents.ATTRIBUTES).forEach((enchantmentattributeeffect) -> {
                if (((Enchantment) holder.value()).definition().slots().contains(equipmentslotgroup)) {
                    biconsumer.accept(enchantmentattributeeffect.attribute(), enchantmentattributeeffect.getModifier(i, equipmentslotgroup));
                }

            });
        });
    }

    public static void forEachModifier(ItemStack itemstack, EnumItemSlot enumitemslot, BiConsumer<Holder<AttributeBase>, AttributeModifier> biconsumer) {
        runIterationOnItem(itemstack, (holder, i) -> {
            ((Enchantment) holder.value()).getEffects(EnchantmentEffectComponents.ATTRIBUTES).forEach((enchantmentattributeeffect) -> {
                if (((Enchantment) holder.value()).matchingSlot(enumitemslot)) {
                    biconsumer.accept(enchantmentattributeeffect.attribute(), enchantmentattributeeffect.getModifier(i, enumitemslot));
                }

            });
        });
    }

    public static int getFishingLuckBonus(WorldServer worldserver, ItemStack itemstack, Entity entity) {
        MutableFloat mutablefloat = new MutableFloat(0.0F);

        runIterationOnItem(itemstack, (holder, i) -> {
            ((Enchantment) holder.value()).modifyFishingLuckBonus(worldserver, i, itemstack, entity, mutablefloat);
        });
        return Math.max(0, mutablefloat.intValue());
    }

    public static float getFishingTimeReduction(WorldServer worldserver, ItemStack itemstack, Entity entity) {
        MutableFloat mutablefloat = new MutableFloat(0.0F);

        runIterationOnItem(itemstack, (holder, i) -> {
            ((Enchantment) holder.value()).modifyFishingTimeReduction(worldserver, i, itemstack, entity, mutablefloat);
        });
        return Math.max(0.0F, mutablefloat.floatValue());
    }

    public static int getTridentReturnToOwnerAcceleration(WorldServer worldserver, ItemStack itemstack, Entity entity) {
        MutableFloat mutablefloat = new MutableFloat(0.0F);

        runIterationOnItem(itemstack, (holder, i) -> {
            ((Enchantment) holder.value()).modifyTridentReturnToOwnerAcceleration(worldserver, i, itemstack, entity, mutablefloat);
        });
        return Math.max(0, mutablefloat.intValue());
    }

    public static float modifyCrossbowChargingTime(ItemStack itemstack, EntityLiving entityliving, float f) {
        MutableFloat mutablefloat = new MutableFloat(f);

        runIterationOnItem(itemstack, (holder, i) -> {
            ((Enchantment) holder.value()).modifyCrossbowChargeTime(entityliving.getRandom(), i, mutablefloat);
        });
        return Math.max(0.0F, mutablefloat.floatValue());
    }

    public static float getTridentSpinAttackStrength(ItemStack itemstack, EntityLiving entityliving) {
        MutableFloat mutablefloat = new MutableFloat(0.0F);

        runIterationOnItem(itemstack, (holder, i) -> {
            ((Enchantment) holder.value()).modifyTridentSpinAttackStrength(entityliving.getRandom(), i, mutablefloat);
        });
        return mutablefloat.floatValue();
    }

    public static boolean hasTag(ItemStack itemstack, TagKey<Enchantment> tagkey) {
        ItemEnchantments itemenchantments = (ItemEnchantments) itemstack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        Iterator iterator = itemenchantments.entrySet().iterator();

        Holder holder;

        do {
            if (!iterator.hasNext()) {
                return false;
            }

            Entry<Holder<Enchantment>> entry = (Entry) iterator.next();

            holder = (Holder) entry.getKey();
        } while (!holder.is(tagkey));

        return true;
    }

    public static boolean has(ItemStack itemstack, DataComponentType<?> datacomponenttype) {
        MutableBoolean mutableboolean = new MutableBoolean(false);

        runIterationOnItem(itemstack, (holder, i) -> {
            if (((Enchantment) holder.value()).effects().has(datacomponenttype)) {
                mutableboolean.setTrue();
            }

        });
        return mutableboolean.booleanValue();
    }

    public static <T> Optional<T> pickHighestLevel(ItemStack itemstack, DataComponentType<List<T>> datacomponenttype) {
        Pair<List<T>, Integer> pair = getHighestLevel(itemstack, datacomponenttype);

        if (pair != null) {
            List<T> list = (List) pair.getFirst();
            int i = (Integer) pair.getSecond();

            return Optional.of(list.get(Math.min(i, list.size()) - 1));
        } else {
            return Optional.empty();
        }
    }

    @Nullable
    public static <T> Pair<T, Integer> getHighestLevel(ItemStack itemstack, DataComponentType<T> datacomponenttype) {
        MutableObject<Pair<T, Integer>> mutableobject = new MutableObject();

        runIterationOnItem(itemstack, (holder, i) -> {
            if (mutableobject.getValue() == null || (Integer) ((Pair) mutableobject.getValue()).getSecond() < i) {
                T t0 = ((Enchantment) holder.value()).effects().get(datacomponenttype);

                if (t0 != null) {
                    mutableobject.setValue(Pair.of(t0, i));
                }
            }

        });
        return (Pair) mutableobject.getValue();
    }

    public static Optional<EnchantedItemInUse> getRandomItemWith(DataComponentType<?> datacomponenttype, EntityLiving entityliving, Predicate<ItemStack> predicate) {
        List<EnchantedItemInUse> list = new ArrayList();
        EnumItemSlot[] aenumitemslot = EnumItemSlot.values();
        int i = aenumitemslot.length;

        for (int j = 0; j < i; ++j) {
            EnumItemSlot enumitemslot = aenumitemslot[j];
            ItemStack itemstack = entityliving.getItemBySlot(enumitemslot);

            if (predicate.test(itemstack)) {
                ItemEnchantments itemenchantments = (ItemEnchantments) itemstack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
                Iterator iterator = itemenchantments.entrySet().iterator();

                while (iterator.hasNext()) {
                    Entry<Holder<Enchantment>> entry = (Entry) iterator.next();
                    Holder<Enchantment> holder = (Holder) entry.getKey();

                    if (((Enchantment) holder.value()).effects().has(datacomponenttype) && ((Enchantment) holder.value()).matchingSlot(enumitemslot)) {
                        list.add(new EnchantedItemInUse(itemstack, enumitemslot, entityliving));
                    }
                }
            }
        }

        return SystemUtils.getRandomSafe(list, entityliving.getRandom());
    }

    public static int getEnchantmentCost(RandomSource randomsource, int i, int j, ItemStack itemstack) {
        Item item = itemstack.getItem();
        int k = item.getEnchantmentValue();

        if (k <= 0) {
            return 0;
        } else {
            if (j > 15) {
                j = 15;
            }

            int l = randomsource.nextInt(8) + 1 + (j >> 1) + randomsource.nextInt(j + 1);

            return i == 0 ? Math.max(l / 3, 1) : (i == 1 ? l * 2 / 3 + 1 : Math.max(l, j * 2));
        }
    }

    public static ItemStack enchantItem(RandomSource randomsource, ItemStack itemstack, int i, IRegistryCustom iregistrycustom, Optional<? extends HolderSet<Enchantment>> optional) {
        return enchantItem(randomsource, itemstack, i, (Stream) optional.map(HolderSet::stream).orElseGet(() -> {
            return iregistrycustom.registryOrThrow(Registries.ENCHANTMENT).holders().map((holder_c) -> {
                return holder_c;
            });
        }));
    }

    public static ItemStack enchantItem(RandomSource randomsource, ItemStack itemstack, int i, Stream<Holder<Enchantment>> stream) {
        List<WeightedRandomEnchant> list = selectEnchantment(randomsource, itemstack, i, stream);

        if (itemstack.is(Items.BOOK)) {
            itemstack = new ItemStack(Items.ENCHANTED_BOOK);
        }

        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            WeightedRandomEnchant weightedrandomenchant = (WeightedRandomEnchant) iterator.next();

            itemstack.enchant(weightedrandomenchant.enchantment, weightedrandomenchant.level);
        }

        return itemstack;
    }

    public static List<WeightedRandomEnchant> selectEnchantment(RandomSource randomsource, ItemStack itemstack, int i, Stream<Holder<Enchantment>> stream) {
        List<WeightedRandomEnchant> list = Lists.newArrayList();
        Item item = itemstack.getItem();
        int j = item.getEnchantmentValue();

        if (j <= 0) {
            return list;
        } else {
            i += 1 + randomsource.nextInt(j / 4 + 1) + randomsource.nextInt(j / 4 + 1);
            float f = (randomsource.nextFloat() + randomsource.nextFloat() - 1.0F) * 0.15F;

            i = MathHelper.clamp(Math.round((float) i + (float) i * f), 1, Integer.MAX_VALUE);
            List<WeightedRandomEnchant> list1 = getAvailableEnchantmentResults(i, itemstack, stream);

            if (!list1.isEmpty()) {
                Optional optional = WeightedRandom2.getRandomItem(randomsource, list1);

                Objects.requireNonNull(list);
                optional.ifPresent(list::add);

                while (randomsource.nextInt(50) <= i) {
                    if (!list.isEmpty()) {
                        filterCompatibleEnchantments(list1, (WeightedRandomEnchant) SystemUtils.lastOf(list));
                    }

                    if (list1.isEmpty()) {
                        break;
                    }

                    optional = WeightedRandom2.getRandomItem(randomsource, list1);
                    Objects.requireNonNull(list);
                    optional.ifPresent(list::add);
                    i /= 2;
                }
            }

            return list;
        }
    }

    public static void filterCompatibleEnchantments(List<WeightedRandomEnchant> list, WeightedRandomEnchant weightedrandomenchant) {
        list.removeIf((weightedrandomenchant1) -> {
            return !Enchantment.areCompatible(weightedrandomenchant.enchantment, weightedrandomenchant1.enchantment);
        });
    }

    public static boolean isEnchantmentCompatible(Collection<Holder<Enchantment>> collection, Holder<Enchantment> holder) {
        Iterator iterator = collection.iterator();

        Holder holder1;

        do {
            if (!iterator.hasNext()) {
                return true;
            }

            holder1 = (Holder) iterator.next();
        } while (Enchantment.areCompatible(holder1, holder));

        return false;
    }

    public static List<WeightedRandomEnchant> getAvailableEnchantmentResults(int i, ItemStack itemstack, Stream<Holder<Enchantment>> stream) {
        List<WeightedRandomEnchant> list = Lists.newArrayList();
        boolean flag = itemstack.is(Items.BOOK);

        stream.filter((holder) -> {
            return ((Enchantment) holder.value()).isPrimaryItem(itemstack) || flag;
        }).forEach((holder) -> {
            Enchantment enchantment = (Enchantment) holder.value();

            for (int j = enchantment.getMaxLevel(); j >= enchantment.getMinLevel(); --j) {
                if (i >= enchantment.getMinCost(j) && i <= enchantment.getMaxCost(j)) {
                    list.add(new WeightedRandomEnchant(holder, j));
                    break;
                }
            }

        });
        return list;
    }

    public static void enchantItemFromProvider(ItemStack itemstack, IRegistryCustom iregistrycustom, ResourceKey<EnchantmentProvider> resourcekey, DifficultyDamageScaler difficultydamagescaler, RandomSource randomsource) {
        EnchantmentProvider enchantmentprovider = (EnchantmentProvider) iregistrycustom.registryOrThrow(Registries.ENCHANTMENT_PROVIDER).get(resourcekey);

        if (enchantmentprovider != null) {
            updateEnchantments(itemstack, (itemenchantments_a) -> {
                enchantmentprovider.enchant(itemstack, itemenchantments_a, randomsource, difficultydamagescaler);
            });
        }

    }

    @FunctionalInterface
    private interface b {

        void accept(Holder<Enchantment> holder, int i);
    }

    @FunctionalInterface
    private interface a {

        void accept(Holder<Enchantment> holder, int i, EnchantedItemInUse enchantediteminuse);
    }
}
