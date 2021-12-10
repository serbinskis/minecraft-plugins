package me.wobbychip.recallpotion.potions;

import java.util.List;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;

import me.wobbychip.recallpotion.utils.ReflectionUtil;
import net.minecraft.world.item.alchemy.PotionRegistry;

public class CustomPotion {
	private PotionRegistry base;
	private Material ingredient;
	private String name;
	private Color color;

	private String displayName = null;
	private List<String> lore = null;

	private boolean allowTippedArrow = false;
	private String tippedArrowName = null;

	public CustomPotion(PotionRegistry base, Material ingredient, String name, Color color) {
		this.base = base;
		this.ingredient = ingredient;
		this.name = name;
		this.color = color;
	}

	public PotionRegistry getBase() {
		return base;
	}

	public Material getMaterial() {
		return ingredient;
	}

	public String getName() {
		return name;
	}

	public Color getColor() {
		return color;
	}

	public int getIntColor() {
		return new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue()).getRGB();
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setLore(List<String> lore) {
		this.lore = lore;
	}

	public void setTippedArrow(boolean allow, String displayName) {
		this.allowTippedArrow = allow;
		this.tippedArrowName = displayName;
	}

	public boolean getAllowTippedArrow() {
		return this.allowTippedArrow;
	}

	public ItemStack getTippedArrow(boolean ignoreAllow, int amount) {
		if (this.allowTippedArrow || ignoreAllow) {
			ItemStack item = new ItemStack(Material.TIPPED_ARROW, amount);
			PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
			potionMeta.setColor(color);
			if (tippedArrowName != null) { potionMeta.setDisplayName(tippedArrowName); }
			if (lore != null) { potionMeta.setLore(lore); }
			potionMeta.setLocalizedName(this.name);
			potionMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
			item.setItemMeta(potionMeta);
			return item;
		} else {
			return new ItemStack(Material.AIR);
		}
	}

	//asBukkitCopy doesn't save custom potion, so I used asCraftMirror
	public ItemStack setPotionTag(ItemStack item) {
		net.minecraft.world.item.ItemStack nmsItem = ReflectionUtil.asNMSCopy(item);
		nmsItem.getOrCreateTag().setString("Potion", "minecraft:" + name);
		return ReflectionUtil.asBukkitMirror(nmsItem);
	}

	public ItemStack setProperties(ItemStack item) {		
		PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
		potionMeta.setColor(color);
		if (displayName != null) { potionMeta.setDisplayName(displayName); }
		if (lore != null) { potionMeta.setLore(lore); }
		potionMeta.setLocalizedName(this.name);
		potionMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
		potionMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		potionMeta.addEnchant(Enchantment.DURABILITY, 1, true);
		item.setItemMeta(potionMeta);

		return setPotionTag(item);
	}

	public void onPotionConsume(PlayerItemConsumeEvent event) {}
	public void onPotionSplash(PotionSplashEvent event) {}
	public void onLingeringPotionSplash(LingeringPotionSplashEvent event) {}
	public void onAreaEffectCloudApply(AreaEffectCloudApplyEvent event) {}
	public void onEntityShootBowEvent(EntityShootBowEvent event) {}
	public void onProjectileHit(ProjectileHitEvent event) {}
	public void onPlayerPickupArrow(PlayerPickupArrowEvent event) {}
	public void onProjectileLaunch(ProjectileLaunchEvent event) {}
}
