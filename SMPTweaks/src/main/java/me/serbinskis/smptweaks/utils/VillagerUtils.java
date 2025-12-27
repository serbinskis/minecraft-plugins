package me.serbinskis.smptweaks.utils;

import me.serbinskis.smptweaks.annotations.Paper;
import me.serbinskis.smptweaks.library.fakeplayer.FakePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.view.MerchantView;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VillagerUtils {
    public static @Paper byte[] encodeMerchantRecipe(MerchantRecipe recipe) {
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            DataOutputStream output = new DataOutputStream(byteOut);

            byte[] result = recipe.getResult().serializeAsBytes();
            output.writeInt(result.length);
            output.write(result);

            output.writeInt(recipe.getMaxUses());
            output.writeInt(recipe.getUses());
            output.writeBoolean(recipe.hasExperienceReward());
            output.writeInt(recipe.getDemand());
            output.writeInt(recipe.getVillagerExperience());
            output.writeInt(recipe.getSpecialPrice());
            output.writeFloat(recipe.getPriceMultiplier());
            output.writeBoolean(PaperUtils.isPaper() && recipe.shouldIgnoreDiscounts());

            byte[] ingredients = ItemStack.serializeItemsAsBytes(recipe.getIngredients());
            output.writeInt(ingredients.length);
            output.write(ingredients);

            return byteOut.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error encoding MerchantRecipe", e);
        }
    }

    public static @Paper @Nullable MerchantRecipe decodeMerchantRecipe(byte[] data) {
        try {
            DataInputStream input = new DataInputStream(new ByteArrayInputStream(data));
            ItemStack result = ItemStack.deserializeBytes(input.readNBytes(input.readInt()));
            MerchantRecipe merchantRecipe = new MerchantRecipe(result, input.readInt());

            merchantRecipe.setUses(input.readInt());
            merchantRecipe.setExperienceReward(input.readBoolean());
            merchantRecipe.setDemand(input.readInt());
            merchantRecipe.setVillagerExperience(input.readInt());
            merchantRecipe.setSpecialPrice(input.readInt());
            merchantRecipe.setPriceMultiplier(input.readFloat());

            boolean ignoreDiscounts = input.readBoolean();
            if (PaperUtils.isPaper()) { merchantRecipe.setIgnoreDiscounts(ignoreDiscounts); }

            byte[] ingredientsData = input.readNBytes(input.readInt());
            List<ItemStack> ingredients = List.of(ItemStack.deserializeItemsFromBytes(ingredientsData));
            merchantRecipe.setIngredients(ingredients);

            return merchantRecipe;
        } catch (Exception e) {
            System.err.println("Error decoding MerchantRecipe: " + e.getMessage());
            System.err.println(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining("\n")));
            return null;
        }
    }

    public static ItemStack adjustItem(Villager villager, Player player, MerchantRecipe recipe, ItemStack item) {
        int reputation = ReflectionUtils.getPlayerReputation(villager, player);
        float f = (float) reputation * recipe.getPriceMultiplier();
        int i = (int) f;

        recipe.setSpecialPrice(-(f < (float) i ? i - 1 : i));
        recipe.adjust(item);
        recipe.setSpecialPrice(0);
        return item;
    }

    public static boolean canBuy(Player player, Villager villager, int trade) {
        player.openMerchant(villager, true);
        TradeSelectEvent event = new TradeSelectEvent((MerchantView) player.getOpenInventory(), trade);
        Bukkit.getPluginManager().callEvent(event);
        player.closeInventory();
        return (event.getResult() != Event.Result.DENY);
    }

    public static @Paper int tradeVillager(Player player, Villager villager, int trade, boolean force) {
        if (!canBuy(player, villager, trade)) { return -1; }
        InventoryView inventoryView = player.openMerchant(villager, force);
        if ((inventoryView == null) || !(inventoryView.getTopInventory() instanceof MerchantInventory)) { return -1; }

        //In case if we don't have required resources add them
        MerchantRecipe recipe = villager.getRecipes().get(trade);
        Stream<ItemStack> itemStream = recipe.getIngredients().stream().map(i -> i.add(999));
        itemStream.forEach(player.getInventory()::addItem);

        //Select trade and move item from result, to trigger trade
        ReflectionUtils.selectTrade(player, trade);
        ReflectionUtils.quickMoveStack(player, 2);

        //Close inventory and clear it if player is fake
        player.closeInventory();
        if (FakePlayer.isFakePlayer(player)) { player.getInventory().clear(); }

        //net.minecraft.world.entity.npc.Villager#rewardTradeXp
        AtomicInteger experience = new AtomicInteger(); //Count all the xp orb's experience and remove them
        List<ExperienceOrb> orbsList = Utils.getNearbyEntities(villager.getLocation().add(0, 0.5, 0), ExperienceOrb.class, 0.05, false); //Get xp orbs around the villager
        orbsList = orbsList.stream().filter(orb -> player.getUniqueId().equals(orb.getSourceEntityId())).toList();
        orbsList.forEach(orb -> { experience.addAndGet(orb.getExperience()); orb.remove(); }); //Count and remove

        return experience.get();
    }

    //Reference: net.minecraft.world.entity.npc.Villager@updateSpecialPrices(Player player)
    //Reference: net.minecraft.world.entity.ai.gossip.GossipType
    //They do reset automatically when inventory is closed
    public static void updateSpecialPrices(Player player, Villager villager) {
        int playerReputation = ReflectionUtils.getPlayerReputation(villager, player);
        List<MerchantRecipe> recipes = villager.getRecipes();

        for (MerchantRecipe merchantRecipe : recipes) {
            merchantRecipe.setSpecialPrice(0); //Reset prices
            if (merchantRecipe.shouldIgnoreDiscounts()) { continue; }
            int specialPrice = (int) (merchantRecipe.getSpecialPrice() - Math.floor(playerReputation * merchantRecipe.getPriceMultiplier()));
            merchantRecipe.setSpecialPrice(specialPrice);
        }

        for (MerchantRecipe merchantRecipe : recipes) {
            if (!player.hasPotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE)) { continue; }
            int amplifier = player.getPotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE).getAmplifier();
            if (merchantRecipe.shouldIgnoreDiscounts()) { continue; }
            ItemStack baseItem = merchantRecipe.getIngredients().getFirst();
            int baseCost = (baseItem != null ? baseItem.getAmount() : 0);
            int i = (int) Math.floor((0.3 + 0.0625 * amplifier) * baseCost);
            double specialPrice = merchantRecipe.getSpecialPrice() - Math.max(i, 1);
            merchantRecipe.setSpecialPrice((int) specialPrice);
        }

        villager.setRecipes(recipes);
    }

    public static void resetSpecialPrices(Villager villager) {
        List<MerchantRecipe> recipes = villager.getRecipes();
        recipes.forEach(merchantRecipe -> merchantRecipe.setSpecialPrice(0));
        villager.setRecipes(recipes);
    }

    public static MerchantRecipe cloneMerchantRecipe(MerchantRecipe recipe) {
        MerchantRecipe clone = new MerchantRecipe(
                recipe.getResult(),
                recipe.getUses(),
                recipe.getMaxUses(),
                recipe.hasExperienceReward(),
                recipe.getVillagerExperience(),
                recipe.getPriceMultiplier(),
                recipe.getDemand(),
                recipe.getSpecialPrice()
        );

        clone.setIngredients(recipe.getIngredients());
        if (PaperUtils.isPaper()) { clone.setIgnoreDiscounts(recipe.shouldIgnoreDiscounts()); }
        return clone;
    }
}
