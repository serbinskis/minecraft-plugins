package net.minecraft.world.item.enchantment;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom2;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;

public class EnchantmentManager {

    private static final float SWIFT_SNEAK_EXTRA_FACTOR = 0.15F;

    public EnchantmentManager() {}

    public static int getItemEnchantmentLevel(Enchantment enchantment, ItemStack itemstack) {
        ItemEnchantments itemenchantments = (ItemEnchantments) itemstack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

        return itemenchantments.getLevel(enchantment);
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

    public static float getSweepingDamageRatio(int i) {
        return 1.0F - 1.0F / (float) (i + 1);
    }

    private static void runIterationOnItem(EnchantmentManager.a enchantmentmanager_a, ItemStack itemstack) {
        ItemEnchantments itemenchantments = (ItemEnchantments) itemstack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        Iterator iterator = itemenchantments.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<Holder<Enchantment>> entry = (Entry) iterator.next();

            enchantmentmanager_a.accept((Enchantment) ((Holder) entry.getKey()).value(), entry.getIntValue());
        }

    }

    private static void runIterationOnInventory(EnchantmentManager.a enchantmentmanager_a, Iterable<ItemStack> iterable) {
        Iterator iterator = iterable.iterator();

        while (iterator.hasNext()) {
            ItemStack itemstack = (ItemStack) iterator.next();

            runIterationOnItem(enchantmentmanager_a, itemstack);
        }

    }

    public static int getDamageProtection(Iterable<ItemStack> iterable, DamageSource damagesource) {
        MutableInt mutableint = new MutableInt();

        runIterationOnInventory((enchantment, i) -> {
            mutableint.add(enchantment.getDamageProtection(i, damagesource));
        }, iterable);
        return mutableint.intValue();
    }

    public static float getDamageBonus(ItemStack itemstack, @Nullable EntityTypes<?> entitytypes) {
        MutableFloat mutablefloat = new MutableFloat();

        runIterationOnItem((enchantment, i) -> {
            mutablefloat.add(enchantment.getDamageBonus(i, entitytypes));
        }, itemstack);
        return mutablefloat.floatValue();
    }

    public static float getSweepingDamageRatio(EntityLiving entityliving) {
        int i = getEnchantmentLevel(Enchantments.SWEEPING_EDGE, entityliving);

        return i > 0 ? getSweepingDamageRatio(i) : 0.0F;
    }

    public static float calculateArmorBreach(@Nullable Entity entity, float f) {
        if (entity instanceof EntityLiving entityliving) {
            int i = getEnchantmentLevel(Enchantments.BREACH, entityliving);

            if (i > 0) {
                return BreachEnchantment.calculateArmorBreach((float) i, f);
            }
        }

        return f;
    }

    public static void doPostHurtEffects(EntityLiving entityliving, Entity entity) {
        EnchantmentManager.a enchantmentmanager_a = (enchantment, i) -> {
            enchantment.doPostHurt(entityliving, entity, i);
        };

        if (entityliving != null) {
            runIterationOnInventory(enchantmentmanager_a, entityliving.getAllSlots());
        }

        if (entity instanceof EntityHuman) {
            runIterationOnItem(enchantmentmanager_a, entityliving.getMainHandItem());
        }

    }

    public static void doPostDamageEffects(EntityLiving entityliving, Entity entity) {
        EnchantmentManager.a enchantmentmanager_a = (enchantment, i) -> {
            enchantment.doPostAttack(entityliving, entity, i);
        };

        if (entityliving != null) {
            runIterationOnInventory(enchantmentmanager_a, entityliving.getAllSlots());
        }

        if (entityliving instanceof EntityHuman) {
            runIterationOnItem(enchantmentmanager_a, entityliving.getMainHandItem());
        }

    }

    public static void doPostItemStackHurtEffects(EntityLiving entityliving, Entity entity, ItemEnchantments itemenchantments) {
        Iterator iterator = itemenchantments.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<Holder<Enchantment>> entry = (Entry) iterator.next();

            ((Enchantment) ((Holder) entry.getKey()).value()).doPostItemStackHurt(entityliving, entity, entry.getIntValue());
        }

    }

    public static int getEnchantmentLevel(Enchantment enchantment, EntityLiving entityliving) {
        Iterable<ItemStack> iterable = enchantment.getSlotItems(entityliving).values();

        if (iterable == null) {
            return 0;
        } else {
            int i = 0;
            Iterator iterator = iterable.iterator();

            while (iterator.hasNext()) {
                ItemStack itemstack = (ItemStack) iterator.next();
                int j = getItemEnchantmentLevel(enchantment, itemstack);

                if (j > i) {
                    i = j;
                }
            }

            return i;
        }
    }

    public static float getSneakingSpeedBonus(EntityLiving entityliving) {
        return (float) getEnchantmentLevel(Enchantments.SWIFT_SNEAK, entityliving) * 0.15F;
    }

    public static int getKnockbackBonus(EntityLiving entityliving) {
        return getEnchantmentLevel(Enchantments.KNOCKBACK, entityliving);
    }

    public static int getFireAspect(EntityLiving entityliving) {
        return getEnchantmentLevel(Enchantments.FIRE_ASPECT, entityliving);
    }

    public static int getRespiration(EntityLiving entityliving) {
        return getEnchantmentLevel(Enchantments.RESPIRATION, entityliving);
    }

    public static int getDepthStrider(EntityLiving entityliving) {
        return getEnchantmentLevel(Enchantments.DEPTH_STRIDER, entityliving);
    }

    public static int getBlockEfficiency(EntityLiving entityliving) {
        return getEnchantmentLevel(Enchantments.EFFICIENCY, entityliving);
    }

    public static int getFishingLuckBonus(ItemStack itemstack) {
        return getItemEnchantmentLevel(Enchantments.LUCK_OF_THE_SEA, itemstack);
    }

    public static int getFishingSpeedBonus(ItemStack itemstack) {
        return getItemEnchantmentLevel(Enchantments.LURE, itemstack);
    }

    public static int getMobLooting(EntityLiving entityliving) {
        return getEnchantmentLevel(Enchantments.LOOTING, entityliving);
    }

    public static boolean hasAquaAffinity(EntityLiving entityliving) {
        return getEnchantmentLevel(Enchantments.AQUA_AFFINITY, entityliving) > 0;
    }

    public static boolean hasFrostWalker(EntityLiving entityliving) {
        return getEnchantmentLevel(Enchantments.FROST_WALKER, entityliving) > 0;
    }

    public static boolean hasSoulSpeed(EntityLiving entityliving) {
        return getEnchantmentLevel(Enchantments.SOUL_SPEED, entityliving) > 0;
    }

    public static boolean hasBindingCurse(ItemStack itemstack) {
        return getItemEnchantmentLevel(Enchantments.BINDING_CURSE, itemstack) > 0;
    }

    public static boolean hasVanishingCurse(ItemStack itemstack) {
        return getItemEnchantmentLevel(Enchantments.VANISHING_CURSE, itemstack) > 0;
    }

    public static boolean hasSilkTouch(ItemStack itemstack) {
        return getItemEnchantmentLevel(Enchantments.SILK_TOUCH, itemstack) > 0;
    }

    public static int getLoyalty(ItemStack itemstack) {
        return getItemEnchantmentLevel(Enchantments.LOYALTY, itemstack);
    }

    public static int getRiptide(ItemStack itemstack) {
        return getItemEnchantmentLevel(Enchantments.RIPTIDE, itemstack);
    }

    public static boolean hasChanneling(ItemStack itemstack) {
        return getItemEnchantmentLevel(Enchantments.CHANNELING, itemstack) > 0;
    }

    @Nullable
    public static java.util.Map.Entry<EnumItemSlot, ItemStack> getRandomItemWith(Enchantment enchantment, EntityLiving entityliving) {
        return getRandomItemWith(enchantment, entityliving, (itemstack) -> {
            return true;
        });
    }

    @Nullable
    public static java.util.Map.Entry<EnumItemSlot, ItemStack> getRandomItemWith(Enchantment enchantment, EntityLiving entityliving, Predicate<ItemStack> predicate) {
        Map<EnumItemSlot, ItemStack> map = enchantment.getSlotItems(entityliving);

        if (map.isEmpty()) {
            return null;
        } else {
            List<java.util.Map.Entry<EnumItemSlot, ItemStack>> list = Lists.newArrayList();
            Iterator iterator = map.entrySet().iterator();

            while (iterator.hasNext()) {
                java.util.Map.Entry<EnumItemSlot, ItemStack> java_util_map_entry = (java.util.Map.Entry) iterator.next();
                ItemStack itemstack = (ItemStack) java_util_map_entry.getValue();

                if (!itemstack.isEmpty() && getItemEnchantmentLevel(enchantment, itemstack) > 0 && predicate.test(itemstack)) {
                    list.add(java_util_map_entry);
                }
            }

            return list.isEmpty() ? null : (java.util.Map.Entry) list.get(entityliving.getRandom().nextInt(list.size()));
        }
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

    public static ItemStack enchantItem(FeatureFlagSet featureflagset, RandomSource randomsource, ItemStack itemstack, int i, boolean flag) {
        List<WeightedRandomEnchant> list = selectEnchantment(featureflagset, randomsource, itemstack, i, flag);

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

    public static List<WeightedRandomEnchant> selectEnchantment(FeatureFlagSet featureflagset, RandomSource randomsource, ItemStack itemstack, int i, boolean flag) {
        List<WeightedRandomEnchant> list = Lists.newArrayList();
        Item item = itemstack.getItem();
        int j = item.getEnchantmentValue();

        if (j <= 0) {
            return list;
        } else {
            i += 1 + randomsource.nextInt(j / 4 + 1) + randomsource.nextInt(j / 4 + 1);
            float f = (randomsource.nextFloat() + randomsource.nextFloat() - 1.0F) * 0.15F;

            i = MathHelper.clamp(Math.round((float) i + (float) i * f), 1, Integer.MAX_VALUE);
            List<WeightedRandomEnchant> list1 = getAvailableEnchantmentResults(featureflagset, i, itemstack, flag);

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
        Iterator<WeightedRandomEnchant> iterator = list.iterator();

        while (iterator.hasNext()) {
            if (!weightedrandomenchant.enchantment.isCompatibleWith(((WeightedRandomEnchant) iterator.next()).enchantment)) {
                iterator.remove();
            }
        }

    }

    public static boolean isEnchantmentCompatible(Collection<Holder<Enchantment>> collection, Enchantment enchantment) {
        Iterator iterator = collection.iterator();

        Holder holder;

        do {
            if (!iterator.hasNext()) {
                return true;
            }

            holder = (Holder) iterator.next();
        } while (((Enchantment) holder.value()).isCompatibleWith(enchantment));

        return false;
    }

    public static List<WeightedRandomEnchant> getAvailableEnchantmentResults(FeatureFlagSet featureflagset, int i, ItemStack itemstack, boolean flag) {
        List<WeightedRandomEnchant> list = Lists.newArrayList();
        boolean flag1 = itemstack.is(Items.BOOK);
        Iterator iterator = BuiltInRegistries.ENCHANTMENT.iterator();

        while (iterator.hasNext()) {
            Enchantment enchantment = (Enchantment) iterator.next();

            if (enchantment.isEnabled(featureflagset) && (!enchantment.isTreasureOnly() || flag) && enchantment.isDiscoverable() && (flag1 || enchantment.canEnchant(itemstack) && enchantment.isPrimaryItem(itemstack))) {
                for (int j = enchantment.getMaxLevel(); j > enchantment.getMinLevel() - 1; --j) {
                    if (i >= enchantment.getMinCost(j) && i <= enchantment.getMaxCost(j)) {
                        list.add(new WeightedRandomEnchant(enchantment, j));
                        break;
                    }
                }
            }
        }

        return list;
    }

    @FunctionalInterface
    private interface a {

        void accept(Enchantment enchantment, int i);
    }
}
