package me.wobbychip.smptweaks.utils;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

import net.minecraft.core.IRegistry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.effect.InstantMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInfo;
import net.minecraft.world.effect.MobEffectList;
import net.minecraft.world.item.alchemy.PotionRegistry;

//net.minecraft.world.item.ItemStack ->
//    net.minecraft.nbt.CompoundTag getOrCreateTag() -> v

//net.minecraft.world.entity.Entity ->
//    boolean save(net.minecraft.nbt.CompoundTag) -> e

//net.minecraft.nbt.CompoundTag ->
//    putString(java.lang.String,java.lang.String) -> a
//    boolean contains(java.lang.String) -> e
//    java.lang.String getString(java.lang.String) -> l

public class NMSUtils {
	public static ItemStack setPotionTag(ItemStack item, String name) {
		net.minecraft.world.item.ItemStack nmsItem = ReflectionUtils.asNMSCopy(item);
		nmsItem.v().a("Potion", name);
		return ReflectionUtils.asBukkitMirror(nmsItem);
	}

	public static String getPotionTag(Object object) {
		if (object instanceof Entity) {
			NBTTagCompound tag = new NBTTagCompound();
			ReflectionUtils.getEntity((Entity) object).e(tag);
			return ((tag != null) && tag.e("Potion")) ? tag.l("Potion").replace("minecraft:", "") : "";
		}

		if (object instanceof ItemStack) {
			NBTTagCompound tag = ReflectionUtils.asNMSCopy((ItemStack) object).v();
			return ((tag != null) && tag.e("Potion")) ? tag.l("Potion").replace("minecraft:", "") : "";
		}

		return "";
	}

	public static String getBaseName(PotionRegistry base) {
		//net.minecraft.world.item.alchemy.Potion ->
		//    java.lang.String getName(java.lang.String) -> b

		return base.b("");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static PotionRegistry registerInstantPotion(String name, int color) {
		//IRegistry -> .d() -> keySet()

		//net.minecraft.world.effect.MobEffectCategory ->
		//    net.minecraft.world.effect.MobEffectCategory NEUTRAL -> c

		//net.minecraft.core.Registry ->
		//    net.minecraft.core.Registry MOB_EFFECT -> U
		//    net.minecraft.core.DefaultedRegistry POTION -> Z
		//    java.lang.Object registerMapping(net.minecraft.core.Registry,int,java.lang.String,java.lang.Object) -> a

		ReflectionUtils.setRegistryFrozen(IRegistry.U, false);
		ReflectionUtils.setRegistryFrozen(IRegistry.Z, false);

		int id = IRegistry.U.d().size()+1;
		InstantMobEffect instantMobEffect = new InstantMobEffect(MobEffectInfo.c, color);
		MobEffectList mobEffectList = (MobEffectList) IRegistry.a(IRegistry.U, id, name, instantMobEffect);
		PotionRegistry potionRegistry = new PotionRegistry(new MobEffect[]{new MobEffect(mobEffectList, 1)});
		potionRegistry =  (PotionRegistry) IRegistry.a((IRegistry) IRegistry.Z, name, (Object) potionRegistry);

		ReflectionUtils.setRegistryFrozen(IRegistry.U, true);
		ReflectionUtils.setRegistryFrozen(IRegistry.Z, true);
		return potionRegistry;
	}

	public static PotionRegistry getPotion(PotionType potionType, boolean extended, boolean upgraded) {
		return ReflectionUtils.getPotion(potionType, extended, upgraded);
	}

	public static boolean registerBrewRecipe(PotionRegistry base, Material ingredient, PotionRegistry result) {
		return ReflectionUtils.registerBrewRecipe(base, ingredient, result);
	}
}
