package net.minecraft.world.item.alchemy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeItemStack;

public class PotionBrewer {

    public static final int BREWING_TIME_SECONDS = 20;
    public static final PotionBrewer EMPTY = new PotionBrewer(List.of(), List.of(), List.of());
    private final List<RecipeItemStack> containers;
    private final List<PotionBrewer.PredicatedCombination<PotionRegistry>> potionMixes;
    private final List<PotionBrewer.PredicatedCombination<Item>> containerMixes;

    PotionBrewer(List<RecipeItemStack> list, List<PotionBrewer.PredicatedCombination<PotionRegistry>> list1, List<PotionBrewer.PredicatedCombination<Item>> list2) {
        this.containers = list;
        this.potionMixes = list1;
        this.containerMixes = list2;
    }

    public boolean isIngredient(ItemStack itemstack) {
        return this.isContainerIngredient(itemstack) || this.isPotionIngredient(itemstack);
    }

    private boolean isContainer(ItemStack itemstack) {
        Iterator iterator = this.containers.iterator();

        RecipeItemStack recipeitemstack;

        do {
            if (!iterator.hasNext()) {
                return false;
            }

            recipeitemstack = (RecipeItemStack) iterator.next();
        } while (!recipeitemstack.test(itemstack));

        return true;
    }

    public boolean isContainerIngredient(ItemStack itemstack) {
        Iterator iterator = this.containerMixes.iterator();

        PotionBrewer.PredicatedCombination potionbrewer_predicatedcombination;

        do {
            if (!iterator.hasNext()) {
                return false;
            }

            potionbrewer_predicatedcombination = (PotionBrewer.PredicatedCombination) iterator.next();
        } while (!potionbrewer_predicatedcombination.ingredient.test(itemstack));

        return true;
    }

    public boolean isPotionIngredient(ItemStack itemstack) {
        Iterator iterator = this.potionMixes.iterator();

        PotionBrewer.PredicatedCombination potionbrewer_predicatedcombination;

        do {
            if (!iterator.hasNext()) {
                return false;
            }

            potionbrewer_predicatedcombination = (PotionBrewer.PredicatedCombination) iterator.next();
        } while (!potionbrewer_predicatedcombination.ingredient.test(itemstack));

        return true;
    }

    public boolean isBrewablePotion(Holder<PotionRegistry> holder) {
        Iterator iterator = this.potionMixes.iterator();

        PotionBrewer.PredicatedCombination potionbrewer_predicatedcombination;

        do {
            if (!iterator.hasNext()) {
                return false;
            }

            potionbrewer_predicatedcombination = (PotionBrewer.PredicatedCombination) iterator.next();
        } while (!potionbrewer_predicatedcombination.to.is(holder));

        return true;
    }

    public boolean hasMix(ItemStack itemstack, ItemStack itemstack1) {
        return !this.isContainer(itemstack) ? false : this.hasContainerMix(itemstack, itemstack1) || this.hasPotionMix(itemstack, itemstack1);
    }

    public boolean hasContainerMix(ItemStack itemstack, ItemStack itemstack1) {
        Iterator iterator = this.containerMixes.iterator();

        PotionBrewer.PredicatedCombination potionbrewer_predicatedcombination;

        do {
            if (!iterator.hasNext()) {
                return false;
            }

            potionbrewer_predicatedcombination = (PotionBrewer.PredicatedCombination) iterator.next();
        } while (!itemstack.is(potionbrewer_predicatedcombination.from) || !potionbrewer_predicatedcombination.ingredient.test(itemstack1));

        return true;
    }

    public boolean hasPotionMix(ItemStack itemstack, ItemStack itemstack1) {
        Optional<Holder<PotionRegistry>> optional = ((PotionContents) itemstack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY)).potion();

        if (optional.isEmpty()) {
            return false;
        } else {
            Iterator iterator = this.potionMixes.iterator();

            PotionBrewer.PredicatedCombination potionbrewer_predicatedcombination;

            do {
                if (!iterator.hasNext()) {
                    return false;
                }

                potionbrewer_predicatedcombination = (PotionBrewer.PredicatedCombination) iterator.next();
            } while (!potionbrewer_predicatedcombination.from.is((Holder) optional.get()) || !potionbrewer_predicatedcombination.ingredient.test(itemstack1));

            return true;
        }
    }

    public ItemStack mix(ItemStack itemstack, ItemStack itemstack1) {
        if (itemstack1.isEmpty()) {
            return itemstack1;
        } else {
            Optional<Holder<PotionRegistry>> optional = ((PotionContents) itemstack1.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY)).potion();

            if (optional.isEmpty()) {
                return itemstack1;
            } else {
                Iterator iterator = this.containerMixes.iterator();

                PotionBrewer.PredicatedCombination potionbrewer_predicatedcombination;

                do {
                    if (!iterator.hasNext()) {
                        iterator = this.potionMixes.iterator();

                        do {
                            if (!iterator.hasNext()) {
                                return itemstack1;
                            }

                            potionbrewer_predicatedcombination = (PotionBrewer.PredicatedCombination) iterator.next();
                        } while (!potionbrewer_predicatedcombination.from.is((Holder) optional.get()) || !potionbrewer_predicatedcombination.ingredient.test(itemstack));

                        return PotionContents.createItemStack(itemstack1.getItem(), potionbrewer_predicatedcombination.to);
                    }

                    potionbrewer_predicatedcombination = (PotionBrewer.PredicatedCombination) iterator.next();
                } while (!itemstack1.is(potionbrewer_predicatedcombination.from) || !potionbrewer_predicatedcombination.ingredient.test(itemstack));

                return PotionContents.createItemStack((Item) potionbrewer_predicatedcombination.to.value(), (Holder) optional.get());
            }
        }
    }

    public static PotionBrewer bootstrap(FeatureFlagSet featureflagset) {
        PotionBrewer.a potionbrewer_a = new PotionBrewer.a(featureflagset);

        addVanillaMixes(potionbrewer_a);
        return potionbrewer_a.build();
    }

    public static void addVanillaMixes(PotionBrewer.a potionbrewer_a) {
        potionbrewer_a.addContainer(Items.POTION);
        potionbrewer_a.addContainer(Items.SPLASH_POTION);
        potionbrewer_a.addContainer(Items.LINGERING_POTION);
        potionbrewer_a.addContainerRecipe(Items.POTION, Items.GUNPOWDER, Items.SPLASH_POTION);
        potionbrewer_a.addContainerRecipe(Items.SPLASH_POTION, Items.DRAGON_BREATH, Items.LINGERING_POTION);
        potionbrewer_a.addMix(Potions.WATER, Items.GLOWSTONE_DUST, Potions.THICK);
        potionbrewer_a.addMix(Potions.WATER, Items.REDSTONE, Potions.MUNDANE);
        potionbrewer_a.addMix(Potions.WATER, Items.NETHER_WART, Potions.AWKWARD);
        potionbrewer_a.addStartMix(Items.BREEZE_ROD, Potions.WIND_CHARGED);
        potionbrewer_a.addStartMix(Items.SLIME_BLOCK, Potions.OOZING);
        potionbrewer_a.addStartMix(Items.STONE, Potions.INFESTED);
        potionbrewer_a.addStartMix(Items.COBWEB, Potions.WEAVING);
        potionbrewer_a.addMix(Potions.AWKWARD, Items.GOLDEN_CARROT, Potions.NIGHT_VISION);
        potionbrewer_a.addMix(Potions.NIGHT_VISION, Items.REDSTONE, Potions.LONG_NIGHT_VISION);
        potionbrewer_a.addMix(Potions.NIGHT_VISION, Items.FERMENTED_SPIDER_EYE, Potions.INVISIBILITY);
        potionbrewer_a.addMix(Potions.LONG_NIGHT_VISION, Items.FERMENTED_SPIDER_EYE, Potions.LONG_INVISIBILITY);
        potionbrewer_a.addMix(Potions.INVISIBILITY, Items.REDSTONE, Potions.LONG_INVISIBILITY);
        potionbrewer_a.addStartMix(Items.MAGMA_CREAM, Potions.FIRE_RESISTANCE);
        potionbrewer_a.addMix(Potions.FIRE_RESISTANCE, Items.REDSTONE, Potions.LONG_FIRE_RESISTANCE);
        potionbrewer_a.addStartMix(Items.RABBIT_FOOT, Potions.LEAPING);
        potionbrewer_a.addMix(Potions.LEAPING, Items.REDSTONE, Potions.LONG_LEAPING);
        potionbrewer_a.addMix(Potions.LEAPING, Items.GLOWSTONE_DUST, Potions.STRONG_LEAPING);
        potionbrewer_a.addMix(Potions.LEAPING, Items.FERMENTED_SPIDER_EYE, Potions.SLOWNESS);
        potionbrewer_a.addMix(Potions.LONG_LEAPING, Items.FERMENTED_SPIDER_EYE, Potions.LONG_SLOWNESS);
        potionbrewer_a.addMix(Potions.SLOWNESS, Items.REDSTONE, Potions.LONG_SLOWNESS);
        potionbrewer_a.addMix(Potions.SLOWNESS, Items.GLOWSTONE_DUST, Potions.STRONG_SLOWNESS);
        potionbrewer_a.addMix(Potions.AWKWARD, Items.TURTLE_HELMET, Potions.TURTLE_MASTER);
        potionbrewer_a.addMix(Potions.TURTLE_MASTER, Items.REDSTONE, Potions.LONG_TURTLE_MASTER);
        potionbrewer_a.addMix(Potions.TURTLE_MASTER, Items.GLOWSTONE_DUST, Potions.STRONG_TURTLE_MASTER);
        potionbrewer_a.addMix(Potions.SWIFTNESS, Items.FERMENTED_SPIDER_EYE, Potions.SLOWNESS);
        potionbrewer_a.addMix(Potions.LONG_SWIFTNESS, Items.FERMENTED_SPIDER_EYE, Potions.LONG_SLOWNESS);
        potionbrewer_a.addStartMix(Items.SUGAR, Potions.SWIFTNESS);
        potionbrewer_a.addMix(Potions.SWIFTNESS, Items.REDSTONE, Potions.LONG_SWIFTNESS);
        potionbrewer_a.addMix(Potions.SWIFTNESS, Items.GLOWSTONE_DUST, Potions.STRONG_SWIFTNESS);
        potionbrewer_a.addMix(Potions.AWKWARD, Items.PUFFERFISH, Potions.WATER_BREATHING);
        potionbrewer_a.addMix(Potions.WATER_BREATHING, Items.REDSTONE, Potions.LONG_WATER_BREATHING);
        potionbrewer_a.addStartMix(Items.GLISTERING_MELON_SLICE, Potions.HEALING);
        potionbrewer_a.addMix(Potions.HEALING, Items.GLOWSTONE_DUST, Potions.STRONG_HEALING);
        potionbrewer_a.addMix(Potions.HEALING, Items.FERMENTED_SPIDER_EYE, Potions.HARMING);
        potionbrewer_a.addMix(Potions.STRONG_HEALING, Items.FERMENTED_SPIDER_EYE, Potions.STRONG_HARMING);
        potionbrewer_a.addMix(Potions.HARMING, Items.GLOWSTONE_DUST, Potions.STRONG_HARMING);
        potionbrewer_a.addMix(Potions.POISON, Items.FERMENTED_SPIDER_EYE, Potions.HARMING);
        potionbrewer_a.addMix(Potions.LONG_POISON, Items.FERMENTED_SPIDER_EYE, Potions.HARMING);
        potionbrewer_a.addMix(Potions.STRONG_POISON, Items.FERMENTED_SPIDER_EYE, Potions.STRONG_HARMING);
        potionbrewer_a.addStartMix(Items.SPIDER_EYE, Potions.POISON);
        potionbrewer_a.addMix(Potions.POISON, Items.REDSTONE, Potions.LONG_POISON);
        potionbrewer_a.addMix(Potions.POISON, Items.GLOWSTONE_DUST, Potions.STRONG_POISON);
        potionbrewer_a.addStartMix(Items.GHAST_TEAR, Potions.REGENERATION);
        potionbrewer_a.addMix(Potions.REGENERATION, Items.REDSTONE, Potions.LONG_REGENERATION);
        potionbrewer_a.addMix(Potions.REGENERATION, Items.GLOWSTONE_DUST, Potions.STRONG_REGENERATION);
        potionbrewer_a.addStartMix(Items.BLAZE_POWDER, Potions.STRENGTH);
        potionbrewer_a.addMix(Potions.STRENGTH, Items.REDSTONE, Potions.LONG_STRENGTH);
        potionbrewer_a.addMix(Potions.STRENGTH, Items.GLOWSTONE_DUST, Potions.STRONG_STRENGTH);
        potionbrewer_a.addMix(Potions.WATER, Items.FERMENTED_SPIDER_EYE, Potions.WEAKNESS);
        potionbrewer_a.addMix(Potions.WEAKNESS, Items.REDSTONE, Potions.LONG_WEAKNESS);
        potionbrewer_a.addMix(Potions.AWKWARD, Items.PHANTOM_MEMBRANE, Potions.SLOW_FALLING);
        potionbrewer_a.addMix(Potions.SLOW_FALLING, Items.REDSTONE, Potions.LONG_SLOW_FALLING);
    }

    private static record PredicatedCombination<T>(Holder<T> from, RecipeItemStack ingredient, Holder<T> to) {

    }

    public static class a {

        private final List<RecipeItemStack> containers = new ArrayList();
        private final List<PotionBrewer.PredicatedCombination<PotionRegistry>> potionMixes = new ArrayList();
        private final List<PotionBrewer.PredicatedCombination<Item>> containerMixes = new ArrayList();
        private final FeatureFlagSet enabledFeatures;

        public a(FeatureFlagSet featureflagset) {
            this.enabledFeatures = featureflagset;
        }

        private static void expectPotion(Item item) {
            if (!(item instanceof ItemPotion)) {
                throw new IllegalArgumentException("Expected a potion, got: " + String.valueOf(BuiltInRegistries.ITEM.getKey(item)));
            }
        }

        public void addContainerRecipe(Item item, Item item1, Item item2) {
            if (item.isEnabled(this.enabledFeatures) && item1.isEnabled(this.enabledFeatures) && item2.isEnabled(this.enabledFeatures)) {
                expectPotion(item);
                expectPotion(item2);
                this.containerMixes.add(new PotionBrewer.PredicatedCombination<>(item.builtInRegistryHolder(), RecipeItemStack.of(item1), item2.builtInRegistryHolder()));
            }
        }

        public void addContainer(Item item) {
            if (item.isEnabled(this.enabledFeatures)) {
                expectPotion(item);
                this.containers.add(RecipeItemStack.of(item));
            }
        }

        public void addMix(Holder<PotionRegistry> holder, Item item, Holder<PotionRegistry> holder1) {
            if (((PotionRegistry) holder.value()).isEnabled(this.enabledFeatures) && item.isEnabled(this.enabledFeatures) && ((PotionRegistry) holder1.value()).isEnabled(this.enabledFeatures)) {
                this.potionMixes.add(new PotionBrewer.PredicatedCombination<>(holder, RecipeItemStack.of(item), holder1));
            }

        }

        public void addStartMix(Item item, Holder<PotionRegistry> holder) {
            if (((PotionRegistry) holder.value()).isEnabled(this.enabledFeatures)) {
                this.addMix(Potions.WATER, item, Potions.MUNDANE);
                this.addMix(Potions.AWKWARD, item, holder);
            }

        }

        public PotionBrewer build() {
            return new PotionBrewer(List.copyOf(this.containers), List.copyOf(this.potionMixes), List.copyOf(this.containerMixes));
        }
    }
}
