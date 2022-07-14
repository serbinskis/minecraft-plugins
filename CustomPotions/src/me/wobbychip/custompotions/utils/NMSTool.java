package me.wobbychip.custompotions.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import net.minecraft.core.IRegistry;
import net.minecraft.core.RegistryMaterials;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.effect.InstantMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInfo;
import net.minecraft.world.effect.MobEffectList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.PotionRegistry;
import net.minecraft.world.item.alchemy.Potions;

//net.minecraft.world.item.ItemStack ->
//    net.minecraft.nbt.CompoundTag getOrCreateTag() -> v

//net.minecraft.world.entity.Entity ->
//    boolean save(net.minecraft.nbt.CompoundTag) -> e

//net.minecraft.nbt.CompoundTag ->
//    putString(java.lang.String,java.lang.String) -> a
//    boolean contains(java.lang.String) -> e
//    java.lang.String getString(java.lang.String) -> l

public class NMSTool {
	public static ItemStack setPotionTag(ItemStack item, String name) {
		net.minecraft.world.item.ItemStack nmsItem = ReflectionUtil.asNMSCopy(item);
		nmsItem.v().a("Potion", name);
		return ReflectionUtil.asBukkitMirror(nmsItem);
	}

	public static String getPotionTag(Object object) {
		if (object instanceof Entity) {
			NBTTagCompound tag = new NBTTagCompound();
			ReflectionUtil.getEntity((Entity) object).e(tag);
			return ((tag != null) && tag.e("Potion")) ? tag.l("Potion").replace("minecraft:", "") : "";
		}

		if (object instanceof ItemStack) {
			NBTTagCompound tag = ReflectionUtil.asNMSCopy((ItemStack) object).v();
			return ((tag != null) && tag.e("Potion")) ? tag.l("Potion").replace("minecraft:", "") : "";
		}

		return "";
	}

	public static String getBaseName(PotionRegistry base) {
		//net.minecraft.world.item.alchemy.Potion ->
		//    java.lang.String getName(java.lang.String) -> b

		return base.b("");
	}

	public static PotionRegistry getPotion(PotionType potionType, boolean extended, boolean upgraded) {
		//net.minecraft.world.item.alchemy.PotionUtils ->
		//	    net.minecraft.world.item.alchemy.Potion getPotion(net.minecraft.world.item.ItemStack) -> d

		//net.minecraft.world.item.alchemy.Potions ->
		//	    net.minecraft.world.item.alchemy.Potion EMPTY -> a

		ItemStack item = new ItemStack(Material.POTION);
		PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
		potionMeta.setBasePotionData(new PotionData(potionType, extended, upgraded));
		item.setItemMeta(potionMeta);

		net.minecraft.world.item.ItemStack nmsItem = ReflectionUtil.asNMSCopy(item);
		if (nmsItem == null) { return null; }

		PotionRegistry potion = net.minecraft.world.item.alchemy.PotionUtil.d(nmsItem);
		return (potion != Potions.a) ? potion : null;
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

		setRegistryFrozen(IRegistry.U, false);
		setRegistryFrozen(IRegistry.Z, false);

		int id = IRegistry.U.d().size()+1;
		InstantMobEffect instantMobEffect = new InstantMobEffect(MobEffectInfo.c, color);
		MobEffectList mobEffectList = (MobEffectList) IRegistry.a(IRegistry.U, id, name, instantMobEffect);
		PotionRegistry potionRegistry = new PotionRegistry(new MobEffect[]{new MobEffect(mobEffectList, 1)});
		potionRegistry =  (PotionRegistry) IRegistry.a((IRegistry) IRegistry.Z, name, (Object) potionRegistry);

		setRegistryFrozen(IRegistry.U, true);
		setRegistryFrozen(IRegistry.Z, true);
		return potionRegistry;
	}

	public static void setRegistryFrozen(Object registry, boolean frozen) {
		//net.minecraft.core.MappedRegistry -> 
		//    boolean frozen -> ca (private)

        try {
            Field field = RegistryMaterials.class.getDeclaredField("ca");
            field.setAccessible(true);
            field.set(registry, frozen);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
            return;
        }
	}

	public static boolean registerBrewRecipe(PotionRegistry base, Material ingredient, PotionRegistry result) {
		//net.minecraft.world.item.ItemStack ->
		//	    net.minecraft.world.item.Item getItem() -> c

		Method method = ReflectionUtil.getRegisterBrewMethod();
		if (method == null) { return false; }

		Item potionIngredient = ReflectionUtil.asNMSCopy(new ItemStack(ingredient)).c();
		if (potionIngredient == null) { return false; }

		try {
			method.invoke(method, base, potionIngredient, result);
			return true;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return false;
		}
	}
}
