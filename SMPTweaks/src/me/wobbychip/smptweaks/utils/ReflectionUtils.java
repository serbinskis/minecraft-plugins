package me.wobbychip.smptweaks.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.IRegistry;
import net.minecraft.core.RegistryBlocks;
import net.minecraft.core.RegistryMaterials;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.protocol.EnumProtocolDirection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutAbilities;
import net.minecraft.network.protocol.game.PacketPlayOutSetSlot;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.effect.MobEffectList;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerAbilities;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.PotionBrewer;
import net.minecraft.world.item.alchemy.PotionRegistry;
import net.minecraft.world.item.alchemy.PotionUtil;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ReflectionUtils {
	public static DataWatcherObject<Byte> DATA_LIVING_ENTITY_FLAGS;
	public static int LIVING_ENTITY_FLAG_IS_USING = 1;

	public static IRegistry<MobEffectList> MOB_EFFECT;;
	public static RegistryBlocks<PotionRegistry> POTION;

	public static String version;
	public static Class<?> CraftPlayer;
	public static Class<?> CraftInventory;
	public static Class<?> CraftWorld;
	public static Class<?> CraftHumanEntity;
	public static Class<?> CraftEntity;
	public static Class<?> CraftItemStack;
	public static Class<?> CraftMagicNumbers;

	public static Field invulnerableTicks = null;
	public static Field playerConnection;
	public static Field playerAbilities;
	public static Field registryFrozen;
	public static Method registerBrewMethod;
	public static Method getPotion;
	public static Method sendPacket;
	public static Method getEntityData;
	public static Method entityData_get;
	public static Method item_get;
	public static Method item_releaseUsing;
	public static Method addPlayer;
	public static Method removePlayer;

	static {
		version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

		CraftPlayer = loadClass("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer");
		CraftInventory = loadClass("org.bukkit.craftbukkit." + version + ".inventory.CraftInventory");
		CraftWorld = loadClass("org.bukkit.craftbukkit." + version + ".CraftWorld");
		CraftHumanEntity = loadClass("org.bukkit.craftbukkit." + version + ".entity.CraftHumanEntity");
		CraftEntity = loadClass("org.bukkit.craftbukkit." + version + ".entity.CraftEntity");
		CraftItemStack = loadClass("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
		CraftMagicNumbers = loadClass("org.bukkit.craftbukkit." + version + ".util.CraftMagicNumbers");

		MOB_EFFECT = (IRegistry<MobEffectList>) getRegistry(IRegistry.class, MobEffectList.class);
		POTION = (RegistryBlocks<PotionRegistry>) getRegistry(RegistryBlocks.class, PotionRegistry.class);

		//Fuck it I am not interested in updating stuff every time
		//So I will just search fields and methods by their types and arguments

		for (Field field : EntityHuman.class.getDeclaredFields()) {
			if (!field.getType().equals(PlayerAbilities.class)) { continue; }
			playerAbilities = field;
			playerAbilities.setAccessible(true);
			break;
		}

		for (Field field : EntityPlayer.class.getDeclaredFields()) {
			if (!field.getType().equals(PlayerConnection.class)) { continue; }
			playerConnection = field;
			break;
		}

		for (Field field : RegistryMaterials.class.getDeclaredFields()) {
			if (!field.getType().equals(boolean.class)) { continue; }
			registryFrozen = field;
			registryFrozen.setAccessible(true);
			break;
		}

		for (Method method : PotionBrewer.class.getDeclaredMethods()) {
			if (method.getParameterCount() != 3) { continue; }
			if (!method.getReturnType().equals(Void.TYPE)) { continue; }
			if (!method.getParameterTypes()[0].equals(PotionRegistry.class)) { continue; }
			if (!method.getParameterTypes()[1].equals(Item.class)) { continue; }
			if (!method.getParameterTypes()[2].equals(PotionRegistry.class)) { continue; }
			registerBrewMethod = method;
			registerBrewMethod.setAccessible(true);
			break;
		}

		for (Method method : PotionUtil.class.getMethods()) {
			if (method.getParameterCount() != 1) { continue; }
			if (!method.getReturnType().equals(PotionRegistry.class)) { continue; }
			if (!method.getParameterTypes()[0].equals(net.minecraft.world.item.ItemStack.class)) { continue; }
			getPotion = method;
			break;
		}

		for (Method method : PlayerConnection.class.getMethods()) {
			if (method.getParameterCount() != 1) { continue; }
			if (!method.getParameterTypes()[0].equals(Packet.class)) { continue; }
			sendPacket = method;
			break;
		}

		for (Method method : net.minecraft.world.entity.Entity.class.getMethods()) {
			if (method.getParameterCount() != 0) { continue; }
			if (!method.getReturnType().equals(DataWatcher.class)) { continue; }
			getEntityData = method;
			break;
		}

		for (Method method : DataWatcher.class.getMethods()) {
			if (method.getParameterCount() != 1) { continue; }
			if (method.getReturnType().equals(Void.TYPE)) { continue; }
			if (!method.getParameterTypes()[0].equals(DataWatcherObject.class)) { continue; }
			entityData_get = method;
			break;
		}

		for (Field field : EntityLiving.class.getDeclaredFields()) {
			if (!field.getType().equals(DataWatcherObject.class)) { continue; }
			Type genericType = field.getGenericType();
			if (!(genericType instanceof ParameterizedType)) { continue; }
			Type[] argTypes = ((ParameterizedType) genericType).getActualTypeArguments();
			if ((argTypes.length == 0) || !(argTypes[0] instanceof Class)) { continue; }
			if (!((Class) argTypes[0]).equals(Byte.class)) { continue; }

			try {
				field.setAccessible(true);
				DATA_LIVING_ENTITY_FLAGS = (DataWatcherObject<Byte>) field.get(null);
				break;
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		for (Method method : Item.class.getMethods()) {
			if (method.getParameterCount() != 1) { continue; }
			if (!method.getReturnType().equals(Item.class)) { continue; }
			if (!method.getParameterTypes()[0].equals(int.class)) { continue; }
			item_get = method;
			break;
		}

		for (Method method : Item.class.getMethods()) {
			if (method.getParameterCount() != 4) { continue; }
			if (!method.getReturnType().equals(Void.TYPE)) { continue; }
			if (!method.getParameterTypes()[0].equals(net.minecraft.world.item.ItemStack.class)) { continue; }
			if (!method.getParameterTypes()[1].equals(net.minecraft.world.level.World.class)) { continue; }
			if (!method.getParameterTypes()[2].equals(net.minecraft.world.entity.EntityLiving.class)) { continue; }
			if (!method.getParameterTypes()[3].equals(int.class)) { continue; }
			item_releaseUsing = method;
			break;
		}

		for (Method method : WorldServer.class.getMethods()) {
			if (method.getParameterCount() != 1) { continue; }
			if (!method.getReturnType().equals(Void.TYPE)) { continue; }
			if (!method.getParameterTypes()[0].equals(EntityPlayer.class)) { continue; }
			addPlayer = method;
			break;
		}

		for (Method method : WorldServer.class.getMethods()) {
			if (method.getParameterCount() != 2) { continue; }
			if (!method.getReturnType().equals(Void.TYPE)) { continue; }
			if (!method.getParameterTypes()[0].equals(EntityPlayer.class)) { continue; }
			if (!method.getParameterTypes()[1].equals(RemovalReason.class)) { continue; }
			removePlayer = method;
			break;
		}
	}

	public static Class<?> loadClass(String arg0) {
		try {
			return Class.forName(arg0);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Method getMethod(Class<?> clazz, String name, Class<?>... parameters) {
		try {
			return clazz.getDeclaredMethod(name, parameters);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static net.minecraft.server.level.EntityPlayer getEntityPlayer(Player player) {
		try {
			Object craftPlayer = CraftPlayer.cast(player);
			return (net.minecraft.server.level.EntityPlayer) player.getClass().getDeclaredMethod("getHandle").invoke(craftPlayer);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static net.minecraft.world.level.World getWorld(World world) {
		try {
			Object craftWorld = CraftWorld.cast(world);
			return (net.minecraft.world.level.World) world.getClass().getDeclaredMethod("getHandle").invoke(craftWorld);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static net.minecraft.world.entity.player.EntityHuman getEntityHuman(HumanEntity humanEntity) {
		try {
			Object craftHumanEntity = CraftHumanEntity.cast(humanEntity);
			return (net.minecraft.world.entity.player.EntityHuman) humanEntity.getClass().getDeclaredMethod("getHandle").invoke(craftHumanEntity);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static net.minecraft.world.entity.Entity getEntity(Entity entity) {
		try {
			Object craftEntity = CraftEntity.cast(entity);
			return (net.minecraft.world.entity.Entity) entity.getClass().getDeclaredMethod("getHandle").invoke(craftEntity);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static net.minecraft.world.item.ItemStack asNMSCopy(ItemStack itemStack) {
		try {
			Method method = CraftItemStack.getDeclaredMethod("asNMSCopy", ItemStack.class);
			return (net.minecraft.world.item.ItemStack) method.invoke(method, itemStack);
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static ItemStack asBukkitCopy(net.minecraft.world.item.ItemStack itemStack) {
		try {
			Method method = CraftItemStack.getDeclaredMethod("asBukkitCopy", net.minecraft.world.item.ItemStack.class);
			return (ItemStack) method.invoke(method, itemStack);
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static ItemStack asBukkitMirror(net.minecraft.world.item.ItemStack itemStack) {
		try {
			Method method = CraftItemStack.getDeclaredMethod("asCraftMirror", net.minecraft.world.item.ItemStack.class);
			return (ItemStack) method.invoke(method, itemStack);
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
			return null;
		}
	}

	@SuppressWarnings("deprecation")
	public static Item getItem(ItemStack itemStack) {
		try {
			Method method = CraftMagicNumbers.getDeclaredMethod("getItem", Material.class, short.class);
			return (Item) method.invoke(method, itemStack.getType(), itemStack.getDurability());
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void setInvulnerableTicks(Player player, int ticks) {
		EntityPlayer entityPlayer = getEntityPlayer(player);

		if (invulnerableTicks == null) {
			for (Field field : EntityPlayer.class.getDeclaredFields()) {
				try {
					if (!field.getType().equals(int.class)) { continue; }
					if (((int) field.get(entityPlayer)) != 60) { continue; }
					invulnerableTicks = field;
					break;
				} catch (IllegalArgumentException | IllegalAccessException e) {}
			}
		}

		try {
			invulnerableTicks.set(entityPlayer, ticks);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public static PlayerAbilities getPlayerAbilities(Player player) {
		try {
			return (PlayerAbilities) playerAbilities.get(getEntityPlayer(player));
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void sendPacket(Player player, Packet<?> packet) {
		try {
			Object connection = playerConnection.get(getEntityPlayer(player));
			sendPacket.invoke(connection, packet);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	//Doesn't work in creative, it sets an actual item
	public static void setGhostItem(Player player, ItemStack item, int slot) {
		if (slot < 9) {
			slot += 36;
		} else if (slot > 39) {
			slot += 5;
		} else if (slot > 35) {
			slot = 8 - (slot - 36);
		}

		sendPacket(player, new PacketPlayOutSetSlot(0, 0, slot, asNMSCopy(item)));
	}

	public static void setInstantBuild(Player player, boolean instantbuild, boolean clientSide, boolean serverSide) {
		//net.minecraft.world.entity.player.Abilities ->
		//    boolean instabuild -> d

		PlayerAbilities abilities = getPlayerAbilities(player);

		if (clientSide) {
			boolean temp = abilities.d;
			abilities.d = instantbuild;
			sendPacket(player, new PacketPlayOutAbilities(abilities));
			abilities.d = temp;
		}

		if (serverSide) {
			abilities.d = instantbuild;
		}
	}

	public static boolean isUsingItem(Player player) {
		try {
			DataWatcher entityData = (DataWatcher) getEntityData.invoke(getEntityPlayer(player));
			Byte entityFalgs = (Byte) entityData_get.invoke(entityData, DATA_LIVING_ENTITY_FLAGS);
			return (entityFalgs & LIVING_ENTITY_FLAG_IS_USING) > 0;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return false;
		}

		//Yes solution bellow is much easier, but I don't want to update it every time
		//So I will better search for everything by types and use reflection

		//net.minecraft.world.entity.LivingEntity ->
		//    boolean isUsingItem() -> eU
		//return getEntityPlayer(player).eU();
	}

	public static void shootBow(Player player, ItemStack bow, int ticks) {
		try {
			net.minecraft.world.item.ItemStack item = ReflectionUtils.asNMSCopy(bow);
			net.minecraft.world.level.World world = ReflectionUtils.getWorld(player.getWorld());
			EntityPlayer entityPlayer = ReflectionUtils.getEntityPlayer(player);
			item_releaseUsing.invoke(item_get.invoke(null, 718), item, world, entityPlayer, (72000 - ticks));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	public static EntityPlayer addFakePlayer(Location location) {
		MinecraftServer server = MinecraftServer.getServer();
		WorldServer world = (WorldServer) getWorld(location.getWorld());
		EntityPlayer player = new EntityPlayer(server, world, new GameProfile(UUID.randomUUID(), ""), null);
		PlayerConnection connection = new PlayerConnection(server, new NetworkManager(EnumProtocolDirection.b), player) {};

		try {
			playerConnection.set(player, connection);
			addPlayer.invoke(world, player);
			player.getBukkitEntity().teleport(location);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}

		return player;
	}

	public static void removeFakePlayer(EntityPlayer player) {
		try {
			WorldServer world = (WorldServer) getWorld(player.getBukkitEntity().getWorld());
			removePlayer.invoke(world, player, RemovalReason.b);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
			e.printStackTrace();
		}
	}

	public static void setRegistryFrozen(Object registry, boolean frozen) {
        try {
            registryFrozen.set(registry, frozen);
        } catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
            return;
        }
	}

	public static boolean registerBrewRecipe(PotionRegistry base, Material ingredient, PotionRegistry result) {
		try {
			Item potionIngredient = getItem(new ItemStack(ingredient));
			registerBrewMethod.invoke(registerBrewMethod, base, potionIngredient, result);
			return true;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static PotionRegistry getPotion(PotionType potionType, boolean extended, boolean upgraded) {
		if (potionType == PotionType.UNCRAFTABLE) { return null; }

		ItemStack item = new ItemStack(Material.POTION);
		PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
		potionMeta.setBasePotionData(new PotionData(potionType, extended, upgraded));
		item.setItemMeta(potionMeta);

		net.minecraft.world.item.ItemStack nmsItem = asNMSCopy(item);
		if (nmsItem == null) { return null; }

		try {
			return (PotionRegistry) getPotion.invoke(getPotion, nmsItem);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static Object getRegistry(Class rType, Class gType) {
		for (Field field : IRegistry.class.getDeclaredFields()) {
			if (!field.getType().equals(rType)) { continue; }
			Type genericType = field.getGenericType();
			if (!(genericType instanceof ParameterizedType)) { continue; }
			Type[] argTypes = ((ParameterizedType) genericType).getActualTypeArguments();
			if ((argTypes.length == 0) || !(argTypes[0] instanceof Class)) { continue; }
			if (!((Class) argTypes[0]).equals(gType)) { continue; }

			try {
				return field.get(null);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		return null;
	}
}
