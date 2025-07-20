package me.serbinskis.smptweaks.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.view.MerchantView;
import org.bukkit.potion.PotionEffectType;

import java.io.*;
import java.util.List;

public class VillagerUtils {
    public static byte[] encodeMerchantRecipe(MerchantRecipe recipe) {
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
        } catch (IOException e) {
            throw new RuntimeException("Error encoding MerchantRecipe", e);
        }
    }

    public static MerchantRecipe decodeMerchantRecipe(byte[] data) {
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
        } catch (IOException e) {
            throw new RuntimeException("Error decoding MerchantRecipe", e);
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
