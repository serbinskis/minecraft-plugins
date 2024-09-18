package me.serbinskis.smptweaks.custom.custompotions.potions;

import me.serbinskis.smptweaks.utils.PersistentUtils;
import me.serbinskis.smptweaks.utils.ReflectionUtils;
import me.serbinskis.smptweaks.custom.custompotions.CustomPotions;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.Arrays;
import java.util.List;

public class CustomPotion implements Listener {
	public static String PLACEHOLDER_POTION = "minecraft:awkward";
	private boolean enabled = true;
	private Object base;
	private String cbase;
	private final Material ingredient;
	private final String name;
	private final Color color;
	private String displayName = "";
	private List<String> lore = null;
	private boolean allowVillagerTrades = false;
	private boolean allowTippedArrow = false;
	private String tippedArrowName = null;
	private PotionEffect potionEffect;
	private PotionEffect arrowEffect;
	private PotionEffect cloudEffect;

	public CustomPotion(String base, Material ingredient, String name, Color color) {
		this((Object) null, ingredient, name, color);
		this.cbase = base;
	}

	public CustomPotion(PotionType potionType, Material ingredient, String name, Color color) {
		this(PotionManager.getPotion(potionType), ingredient, name, color);
	}

	public CustomPotion(Object base, Material ingredient, String name, Color color) {
		this.base = base;
		this.ingredient = ingredient;
		this.name = name;
		this.color = color;

		if (!CustomPotions.config.getConfig().isConfigurationSection("potions")) { CustomPotions.config.getConfig().createSection("potions"); }
		ConfigurationSection section = CustomPotions.config.getConfig().getConfigurationSection("potions");

		if (!section.contains(name.toUpperCase())) {
			section.set(name.toUpperCase(), enabled);
			CustomPotions.config.save();
		} else {
			this.enabled = section.getBoolean(name.toUpperCase());
		}
	}

	protected void addPotionEffect(PotionEffectType type, int duration, int amplifier) {
		this.potionEffect = new PotionEffect(type, duration, amplifier);
		this.arrowEffect = new PotionEffect(type, (int) (duration * 0.125f), amplifier);
		this.cloudEffect = new PotionEffect(type, (int) (duration * 0.25f), amplifier);
	}

	public PotionEffect getPotionEffect() {
		return this.potionEffect;
	}

	public PotionEffect getArrowEffect() {
		return this.arrowEffect;
	}

	public PotionEffect getCloudEffect() {
		return this.cloudEffect;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public Object getBase() {
		return base;
	}

	public String getBaseName() {
		return (base != null) ? ReflectionUtils.getPotionRegistryName(base) : cbase;
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

	public void setBase(Object base) {
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
			if (potionEffect == null) { potionMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP); }
			if (potionEffect != null) { potionMeta.addCustomEffect(potionEffect, true); }
			item.setItemMeta(potionMeta);
			PersistentUtils.setPersistentDataString(item, CustomPotions.TAG_CUSTOM_POTION, name);
			return item;
		} else {
			return new ItemStack(Material.AIR);
		}
	}

	public ItemStack setPotionTag(ItemStack item) {
		return ReflectionUtils.setPotionTag(item, "minecraft:" + name);
	}

	public ItemStack setProperties(ItemStack item, boolean tag) {
		PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
		potionMeta.setColor(color);
		potionMeta.setDisplayName(getPrefix(item.getType()) + displayName);
		if (lore != null) { potionMeta.setLore(lore); }
		if (potionEffect == null) { potionMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP); }
		if (potionEffect != null) { potionMeta.addCustomEffect(potionEffect, true); }
		item.setItemMeta(potionMeta);
		PersistentUtils.setPersistentDataString(item, CustomPotions.TAG_CUSTOM_POTION, name);
		return tag ? setPotionTag(item) : item;
	}

	public ItemStack getDisabledPotion(ItemStack item) {		
		PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
		potionMeta.setDisplayName("§r§fPotions are disabled");
		potionMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
		item.setItemMeta(potionMeta);
		return ReflectionUtils.setPotionTag(item, PLACEHOLDER_POTION);
	}

	public boolean isCustomPotion(ItemStack itemStack) {
		CustomPotion customPotion = CustomPotions.manager.getCustomPotion(itemStack);
		if (customPotion == null) { return false; }
		return customPotion.getName().equals(this.getName());
	}

	public void onPotionConsume(PlayerItemConsumeEvent event) {
		onAffectPlayer(event.getPlayer(), event);
		onAffectLivingEntity(event.getPlayer(), event);
	}

	public void onPotionSplash(PotionSplashEvent event) {
		for (LivingEntity livingEntity : event.getAffectedEntities()) {
			if (!onAffectLivingEntity(livingEntity, event)) { break; }
		}

		for (LivingEntity livingEntity : event.getAffectedEntities()) {
			if (livingEntity instanceof Player player) {
				if (!onAffectPlayer(player, event)) { break; }
			}
		}
	}

	public void onLingeringPotionSplash(LingeringPotionSplashEvent event) {}

	public void onAreaEffectCloudApply(AreaEffectCloudApplyEvent event) {
		for (LivingEntity livingEntity : event.getAffectedEntities()) {
			if (!onAffectLivingEntity(livingEntity, event)) { break; }
		}

		for (LivingEntity livingEntity : event.getAffectedEntities()) {
			if (livingEntity instanceof Player player) {
				if (!onAffectPlayer(player, event)) { break; }
			}
		}
	}

	public void onProjectileHit(ProjectileHitEvent event) {
		if (event.getEntity() instanceof Arrow) {
			if (event.getHitEntity() instanceof LivingEntity livingEntity) { onAffectLivingEntity(livingEntity, event); }
			if (event.getHitEntity() instanceof Player player) { onAffectPlayer(player, event); }
		}
	}

	public void onEntityShootBowEvent(EntityShootBowEvent event) {}
	public void onPlayerPickupArrow(PlayerPickupArrowEvent event) {}
	public void onProjectileLaunch(ProjectileLaunchEvent event) {}
	public boolean onAffectPlayer(Player player, Event event) { return true; }
	public boolean onAffectLivingEntity(LivingEntity livingEntity, Event event) { return true; }
}
