package net.minecraft.world.entity.npc;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagsItem;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectList;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.EnumColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDye;
import net.minecraft.world.item.ItemEnchantedBook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemWorldMap;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.PotionRegistry;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.WeightedRandomEnchant;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantRecipe;
import net.minecraft.world.level.IMaterial;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.WorldMap;
import org.apache.commons.lang3.tuple.Pair;

public class VillagerTrades {

    private static final int DEFAULT_SUPPLY = 12;
    private static final int COMMON_ITEMS_SUPPLY = 16;
    private static final int UNCOMMON_ITEMS_SUPPLY = 3;
    private static final int XP_LEVEL_1_SELL = 1;
    private static final int XP_LEVEL_1_BUY = 2;
    private static final int XP_LEVEL_2_SELL = 5;
    private static final int XP_LEVEL_2_BUY = 10;
    private static final int XP_LEVEL_3_SELL = 10;
    private static final int XP_LEVEL_3_BUY = 20;
    private static final int XP_LEVEL_4_SELL = 15;
    private static final int XP_LEVEL_4_BUY = 30;
    private static final int XP_LEVEL_5_TRADE = 30;
    private static final float LOW_TIER_PRICE_MULTIPLIER = 0.05F;
    private static final float HIGH_TIER_PRICE_MULTIPLIER = 0.2F;
    public static final Map<VillagerProfession, Int2ObjectMap<VillagerTrades.IMerchantRecipeOption[]>> TRADES = (Map) SystemUtils.make(Maps.newHashMap(), (hashmap) -> {
        hashmap.put(VillagerProfession.FARMER, toIntMap(ImmutableMap.of(1, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.WHEAT, 20, 16, 2), new VillagerTrades.b(Items.POTATO, 26, 16, 2), new VillagerTrades.b(Items.CARROT, 22, 16, 2), new VillagerTrades.b(Items.BEETROOT, 15, 16, 2), new VillagerTrades.i(Items.BREAD, 1, 6, 16, 1)}, 2, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Blocks.PUMPKIN, 6, 12, 10), new VillagerTrades.i(Items.PUMPKIN_PIE, 1, 4, 5), new VillagerTrades.i(Items.APPLE, 1, 4, 16, 5)}, 3, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.i(Items.COOKIE, 3, 18, 10), new VillagerTrades.b(Blocks.MELON, 4, 12, 20)}, 4, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.i(Blocks.CAKE, 1, 1, 12, 15), new VillagerTrades.j(MobEffects.NIGHT_VISION, 100, 15), new VillagerTrades.j(MobEffects.JUMP, 160, 15), new VillagerTrades.j(MobEffects.WEAKNESS, 140, 15), new VillagerTrades.j(MobEffects.BLINDNESS, 120, 15), new VillagerTrades.j(MobEffects.POISON, 280, 15), new VillagerTrades.j(MobEffects.SATURATION, 7, 15)}, 5, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.i(Items.GOLDEN_CARROT, 3, 3, 30), new VillagerTrades.i(Items.GLISTERING_MELON_SLICE, 4, 3, 30)})));
        hashmap.put(VillagerProfession.FISHERMAN, toIntMap(ImmutableMap.of(1, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.STRING, 20, 16, 2), new VillagerTrades.b(Items.COAL, 10, 16, 2), new VillagerTrades.h(Items.COD, 6, 1, Items.COOKED_COD, 6, 16, 1, 0.05F), new VillagerTrades.i(Items.COD_BUCKET, 3, 1, 16, 1)}, 2, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.COD, 15, 16, 10), new VillagerTrades.h(Items.SALMON, 6, 1, Items.COOKED_SALMON, 6, 16, 5, 0.05F), new VillagerTrades.i(Items.CAMPFIRE, 2, 1, 5)}, 3, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.SALMON, 13, 16, 20), new VillagerTrades.e(Items.FISHING_ROD, 3, 3, 10, 0.2F)}, 4, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.TROPICAL_FISH, 6, 12, 30)}, 5, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.PUFFERFISH, 4, 12, 30), new VillagerTrades.c(1, 12, 30, ImmutableMap.builder().put(VillagerType.PLAINS, Items.OAK_BOAT).put(VillagerType.TAIGA, Items.SPRUCE_BOAT).put(VillagerType.SNOW, Items.SPRUCE_BOAT).put(VillagerType.DESERT, Items.JUNGLE_BOAT).put(VillagerType.JUNGLE, Items.JUNGLE_BOAT).put(VillagerType.SAVANNA, Items.ACACIA_BOAT).put(VillagerType.SWAMP, Items.DARK_OAK_BOAT).build())})));
        hashmap.put(VillagerProfession.SHEPHERD, toIntMap(ImmutableMap.of(1, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Blocks.WHITE_WOOL, 18, 16, 2), new VillagerTrades.b(Blocks.BROWN_WOOL, 18, 16, 2), new VillagerTrades.b(Blocks.BLACK_WOOL, 18, 16, 2), new VillagerTrades.b(Blocks.GRAY_WOOL, 18, 16, 2), new VillagerTrades.i(Items.SHEARS, 2, 1, 1)}, 2, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.WHITE_DYE, 12, 16, 10), new VillagerTrades.b(Items.GRAY_DYE, 12, 16, 10), new VillagerTrades.b(Items.BLACK_DYE, 12, 16, 10), new VillagerTrades.b(Items.LIGHT_BLUE_DYE, 12, 16, 10), new VillagerTrades.b(Items.LIME_DYE, 12, 16, 10), new VillagerTrades.i(Blocks.WHITE_WOOL, 1, 1, 16, 5), new VillagerTrades.i(Blocks.ORANGE_WOOL, 1, 1, 16, 5), new VillagerTrades.i(Blocks.MAGENTA_WOOL, 1, 1, 16, 5), new VillagerTrades.i(Blocks.LIGHT_BLUE_WOOL, 1, 1, 16, 5), new VillagerTrades.i(Blocks.YELLOW_WOOL, 1, 1, 16, 5), new VillagerTrades.i(Blocks.LIME_WOOL, 1, 1, 16, 5), new VillagerTrades.i(Blocks.PINK_WOOL, 1, 1, 16, 5), new VillagerTrades.i(Blocks.GRAY_WOOL, 1, 1, 16, 5), new VillagerTrades.i(Blocks.LIGHT_GRAY_WOOL, 1, 1, 16, 5), new VillagerTrades.i(Blocks.CYAN_WOOL, 1, 1, 16, 5), new VillagerTrades.i(Blocks.PURPLE_WOOL, 1, 1, 16, 5), new VillagerTrades.i(Blocks.BLUE_WOOL, 1, 1, 16, 5), new VillagerTrades.i(Blocks.BROWN_WOOL, 1, 1, 16, 5), new VillagerTrades.i(Blocks.GREEN_WOOL, 1, 1, 16, 5), new VillagerTrades.i(Blocks.RED_WOOL, 1, 1, 16, 5), new VillagerTrades.i(Blocks.BLACK_WOOL, 1, 1, 16, 5), new VillagerTrades.i(Blocks.WHITE_CARPET, 1, 4, 16, 5), new VillagerTrades.i(Blocks.ORANGE_CARPET, 1, 4, 16, 5), new VillagerTrades.i(Blocks.MAGENTA_CARPET, 1, 4, 16, 5), new VillagerTrades.i(Blocks.LIGHT_BLUE_CARPET, 1, 4, 16, 5), new VillagerTrades.i(Blocks.YELLOW_CARPET, 1, 4, 16, 5), new VillagerTrades.i(Blocks.LIME_CARPET, 1, 4, 16, 5), new VillagerTrades.i(Blocks.PINK_CARPET, 1, 4, 16, 5), new VillagerTrades.i(Blocks.GRAY_CARPET, 1, 4, 16, 5), new VillagerTrades.i(Blocks.LIGHT_GRAY_CARPET, 1, 4, 16, 5), new VillagerTrades.i(Blocks.CYAN_CARPET, 1, 4, 16, 5), new VillagerTrades.i(Blocks.PURPLE_CARPET, 1, 4, 16, 5), new VillagerTrades.i(Blocks.BLUE_CARPET, 1, 4, 16, 5), new VillagerTrades.i(Blocks.BROWN_CARPET, 1, 4, 16, 5), new VillagerTrades.i(Blocks.GREEN_CARPET, 1, 4, 16, 5), new VillagerTrades.i(Blocks.RED_CARPET, 1, 4, 16, 5), new VillagerTrades.i(Blocks.BLACK_CARPET, 1, 4, 16, 5)}, 3, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.YELLOW_DYE, 12, 16, 20), new VillagerTrades.b(Items.LIGHT_GRAY_DYE, 12, 16, 20), new VillagerTrades.b(Items.ORANGE_DYE, 12, 16, 20), new VillagerTrades.b(Items.RED_DYE, 12, 16, 20), new VillagerTrades.b(Items.PINK_DYE, 12, 16, 20), new VillagerTrades.i(Blocks.WHITE_BED, 3, 1, 12, 10), new VillagerTrades.i(Blocks.YELLOW_BED, 3, 1, 12, 10), new VillagerTrades.i(Blocks.RED_BED, 3, 1, 12, 10), new VillagerTrades.i(Blocks.BLACK_BED, 3, 1, 12, 10), new VillagerTrades.i(Blocks.BLUE_BED, 3, 1, 12, 10), new VillagerTrades.i(Blocks.BROWN_BED, 3, 1, 12, 10), new VillagerTrades.i(Blocks.CYAN_BED, 3, 1, 12, 10), new VillagerTrades.i(Blocks.GRAY_BED, 3, 1, 12, 10), new VillagerTrades.i(Blocks.GREEN_BED, 3, 1, 12, 10), new VillagerTrades.i(Blocks.LIGHT_BLUE_BED, 3, 1, 12, 10), new VillagerTrades.i(Blocks.LIGHT_GRAY_BED, 3, 1, 12, 10), new VillagerTrades.i(Blocks.LIME_BED, 3, 1, 12, 10), new VillagerTrades.i(Blocks.MAGENTA_BED, 3, 1, 12, 10), new VillagerTrades.i(Blocks.ORANGE_BED, 3, 1, 12, 10), new VillagerTrades.i(Blocks.PINK_BED, 3, 1, 12, 10), new VillagerTrades.i(Blocks.PURPLE_BED, 3, 1, 12, 10)}, 4, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.BROWN_DYE, 12, 16, 30), new VillagerTrades.b(Items.PURPLE_DYE, 12, 16, 30), new VillagerTrades.b(Items.BLUE_DYE, 12, 16, 30), new VillagerTrades.b(Items.GREEN_DYE, 12, 16, 30), new VillagerTrades.b(Items.MAGENTA_DYE, 12, 16, 30), new VillagerTrades.b(Items.CYAN_DYE, 12, 16, 30), new VillagerTrades.i(Items.WHITE_BANNER, 3, 1, 12, 15), new VillagerTrades.i(Items.BLUE_BANNER, 3, 1, 12, 15), new VillagerTrades.i(Items.LIGHT_BLUE_BANNER, 3, 1, 12, 15), new VillagerTrades.i(Items.RED_BANNER, 3, 1, 12, 15), new VillagerTrades.i(Items.PINK_BANNER, 3, 1, 12, 15), new VillagerTrades.i(Items.GREEN_BANNER, 3, 1, 12, 15), new VillagerTrades.i(Items.LIME_BANNER, 3, 1, 12, 15), new VillagerTrades.i(Items.GRAY_BANNER, 3, 1, 12, 15), new VillagerTrades.i(Items.BLACK_BANNER, 3, 1, 12, 15), new VillagerTrades.i(Items.PURPLE_BANNER, 3, 1, 12, 15), new VillagerTrades.i(Items.MAGENTA_BANNER, 3, 1, 12, 15), new VillagerTrades.i(Items.CYAN_BANNER, 3, 1, 12, 15), new VillagerTrades.i(Items.BROWN_BANNER, 3, 1, 12, 15), new VillagerTrades.i(Items.YELLOW_BANNER, 3, 1, 12, 15), new VillagerTrades.i(Items.ORANGE_BANNER, 3, 1, 12, 15), new VillagerTrades.i(Items.LIGHT_GRAY_BANNER, 3, 1, 12, 15)}, 5, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.i(Items.PAINTING, 2, 3, 30)})));
        hashmap.put(VillagerProfession.FLETCHER, toIntMap(ImmutableMap.of(1, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.STICK, 32, 16, 2), new VillagerTrades.i(Items.ARROW, 1, 16, 1), new VillagerTrades.h(Blocks.GRAVEL, 10, 1, Items.FLINT, 10, 12, 1, 0.05F)}, 2, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.FLINT, 26, 12, 10), new VillagerTrades.i(Items.BOW, 2, 1, 5)}, 3, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.STRING, 14, 16, 20), new VillagerTrades.i(Items.CROSSBOW, 3, 1, 10)}, 4, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.FEATHER, 24, 16, 30), new VillagerTrades.e(Items.BOW, 2, 3, 15)}, 5, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.TRIPWIRE_HOOK, 8, 12, 30), new VillagerTrades.e(Items.CROSSBOW, 3, 3, 15), new VillagerTrades.k(Items.ARROW, 5, Items.TIPPED_ARROW, 5, 2, 12, 30)})));
        hashmap.put(VillagerProfession.LIBRARIAN, toIntMap(ImmutableMap.builder().put(1, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.PAPER, 24, 16, 2), new VillagerTrades.d(1), new VillagerTrades.i(Blocks.BOOKSHELF, 9, 1, 12, 1)}).put(2, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.BOOK, 4, 12, 10), new VillagerTrades.d(5), new VillagerTrades.i(Items.LANTERN, 1, 1, 5)}).put(3, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.INK_SAC, 5, 12, 20), new VillagerTrades.d(10), new VillagerTrades.i(Items.GLASS, 1, 4, 10)}).put(4, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.WRITABLE_BOOK, 2, 12, 30), new VillagerTrades.d(15), new VillagerTrades.i(Items.CLOCK, 5, 1, 15), new VillagerTrades.i(Items.COMPASS, 4, 1, 15)}).put(5, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.i(Items.NAME_TAG, 20, 1, 30)}).build()));
        hashmap.put(VillagerProfession.CARTOGRAPHER, toIntMap(ImmutableMap.of(1, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.PAPER, 24, 16, 2), new VillagerTrades.i(Items.MAP, 7, 1, 1)}, 2, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.GLASS_PANE, 11, 16, 10), new VillagerTrades.l(13, StructureTags.ON_OCEAN_EXPLORER_MAPS, "filled_map.monument", MapDecorationTypes.OCEAN_MONUMENT, 12, 5)}, 3, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.COMPASS, 1, 12, 20), new VillagerTrades.l(14, StructureTags.ON_WOODLAND_EXPLORER_MAPS, "filled_map.mansion", MapDecorationTypes.WOODLAND_MANSION, 12, 10), new VillagerTrades.l(12, StructureTags.ON_TRIAL_CHAMBERS_MAPS, "filled_map.trial_chambers", MapDecorationTypes.TRIAL_CHAMBERS, 12, 10)}, 4, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.i(Items.ITEM_FRAME, 7, 1, 15), new VillagerTrades.i(Items.WHITE_BANNER, 3, 1, 15), new VillagerTrades.i(Items.BLUE_BANNER, 3, 1, 15), new VillagerTrades.i(Items.LIGHT_BLUE_BANNER, 3, 1, 15), new VillagerTrades.i(Items.RED_BANNER, 3, 1, 15), new VillagerTrades.i(Items.PINK_BANNER, 3, 1, 15), new VillagerTrades.i(Items.GREEN_BANNER, 3, 1, 15), new VillagerTrades.i(Items.LIME_BANNER, 3, 1, 15), new VillagerTrades.i(Items.GRAY_BANNER, 3, 1, 15), new VillagerTrades.i(Items.BLACK_BANNER, 3, 1, 15), new VillagerTrades.i(Items.PURPLE_BANNER, 3, 1, 15), new VillagerTrades.i(Items.MAGENTA_BANNER, 3, 1, 15), new VillagerTrades.i(Items.CYAN_BANNER, 3, 1, 15), new VillagerTrades.i(Items.BROWN_BANNER, 3, 1, 15), new VillagerTrades.i(Items.YELLOW_BANNER, 3, 1, 15), new VillagerTrades.i(Items.ORANGE_BANNER, 3, 1, 15), new VillagerTrades.i(Items.LIGHT_GRAY_BANNER, 3, 1, 15)}, 5, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.i(Items.GLOBE_BANNER_PATTERN, 8, 1, 30)})));
        hashmap.put(VillagerProfession.CLERIC, toIntMap(ImmutableMap.of(1, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.ROTTEN_FLESH, 32, 16, 2), new VillagerTrades.i(Items.REDSTONE, 1, 2, 1)}, 2, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.GOLD_INGOT, 3, 12, 10), new VillagerTrades.i(Items.LAPIS_LAZULI, 1, 1, 5)}, 3, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.RABBIT_FOOT, 2, 12, 20), new VillagerTrades.i(Blocks.GLOWSTONE, 4, 1, 12, 10)}, 4, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.TURTLE_SCUTE, 4, 12, 30), new VillagerTrades.b(Items.GLASS_BOTTLE, 9, 12, 30), new VillagerTrades.i(Items.ENDER_PEARL, 5, 1, 15)}, 5, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.NETHER_WART, 22, 12, 30), new VillagerTrades.i(Items.EXPERIENCE_BOTTLE, 3, 1, 30)})));
        hashmap.put(VillagerProfession.ARMORER, toIntMap(ImmutableMap.of(1, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.COAL, 15, 16, 2), new VillagerTrades.i(new ItemStack(Items.IRON_LEGGINGS), 7, 1, 12, 1, 0.2F), new VillagerTrades.i(new ItemStack(Items.IRON_BOOTS), 4, 1, 12, 1, 0.2F), new VillagerTrades.i(new ItemStack(Items.IRON_HELMET), 5, 1, 12, 1, 0.2F), new VillagerTrades.i(new ItemStack(Items.IRON_CHESTPLATE), 9, 1, 12, 1, 0.2F)}, 2, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.IRON_INGOT, 4, 12, 10), new VillagerTrades.i(new ItemStack(Items.BELL), 36, 1, 12, 5, 0.2F), new VillagerTrades.i(new ItemStack(Items.CHAINMAIL_BOOTS), 1, 1, 12, 5, 0.2F), new VillagerTrades.i(new ItemStack(Items.CHAINMAIL_LEGGINGS), 3, 1, 12, 5, 0.2F)}, 3, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.LAVA_BUCKET, 1, 12, 20), new VillagerTrades.b(Items.DIAMOND, 1, 12, 20), new VillagerTrades.i(new ItemStack(Items.CHAINMAIL_HELMET), 1, 1, 12, 10, 0.2F), new VillagerTrades.i(new ItemStack(Items.CHAINMAIL_CHESTPLATE), 4, 1, 12, 10, 0.2F), new VillagerTrades.i(new ItemStack(Items.SHIELD), 5, 1, 12, 10, 0.2F)}, 4, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.e(Items.DIAMOND_LEGGINGS, 14, 3, 15, 0.2F), new VillagerTrades.e(Items.DIAMOND_BOOTS, 8, 3, 15, 0.2F)}, 5, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.e(Items.DIAMOND_HELMET, 8, 3, 30, 0.2F), new VillagerTrades.e(Items.DIAMOND_CHESTPLATE, 16, 3, 30, 0.2F)})));
        hashmap.put(VillagerProfession.WEAPONSMITH, toIntMap(ImmutableMap.of(1, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.COAL, 15, 16, 2), new VillagerTrades.i(new ItemStack(Items.IRON_AXE), 3, 1, 12, 1, 0.2F), new VillagerTrades.e(Items.IRON_SWORD, 2, 3, 1)}, 2, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.IRON_INGOT, 4, 12, 10), new VillagerTrades.i(new ItemStack(Items.BELL), 36, 1, 12, 5, 0.2F)}, 3, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.FLINT, 24, 12, 20)}, 4, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.DIAMOND, 1, 12, 30), new VillagerTrades.e(Items.DIAMOND_AXE, 12, 3, 15, 0.2F)}, 5, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.e(Items.DIAMOND_SWORD, 8, 3, 30, 0.2F)})));
        hashmap.put(VillagerProfession.TOOLSMITH, toIntMap(ImmutableMap.of(1, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.COAL, 15, 16, 2), new VillagerTrades.i(new ItemStack(Items.STONE_AXE), 1, 1, 12, 1, 0.2F), new VillagerTrades.i(new ItemStack(Items.STONE_SHOVEL), 1, 1, 12, 1, 0.2F), new VillagerTrades.i(new ItemStack(Items.STONE_PICKAXE), 1, 1, 12, 1, 0.2F), new VillagerTrades.i(new ItemStack(Items.STONE_HOE), 1, 1, 12, 1, 0.2F)}, 2, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.IRON_INGOT, 4, 12, 10), new VillagerTrades.i(new ItemStack(Items.BELL), 36, 1, 12, 5, 0.2F)}, 3, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.FLINT, 30, 12, 20), new VillagerTrades.e(Items.IRON_AXE, 1, 3, 10, 0.2F), new VillagerTrades.e(Items.IRON_SHOVEL, 2, 3, 10, 0.2F), new VillagerTrades.e(Items.IRON_PICKAXE, 3, 3, 10, 0.2F), new VillagerTrades.i(new ItemStack(Items.DIAMOND_HOE), 4, 1, 3, 10, 0.2F)}, 4, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.DIAMOND, 1, 12, 30), new VillagerTrades.e(Items.DIAMOND_AXE, 12, 3, 15, 0.2F), new VillagerTrades.e(Items.DIAMOND_SHOVEL, 5, 3, 15, 0.2F)}, 5, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.e(Items.DIAMOND_PICKAXE, 13, 3, 30, 0.2F)})));
        hashmap.put(VillagerProfession.BUTCHER, toIntMap(ImmutableMap.of(1, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.CHICKEN, 14, 16, 2), new VillagerTrades.b(Items.PORKCHOP, 7, 16, 2), new VillagerTrades.b(Items.RABBIT, 4, 16, 2), new VillagerTrades.i(Items.RABBIT_STEW, 1, 1, 1)}, 2, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.COAL, 15, 16, 2), new VillagerTrades.i(Items.COOKED_PORKCHOP, 1, 5, 16, 5), new VillagerTrades.i(Items.COOKED_CHICKEN, 1, 8, 16, 5)}, 3, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.MUTTON, 7, 16, 20), new VillagerTrades.b(Items.BEEF, 10, 16, 20)}, 4, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.DRIED_KELP_BLOCK, 10, 12, 30)}, 5, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.SWEET_BERRIES, 10, 12, 30)})));
        hashmap.put(VillagerProfession.LEATHERWORKER, toIntMap(ImmutableMap.of(1, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.LEATHER, 6, 16, 2), new VillagerTrades.a(Items.LEATHER_LEGGINGS, 3), new VillagerTrades.a(Items.LEATHER_CHESTPLATE, 7)}, 2, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.FLINT, 26, 12, 10), new VillagerTrades.a(Items.LEATHER_HELMET, 5, 12, 5), new VillagerTrades.a(Items.LEATHER_BOOTS, 4, 12, 5)}, 3, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.RABBIT_HIDE, 9, 12, 20), new VillagerTrades.a(Items.LEATHER_CHESTPLATE, 7)}, 4, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.TURTLE_SCUTE, 4, 12, 30), new VillagerTrades.a(Items.LEATHER_HORSE_ARMOR, 6, 12, 15)}, 5, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.i(new ItemStack(Items.SADDLE), 6, 1, 12, 30, 0.2F), new VillagerTrades.a(Items.LEATHER_HELMET, 5, 12, 30)})));
        hashmap.put(VillagerProfession.MASON, toIntMap(ImmutableMap.of(1, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.CLAY_BALL, 10, 16, 2), new VillagerTrades.i(Items.BRICK, 1, 10, 16, 1)}, 2, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Blocks.STONE, 20, 16, 10), new VillagerTrades.i(Blocks.CHISELED_STONE_BRICKS, 1, 4, 16, 5)}, 3, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Blocks.GRANITE, 16, 16, 20), new VillagerTrades.b(Blocks.ANDESITE, 16, 16, 20), new VillagerTrades.b(Blocks.DIORITE, 16, 16, 20), new VillagerTrades.i(Blocks.DRIPSTONE_BLOCK, 1, 4, 16, 10), new VillagerTrades.i(Blocks.POLISHED_ANDESITE, 1, 4, 16, 10), new VillagerTrades.i(Blocks.POLISHED_DIORITE, 1, 4, 16, 10), new VillagerTrades.i(Blocks.POLISHED_GRANITE, 1, 4, 16, 10)}, 4, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.QUARTZ, 12, 12, 30), new VillagerTrades.i(Blocks.ORANGE_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.i(Blocks.WHITE_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.i(Blocks.BLUE_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.i(Blocks.LIGHT_BLUE_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.i(Blocks.GRAY_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.i(Blocks.LIGHT_GRAY_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.i(Blocks.BLACK_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.i(Blocks.RED_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.i(Blocks.PINK_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.i(Blocks.MAGENTA_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.i(Blocks.LIME_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.i(Blocks.GREEN_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.i(Blocks.CYAN_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.i(Blocks.PURPLE_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.i(Blocks.YELLOW_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.i(Blocks.BROWN_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.i(Blocks.ORANGE_GLAZED_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.i(Blocks.WHITE_GLAZED_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.i(Blocks.BLUE_GLAZED_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.i(Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.i(Blocks.GRAY_GLAZED_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.i(Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.i(Blocks.BLACK_GLAZED_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.i(Blocks.RED_GLAZED_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.i(Blocks.PINK_GLAZED_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.i(Blocks.MAGENTA_GLAZED_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.i(Blocks.LIME_GLAZED_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.i(Blocks.GREEN_GLAZED_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.i(Blocks.CYAN_GLAZED_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.i(Blocks.PURPLE_GLAZED_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.i(Blocks.YELLOW_GLAZED_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.i(Blocks.BROWN_GLAZED_TERRACOTTA, 1, 1, 12, 15)}, 5, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.i(Blocks.QUARTZ_PILLAR, 1, 1, 12, 30), new VillagerTrades.i(Blocks.QUARTZ_BLOCK, 1, 1, 12, 30)})));
    });
    public static final Int2ObjectMap<VillagerTrades.IMerchantRecipeOption[]> WANDERING_TRADER_TRADES = toIntMap(ImmutableMap.of(1, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.i(Items.SEA_PICKLE, 2, 1, 5, 1), new VillagerTrades.i(Items.SLIME_BALL, 4, 1, 5, 1), new VillagerTrades.i(Items.GLOWSTONE, 2, 1, 5, 1), new VillagerTrades.i(Items.NAUTILUS_SHELL, 5, 1, 5, 1), new VillagerTrades.i(Items.FERN, 1, 1, 12, 1), new VillagerTrades.i(Items.SUGAR_CANE, 1, 1, 8, 1), new VillagerTrades.i(Items.PUMPKIN, 1, 1, 4, 1), new VillagerTrades.i(Items.KELP, 3, 1, 12, 1), new VillagerTrades.i(Items.CACTUS, 3, 1, 8, 1), new VillagerTrades.i(Items.DANDELION, 1, 1, 12, 1), new VillagerTrades.i(Items.POPPY, 1, 1, 12, 1), new VillagerTrades.i(Items.BLUE_ORCHID, 1, 1, 8, 1), new VillagerTrades.i(Items.ALLIUM, 1, 1, 12, 1), new VillagerTrades.i(Items.AZURE_BLUET, 1, 1, 12, 1), new VillagerTrades.i(Items.RED_TULIP, 1, 1, 12, 1), new VillagerTrades.i(Items.ORANGE_TULIP, 1, 1, 12, 1), new VillagerTrades.i(Items.WHITE_TULIP, 1, 1, 12, 1), new VillagerTrades.i(Items.PINK_TULIP, 1, 1, 12, 1), new VillagerTrades.i(Items.OXEYE_DAISY, 1, 1, 12, 1), new VillagerTrades.i(Items.CORNFLOWER, 1, 1, 12, 1), new VillagerTrades.i(Items.LILY_OF_THE_VALLEY, 1, 1, 7, 1), new VillagerTrades.i(Items.WHEAT_SEEDS, 1, 1, 12, 1), new VillagerTrades.i(Items.BEETROOT_SEEDS, 1, 1, 12, 1), new VillagerTrades.i(Items.PUMPKIN_SEEDS, 1, 1, 12, 1), new VillagerTrades.i(Items.MELON_SEEDS, 1, 1, 12, 1), new VillagerTrades.i(Items.ACACIA_SAPLING, 5, 1, 8, 1), new VillagerTrades.i(Items.BIRCH_SAPLING, 5, 1, 8, 1), new VillagerTrades.i(Items.DARK_OAK_SAPLING, 5, 1, 8, 1), new VillagerTrades.i(Items.JUNGLE_SAPLING, 5, 1, 8, 1), new VillagerTrades.i(Items.OAK_SAPLING, 5, 1, 8, 1), new VillagerTrades.i(Items.SPRUCE_SAPLING, 5, 1, 8, 1), new VillagerTrades.i(Items.CHERRY_SAPLING, 5, 1, 8, 1), new VillagerTrades.i(Items.MANGROVE_PROPAGULE, 5, 1, 8, 1), new VillagerTrades.i(Items.RED_DYE, 1, 3, 12, 1), new VillagerTrades.i(Items.WHITE_DYE, 1, 3, 12, 1), new VillagerTrades.i(Items.BLUE_DYE, 1, 3, 12, 1), new VillagerTrades.i(Items.PINK_DYE, 1, 3, 12, 1), new VillagerTrades.i(Items.BLACK_DYE, 1, 3, 12, 1), new VillagerTrades.i(Items.GREEN_DYE, 1, 3, 12, 1), new VillagerTrades.i(Items.LIGHT_GRAY_DYE, 1, 3, 12, 1), new VillagerTrades.i(Items.MAGENTA_DYE, 1, 3, 12, 1), new VillagerTrades.i(Items.YELLOW_DYE, 1, 3, 12, 1), new VillagerTrades.i(Items.GRAY_DYE, 1, 3, 12, 1), new VillagerTrades.i(Items.PURPLE_DYE, 1, 3, 12, 1), new VillagerTrades.i(Items.LIGHT_BLUE_DYE, 1, 3, 12, 1), new VillagerTrades.i(Items.LIME_DYE, 1, 3, 12, 1), new VillagerTrades.i(Items.ORANGE_DYE, 1, 3, 12, 1), new VillagerTrades.i(Items.BROWN_DYE, 1, 3, 12, 1), new VillagerTrades.i(Items.CYAN_DYE, 1, 3, 12, 1), new VillagerTrades.i(Items.BRAIN_CORAL_BLOCK, 3, 1, 8, 1), new VillagerTrades.i(Items.BUBBLE_CORAL_BLOCK, 3, 1, 8, 1), new VillagerTrades.i(Items.FIRE_CORAL_BLOCK, 3, 1, 8, 1), new VillagerTrades.i(Items.HORN_CORAL_BLOCK, 3, 1, 8, 1), new VillagerTrades.i(Items.TUBE_CORAL_BLOCK, 3, 1, 8, 1), new VillagerTrades.i(Items.VINE, 1, 1, 12, 1), new VillagerTrades.i(Items.BROWN_MUSHROOM, 1, 1, 12, 1), new VillagerTrades.i(Items.RED_MUSHROOM, 1, 1, 12, 1), new VillagerTrades.i(Items.LILY_PAD, 1, 2, 5, 1), new VillagerTrades.i(Items.SMALL_DRIPLEAF, 1, 2, 5, 1), new VillagerTrades.i(Items.SAND, 1, 8, 8, 1), new VillagerTrades.i(Items.RED_SAND, 1, 4, 6, 1), new VillagerTrades.i(Items.POINTED_DRIPSTONE, 1, 2, 5, 1), new VillagerTrades.i(Items.ROOTED_DIRT, 1, 2, 5, 1), new VillagerTrades.i(Items.MOSS_BLOCK, 1, 2, 5, 1)}, 2, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.i(Items.TROPICAL_FISH_BUCKET, 5, 1, 4, 1), new VillagerTrades.i(Items.PUFFERFISH_BUCKET, 5, 1, 4, 1), new VillagerTrades.i(Items.PACKED_ICE, 3, 1, 6, 1), new VillagerTrades.i(Items.BLUE_ICE, 6, 1, 6, 1), new VillagerTrades.i(Items.GUNPOWDER, 1, 1, 8, 1), new VillagerTrades.i(Items.PODZOL, 3, 3, 6, 1)}));
    private static final VillagerTrades.l DESERT_MAP = new VillagerTrades.l(8, StructureTags.ON_DESERT_VILLAGE_MAPS, "filled_map.village_desert", MapDecorationTypes.DESERT_VILLAGE, 12, 5);
    private static final VillagerTrades.l SAVANNA_MAP = new VillagerTrades.l(8, StructureTags.ON_SAVANNA_VILLAGE_MAPS, "filled_map.village_savanna", MapDecorationTypes.SAVANNA_VILLAGE, 12, 5);
    private static final VillagerTrades.l PLAINS_MAP = new VillagerTrades.l(8, StructureTags.ON_PLAINS_VILLAGE_MAPS, "filled_map.village_plains", MapDecorationTypes.PLAINS_VILLAGE, 12, 5);
    private static final VillagerTrades.l TAIGA_MAP = new VillagerTrades.l(8, StructureTags.ON_TAIGA_VILLAGE_MAPS, "filled_map.village_taiga", MapDecorationTypes.TAIGA_VILLAGE, 12, 5);
    private static final VillagerTrades.l SNOWY_MAP = new VillagerTrades.l(8, StructureTags.ON_SNOWY_VILLAGE_MAPS, "filled_map.village_snowy", MapDecorationTypes.SNOWY_VILLAGE, 12, 5);
    private static final VillagerTrades.l JUNGLE_MAP = new VillagerTrades.l(8, StructureTags.ON_JUNGLE_EXPLORER_MAPS, "filled_map.explorer_jungle", MapDecorationTypes.JUNGLE_TEMPLE, 12, 5);
    private static final VillagerTrades.l SWAMP_MAP = new VillagerTrades.l(8, StructureTags.ON_SWAMP_EXPLORER_MAPS, "filled_map.explorer_swamp", MapDecorationTypes.SWAMP_HUT, 12, 5);
    public static final Map<VillagerProfession, Int2ObjectMap<VillagerTrades.IMerchantRecipeOption[]>> EXPERIMENTAL_TRADES = Map.of(VillagerProfession.LIBRARIAN, toIntMap(ImmutableMap.builder().put(1, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.PAPER, 24, 16, 2), commonBooks(1), new VillagerTrades.i(Blocks.BOOKSHELF, 9, 1, 12, 1)}).put(2, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.BOOK, 4, 12, 10), commonBooks(5), new VillagerTrades.i(Items.LANTERN, 1, 1, 5)}).put(3, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.INK_SAC, 5, 12, 20), commonBooks(10), new VillagerTrades.i(Items.GLASS, 1, 4, 10)}).put(4, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.WRITABLE_BOOK, 2, 12, 30), new VillagerTrades.i(Items.CLOCK, 5, 1, 15), new VillagerTrades.i(Items.COMPASS, 4, 1, 15)}).put(5, new VillagerTrades.IMerchantRecipeOption[]{specialBooks(), new VillagerTrades.i(Items.NAME_TAG, 20, 1, 30)}).build()), VillagerProfession.ARMORER, toIntMap(ImmutableMap.builder().put(1, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.COAL, 15, 12, 2), new VillagerTrades.b(Items.IRON_INGOT, 5, 12, 2)}).put(2, new VillagerTrades.IMerchantRecipeOption[]{VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.i(Items.IRON_BOOTS, 4, 1, 12, 5, 0.05F), VillagerType.DESERT, VillagerType.PLAINS, VillagerType.SAVANNA, VillagerType.SNOW, VillagerType.TAIGA), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.i(Items.CHAINMAIL_BOOTS, 4, 1, 12, 5, 0.05F), VillagerType.JUNGLE, VillagerType.SWAMP), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.i(Items.IRON_HELMET, 5, 1, 12, 5, 0.05F), VillagerType.DESERT, VillagerType.PLAINS, VillagerType.SAVANNA, VillagerType.SNOW, VillagerType.TAIGA), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.i(Items.CHAINMAIL_HELMET, 5, 1, 12, 5, 0.05F), VillagerType.JUNGLE, VillagerType.SWAMP), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.i(Items.IRON_LEGGINGS, 7, 1, 12, 5, 0.05F), VillagerType.DESERT, VillagerType.PLAINS, VillagerType.SAVANNA, VillagerType.SNOW, VillagerType.TAIGA), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.i(Items.CHAINMAIL_LEGGINGS, 7, 1, 12, 5, 0.05F), VillagerType.JUNGLE, VillagerType.SWAMP), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.i(Items.IRON_CHESTPLATE, 9, 1, 12, 5, 0.05F), VillagerType.DESERT, VillagerType.PLAINS, VillagerType.SAVANNA, VillagerType.SNOW, VillagerType.TAIGA), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.i(Items.CHAINMAIL_CHESTPLATE, 9, 1, 12, 5, 0.05F), VillagerType.JUNGLE, VillagerType.SWAMP)}).put(3, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.LAVA_BUCKET, 1, 12, 20), new VillagerTrades.i(Items.SHIELD, 5, 1, 12, 10, 0.05F), new VillagerTrades.i(Items.BELL, 36, 1, 12, 10, 0.2F)}).put(4, new VillagerTrades.IMerchantRecipeOption[]{VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.i(enchant(Items.IRON_BOOTS, Enchantments.THORNS, 1), 8, 1, 3, 15, 0.05F), VillagerType.DESERT), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.i(enchant(Items.IRON_HELMET, Enchantments.THORNS, 1), 9, 1, 3, 15, 0.05F), VillagerType.DESERT), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.i(enchant(Items.IRON_LEGGINGS, Enchantments.THORNS, 1), 11, 1, 3, 15, 0.05F), VillagerType.DESERT), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.i(enchant(Items.IRON_CHESTPLATE, Enchantments.THORNS, 1), 13, 1, 3, 15, 0.05F), VillagerType.DESERT), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.i(enchant(Items.IRON_BOOTS, Enchantments.PROTECTION, 1), 8, 1, 3, 15, 0.05F), VillagerType.PLAINS), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.i(enchant(Items.IRON_HELMET, Enchantments.PROTECTION, 1), 9, 1, 3, 15, 0.05F), VillagerType.PLAINS), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.i(enchant(Items.IRON_LEGGINGS, Enchantments.PROTECTION, 1), 11, 1, 3, 15, 0.05F), VillagerType.PLAINS), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.i(enchant(Items.IRON_CHESTPLATE, Enchantments.PROTECTION, 1), 13, 1, 3, 15, 0.05F), VillagerType.PLAINS), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.i(enchant(Items.IRON_BOOTS, Enchantments.BINDING_CURSE, 1), 2, 1, 3, 15, 0.05F), VillagerType.SAVANNA), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.i(enchant(Items.IRON_HELMET, Enchantments.BINDING_CURSE, 1), 3, 1, 3, 15, 0.05F), VillagerType.SAVANNA), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.i(enchant(Items.IRON_LEGGINGS, Enchantments.BINDING_CURSE, 1), 5, 1, 3, 15, 0.05F), VillagerType.SAVANNA), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.i(enchant(Items.IRON_CHESTPLATE, Enchantments.BINDING_CURSE, 1), 7, 1, 3, 15, 0.05F), VillagerType.SAVANNA), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.i(enchant(Items.IRON_BOOTS, Enchantments.FROST_WALKER, 1), 8, 1, 3, 15, 0.05F), VillagerType.SNOW), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.i(enchant(Items.IRON_HELMET, Enchantments.AQUA_AFFINITY, 1), 9, 1, 3, 15, 0.05F), VillagerType.SNOW), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.i(enchant(Items.CHAINMAIL_BOOTS, Enchantments.UNBREAKING, 1), 8, 1, 3, 15, 0.05F), VillagerType.JUNGLE), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.i(enchant(Items.CHAINMAIL_HELMET, Enchantments.UNBREAKING, 1), 9, 1, 3, 15, 0.05F), VillagerType.JUNGLE), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.i(enchant(Items.CHAINMAIL_LEGGINGS, Enchantments.UNBREAKING, 1), 11, 1, 3, 15, 0.05F), VillagerType.JUNGLE), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.i(enchant(Items.CHAINMAIL_CHESTPLATE, Enchantments.UNBREAKING, 1), 13, 1, 3, 15, 0.05F), VillagerType.JUNGLE), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.i(enchant(Items.CHAINMAIL_BOOTS, Enchantments.MENDING, 1), 8, 1, 3, 15, 0.05F), VillagerType.SWAMP), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.i(enchant(Items.CHAINMAIL_HELMET, Enchantments.MENDING, 1), 9, 1, 3, 15, 0.05F), VillagerType.SWAMP), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.i(enchant(Items.CHAINMAIL_LEGGINGS, Enchantments.MENDING, 1), 11, 1, 3, 15, 0.05F), VillagerType.SWAMP), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.i(enchant(Items.CHAINMAIL_CHESTPLATE, Enchantments.MENDING, 1), 13, 1, 3, 15, 0.05F), VillagerType.SWAMP), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.h(Items.DIAMOND_BOOTS, 1, 4, Items.DIAMOND_LEGGINGS, 1, 3, 15, 0.05F), VillagerType.TAIGA), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.h(Items.DIAMOND_LEGGINGS, 1, 4, Items.DIAMOND_CHESTPLATE, 1, 3, 15, 0.05F), VillagerType.TAIGA), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.h(Items.DIAMOND_HELMET, 1, 4, Items.DIAMOND_BOOTS, 1, 3, 15, 0.05F), VillagerType.TAIGA), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.h(Items.DIAMOND_CHESTPLATE, 1, 2, Items.DIAMOND_HELMET, 1, 3, 15, 0.05F), VillagerType.TAIGA)}).put(5, new VillagerTrades.IMerchantRecipeOption[]{VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.h(Items.DIAMOND, 4, 16, enchant(Items.DIAMOND_CHESTPLATE, Enchantments.THORNS, 1), 1, 3, 30, 0.05F), VillagerType.DESERT), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.h(Items.DIAMOND, 3, 16, enchant(Items.DIAMOND_LEGGINGS, Enchantments.THORNS, 1), 1, 3, 30, 0.05F), VillagerType.DESERT), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.h(Items.DIAMOND, 3, 16, enchant(Items.DIAMOND_LEGGINGS, Enchantments.PROTECTION, 1), 1, 3, 30, 0.05F), VillagerType.PLAINS), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.h(Items.DIAMOND, 2, 12, enchant(Items.DIAMOND_BOOTS, Enchantments.PROTECTION, 1), 1, 3, 30, 0.05F), VillagerType.PLAINS), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.h(Items.DIAMOND, 2, 6, enchant(Items.DIAMOND_HELMET, Enchantments.BINDING_CURSE, 1), 1, 3, 30, 0.05F), VillagerType.SAVANNA), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.h(Items.DIAMOND, 3, 8, enchant(Items.DIAMOND_CHESTPLATE, Enchantments.BINDING_CURSE, 1), 1, 3, 30, 0.05F), VillagerType.SAVANNA), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.h(Items.DIAMOND, 2, 12, enchant(Items.DIAMOND_BOOTS, Enchantments.FROST_WALKER, 1), 1, 3, 30, 0.05F), VillagerType.SNOW), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.h(Items.DIAMOND, 3, 12, enchant(Items.DIAMOND_HELMET, Enchantments.AQUA_AFFINITY, 1), 1, 3, 30, 0.05F), VillagerType.SNOW), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.i(enchant(Items.CHAINMAIL_HELMET, Enchantments.PROJECTILE_PROTECTION, 1), 9, 1, 3, 30, 0.05F), VillagerType.JUNGLE), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.i(enchant(Items.CHAINMAIL_BOOTS, Enchantments.FEATHER_FALLING, 1), 8, 1, 3, 30, 0.05F), VillagerType.JUNGLE), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.i(enchant(Items.CHAINMAIL_HELMET, Enchantments.RESPIRATION, 1), 9, 1, 3, 30, 0.05F), VillagerType.SWAMP), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.i(enchant(Items.CHAINMAIL_BOOTS, Enchantments.DEPTH_STRIDER, 1), 8, 1, 3, 30, 0.05F), VillagerType.SWAMP), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.h(Items.DIAMOND, 4, 18, enchant(Items.DIAMOND_CHESTPLATE, Enchantments.BLAST_PROTECTION, 1), 1, 3, 30, 0.05F), VillagerType.TAIGA), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.h(Items.DIAMOND, 3, 18, enchant(Items.DIAMOND_LEGGINGS, Enchantments.BLAST_PROTECTION, 1), 1, 3, 30, 0.05F), VillagerType.TAIGA), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.b(Items.DIAMOND_BLOCK, 1, 12, 30, 42), VillagerType.TAIGA), VillagerTrades.m.oneTradeInBiomes(new VillagerTrades.b(Items.IRON_BLOCK, 1, 12, 30, 4), VillagerType.DESERT, VillagerType.JUNGLE, VillagerType.PLAINS, VillagerType.SAVANNA, VillagerType.SNOW, VillagerType.SWAMP)}).build()), VillagerProfession.CARTOGRAPHER, toIntMap(ImmutableMap.of(1, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.PAPER, 24, 16, 2), new VillagerTrades.i(Items.MAP, 7, 1, 1)}, 2, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.GLASS_PANE, 11, 16, 10), new VillagerTrades.m(ImmutableMap.builder().put(VillagerType.DESERT, VillagerTrades.SAVANNA_MAP).put(VillagerType.SAVANNA, VillagerTrades.PLAINS_MAP).put(VillagerType.PLAINS, VillagerTrades.TAIGA_MAP).put(VillagerType.TAIGA, VillagerTrades.SNOWY_MAP).put(VillagerType.SNOW, VillagerTrades.PLAINS_MAP).put(VillagerType.JUNGLE, VillagerTrades.SAVANNA_MAP).put(VillagerType.SWAMP, VillagerTrades.SNOWY_MAP).build()), new VillagerTrades.m(ImmutableMap.builder().put(VillagerType.DESERT, VillagerTrades.PLAINS_MAP).put(VillagerType.SAVANNA, VillagerTrades.DESERT_MAP).put(VillagerType.PLAINS, VillagerTrades.SAVANNA_MAP).put(VillagerType.TAIGA, VillagerTrades.PLAINS_MAP).put(VillagerType.SNOW, VillagerTrades.TAIGA_MAP).put(VillagerType.JUNGLE, VillagerTrades.DESERT_MAP).put(VillagerType.SWAMP, VillagerTrades.TAIGA_MAP).build()), new VillagerTrades.m(ImmutableMap.builder().put(VillagerType.DESERT, VillagerTrades.JUNGLE_MAP).put(VillagerType.SAVANNA, VillagerTrades.JUNGLE_MAP).put(VillagerType.PLAINS, new VillagerTrades.f()).put(VillagerType.TAIGA, VillagerTrades.SWAMP_MAP).put(VillagerType.SNOW, VillagerTrades.SWAMP_MAP).put(VillagerType.JUNGLE, VillagerTrades.SWAMP_MAP).put(VillagerType.SWAMP, VillagerTrades.JUNGLE_MAP).build())}, 3, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(Items.COMPASS, 1, 12, 20), new VillagerTrades.l(13, StructureTags.ON_OCEAN_EXPLORER_MAPS, "filled_map.monument", MapDecorationTypes.OCEAN_MONUMENT, 12, 10), new VillagerTrades.l(12, StructureTags.ON_TRIAL_CHAMBERS_MAPS, "filled_map.trial_chambers", MapDecorationTypes.TRIAL_CHAMBERS, 12, 10)}, 4, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.i(Items.ITEM_FRAME, 7, 1, 15), new VillagerTrades.i(Items.WHITE_BANNER, 3, 1, 15), new VillagerTrades.i(Items.BLUE_BANNER, 3, 1, 15), new VillagerTrades.i(Items.LIGHT_BLUE_BANNER, 3, 1, 15), new VillagerTrades.i(Items.RED_BANNER, 3, 1, 15), new VillagerTrades.i(Items.PINK_BANNER, 3, 1, 15), new VillagerTrades.i(Items.GREEN_BANNER, 3, 1, 15), new VillagerTrades.i(Items.LIME_BANNER, 3, 1, 15), new VillagerTrades.i(Items.GRAY_BANNER, 3, 1, 15), new VillagerTrades.i(Items.BLACK_BANNER, 3, 1, 15), new VillagerTrades.i(Items.PURPLE_BANNER, 3, 1, 15), new VillagerTrades.i(Items.MAGENTA_BANNER, 3, 1, 15), new VillagerTrades.i(Items.CYAN_BANNER, 3, 1, 15), new VillagerTrades.i(Items.BROWN_BANNER, 3, 1, 15), new VillagerTrades.i(Items.YELLOW_BANNER, 3, 1, 15), new VillagerTrades.i(Items.ORANGE_BANNER, 3, 1, 15), new VillagerTrades.i(Items.LIGHT_GRAY_BANNER, 3, 1, 15)}, 5, new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.i(Items.GLOBE_BANNER_PATTERN, 8, 1, 30), new VillagerTrades.l(14, StructureTags.ON_WOODLAND_EXPLORER_MAPS, "filled_map.mansion", MapDecorationTypes.WOODLAND_MANSION, 1, 30)})));
    public static final List<Pair<VillagerTrades.IMerchantRecipeOption[], Integer>> EXPERIMENTAL_WANDERING_TRADER_TRADES = ImmutableList.builder().add(Pair.of(new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.b(potionCost(Potions.WATER), 1, 1, 1), new VillagerTrades.b(Items.WATER_BUCKET, 1, 1, 1, 2), new VillagerTrades.b(Items.MILK_BUCKET, 1, 1, 1, 2), new VillagerTrades.b(Items.FERMENTED_SPIDER_EYE, 1, 1, 1, 3), new VillagerTrades.b(Items.BAKED_POTATO, 4, 1, 1), new VillagerTrades.b(Items.HAY_BLOCK, 1, 1, 1)}, 2)).add(Pair.of(new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.i(Items.PACKED_ICE, 1, 1, 6, 1), new VillagerTrades.i(Items.BLUE_ICE, 6, 1, 6, 1), new VillagerTrades.i(Items.GUNPOWDER, 1, 4, 2, 1), new VillagerTrades.i(Items.PODZOL, 3, 3, 6, 1), new VillagerTrades.i(Blocks.ACACIA_LOG, 1, 8, 4, 1), new VillagerTrades.i(Blocks.BIRCH_LOG, 1, 8, 4, 1), new VillagerTrades.i(Blocks.DARK_OAK_LOG, 1, 8, 4, 1), new VillagerTrades.i(Blocks.JUNGLE_LOG, 1, 8, 4, 1), new VillagerTrades.i(Blocks.OAK_LOG, 1, 8, 4, 1), new VillagerTrades.i(Blocks.SPRUCE_LOG, 1, 8, 4, 1), new VillagerTrades.i(Blocks.CHERRY_LOG, 1, 8, 4, 1), new VillagerTrades.e(Items.IRON_PICKAXE, 1, 1, 1, 0.2F), new VillagerTrades.i(potion(Potions.LONG_INVISIBILITY), 5, 1, 1, 1)}, 2)).add(Pair.of(new VillagerTrades.IMerchantRecipeOption[]{new VillagerTrades.i(Items.TROPICAL_FISH_BUCKET, 3, 1, 4, 1), new VillagerTrades.i(Items.PUFFERFISH_BUCKET, 3, 1, 4, 1), new VillagerTrades.i(Items.SEA_PICKLE, 2, 1, 5, 1), new VillagerTrades.i(Items.SLIME_BALL, 4, 1, 5, 1), new VillagerTrades.i(Items.GLOWSTONE, 2, 1, 5, 1), new VillagerTrades.i(Items.NAUTILUS_SHELL, 5, 1, 5, 1), new VillagerTrades.i(Items.FERN, 1, 1, 12, 1), new VillagerTrades.i(Items.SUGAR_CANE, 1, 1, 8, 1), new VillagerTrades.i(Items.PUMPKIN, 1, 1, 4, 1), new VillagerTrades.i(Items.KELP, 3, 1, 12, 1), new VillagerTrades.i(Items.CACTUS, 3, 1, 8, 1), new VillagerTrades.i(Items.DANDELION, 1, 1, 12, 1), new VillagerTrades.i(Items.POPPY, 1, 1, 12, 1), new VillagerTrades.i(Items.BLUE_ORCHID, 1, 1, 8, 1), new VillagerTrades.i(Items.ALLIUM, 1, 1, 12, 1), new VillagerTrades.i(Items.AZURE_BLUET, 1, 1, 12, 1), new VillagerTrades.i(Items.RED_TULIP, 1, 1, 12, 1), new VillagerTrades.i(Items.ORANGE_TULIP, 1, 1, 12, 1), new VillagerTrades.i(Items.WHITE_TULIP, 1, 1, 12, 1), new VillagerTrades.i(Items.PINK_TULIP, 1, 1, 12, 1), new VillagerTrades.i(Items.OXEYE_DAISY, 1, 1, 12, 1), new VillagerTrades.i(Items.CORNFLOWER, 1, 1, 12, 1), new VillagerTrades.i(Items.LILY_OF_THE_VALLEY, 1, 1, 7, 1), new VillagerTrades.i(Items.WHEAT_SEEDS, 1, 1, 12, 1), new VillagerTrades.i(Items.BEETROOT_SEEDS, 1, 1, 12, 1), new VillagerTrades.i(Items.PUMPKIN_SEEDS, 1, 1, 12, 1), new VillagerTrades.i(Items.MELON_SEEDS, 1, 1, 12, 1), new VillagerTrades.i(Items.ACACIA_SAPLING, 5, 1, 8, 1), new VillagerTrades.i(Items.BIRCH_SAPLING, 5, 1, 8, 1), new VillagerTrades.i(Items.DARK_OAK_SAPLING, 5, 1, 8, 1), new VillagerTrades.i(Items.JUNGLE_SAPLING, 5, 1, 8, 1), new VillagerTrades.i(Items.OAK_SAPLING, 5, 1, 8, 1), new VillagerTrades.i(Items.SPRUCE_SAPLING, 5, 1, 8, 1), new VillagerTrades.i(Items.CHERRY_SAPLING, 5, 1, 8, 1), new VillagerTrades.i(Items.MANGROVE_PROPAGULE, 5, 1, 8, 1), new VillagerTrades.i(Items.RED_DYE, 1, 3, 12, 1), new VillagerTrades.i(Items.WHITE_DYE, 1, 3, 12, 1), new VillagerTrades.i(Items.BLUE_DYE, 1, 3, 12, 1), new VillagerTrades.i(Items.PINK_DYE, 1, 3, 12, 1), new VillagerTrades.i(Items.BLACK_DYE, 1, 3, 12, 1), new VillagerTrades.i(Items.GREEN_DYE, 1, 3, 12, 1), new VillagerTrades.i(Items.LIGHT_GRAY_DYE, 1, 3, 12, 1), new VillagerTrades.i(Items.MAGENTA_DYE, 1, 3, 12, 1), new VillagerTrades.i(Items.YELLOW_DYE, 1, 3, 12, 1), new VillagerTrades.i(Items.GRAY_DYE, 1, 3, 12, 1), new VillagerTrades.i(Items.PURPLE_DYE, 1, 3, 12, 1), new VillagerTrades.i(Items.LIGHT_BLUE_DYE, 1, 3, 12, 1), new VillagerTrades.i(Items.LIME_DYE, 1, 3, 12, 1), new VillagerTrades.i(Items.ORANGE_DYE, 1, 3, 12, 1), new VillagerTrades.i(Items.BROWN_DYE, 1, 3, 12, 1), new VillagerTrades.i(Items.CYAN_DYE, 1, 3, 12, 1), new VillagerTrades.i(Items.BRAIN_CORAL_BLOCK, 3, 1, 8, 1), new VillagerTrades.i(Items.BUBBLE_CORAL_BLOCK, 3, 1, 8, 1), new VillagerTrades.i(Items.FIRE_CORAL_BLOCK, 3, 1, 8, 1), new VillagerTrades.i(Items.HORN_CORAL_BLOCK, 3, 1, 8, 1), new VillagerTrades.i(Items.TUBE_CORAL_BLOCK, 3, 1, 8, 1), new VillagerTrades.i(Items.VINE, 1, 3, 4, 1), new VillagerTrades.i(Items.BROWN_MUSHROOM, 1, 3, 4, 1), new VillagerTrades.i(Items.RED_MUSHROOM, 1, 3, 4, 1), new VillagerTrades.i(Items.LILY_PAD, 1, 5, 2, 1), new VillagerTrades.i(Items.SMALL_DRIPLEAF, 1, 2, 5, 1), new VillagerTrades.i(Items.SAND, 1, 8, 8, 1), new VillagerTrades.i(Items.RED_SAND, 1, 4, 6, 1), new VillagerTrades.i(Items.POINTED_DRIPSTONE, 1, 2, 5, 1), new VillagerTrades.i(Items.ROOTED_DIRT, 1, 2, 5, 1), new VillagerTrades.i(Items.MOSS_BLOCK, 1, 2, 5, 1)}, 5)).build();

    public VillagerTrades() {}

    private static VillagerTrades.IMerchantRecipeOption commonBooks(int i) {
        return new VillagerTrades.m(ImmutableMap.builder().put(VillagerType.DESERT, new VillagerTrades.d(i, new Enchantment[]{Enchantments.FIRE_PROTECTION, Enchantments.THORNS, Enchantments.INFINITY})).put(VillagerType.JUNGLE, new VillagerTrades.d(i, new Enchantment[]{Enchantments.FEATHER_FALLING, Enchantments.PROJECTILE_PROTECTION, Enchantments.POWER})).put(VillagerType.PLAINS, new VillagerTrades.d(i, new Enchantment[]{Enchantments.PUNCH, Enchantments.SMITE, Enchantments.BANE_OF_ARTHROPODS})).put(VillagerType.SAVANNA, new VillagerTrades.d(i, new Enchantment[]{Enchantments.KNOCKBACK, Enchantments.BINDING_CURSE, Enchantments.SWEEPING_EDGE})).put(VillagerType.SNOW, new VillagerTrades.d(i, new Enchantment[]{Enchantments.AQUA_AFFINITY, Enchantments.LOOTING, Enchantments.FROST_WALKER})).put(VillagerType.SWAMP, new VillagerTrades.d(i, new Enchantment[]{Enchantments.DEPTH_STRIDER, Enchantments.RESPIRATION, Enchantments.VANISHING_CURSE})).put(VillagerType.TAIGA, new VillagerTrades.d(i, new Enchantment[]{Enchantments.BLAST_PROTECTION, Enchantments.FIRE_ASPECT, Enchantments.FLAME})).build());
    }

    private static VillagerTrades.IMerchantRecipeOption specialBooks() {
        return new VillagerTrades.m(ImmutableMap.builder().put(VillagerType.DESERT, new VillagerTrades.d(30, 3, 3, new Enchantment[]{Enchantments.EFFICIENCY})).put(VillagerType.JUNGLE, new VillagerTrades.d(30, 2, 2, new Enchantment[]{Enchantments.UNBREAKING})).put(VillagerType.PLAINS, new VillagerTrades.d(30, 3, 3, new Enchantment[]{Enchantments.PROTECTION})).put(VillagerType.SAVANNA, new VillagerTrades.d(30, 3, 3, new Enchantment[]{Enchantments.SHARPNESS})).put(VillagerType.SNOW, new VillagerTrades.d(30, new Enchantment[]{Enchantments.SILK_TOUCH})).put(VillagerType.SWAMP, new VillagerTrades.d(30, new Enchantment[]{Enchantments.MENDING})).put(VillagerType.TAIGA, new VillagerTrades.d(30, 2, 2, new Enchantment[]{Enchantments.FORTUNE})).build());
    }

    private static Int2ObjectMap<VillagerTrades.IMerchantRecipeOption[]> toIntMap(ImmutableMap<Integer, VillagerTrades.IMerchantRecipeOption[]> immutablemap) {
        return new Int2ObjectOpenHashMap(immutablemap);
    }

    private static ItemCost potionCost(Holder<PotionRegistry> holder) {
        return (new ItemCost(Items.POTION)).withComponents((datacomponentpredicate_a) -> {
            return datacomponentpredicate_a.expect(DataComponents.POTION_CONTENTS, new PotionContents(holder));
        });
    }

    private static ItemStack potion(Holder<PotionRegistry> holder) {
        return PotionContents.createItemStack(Items.POTION, holder);
    }

    private static ItemStack enchant(Item item, Enchantment enchantment, int i) {
        ItemStack itemstack = new ItemStack(item);

        itemstack.enchant(enchantment, i);
        return itemstack;
    }

    private static record m(Map<VillagerType, VillagerTrades.IMerchantRecipeOption> trades) implements VillagerTrades.IMerchantRecipeOption {

        public static VillagerTrades.m oneTradeInBiomes(VillagerTrades.IMerchantRecipeOption villagertrades_imerchantrecipeoption, VillagerType... avillagertype) {
            return new VillagerTrades.m((Map) Arrays.stream(avillagertype).collect(Collectors.toMap((villagertype) -> {
                return villagertype;
            }, (villagertype) -> {
                return villagertrades_imerchantrecipeoption;
            })));
        }

        @Nullable
        @Override
        public MerchantRecipe getOffer(Entity entity, RandomSource randomsource) {
            if (entity instanceof VillagerDataHolder villagerdataholder) {
                VillagerType villagertype = villagerdataholder.getVillagerData().getType();
                VillagerTrades.IMerchantRecipeOption villagertrades_imerchantrecipeoption = (VillagerTrades.IMerchantRecipeOption) this.trades.get(villagertype);

                return villagertrades_imerchantrecipeoption == null ? null : villagertrades_imerchantrecipeoption.getOffer(entity, randomsource);
            } else {
                return null;
            }
        }
    }

    private static class d implements VillagerTrades.IMerchantRecipeOption {

        private final int villagerXp;
        private final List<Enchantment> tradeableEnchantments;
        private final int minLevel;
        private final int maxLevel;

        public d(int i) {
            this(i, (Enchantment[]) BuiltInRegistries.ENCHANTMENT.stream().filter(Enchantment::isTradeable).toArray((j) -> {
                return new Enchantment[j];
            }));
        }

        public d(int i, Enchantment... aenchantment) {
            this(i, 0, Integer.MAX_VALUE, aenchantment);
        }

        public d(int i, int j, int k, Enchantment... aenchantment) {
            this.minLevel = j;
            this.maxLevel = k;
            this.villagerXp = i;
            this.tradeableEnchantments = Arrays.asList(aenchantment);
        }

        private Enchantment getEnchantment(RandomSource randomsource, FeatureFlagSet featureflagset) {
            List<Enchantment> list = this.tradeableEnchantments.stream().filter((enchantment) -> {
                return enchantment.isEnabled(featureflagset);
            }).toList();

            return (Enchantment) list.get(randomsource.nextInt(list.size()));
        }

        @Override
        public MerchantRecipe getOffer(Entity entity, RandomSource randomsource) {
            Enchantment enchantment = this.getEnchantment(randomsource, entity.level().enabledFeatures());
            int i = Math.max(enchantment.getMinLevel(), this.minLevel);
            int j = Math.min(enchantment.getMaxLevel(), this.maxLevel);
            int k = MathHelper.nextInt(randomsource, i, j);
            ItemStack itemstack = ItemEnchantedBook.createForEnchantment(new WeightedRandomEnchant(enchantment, k));
            int l = 2 + randomsource.nextInt(5 + k * 10) + 3 * k;

            if (enchantment.isTreasureOnly()) {
                l *= 2;
            }

            if (l > 64) {
                l = 64;
            }

            return new MerchantRecipe(new ItemCost(Items.EMERALD, l), Optional.of(new ItemCost(Items.BOOK)), itemstack, 12, this.villagerXp, 0.2F);
        }
    }

    public interface IMerchantRecipeOption {

        @Nullable
        MerchantRecipe getOffer(Entity entity, RandomSource randomsource);
    }

    private static class b implements VillagerTrades.IMerchantRecipeOption {

        private final ItemCost itemStack;
        private final int maxUses;
        private final int villagerXp;
        private final int emeraldAmount;
        private final float priceMultiplier;

        public b(IMaterial imaterial, int i, int j, int k) {
            this(imaterial, i, j, k, 1);
        }

        public b(IMaterial imaterial, int i, int j, int k, int l) {
            this(new ItemCost(imaterial.asItem(), i), j, k, l);
        }

        public b(ItemCost itemcost, int i, int j, int k) {
            this.itemStack = itemcost;
            this.maxUses = i;
            this.villagerXp = j;
            this.emeraldAmount = k;
            this.priceMultiplier = 0.05F;
        }

        @Override
        public MerchantRecipe getOffer(Entity entity, RandomSource randomsource) {
            return new MerchantRecipe(this.itemStack, new ItemStack(Items.EMERALD, this.emeraldAmount), this.maxUses, this.villagerXp, this.priceMultiplier);
        }
    }

    private static class i implements VillagerTrades.IMerchantRecipeOption {

        private final ItemStack itemStack;
        private final int emeraldCost;
        private final int maxUses;
        private final int villagerXp;
        private final float priceMultiplier;

        public i(Block block, int i, int j, int k, int l) {
            this(new ItemStack(block), i, j, k, l);
        }

        public i(Item item, int i, int j, int k) {
            this(new ItemStack(item), i, j, 12, k);
        }

        public i(Item item, int i, int j, int k, int l) {
            this(new ItemStack(item), i, j, k, l);
        }

        public i(ItemStack itemstack, int i, int j, int k, int l) {
            this(itemstack, i, j, k, l, 0.05F);
        }

        public i(Item item, int i, int j, int k, int l, float f) {
            this(new ItemStack(item), i, j, k, l, f);
        }

        public i(ItemStack itemstack, int i, int j, int k, int l, float f) {
            this.itemStack = itemstack;
            this.emeraldCost = i;
            this.itemStack.setCount(j);
            this.maxUses = k;
            this.villagerXp = l;
            this.priceMultiplier = f;
        }

        @Override
        public MerchantRecipe getOffer(Entity entity, RandomSource randomsource) {
            return new MerchantRecipe(new ItemCost(Items.EMERALD, this.emeraldCost), this.itemStack.copy(), this.maxUses, this.villagerXp, this.priceMultiplier);
        }
    }

    private static class j implements VillagerTrades.IMerchantRecipeOption {

        private final SuspiciousStewEffects effects;
        private final int xp;
        private final float priceMultiplier;

        public j(Holder<MobEffectList> holder, int i, int j) {
            this(new SuspiciousStewEffects(List.of(new SuspiciousStewEffects.a(holder, i))), j, 0.05F);
        }

        public j(SuspiciousStewEffects suspicioussteweffects, int i, float f) {
            this.effects = suspicioussteweffects;
            this.xp = i;
            this.priceMultiplier = f;
        }

        @Nullable
        @Override
        public MerchantRecipe getOffer(Entity entity, RandomSource randomsource) {
            ItemStack itemstack = new ItemStack(Items.SUSPICIOUS_STEW, 1);

            itemstack.set(DataComponents.SUSPICIOUS_STEW_EFFECTS, this.effects);
            return new MerchantRecipe(new ItemCost(Items.EMERALD), itemstack, 12, this.xp, this.priceMultiplier);
        }
    }

    private static class h implements VillagerTrades.IMerchantRecipeOption {

        private final ItemCost fromItem;
        private final int emeraldCost;
        private final ItemStack toItem;
        private final int maxUses;
        private final int villagerXp;
        private final float priceMultiplier;

        public h(IMaterial imaterial, int i, int j, Item item, int k, int l, int i1, float f) {
            this(imaterial, i, j, new ItemStack(item), k, l, i1, f);
        }

        h(IMaterial imaterial, int i, int j, ItemStack itemstack, int k, int l, int i1, float f) {
            this(new ItemCost(imaterial, i), j, itemstack.copyWithCount(k), l, i1, f);
        }

        public h(ItemCost itemcost, int i, ItemStack itemstack, int j, int k, float f) {
            this.fromItem = itemcost;
            this.emeraldCost = i;
            this.toItem = itemstack;
            this.maxUses = j;
            this.villagerXp = k;
            this.priceMultiplier = f;
        }

        @Nullable
        @Override
        public MerchantRecipe getOffer(Entity entity, RandomSource randomsource) {
            return new MerchantRecipe(new ItemCost(Items.EMERALD, this.emeraldCost), Optional.of(this.fromItem), this.toItem.copy(), 0, this.maxUses, this.villagerXp, this.priceMultiplier, 0);
        }
    }

    private static class e implements VillagerTrades.IMerchantRecipeOption {

        private final ItemStack itemStack;
        private final int baseEmeraldCost;
        private final int maxUses;
        private final int villagerXp;
        private final float priceMultiplier;

        public e(Item item, int i, int j, int k) {
            this(item, i, j, k, 0.05F);
        }

        public e(Item item, int i, int j, int k, float f) {
            this.itemStack = new ItemStack(item);
            this.baseEmeraldCost = i;
            this.maxUses = j;
            this.villagerXp = k;
            this.priceMultiplier = f;
        }

        @Override
        public MerchantRecipe getOffer(Entity entity, RandomSource randomsource) {
            int i = 5 + randomsource.nextInt(15);
            ItemStack itemstack = EnchantmentManager.enchantItem(entity.level().enabledFeatures(), randomsource, new ItemStack(this.itemStack.getItem()), i, false);
            int j = Math.min(this.baseEmeraldCost + i, 64);
            ItemCost itemcost = new ItemCost(Items.EMERALD, j);

            return new MerchantRecipe(itemcost, itemstack, this.maxUses, this.villagerXp, this.priceMultiplier);
        }
    }

    private static class c implements VillagerTrades.IMerchantRecipeOption {

        private final Map<VillagerType, Item> trades;
        private final int cost;
        private final int maxUses;
        private final int villagerXp;

        public c(int i, int j, int k, Map<VillagerType, Item> map) {
            BuiltInRegistries.VILLAGER_TYPE.stream().filter((villagertype) -> {
                return !map.containsKey(villagertype);
            }).findAny().ifPresent((villagertype) -> {
                throw new IllegalStateException("Missing trade for villager type: " + String.valueOf(BuiltInRegistries.VILLAGER_TYPE.getKey(villagertype)));
            });
            this.trades = map;
            this.cost = i;
            this.maxUses = j;
            this.villagerXp = k;
        }

        @Nullable
        @Override
        public MerchantRecipe getOffer(Entity entity, RandomSource randomsource) {
            if (entity instanceof VillagerDataHolder villagerdataholder) {
                ItemCost itemcost = new ItemCost((IMaterial) this.trades.get(villagerdataholder.getVillagerData().getType()), this.cost);

                return new MerchantRecipe(itemcost, new ItemStack(Items.EMERALD), this.maxUses, this.villagerXp, 0.05F);
            } else {
                return null;
            }
        }
    }

    private static class k implements VillagerTrades.IMerchantRecipeOption {

        private final ItemStack toItem;
        private final int toCount;
        private final int emeraldCost;
        private final int maxUses;
        private final int villagerXp;
        private final Item fromItem;
        private final int fromCount;
        private final float priceMultiplier;

        public k(Item item, int i, Item item1, int j, int k, int l, int i1) {
            this.toItem = new ItemStack(item1);
            this.emeraldCost = k;
            this.maxUses = l;
            this.villagerXp = i1;
            this.fromItem = item;
            this.fromCount = i;
            this.toCount = j;
            this.priceMultiplier = 0.05F;
        }

        @Override
        public MerchantRecipe getOffer(Entity entity, RandomSource randomsource) {
            ItemCost itemcost = new ItemCost(Items.EMERALD, this.emeraldCost);
            List<Holder<PotionRegistry>> list = (List) BuiltInRegistries.POTION.holders().filter((holder_c) -> {
                return !((PotionRegistry) holder_c.value()).getEffects().isEmpty() && entity.level().potionBrewing().isBrewablePotion(holder_c);
            }).collect(Collectors.toList());
            Holder<PotionRegistry> holder = (Holder) SystemUtils.getRandom(list, randomsource);
            ItemStack itemstack = new ItemStack(this.toItem.getItem(), this.toCount);

            itemstack.set(DataComponents.POTION_CONTENTS, new PotionContents(holder));
            return new MerchantRecipe(itemcost, Optional.of(new ItemCost(this.fromItem, this.fromCount)), itemstack, this.maxUses, this.villagerXp, this.priceMultiplier);
        }
    }

    private static class l implements VillagerTrades.IMerchantRecipeOption {

        private final int emeraldCost;
        private final TagKey<Structure> destination;
        private final String displayName;
        private final Holder<MapDecorationType> destinationType;
        private final int maxUses;
        private final int villagerXp;

        public l(int i, TagKey<Structure> tagkey, String s, Holder<MapDecorationType> holder, int j, int k) {
            this.emeraldCost = i;
            this.destination = tagkey;
            this.displayName = s;
            this.destinationType = holder;
            this.maxUses = j;
            this.villagerXp = k;
        }

        @Nullable
        @Override
        public MerchantRecipe getOffer(Entity entity, RandomSource randomsource) {
            if (!(entity.level() instanceof WorldServer)) {
                return null;
            } else {
                WorldServer worldserver = (WorldServer) entity.level();
                BlockPosition blockposition = worldserver.findNearestMapStructure(this.destination, entity.blockPosition(), 100, true);

                if (blockposition != null) {
                    ItemStack itemstack = ItemWorldMap.create(worldserver, blockposition.getX(), blockposition.getZ(), (byte) 2, true, true);

                    ItemWorldMap.renderBiomePreviewMap(worldserver, itemstack);
                    WorldMap.addTargetDecoration(itemstack, blockposition, "+", this.destinationType);
                    itemstack.set(DataComponents.ITEM_NAME, IChatBaseComponent.translatable(this.displayName));
                    return new MerchantRecipe(new ItemCost(Items.EMERALD, this.emeraldCost), Optional.of(new ItemCost(Items.COMPASS)), itemstack, this.maxUses, this.villagerXp, 0.2F);
                } else {
                    return null;
                }
            }
        }
    }

    private static class a implements VillagerTrades.IMerchantRecipeOption {

        private final Item item;
        private final int value;
        private final int maxUses;
        private final int villagerXp;

        public a(Item item, int i) {
            this(item, i, 12, 1);
        }

        public a(Item item, int i, int j, int k) {
            this.item = item;
            this.value = i;
            this.maxUses = j;
            this.villagerXp = k;
        }

        @Override
        public MerchantRecipe getOffer(Entity entity, RandomSource randomsource) {
            ItemCost itemcost = new ItemCost(Items.EMERALD, this.value);
            ItemStack itemstack = new ItemStack(this.item);

            if (itemstack.is(TagsItem.DYEABLE)) {
                List<ItemDye> list = Lists.newArrayList();

                list.add(getRandomDye(randomsource));
                if (randomsource.nextFloat() > 0.7F) {
                    list.add(getRandomDye(randomsource));
                }

                if (randomsource.nextFloat() > 0.8F) {
                    list.add(getRandomDye(randomsource));
                }

                itemstack = DyedItemColor.applyDyes(itemstack, list);
            }

            return new MerchantRecipe(itemcost, itemstack, this.maxUses, this.villagerXp, 0.2F);
        }

        private static ItemDye getRandomDye(RandomSource randomsource) {
            return ItemDye.byColor(EnumColor.byId(randomsource.nextInt(16)));
        }
    }

    private static class f implements VillagerTrades.IMerchantRecipeOption {

        f() {}

        @Override
        public MerchantRecipe getOffer(Entity entity, RandomSource randomsource) {
            return null;
        }
    }
}
