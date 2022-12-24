package com.willfp.ecoenchants.mendfinity;

import com.willfp.eco.core.EcoPlugin;
import com.willfp.eco.core.extensions.Extension;
import com.willfp.ecoenchants.enchantments.EcoEnchant;
import org.jetbrains.annotations.NotNull;

public class MendfinityMain extends Extension {
	public static final EcoEnchant MENDFINITY = new Mendfinity();

	public MendfinityMain(@NotNull final EcoPlugin plugin) {
		super(plugin);
	}

	@Override
	public void onEnable() {}

	@Override
	public void onDisable() {}
}
