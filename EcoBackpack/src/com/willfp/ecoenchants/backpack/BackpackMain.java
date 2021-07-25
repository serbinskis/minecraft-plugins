package com.willfp.ecoenchants.backpack;

import com.willfp.eco.core.EcoPlugin;
import com.willfp.eco.core.extensions.Extension;
import com.willfp.ecoenchants.enchantments.EcoEnchant;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class BackpackMain extends Extension {
    public static final EcoEnchant BACKPACK = new Backpack();

    public BackpackMain(@NotNull final EcoPlugin plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        // Handled by super
    }

    @Override
    public void onDisable() {
    	for(Player player : Bukkit.getServer().getOnlinePlayers()) {
    		Inventory inv = player.getOpenInventory().getTopInventory();

    		if (inv != null && (inv.getHolder() instanceof BackpackGUI)) {
    			((BackpackGUI) inv.getHolder()).saveInventory();
    		}
    	}
    }
}
