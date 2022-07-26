package me.wobbychip.smptweaks.custom.custompotions.potions;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
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

import me.wobbychip.smptweaks.custom.custompotions.CustomPotions;
import me.wobbychip.smptweaks.utils.NMSUtils;
import net.minecraft.world.item.alchemy.PotionRegistry;

public class CustomPotion implements Listener {
	private boolean enabled = true;
	private PotionRegistry base;
	private String cbase;
	private Material ingredient;
	private String name;
	private Color color;

	private String displayName = "";
	private List<String> lore = null;

	private boolean allowVillagerTrades = false;
	private boolean allowTippedArrow = false;
	private String tippedArrowName = null;

	public CustomPotion(String base, Material ingredient, String name, Color color) {
		this((PotionRegistry) null, ingredient, name, color);
		this.cbase = base;
	}

	public CustomPotion(PotionRegistry base, Material ingredient, String name, Color color) {
		this.base = base;
		this.ingredient = ingredient;
		this.name = name;
		this.color = color;

		if (!CustomPotions.config.getConfig().isConfigurationSection("potions")) { CustomPotions.config.getConfig().createSection("potions"); }
		ConfigurationSection section = CustomPotions.config.getConfig().getConfigurationSection("potions");

		if (!section.contains(name.toUpperCase())) {
			section.set(name.toUpperCase(), enabled);
			CustomPotions.config.Save();
		} else {
			this.enabled = section.getBoolean(name.toUpperCase());
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public PotionRegistry getBase() {
		return base;
	}

	public String getBaseName() {
		return (base != null) ? NMSUtils.getBaseName(base) : cbase;
	}

	public String getPrefix(Material material) {
		String result = "";

		for (int i = 0 ; i < displayName.length() ; i++) {
			if ((displayName.charAt(i) == '§') && (i+1 < displayName.length())) {
				result += displayName.substring(i, i+2);
				i += 1;
			} else { break; }
		}

		if (material == Material.SPLASH_POTION) { return result + "Splash "; }
		if (material == Material.LINGERING_POTION) { return result + "Lingering "; }
		return "";
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

	public void setBase(PotionRegistry base) {
		this.base = base;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setLore(List<String> lore) {
		this.lore = lore;
	}

	public List<String> getLore() {
		return (lore != null) ? lore : Arrays.asList("");
	}

	public void setAllowVillagerTrades(boolean allow) {
		allowVillagerTrades = allow;
	}

	public boolean getAllowVillagerTrades() {
		return (enabled && allowVillagerTrades);
	}

	public void setTippedArrow(boolean allow, String displayName) {
		allowTippedArrow = allow;
		tippedArrowName = displayName;
	}

	public boolean getAllowTippedArrow() {
		return (enabled && allowTippedArrow);
	}

	public ItemStack getTippedArrow(boolean ignoreAllow, int amount) {
		if (getAllowTippedArrow() || ignoreAllow) {
			ItemStack item = new ItemStack(Material.TIPPED_ARROW, amount);
			PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
			potionMeta.setColor(color);
			if (tippedArrowName != null) { potionMeta.setDisplayName(tippedArrowName); }
			if (lore != null) { potionMeta.setLore(lore); }
			potionMeta.setLocalizedName(name);
			potionMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
			item.setItemMeta(potionMeta);
			return item;
		} else {
			return new ItemStack(Material.AIR);
		}
	}

	//asBukkitCopy doesn't save custom potion tag, so I used asCraftMirror
	public ItemStack setPotionTag(ItemStack item) {
		return NMSUtils.setPotionTag(item, "minecraft:" + name);
	}

	public ItemStack setProperties(ItemStack item) {		
		PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
		potionMeta.setColor(color);
		potionMeta.setDisplayName(getPrefix(item.getType()) + displayName);
		if (lore != null) { potionMeta.setLore(lore); }
		potionMeta.setLocalizedName(name);
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
