package com.willfp.ecoenchants.mendfinity;

import com.willfp.ecoenchants.enchantments.EcoEnchant;
import com.willfp.ecoenchants.enchantments.meta.EnchantmentType;
import com.willfp.ecoenchants.enchantments.util.EnchantChecks;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class Mendfinity extends EcoEnchant {
    public Mendfinity() {
        super("mendfinity", EnchantmentType.SPECIAL);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBowShoot(@NotNull final EntityShootBowEvent event) {
        //Other mobs also can shoot
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        //Get player
        Player player = (Player) event.getEntity();

        //Check if gamemode is not creative
        if (player.getGameMode() == GameMode.CREATIVE) {
        	return;
        }

        //Check if bow has enchantment
        if (!EnchantChecks.mainhand(player, this)) {
            return;
        }

        //Check for disabled worlds
        if (this.getDisabledWorlds().contains(player.getWorld())) {
            return;
        }

        //Get arrow itemstack
        ItemStack itemStack = event.getConsumable();

        //Check if shooting arrow is standart arrow
    	if (itemStack.getType() != Material.ARROW) {
    		return;
    	}

    	//Dont consume arrow and disable it pickup 
        ((Arrow) event.getProjectile()).setPickupStatus(PickupStatus.CREATIVE_ONLY);
        itemStack.setAmount(itemStack.getAmount()+1);

        //Update inventory
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.getPlugin(), new Runnable() {
            public void run() {
            	player.updateInventory();
            }
        }, 1L);
    }

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.LOW)
	public void onXpPickup(@NotNull final PlayerExpChangeEvent event) {
        //Check if bow has enchantment
        if (!EnchantChecks.mainhand(event.getPlayer(), this)) {
            return;
        }

        //Check for disabled worlds
        if (this.getDisabledWorlds().contains(event.getPlayer().getWorld())) {
            return;
        }

        //Get item in main hand
		ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
		if (itemStack.getDurability() == 0) { return; }

        //Repair it
		int j = Math.min(2*event.getAmount(), itemStack.getDurability());
		itemStack.setDurability((short) (itemStack.getDurability() - j));

        //Give rest xp to player
	    event.setAmount(event.getAmount() - j/2);
	}
}
