package me.serbinskis.smptweaks.utils;

import com.google.common.reflect.ClassPath;
import com.mojang.authlib.GameProfile;
import io.netty.channel.*;
import io.netty.channel.embedded.EmbeddedChannel;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.commands.CacheableFunction;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.core.Registry;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.PortalProcessor;
import net.minecraft.world.entity.ai.gossip.GossipContainer;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.redstone.NeighborUpdater;
import net.minecraft.world.level.storage.loot.LootTable;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.block.BlockFace;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Comparator;
import org.bukkit.craftbukkit.potion.CraftPotionType;
import org.bukkit.entity.*;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public class ReflectionUtils {
	public static Registry<Potion> POTION = BuiltInRegistries.POTION;
	public static HashMap<String, GossipType> reputations = new HashMap<>();
	public static Class<?> CraftAdvancement;
	public static Class<?> CraftServer;
	public static Class<?> CraftEntity;
	public static Class<?> CraftHumanEntity;
	public static Class<?> CraftPlayer;
	public static Class<?> CraftVillager;
	public static Class<?> CraftInventory;
	public static Class<?> CraftItemStack;
	public static Class<?> CraftWorld;
	public static Class<?> CraftSound;
	public static Class<?> CraftMagicNumbers;
	public static Class<?> GossipContainer_EntityGossips;
	public static Field Entity_bukkitEntity;
	public static Field EntityPlayer_playerConnection;
	public static Field EntityPlayer_chatVisibility;
	public static Field EntityPlayer_advancements;
	public static Field MinecraftServer_levels;
	public static Field CustomFunctionData_ticking;
	public static Field CustomFunctionData_postReload;
	public static Field RegistryMaterials_frozen;
	public static Field BlockPhysicsEvent_changed;
	public static Field PotionSplashEvent_affectedEntities;
	public static Field AreaEffectCloudApplyEvent_affectedEntities;
	public static Field Level_dimensionTypeRegistration;
	public static Field Holder_owner;
	public static Field Holder_tags;
	public static Field Holder_key;
	public static Field Holder_value;
	public static Field ServerCommonPacketListenerImpl_connection;
	public static Field ServerConnectionListener_channels;
	public static Field CacheableFunction_id;
	public static Field CacheableFunction_resolved;
	public static Field CacheableFunction_function;
	public static Field GossipContainer_gossips;
	public static Field EntityGossips_entries;
	public static Field PotionBrewing_potionMixes;
	public static Method EntityVillager_startTrading_Or_updateSpecialPrices;
	public static Method IRegistry_keySet;
	public static Method Potions_register;
	public static Method RegistryMaterials_getHolder;

	static {
		CraftAdvancement = Objects.requireNonNull(getCraftBukkitClass("org.bukkit.craftbukkit.advancement.CraftAdvancement"));
		CraftServer = Objects.requireNonNull(getCraftBukkitClass("org.bukkit.craftbukkit.CraftServer"));
		CraftEntity = Objects.requireNonNull(getCraftBukkitClass("org.bukkit.craftbukkit.entity.CraftEntity"));
		CraftHumanEntity = Objects.requireNonNull(getCraftBukkitClass("org.bukkit.craftbukkit.entity.CraftHumanEntity"));
		CraftPlayer = Objects.requireNonNull(getCraftBukkitClass("org.bukkit.craftbukkit.entity.CraftPlayer"));
		CraftVillager = Objects.requireNonNull(getCraftBukkitClass("org.bukkit.craftbukkit.entity.CraftVillager"));
		CraftInventory = Objects.requireNonNull(getCraftBukkitClass("org.bukkit.craftbukkit.inventory.CraftInventory"));
		CraftItemStack = Objects.requireNonNull(getCraftBukkitClass("org.bukkit.craftbukkit.inventory.CraftItemStack"));
		CraftWorld = Objects.requireNonNull(getCraftBukkitClass("org.bukkit.craftbukkit.CraftWorld"));
		CraftSound = Objects.requireNonNull(getCraftBukkitClass("org.bukkit.craftbukkit.CraftSound"));
		CraftMagicNumbers = Objects.requireNonNull(getCraftBukkitClass("org.bukkit.craftbukkit.util.CraftMagicNumbers"));

		//Fuck it I am not interested in updating nms every time
		//So I will just search fields and methods by their types and arguments

		for (GossipType type : GossipType.values()) { reputations.put(type.getSerializedName().toUpperCase(), type); }
		setRegistryMap(POTION, new HashMap<>());

		MinecraftServer_levels = Objects.requireNonNull(getField(MinecraftServer.class, Map.class, null, true));
		CustomFunctionData_ticking = Objects.requireNonNull(getField(ServerFunctionManager.class, List.class, null, true));
		CustomFunctionData_postReload = Objects.requireNonNull(getField(ServerFunctionManager.class, boolean.class, null, true));
		BlockPhysicsEvent_changed = Objects.requireNonNull(getField(BlockPhysicsEvent.class, BlockData.class, null, true));
		PotionSplashEvent_affectedEntities = Objects.requireNonNull(getField(PotionSplashEvent.class, Map.class, null, true));
		AreaEffectCloudApplyEvent_affectedEntities = Objects.requireNonNull(getField(AreaEffectCloudApplyEvent.class, List.class, null, true));
		Entity_bukkitEntity = Objects.requireNonNull(getField(net.minecraft.world.entity.Entity.class, CraftEntity, null, true));
		EntityPlayer_playerConnection = Objects.requireNonNull(getField(ServerPlayer.class, ServerGamePacketListenerImpl.class, null, true));
		EntityPlayer_chatVisibility = Objects.requireNonNull(getField(ServerPlayer.class, ChatVisiblity.class, null, true));
		EntityPlayer_advancements = Objects.requireNonNull(getField(ServerPlayer.class, PlayerAdvancements.class, null, true));
		RegistryMaterials_frozen = Objects.requireNonNull(getField(MappedRegistry.class, boolean.class, null, true));
		GossipContainer_gossips = Objects.requireNonNull(getField(GossipContainer.class, Map.class, null, true));
		GossipContainer_EntityGossips = Objects.requireNonNull((Class<?>) ((ParameterizedType) GossipContainer_gossips.getGenericType()).getActualTypeArguments()[1]);
		EntityGossips_entries = Objects.requireNonNull(getField(GossipContainer_EntityGossips, Object2IntMap.class, null, true));
		EntityVillager_startTrading_Or_updateSpecialPrices = Objects.requireNonNull(findMethod(false, Modifier.PRIVATE, net.minecraft.world.entity.npc.Villager.class, Void.TYPE, null, net.minecraft.world.entity.player.Player.class));
		IRegistry_keySet = Objects.requireNonNull(findMethod(true, null, Registry.class, Set.class, ResourceLocation.class));
		Potions_register = Objects.requireNonNull(findMethod(false, Modifier.PRIVATE, Potions.class, Holder.class, null, String.class, Potion.class));
		RegistryMaterials_getHolder = Objects.requireNonNull(findMethod(true, null, Registry.class, Optional.class, null, int.class));
		Level_dimensionTypeRegistration = Objects.requireNonNull(getField(Level.class, Holder.class, DimensionType.class, true));
		Holder_owner = Objects.requireNonNull(getField(Holder.Reference.class, HolderOwner.class, null, true));
		Holder_tags = Objects.requireNonNull(getField(Holder.Reference.class, Set.class, null, true));
		Holder_key = Objects.requireNonNull(getField(Holder.Reference.class, ResourceKey.class, null, true));
		Holder_value = Objects.requireNonNull(getField(Holder.Reference.class, Object.class, null, true));
		ServerCommonPacketListenerImpl_connection = Objects.requireNonNull(getField(ServerCommonPacketListenerImpl.class, Connection.class, null, true));
		ServerConnectionListener_channels = Objects.requireNonNull(getField(ServerConnectionListener.class, List.class, ChannelFuture.class, true));
		CacheableFunction_id = Objects.requireNonNull(getField(CacheableFunction.class, ResourceLocation.class, null, true));
		CacheableFunction_resolved = Objects.requireNonNull(getField(CacheableFunction.class, boolean.class, null, true));
		CacheableFunction_function = Objects.requireNonNull(getField(CacheableFunction.class, Optional.class, CommandFunction.class, true));
	}

	public static Class<?> loadClass(String name, boolean verbose) {
		try {
			return Class.forName(name);
		} catch (ClassNotFoundException e) {
			if (verbose) { e.printStackTrace(); }
			return null;
		}
	}

	public static Class<?> loadClass(String name, ClassLoader loader, boolean initialize, boolean verbose) {
		try {
			return Class.forName(name, initialize, loader);
		} catch (ClassNotFoundException | NoClassDefFoundError e) {
			if (verbose) { e.printStackTrace(); }
			return null;
		}
	}

	public static Method getMethod(Class<?> clazz, String name, Class<?>... parameters) {
		try {
			Method method = clazz.getDeclaredMethod(name, parameters);
			method.setAccessible(true);
			return method;
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Class<?> getCraftBukkitClass(String path) {
		String version = PaperUtils.isPaper() ? "craftbukkit" : "craftbukkit." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
		return loadClass(path.replace("craftbukkit", version), true);
	}

	/**
	 * Finds a method within a given class based on specified criteria.
	 *
	 * @param isAccessible Flag indicating whether to use getMethods() or getDeclaredMethods()
	 * @param modifier     The modifier to match for the method (optional).
	 * @param clazz        The class containing the method.
	 * @param rType        The return type of the method (optional).
	 * @param gType        The generic type to match against the actual type argument of the return type (optional).
	 * @param parameters   The types of the method parameters.
	 * @return             The {@code Method} object matching the specified criteria, or {@code null} if not found.
	 */
	public static Method findMethod(boolean isAccessible, Integer modifier, Class<?> clazz, Class<?> rType, Class<?> gType, Class<?>... parameters) {
		for (Method method : !isAccessible ? clazz.getDeclaredMethods() : clazz.getMethods()) {
			if (method.getParameterCount() != parameters.length) { continue; }
			if ((rType != null) && !method.getReturnType().equals(rType)) { continue; }

			if ((modifier != null) && ((modifier == Modifier.STATIC) && !Modifier.isStatic(method.getModifiers()))) { continue; }
			if ((modifier != null) && ((modifier == Modifier.PUBLIC) && !Modifier.isPublic(method.getModifiers()))) { continue; }
			if ((modifier != null) && ((modifier == Modifier.PRIVATE) && !Modifier.isPrivate(method.getModifiers()))) { continue; }
			if ((modifier != null) && ((modifier == Modifier.PROTECTED) && !Modifier.isProtected(method.getModifiers()))) { continue; }

			if (gType != null) {
				Type genericType = method.getGenericReturnType();
				if (!(genericType instanceof ParameterizedType)) { continue; }
				Type[] argTypes = ((ParameterizedType) genericType).getActualTypeArguments();
				if ((argTypes.length == 0) || !(argTypes[0] instanceof Class)) { continue; }
				if (!argTypes[0].equals(gType)) { continue; }
			}

			boolean match = true;
			for (int i = 0; i < parameters.length; i++) {
				match = method.getParameterTypes()[i].equals(parameters[i]);
				if (!match) { break; }
			}

			if (!match) { continue; }
			method.setAccessible(true);
			return method;
		}

		return null;
	}

	public static Field getField(Class<?> clazz, Class<?> fType, Class<?> gType, boolean isPrivate) {
		return getField(clazz, fType, gType, null, null, isPrivate);
	}

	public static Field getField(Class<?> clazz, Class<?> fType, Class<?> gType, Object parent, Object value, boolean isPrivate) {
		return getField(clazz, fType, ((gType != null) ? new Class<?>[] { gType } : null), parent, value, isPrivate, null, null);
	}

	/**
	 * Retrieves a {@code Field} object based on specified criteria within a given class.
	 *
	 * @param clazz         The class containing the field.
	 * @param fType         The type of the field to retrieve.
	 * @param gTypes        An array of generic types to match against the actual type arguments of the field (optional).
	 * @param parent        The object instance (or null for static fields) whose field value should match (optional).
	 * @param value         The value that the field should contain (optional).
	 * @param isPrivate     Flag indicating whether to use getDeclaredFields() or getFields().
	 * @param modifier      The modifier to match for the field (optional).
	 * @param notModifier   The modifier to exclude for the field (optional).
	 * @return              The {@code Field} object matching the specified criteria, or {@code null} if not found.
	 */
	public static Field getField(Class<?> clazz, Class<?> fType, Object[] gTypes, Object parent, Object value, boolean isPrivate, Integer modifier, Integer notModifier) {
		for (Field field : isPrivate ? clazz.getDeclaredFields() : clazz.getFields()) {
			if (!field.getType().equals(fType)) { continue; }

			if ((modifier != null) && ((modifier == Modifier.FINAL) && !Modifier.isFinal(field.getModifiers()))) { continue; }
			if ((modifier != null) && ((modifier == Modifier.STATIC) && !Modifier.isStatic(field.getModifiers()))) { continue; }
			if ((modifier != null) && ((modifier == Modifier.PUBLIC) && !Modifier.isPublic(field.getModifiers()))) { continue; }
			if ((modifier != null) && ((modifier == Modifier.PRIVATE) && !Modifier.isPrivate(field.getModifiers()))) { continue; }
			if ((modifier != null) && ((modifier == Modifier.PROTECTED) && !Modifier.isProtected(field.getModifiers()))) { continue; }

			if ((notModifier != null) && ((notModifier == Modifier.FINAL) && Modifier.isFinal(field.getModifiers()))) { continue; }
			if ((notModifier != null) && ((notModifier == Modifier.STATIC) && Modifier.isStatic(field.getModifiers()))) { continue; }
			if ((notModifier != null) && ((notModifier == Modifier.PUBLIC) && Modifier.isPublic(field.getModifiers()))) { continue; }
			if ((notModifier != null) && ((notModifier == Modifier.PRIVATE) && Modifier.isPrivate(field.getModifiers()))) { continue; }
			if ((notModifier != null) && ((notModifier == Modifier.PROTECTED) && Modifier.isProtected(field.getModifiers()))) { continue; }

			if ((gTypes != null) && (gTypes.length > 0)) {
				Type genericType = field.getGenericType();
				if (!(genericType instanceof ParameterizedType)) { continue; }
				Type[] argTypes = ((ParameterizedType) genericType).getActualTypeArguments();
				if (argTypes.length < gTypes.length) { continue; }

				boolean match = true;
				for (int i = 0; i < gTypes.length; i++) {
					if (argTypes[i] instanceof ParameterizedType) { argTypes[i] = ((ParameterizedType) argTypes[i]).getRawType(); }
					if ((gTypes[i] instanceof String) && !argTypes[i].getTypeName().equals(gTypes[i])) { match = false; break; }
					if (!(gTypes[i] instanceof String) && !argTypes[i].equals(gTypes[i])) { match = false; break; }
				}
				if (!match) { continue; }
			}

			if (value != null) { //if parent is null then value is static
				Object pvalue = getValue(field, parent);
				if ((pvalue == null) || !pvalue.equals(value)) { continue; }
			}

			field.setAccessible(true);
			return field;
		}

		return null;
	}

	public static Object getValue(Field field, Object obj) {
		try {
			field.setAccessible(true);
			return field.get(obj);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void setValue(Field field, Object target, Object obj) {
		try {
			field.setAccessible(true);
			field.set(target, obj);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public static Object newInstance(Class<?> clazz, Class<?>[] parameters, Object[] args, boolean isPrivate, boolean verbose) {
		try {
			if (parameters == null) { parameters = new Class<?>[] {}; }
			Constructor<?> constructor = isPrivate ? clazz.getDeclaredConstructor(parameters) : clazz.getConstructor(parameters);
			constructor.setAccessible(true);
			return constructor.newInstance((args != null) ? args : new Object[] {});
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			if (verbose) { e.printStackTrace(); }
			return null;
		}
	}

	public static <T> List<T> getInstances(ClassLoader loader, String packagePrefix, Class<T> clazz, boolean recursive, boolean sort, boolean excludeParentClass) {
		return getInstances(loader, packagePrefix, clazz, recursive, sort, excludeParentClass, new Class<?>[] {}, new Object[] {});
	}

	public static <T> List<T> getInstances(ClassLoader loader, String packagePrefix, Class<T> clazz, boolean recursive, boolean sort, boolean excludeParentClass, Class<?>[] parameters, Object[] args) {
		List<T> instances = new ArrayList<>();

		try {
			ClassPath classPath = ClassPath.from(loader);

			for (ClassPath.ClassInfo classInfo : recursive ? classPath.getTopLevelClassesRecursive(packagePrefix) : classPath.getTopLevelClasses(packagePrefix)) {
				Class<?> newClazz = loadClass(classInfo.getName(), loader, true, false);
				if ((newClazz == null) || !clazz.isAssignableFrom(newClazz)) { continue; }
				if (newClazz.equals(clazz) && excludeParentClass) { continue; }
				instances.add((T) newClazz.getConstructor(parameters).newInstance(args));
			}
		} catch (IOException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}

		if (sort) { instances = instances.stream().sorted(java.util.Comparator.comparing(e -> e.getClass().getSimpleName())).toList(); }
		return instances;
	}

	public static net.minecraft.server.level.ServerPlayer getEntityPlayer(Player player) {
		try {
			Object craftPlayer = CraftPlayer.cast(player);
			return (net.minecraft.server.level.ServerPlayer) player.getClass().getDeclaredMethod("getHandle").invoke(craftPlayer);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static ServerLevel getWorld(World world) {
		try {
			Object craftWorld = CraftWorld.cast(world);
			return (ServerLevel) world.getClass().getDeclaredMethod("getHandle").invoke(craftWorld);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static net.minecraft.world.entity.player.Player getEntityHuman(HumanEntity humanEntity) {
		try {
			Object craftHumanEntity = CraftHumanEntity.cast(humanEntity);
			return (net.minecraft.world.entity.player.Player) humanEntity.getClass().getDeclaredMethod("getHandle").invoke(craftHumanEntity);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static net.minecraft.world.entity.npc.Villager getEntityVillager(Villager villager) {
		try {
			Object craftEntity = CraftVillager.cast(villager);
			return (net.minecraft.world.entity.npc.Villager) villager.getClass().getDeclaredMethod("getHandle").invoke(craftEntity);
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

	public static org.bukkit.entity.Entity getBukkitEntity(net.minecraft.world.entity.Entity entity) {
		try {
			Object bukkitEntity = Entity_bukkitEntity.get(entity);

			if (bukkitEntity == null) {
				Method method = CraftEntity.getDeclaredMethod("getEntity", CraftServer, net.minecraft.world.entity.Entity.class);
				bukkitEntity = method.invoke(null, CraftServer.cast(Bukkit.getServer()), entity);
				Entity_bukkitEntity.set(entity, bukkitEntity);
			}

			return (org.bukkit.entity.Entity) bukkitEntity;
		} catch (IllegalAccessException | IllegalArgumentException | SecurityException | NoSuchMethodException | InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Sound getBukkitSound(Object obj) {
		try {
			if (obj instanceof ClientboundSoundPacket packet) { return getBukkitSound(packet.getSound().value()); }
			if (!(obj instanceof SoundEvent)) { return null; }
			Method method = CraftSound.getDeclaredMethod("minecraftToBukkit", SoundEvent.class);
			return (Sound) method.invoke(method, obj);
		} catch (IllegalAccessException | IllegalArgumentException | SecurityException | NoSuchMethodException | InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static net.minecraft.advancements.Advancement getNMSAdvancement(Advancement advancement) {
		try {
			Object craftAdvancement = CraftAdvancement.cast(advancement);
			AdvancementHolder advancementHolder = (AdvancementHolder) craftAdvancement.getClass().getDeclaredMethod("getHandle").invoke(craftAdvancement);
			return advancementHolder.value();
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

	public static Block getBlock(Material block) {
		if (block == Material.AIR) { return Blocks.AIR; }
		return block.isBlock() ? ((BlockItem) getItem(new ItemStack(block))).getBlock() : null;
	}

	public static int getBlockId(Material block) {
		return Block.getId(getBlock(block).defaultBlockState());
	}

	public static World getRespawnWorld(Player player) {
		return MinecraftServer.getServer().getLevel(getEntityPlayer(player).getRespawnDimension()).getWorld();
	}

	//Get block destroy time per tick which is based on player
	public static float getPlayerDestroyTime(Player player, Material block) {
		return getEntityHuman(player).getDestroySpeed(getBlock(block).defaultBlockState());
	}

	//Get raw block destroy time which is not based on player
	public static float getBlockDestroyTime(Material block) {
		return getBlock(block).defaultBlockState().destroySpeed;
	}

	public static void popResource(Location location, ItemStack item) {
		BlockPos position = new BlockPos((int) location.getX(), (int) location.getY(), (int) location.getZ());
		Block.popResource(getWorld(location.getWorld()), position, asNMSCopy(item));
	}

	public static void setInvulnerableTicks(Player player, int ticks) {
		getEntityPlayer(player).spawnInvulnerableTime = ticks;
	}

	public static Abilities getPlayerAbilities(Player player) {
		return getEntityPlayer(player).getAbilities();
	}

	public static int getEntityId(Entity entity) {
		return Objects.requireNonNull(getEntity(entity)).getId();
	}

	public static void sendPacket(Player player, Packet<?> packet) {
		getEntityPlayer(player).connection.send(packet);
	}

	public static void handlePacket(Player player, Packet<?> packet) {
		try {
			Object connection = EntityPlayer_playerConnection.get(getEntityPlayer(player));
			Method handle = findMethod(true, null, connection.getClass(), Void.TYPE, null, packet.getClass());
			handle.invoke(connection, packet);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public static String getPacketType(Object packet) {
		try {
			return ((Packet<?>) packet).type().id().getPath();
		} catch (Exception ex) {
			return "";
		}
	}

	public static Collection<Channel> getConnections() {
		List<Connection> serverConnection = MinecraftServer.getServer().getConnection().getConnections();
		return serverConnection.stream().collect(Collectors.toMap(e -> e, e -> e.channel)).values();
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

		sendPacket(player, new ClientboundContainerSetSlotPacket(0, 0, slot, asNMSCopy(item)));
	}

	public static void setInstantBuild(Player player, boolean instantbuild, boolean clientSide, boolean serverSide) {
		Abilities abilities = getPlayerAbilities(player);

		if (clientSide) {
			boolean temp = abilities.instabuild;
			abilities.instabuild = instantbuild;
			sendPacket(player, new ClientboundPlayerAbilitiesPacket(abilities));
			abilities.instabuild = temp;
		}

		if (serverSide) {
			abilities.instabuild = instantbuild;
		}
	}

	public static void setChatVisibility(Player player, String visibility) {
		if (player == null) { return; }

		try {
			for (ChatVisiblity chat : ChatVisiblity.values()) {
				if (!chat.getKey().equalsIgnoreCase(visibility)) { continue; }
				EntityPlayer_chatVisibility.set(getEntityPlayer(player), chat);
			}
		} catch (IllegalAccessException | IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	public static String getChatVisibility(Player player) {
		return getEntityPlayer(player).getChatVisibility().getKey();
	}

	public static int getTickCount() {
		return MinecraftServer.getServer().getTickCount();
	}

	public static Channel getChannel(Player player) {
		try {
			ServerGamePacketListenerImpl packetListener = getEntityPlayer(player).connection;
			Connection connection = (Connection) getValue(ServerCommonPacketListenerImpl_connection, packetListener);
			return connection.channel;
		} catch (Exception ex) { return null; }
	}

	public static void resetOfflinePlayer(Location location, OfflinePlayer offlinePlayer) throws UnknownHostException {
		MinecraftServer server = MinecraftServer.getServer();
		ServerLevel serverLevel = getWorld(Bukkit.getWorlds().get(0));
		ServerPlayer entityPlayer = new ServerPlayer(server, serverLevel, new GameProfile(offlinePlayer.getUniqueId(), offlinePlayer.getName()), ClientInformation.createDefault());

		Connection networkManager = new Connection(PacketFlow.SERVERBOUND);
		EmbeddedChannel embeddedChannel = new EmbeddedChannel(networkManager); //For some reason this is needed: GameTestHelper#makeMockServerPlayerInLevel()
		CommonListenerCookie commonListenerCookie = CommonListenerCookie.createInitial(new GameProfile(offlinePlayer.getUniqueId(), offlinePlayer.getName()), false);

		entityPlayer.connection = new ServerGamePacketListenerImpl(server, networkManager, entityPlayer, commonListenerCookie) {
			public void send(Packet<?> packet, @org.jetbrains.annotations.Nullable PacketSendListener callbacks) {}
		};

		entityPlayer.connection.connection.setupInboundProtocol(GameProtocols.SERVERBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(MinecraftServer.getServer().registries().compositeAccess())), entityPlayer.connection);
		entityPlayer.connection.connection.address = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 0);

		Player player = (Player) getBukkitEntity(entityPlayer);
		player.loadData();
		player.teleport(location);
		Utils.dropItems(player);
		player.setExp(0);
		player.setTotalExperience(0);
		player.setLevel(0);
		player.teleport(new Location(Bukkit.getWorlds().get(0), 0, 65, 0));
		player.saveData();
		player.kick();
	}

	public static Player addFakePlayer(Location location, UUID uuid, boolean addPlayer, boolean hideOnline, boolean hideWorld) {
		if (uuid == null) { uuid = UUID.randomUUID(); }

		MinecraftServer server = MinecraftServer.getServer();
		ServerLevel world = getWorld(location.getWorld());
		ServerPlayer entityPlayer = new ServerPlayer(server, world, new GameProfile(uuid, " ".repeat(5)), ClientInformation.createDefault());

		Connection networkManager = new Connection(PacketFlow.SERVERBOUND);
		EmbeddedChannel embeddedChannel = new EmbeddedChannel(networkManager); //For some reason this is needed: GameTestHelper#makeMockServerPlayerInLevel()
		CommonListenerCookie commonListenerCookie = CommonListenerCookie.createInitial(new GameProfile(uuid, " ".repeat(5)), false);
        entityPlayer.connection = new ServerGamePacketListenerImpl(server, networkManager, entityPlayer, commonListenerCookie) {};
		entityPlayer.connection.teleport(location);

		if (addPlayer) { world.addNewPlayer(entityPlayer); }
		Player player = (Player) getBukkitEntity(entityPlayer);

		//Will hide from Bukkit.getOnlinePlayers()
		if (addPlayer && hideOnline) {
			List<ServerPlayer> players = server.getPlayerList().players;
			players.removeIf(e -> getBukkitEntity(e).getUniqueId().equals(player.getUniqueId()));
		}

		//Will hide from World.getPlayers(), but also will prevent mob spawning
		if (addPlayer && hideWorld) {
			world.players().removeIf(e -> getBukkitEntity(e).getUniqueId().equals(player.getUniqueId()));
		}

		player.setGameMode(GameMode.CREATIVE);
		player.setCollidable(false);
		player.setSleepingIgnored(true);
		player.setInvulnerable(true);
		player.setGravity(false);
		player.setAllowFlight(true);
		player.setFlying(true);
		return player;
	}

	public static void changeDimension(Player player, World.Environment environment) {
		if (player.getWorld().getEnvironment() == environment) { return; }

		Block portalBlock = null;
		if ((player.getWorld().getEnvironment() == World.Environment.NORMAL) && (environment == World.Environment.NETHER)) { portalBlock = Blocks.NETHER_PORTAL; }
		if ((player.getWorld().getEnvironment() == World.Environment.NORMAL) && (environment == World.Environment.THE_END)) { portalBlock = Blocks.END_PORTAL; }
		if ((player.getWorld().getEnvironment() == World.Environment.NETHER) && (environment == World.Environment.NORMAL)) { portalBlock = Blocks.NETHER_PORTAL; }
		if ((player.getWorld().getEnvironment() == World.Environment.NETHER) && (environment == World.Environment.THE_END)) { portalBlock = Blocks.END_PORTAL; }
		if ((player.getWorld().getEnvironment() == World.Environment.THE_END) && (environment == World.Environment.NORMAL)) { portalBlock = Blocks.END_PORTAL; }
		if ((player.getWorld().getEnvironment() == World.Environment.THE_END) && (environment == World.Environment.NETHER)) { portalBlock = Blocks.NETHER_PORTAL; }
		if (portalBlock == null) { return; }

		ServerPlayer serverPlayer = getEntityPlayer(player);
		PortalProcessor portalProcessor = new PortalProcessor((Portal) portalBlock, serverPlayer.blockPosition());
		serverPlayer.setPortalCooldown();

		DimensionTransition portalDestination = portalProcessor.getPortalDestination(getWorld(player.getWorld()), serverPlayer);
		if (portalDestination == null) { return; }
		serverPlayer.changeDimension(portalDestination);
		serverPlayer.portalProcess = null;
	}

	public static PlayerAdvancements getPlayerAdvancements(Player player) {
		return getEntityPlayer(player).getAdvancements();
	}

	public static void setPlayerAdvancements(Player player, Object playerAdvancements) {
		if (!(playerAdvancements instanceof PlayerAdvancements)) { return; }
		setValue(EntityPlayer_advancements, getEntityPlayer(player), playerAdvancements);
	}

	public static Player removeFakePlayer(Player player) {
		ServerPlayer entityPlayer = getEntityPlayer(player);
		ServerLevel world = getWorld(player.getWorld());
		world.removePlayerImmediately(entityPlayer, RemovalReason.DISCARDED);
		return player;
	}

	//This needed to update player chunks and make them tickable
	public static void updateFakePlayerChunks(Player player) {
		ServerLevel world = getWorld(player.getWorld());
		world.getChunkSource().move(getEntityPlayer(player));
	}

	//Open trading screen of a villager
	public static void startTrading(Player player, Villager villager) {
		try {
			net.minecraft.world.entity.npc.Villager entityVillager = getEntityVillager(villager);
			ServerPlayer entityPlayer = getEntityPlayer(player);
			EntityVillager_startTrading_Or_updateSpecialPrices.invoke(entityVillager, entityPlayer);
			player.openMerchant(villager, false);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
			e.printStackTrace();
		}
	}

	public static void selectTrade(Player player, int slot) {
		handlePacket(player, new ServerboundSelectTradePacket(slot));
	}

	public static int getAdvancementExperience(Advancement advancement) {
		return getNMSAdvancement(advancement).rewards().experience();
	}

	public static List<ResourceKey<LootTable>> getAdvancementLoot(Advancement advancement) {
		List<ResourceKey<LootTable>> loot = getNMSAdvancement(advancement).rewards().loot();
		return loot.isEmpty() ? List.of() : loot;
	}

	public static List<ResourceLocation> getAdvancementRecipes(Advancement advancement) {
		return getNMSAdvancement(advancement).rewards().recipes();
	}

	public static String[] getAdvancementFunction(Advancement advancement) {
		Optional<CacheableFunction> function = getNMSAdvancement(advancement).rewards().function();
		if (function.isEmpty()) { return null; }

		ResourceLocation resourceLocation = (ResourceLocation) getValue(CacheableFunction_id, function.get());
		return new String[] { resourceLocation.getNamespace(), resourceLocation.getPath() };
	}

	public static void setAdvancementFunction(Advancement advancement, String namespace, String path) {
		Optional<CacheableFunction> function = getNMSAdvancement(advancement).rewards().function();
		if (function.isEmpty()) { return; }

		setValue(CacheableFunction_resolved, function.get(), false);
		setValue(CacheableFunction_function, function.get(), null);
		setResourceLocation((ResourceLocation) getValue(CacheableFunction_id, function.get()), namespace, path);
	}

	public static boolean getAnnounceChat(Advancement advancement) {
		DisplayInfo displayInfo = getNMSAdvancement(advancement).display().orElse(null);
		return ((displayInfo != null) && displayInfo.shouldAnnounceChat());
	}

	public static void setAnnounceChat(Advancement advancement, @Nullable PlayerAdvancementDoneEvent event, boolean announceChat) {
		if (PaperUtils.isPaper && (event != null) && !announceChat) { event.message(null); }
		DisplayInfo displayInfo = new DisplayInfo(null, null, null, null, null, false, true, false);
		Field DisplayInfo_announceChat = getField(DisplayInfo.class, boolean.class, null, displayInfo, displayInfo.shouldAnnounceChat(), true);
		getNMSAdvancement(advancement).display().ifPresent(display -> setValue(DisplayInfo_announceChat, display, announceChat));
	}

	public static String[] getResourceLocation(ResourceKey<?> resourceKey) {
		return getResourceLocation(resourceKey.location());
	}

	public static String[] getResourceLocation(ResourceLocation resourceLocation) {
		return new String[] { resourceLocation.getNamespace(), resourceLocation.getPath() };
	}

	public static void setResourceLocation(ResourceKey<?> resourceKey, String namespace, String path) {
		setResourceLocation(resourceKey.location(), namespace, path);
	}

	public static void setResourceLocation(ResourceLocation resourceLocation, String namespace, String path) {
		Field ResourceLocation_namespace = getField(ResourceLocation.class, String.class, null, resourceLocation, resourceLocation.getNamespace(), true, null, Modifier.STATIC);
		Field ResourceLocation_path = getField(ResourceLocation.class, String.class, null, resourceLocation, resourceLocation.getPath(), true, null, Modifier.STATIC);
		setValue(ResourceLocation_namespace, resourceLocation, namespace);
		setValue(ResourceLocation_path, resourceLocation, path);
	}

	//Simulate shift+click on specific slot on opened inventory
	public static void quickMoveStack(Player player, int slot) {
		ServerPlayer entityPlayer = getEntityPlayer(player);
		entityPlayer.containerMenu.quickMoveStack(entityPlayer, slot);
	}

	//Gets player's reputation from villager (all types)
	public static int getPlayerReputation(Villager villager, Player player) {
		return getEntityVillager(villager).getPlayerReputation(getEntityPlayer(player));
	}

	public static int getPlayerReputation(Villager villager, UUID uuid, String type) {
		try {
			GossipType gossipType = reputations.get(type);
			GossipContainer container = getEntityVillager(villager).getGossips();
			Map<UUID, Object> gossips = (Map<UUID, Object>) GossipContainer_gossips.get(container);
			if (!gossips.containsKey(uuid)) { return -1; }
			Object2IntMap<GossipType> entries = (Object2IntMap<GossipType>) EntityGossips_entries.get(gossips.get(uuid));
			return entries.getOrDefault(gossipType, -1);
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
			return -1;
		}
	}

	public static void setPlayerReputation(Villager villager, UUID uuid, String type, int amount) {
		try {
			GossipType gossipType = reputations.get(type);
			GossipContainer container = getEntityVillager(villager).getGossips();
			Map<UUID, Object> gossips = (Map<UUID, Object>) GossipContainer_gossips.get(container);
			if (!gossips.containsKey(uuid)) { gossips.put(uuid, newInstance(GossipContainer_EntityGossips, null, null, true, false)); }
			Object2IntMap<GossipType> entries = (Object2IntMap<GossipType>) EntityGossips_entries.get(gossips.get(uuid));
			entries.put(gossipType, amount);
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
		}
	}

	public static boolean registerBrewingRecipe(Object base, Material ingredient, Object result) {
		return registerBrewingRecipe((Holder<Potion>) base, ingredient, (Holder<Potion>) result);
	}

	public static Holder<Potion> getNMSPotion(PotionType potionType) {
		return CraftPotionType.bukkitToMinecraftHolder(potionType);
	}

	public static String getPotionRegistryName(Object potion) {
		return getPotionRegistryName((Holder<Potion>) potion);
	}

	public static String getPotionRegistryName(Holder<Potion> potion) {
		return Potion.getName(Optional.of(potion), "");
	}

	public static String getPotionTag(ItemStack item) {
		PotionContents potionContents = asNMSCopy(item).get(DataComponents.POTION_CONTENTS);
		if (potionContents == null) { return ""; }
		Holder<Potion> potionHolder = potionContents.potion().orElse(null);
		if (potionHolder == null) { return ""; }
		return getPotionRegistryName(potionHolder).replace("minecraft:", "");
	}

	//!!! This will soft lock the game, because FUCKING MOJANG cannot decode custom potion tag client side
	public static ItemStack setPotionTag(ItemStack item, String name) {
		return setItemNbt(item, List.of("components", "minecraft:potion_contents", "potion"), name);
	}

	public static Object fakePotionTags(Object pck, String potionTag, Predicate<ItemStack> predicate) {
		if (pck instanceof ClientboundContainerSetSlotPacket packet) {
			ItemStack itemStack = asBukkitMirror(packet.getItem());
			if (!predicate.test(itemStack)) { return packet; }
			net.minecraft.world.item.ItemStack nmsStack = asNMSCopy(setPotionTag(itemStack, potionTag));
			return new ClientboundContainerSetSlotPacket(packet.getContainerId(), packet.getStateId(), packet.getSlot(), Objects.requireNonNull(nmsStack));
		}

		if (pck instanceof ClientboundContainerSetContentPacket packet) {
			ItemStack carriedItem = asBukkitMirror(packet.getCarriedItem());

			net.minecraft.world.item.ItemStack[] collect = packet.getItems().stream().map(e -> {
				ItemStack itemStack = asBukkitMirror(e);
				if (!predicate.test(itemStack)) { return e; }
				return asNMSCopy(setPotionTag(itemStack, potionTag));
			}).toArray(net.minecraft.world.item.ItemStack[]::new);

			if (predicate.test(carriedItem)) { carriedItem = setPotionTag(carriedItem, potionTag); }
			net.minecraft.world.item.ItemStack nmsStack = Objects.requireNonNull(asNMSCopy(carriedItem));
			NonNullList<net.minecraft.world.item.ItemStack> contents = NonNullList.of(net.minecraft.world.item.ItemStack.EMPTY, collect);
			return new ClientboundContainerSetContentPacket(packet.getContainerId(), packet.getStateId(), contents, nmsStack);
		}

		return pck;
	}

	public static <E> Registry<E> getRegistry(ResourceKey<? extends Registry<? extends E>> key) {
		return MinecraftServer.getServer().registries().compositeAccess().registryOrThrow(key);
	}

	public static void setRegistryFrozen(Object registry, boolean frozen) {
		try {
			RegistryMaterials_frozen.set(registry, frozen);
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public static void setRegistryMap(Object registry, Object hashMap) {
		Field[] fields = MappedRegistry.class.getDeclaredFields();

		for (Field field : fields) {
			if (!field.getType().equals(Map.class)) { continue; }
			if (getValue(field, registry) != null) { continue; }
			setValue(field, registry, hashMap); break;
		}
	}

	public static Holder<Potion> registerInstantPotion(String name) {
		try {
			setRegistryFrozen(POTION, false);
			Potion placeHolder = new Potion();
			POTION.createIntrusiveHolder(placeHolder);
			Holder<Potion> potionHolder = (Holder<Potion>) Potions_register.invoke(Potions_register, name, placeHolder);
			setRegistryFrozen(POTION, true);

			Field SimpleRegistry_map = Objects.requireNonNull(getField(org.bukkit.Registry.SimpleRegistry.class, Map.class, null, true));
			Map<NamespacedKey, Object> immutableMap = (Map<NamespacedKey, Object>) getValue(SimpleRegistry_map, org.bukkit.Registry.POTION);
			HashMap<NamespacedKey, Object> map = new HashMap<>(immutableMap);
			map.put(NamespacedKey.minecraft(name), PotionType.WATER);
			setValue(SimpleRegistry_map, org.bukkit.Registry.POTION, map);
			return potionHolder;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static boolean registerBrewingRecipe(Holder<Potion> base, Material ingredient, Holder<Potion> result) {
		try {
			Item potionIngredient = getItem(new ItemStack(ingredient));
			PotionBrewing.Builder builder = new PotionBrewing.Builder(FeatureFlags.VANILLA_SET);
			builder.addMix(base, potionIngredient, result);
			PotionBrewing brewing = builder.build();

			if (PotionBrewing_potionMixes == null) {
				for (Field field : PotionBrewing.class.getDeclaredFields()) {
					field.setAccessible(true);
					if (!field.getType().equals(List.class)) { continue; }
					List<?> potions = (List<?>) field.get(brewing);
					if (potions.isEmpty()) { continue; }
					PotionBrewing_potionMixes = field;
				}
			}

			PotionBrewing potionBrewing = MinecraftServer.getServer().potionBrewing();
			ArrayList<Object> potionMixes = new ArrayList<>((List<?>) PotionBrewing_potionMixes.get(potionBrewing));
			potionMixes.add(((List<Object>) PotionBrewing_potionMixes.get(brewing)).get(0));
			PotionBrewing_potionMixes.set(potionBrewing, potionMixes);
			return true;
		} catch (IllegalAccessException | IllegalArgumentException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static List<String> getVanillaPotions(boolean brewable, boolean custom, Predicate<String> predicate) {
		Stream<Potion> potionStream = POTION.stream().filter(potion -> !potion.getEffects().isEmpty());
		if (brewable) { potionStream = potionStream.filter(e -> MinecraftServer.getServer().potionBrewing().isBrewablePotion(Holder.direct(e))); }

		//Goofy way to get rid of "minecraft:" or "anything:", or ":"
		List<String> potions = potionStream.map(e -> Arrays.stream(Potion.getName(Optional.of(Holder.direct(e)), "").split(":")).reduce((a, b) -> b).stream().toList().get(0)).toList();
		if (!custom) { potions = potions.stream().filter(predicate).toList(); }
		return potions;
	}

	//FUCK MOJANG DEVELOPERS, how the fuck do you forget to set value of a holder
	//when registering a new mapping, how brain-damaged you must be.
	public static void fixHolder(Registry<?> registry, Object value, int ...currentId)
	{
		try {
			if ((currentId.length == 0) || (currentId[0] < 0)) { currentId = new int[] { (int) registry.holders().count()-1 }; }
			Optional<Holder<?>> holder = (Optional<Holder<?>>) RegistryMaterials_getHolder.invoke(registry, currentId[0]);
			Holder.Reference<?> holder_c = (Holder.Reference<?>) holder.get();

			for (Field field : Holder.Reference.class.getDeclaredFields()) {
				field.setAccessible(true);
				if (field.get(holder_c) == null) { field.set(holder_c, value); }
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	//MIX_ID is required to prevent weird visual bug when client side and server side breaking are overlapping
	//Making two animations replace each other at the same time
	public static void destroyBlockProgress(org.bukkit.block.Block block, int progress, int mix_id) {
		for (Player player : block.getWorld().getPlayers()) {
			double d0 = (double) block.getX() - player.getLocation().getX();
			double d1 = (double) block.getY() - player.getLocation().getY();
			double d2 = (double) block.getZ() - player.getLocation().getZ();

			if (d0 * d0 + d1 * d1 + d2 * d2 >= 1024.0D) { continue; }
			BlockPos location = new BlockPos(block.getX(), block.getY(), block.getZ());
			sendPacket(player, new ClientboundBlockDestructionPacket(player.getEntityId()+mix_id, location, progress));
		}
	}

	public static void destroyBlock(Player player, org.bukkit.block.Block block) {
		destroyBlockProgress(block, -1, 0);
		BlockPos location = new BlockPos(block.getX(), block.getY(), block.getZ());
		int id = getBlockId(block.getType());
		sendPacket(player, new ClientboundLevelEventPacket(2001, location, id, false));
		player.breakBlock(block);
	}

	public static void dropBlockItem(org.bukkit.block.Block block, @Nullable Player player, ItemStack itemStack) {
		double x = block.getLocation().getX() + 0.5;
		double y = block.getLocation().getY() + 0.5;
		double z = block.getLocation().getZ() + 0.5;

		double f = (Utils.ITEM_HEIGHT / 2.0F);
		double d0 = x + Utils.randomRange(-Utils.ITEM_SPAWN_OFFSET, Utils.ITEM_SPAWN_OFFSET);
		double d1 = y + Utils.randomRange(-Utils.ITEM_SPAWN_OFFSET, Utils.ITEM_SPAWN_OFFSET) - f;
		double d2 = z + Utils.randomRange(-Utils.ITEM_SPAWN_OFFSET, Utils.ITEM_SPAWN_OFFSET);

		ItemEntity entityItem = new ItemEntity(getWorld(block.getWorld()), d0, d1, d2, asNMSCopy(itemStack));
		getBukkitEntity(entityItem).setVelocity(new Vector(Math.random()*0.2F-0.1F, 0.2F, Math.random()*0.2F-0.1F));
		List<org.bukkit.entity.Item> items = new ArrayList<>(Arrays.asList((org.bukkit.entity.Item) getBukkitEntity(entityItem)));

		if (player != null) {
			BlockDropItemEvent dropEvent = new BlockDropItemEvent(block, block.getState(), player, items);
			Bukkit.getServer().getPluginManager().callEvent(dropEvent);
			if (dropEvent.isCancelled()) { return; } else { items = dropEvent.getItems(); }
		}

		for (org.bukkit.entity.Item drop : items) {
			block.getWorld().spawn(drop.getLocation(), org.bukkit.entity.Item.class, (item) -> {
				item.setItemStack(drop.getItemStack());
				item.setVelocity(drop.getVelocity());
			});
		}
	}

	public static Object getSetLevels(Object obj) throws IllegalAccessException {
		Object levels = MinecraftServer_levels.get(MinecraftServer.getServer());
		MinecraftServer_levels.set(MinecraftServer.getServer(), obj);
		return levels;
	}

	public static Object getSetCustomFunctionDataTicking(Object obj) throws IllegalAccessException {
		Object functionManager = MinecraftServer.getServer().getFunctions();
		Object ticking = CustomFunctionData_ticking.get(functionManager);
		CustomFunctionData_ticking.set(functionManager, obj);
		return ticking;
	}

	public static Object getSetCustomFunctionPostReload(Object obj) throws IllegalAccessException {
		Object functionManager = MinecraftServer.getServer().getFunctions();
		Object postReload = CustomFunctionData_postReload.get(functionManager);
		CustomFunctionData_postReload.set(functionManager, obj);
		return postReload;
	}

	public static Object getSetImageFrame_itemFrames(Object obj, boolean checkNull) {
		Class<?> ImageFrame = loadClass("com.loohp.imageframe.ImageFrame", false);
		if ((ImageFrame == null) || (checkNull && (obj == null))) { return null; }

		Class<?> AnimatedFakeMapManager = loadClass("com.loohp.imageframe.objectholders.AnimatedFakeMapManager", true);
		Field ImageFrame_animatedFakeMapManager = getField(ImageFrame, AnimatedFakeMapManager, null, false);
		Field AnimatedFakeMapManager_itemFrames = getField(AnimatedFakeMapManager, Map.class, UUID.class, true);

		Object animatedFakeMapManager = getValue(ImageFrame_animatedFakeMapManager, null);
		Object itemFrames = getValue(AnimatedFakeMapManager_itemFrames, animatedFakeMapManager);
		setValue(AnimatedFakeMapManager_itemFrames, animatedFakeMapManager, obj);
		return itemFrames;
	}

	public static <T> T getItemNbt(ItemStack itemStack, List<String> location) {
		if (location.isEmpty()) { return null; }
		location = new ArrayList<>(location); //Convert list to mutable list
		net.minecraft.world.item.ItemStack item = Objects.requireNonNull(asNMSCopy(itemStack));
		CompoundTag tag = (CompoundTag) item.saveOptional(MinecraftServer.getServer().registryAccess());
		if (tag.isEmpty()) { return null; }

		while (!location.isEmpty() && (tag.getTagType(location.getFirst()) == Tag.TAG_COMPOUND)) {
			tag = tag.getCompound(location.getFirst());
			location.removeFirst();
		}

		if (location.isEmpty()) { return (T) tag; }
		if (tag.getTagType(location.getFirst()) == Tag.TAG_BYTE) { return (T) Boolean.valueOf(tag.getBoolean(location.getFirst())); }
		if (tag.getTagType(location.getFirst()) == Tag.TAG_INT) { return (T) Integer.valueOf(tag.getInt(location.getFirst())); }
		if (tag.getTagType(location.getFirst()) == Tag.TAG_STRING) { return (T) tag.getString(location.getFirst()); }
		if (tag.getTagType(location.getFirst()) == Tag.TAG_SHORT) { return (T) (Short) tag.getShort(location.getFirst()); }
		if (tag.getTagType(location.getFirst()) == Tag.TAG_FLOAT) { return (T) (Float) tag.getFloat(location.getFirst()); }
		if (tag.getTagType(location.getFirst()) == Tag.TAG_DOUBLE) { return (T) (Double) tag.getDouble(location.getFirst()); }
		if (tag.getTagType(location.getFirst()) == Tag.TAG_LONG) { return (T) (Long) tag.getLong(location.getFirst()); }
		if (tag.getTagType(location.getFirst()) == Tag.TAG_BYTE_ARRAY) { return (T) tag.getByteArray(location.getFirst()); }
		if (tag.getTagType(location.getFirst()) == Tag.TAG_INT_ARRAY) { return (T) tag.getIntArray(location.getFirst()); }
		if (tag.getTagType(location.getFirst()) == Tag.TAG_LONG_ARRAY) { return (T) tag.getLongArray(location.getFirst()); }
		return null;
	}

	public static ItemStack setItemNbt(ItemStack itemStack, List<String> location, Object value) {
		if (location.isEmpty()) { return null; }
		location = new ArrayList<>(location); //Convert list to mutable list
		net.minecraft.world.item.ItemStack item = Objects.requireNonNull(asNMSCopy(itemStack));
		CompoundTag original = (CompoundTag) item.saveOptional(MinecraftServer.getServer().registryAccess());
		if (original.isEmpty()) { original = new CompoundTag(); }
		CompoundTag tag = original;

		while ((location.size() > 1) && ((tag.getTagType(location.getFirst()) == Tag.TAG_COMPOUND) || !tag.contains(location.getFirst()))) {
			if (!tag.contains(location.getFirst())) { tag.put(location.getFirst(), new CompoundTag()); }
			tag = tag.getCompound(location.getFirst());
			location.removeFirst();
		}

		if (location.isEmpty()) { return null; }
		if (value.getClass().equals(Byte.class)) { tag.putByte(location.getFirst(), (Byte) value); }
		if (value.getClass().equals(Integer.class)) { tag.putInt(location.getFirst(), (Integer) value); }
		if (value.getClass().equals(String.class)) { tag.putString(location.getFirst(), (String) value); }
		if (value.getClass().equals(Short.class)) { tag.putShort(location.getFirst(), (Short) value); }
		if (value.getClass().equals(Float.class)) { tag.putFloat(location.getFirst(), (Float) value); }
		if (value.getClass().equals(Double.class)) { tag.putDouble(location.getFirst(), (Double) value); }
		if (value.getClass().equals(Long.class)) { tag.putLong(location.getFirst(), (Long) value); }
		if (value.getClass().equals(byte[].class)) { tag.putByteArray(location.getFirst(), (byte[]) value); }
		if (value.getClass().equals(int[].class)) { tag.putIntArray(location.getFirst(), (int[]) value); }
		if (value.getClass().equals(long[].class)) { tag.putLongArray(location.getFirst(), (long[]) value); }

		net.minecraft.world.item.ItemStack itemStack1 = net.minecraft.world.item.ItemStack.parseOptional(MinecraftServer.getServer().registryAccess(), original);
		return asBukkitMirror(itemStack1);
	}

	public static void setBlockNbt(org.bukkit.block.Block block, String name, Object value, boolean applyPhysics) {
		BlockPos blockPos = new BlockPos(block.getX(), block.getY(), block.getZ());
		ServerLevel serverLevel = getWorld(block.getLocation().getWorld());

		if (serverLevel == null) { return; }
		BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
		if (blockEntity == null) { return; }
		CompoundTag tag = blockEntity.saveWithoutMetadata(MinecraftServer.getServer().registryAccess());

		tag.remove(name);
		if (value.getClass().equals(Boolean.class)) { tag.putBoolean(name, (Boolean) value); }
		if (value.getClass().equals(Integer.class)) { tag.putInt(name, (Integer) value); }
		if (value.getClass().equals(String.class)) { tag.putString(name, (String) value); }

		blockEntity.loadWithComponents(tag, MinecraftServer.getServer().registryAccess()); //Loading NBT doesn't trigger physics update of block itself
		if (applyPhysics) { blockEntity.setChanged(); } //This only updates block around (AND IT DOESN'T WORK, FUCK YOU MOJANG)
		if (applyPhysics) { NeighborUpdater.executeUpdate(serverLevel, blockEntity.getBlockState(), blockPos, blockEntity.getBlockState().getBlock(), blockPos, true); }
	}

	public static void forceUpdateBlock(org.bukkit.block.Block block) {
		BlockPos blockPos = new BlockPos(block.getX(), block.getY(), block.getZ());
		ServerLevel serverLevel = getWorld(block.getLocation().getWorld());
		BlockState blockState = serverLevel.getBlockState(blockPos);
		NeighborUpdater.executeUpdate(serverLevel, blockState, blockPos, blockState.getBlock(), blockPos, true);
	}

	public static void forceUpdateNeighbors(org.bukkit.block.Block block, int distance, @Nullable Material type, @Nullable Material exclude) {
		for (org.bukkit.block.Block nblock: Utils.getNearbyBlocks(block.getLocation(), type, distance)) {
			if ((exclude != null) && (nblock.getType() == exclude)) { continue; }
			if (!nblock.getLocation().equals(block.getLocation())) { forceUpdateBlock(nblock); }
		}
	}

	public static void updateNeighborsInFront(org.bukkit.block.Block block) {
		BlockPos block_pos = new BlockPos(block.getX(), block.getY(), block.getZ());
		ServerLevel block_world = getWorld(block.getWorld());
		BlockState block_sate = block_world.getBlockState(block_pos);
		net.minecraft.world.level.block.ComparatorBlock block_nms = (net.minecraft.world.level.block.ComparatorBlock) block_sate.getBlock();

		Direction enumdirection = block_sate.getValue(DiodeBlock.FACING);
		BlockPos blockposition1 = block_pos.relative(enumdirection.getOpposite());

		block_world.neighborChanged(blockposition1, block_nms, block_pos);
		block_world.updateNeighborsAtExceptFromFacing(blockposition1, block_nms, enumdirection);
	}

	public static int getComparatorOutputSignal(org.bukkit.block.Block block, @Nullable BlockData blockData, int power) {
		int j = getAlternateSignal(block);
		if (blockData == null) { blockData = block.getBlockData(); }
		return ((((Comparator) blockData).getMode() == Comparator.Mode.SUBTRACT)) ? Math.max(power-j, 0) : power;
	}

	//This is useless and will only set power for 1 tick, because in next tick it will calculate power signal again
	public static void setComparatorPower(org.bukkit.block.Block block, int power, boolean applyPhysics) {
		org.bukkit.block.data.type.Comparator comparator = ((Comparator) block.getBlockData());
		comparator.setPowered(power > 0);
		block.setBlockData(comparator, false);
		setBlockNbt(block, "OutputSignal", power, applyPhysics);
	}

	public static boolean isRedstoneConductor(org.bukkit.block.Block block) {
		BlockPos blockPos = new BlockPos(block.getX(), block.getY(), block.getZ());
		ServerLevel serverLevel = getWorld(block.getLocation().getWorld());
		BlockState blockState = serverLevel.getBlockState(blockPos);
		return blockState.isRedstoneConductor(serverLevel, blockPos);
	}

	//This is used to get signal that is going inside comparator
	public static int getAlternateSignal(org.bukkit.block.Block block) {
		if (block.getType() != Material.COMPARATOR) { return 0; }

		BlockPos pos = new BlockPos(block.getX(), block.getY(), block.getZ());
		ServerLevel serverLevel = getWorld(block.getLocation().getWorld());
		BlockEntity blockEntity = serverLevel.getBlockEntity(pos);
		BlockState state = blockEntity.getBlockState();
		SignalGetter world = blockEntity.getLevel();

		Direction enumdirection = state.getValue(DiodeBlock.FACING);
		Direction enumdirection1 = enumdirection.getClockWise();
		Direction enumdirection2 = enumdirection.getCounterClockWise();
		boolean flag = false;

		return Math.max(world.getControlInputSignal(pos.relative(enumdirection1), enumdirection1, flag), world.getControlInputSignal(pos.relative(enumdirection2), enumdirection2, flag));
	}

	public static void unpackLoot(org.bukkit.block.Block block) {
		if (!(block.getState() instanceof org.bukkit.block.Container)) { return; }

		ServerLevel serverLevel = getWorld(block.getLocation().getWorld());
		if (serverLevel == null) { return; }
		BlockEntity blockEntity = serverLevel.getBlockEntity(new BlockPos(block.getX(), block.getY(), block.getZ()));
		if (blockEntity == null) { return; }

		if (!(blockEntity instanceof RandomizableContainerBlockEntity lootableBlock)) { return; }
		lootableBlock.unpackLootTable(null);
	}

	public static BlockData getChangedBlockData(BlockPhysicsEvent event) {
		return (BlockData) getValue(BlockPhysicsEvent_changed, event);
	}

	public static Map<org.bukkit.entity.LivingEntity, Double> getAffectedEntities(PotionSplashEvent event) {
		return (Map<org.bukkit.entity.LivingEntity, Double>) getValue(PotionSplashEvent_affectedEntities, event);
	}

	public static List<org.bukkit.entity.LivingEntity> getAffectedEntities(AreaEffectCloudApplyEvent event) {
		return (List<org.bukkit.entity.LivingEntity>) getValue(AreaEffectCloudApplyEvent_affectedEntities, event);
	}

	//ItemStack to remove can be null which means it will not take any items at all from source inventory and only dispense.
	//If slot is positive it will try to take item from that slot, but if it fails it will take from first available,
	//slot can be also negative, then it will immediately take from first available slot, if item is not found it will (TODO: fail or succeed?)
	public static boolean dispenseItem(org.bukkit.block.Block source, ItemStack drop, @Nullable ItemStack remove, int slot) {
		if (source.getType() == Material.DISPENSER) { return dispenseDispenser(source, drop, remove, slot); }
		if (source.getType() == Material.DROPPER) { return dispenseDropper(source, drop, remove, slot); }
		if (source.getType() == Material.CRAFTER) { return dispenseDropper(source, drop, remove, slot); }
		return false;
	}

	//TODO: Should we remove item, if result of dispense is modified item? (Currently: NO)
	private static boolean dispenseDispenser(org.bukkit.block.Block source, ItemStack drop, @Nullable ItemStack remove, int slot) {
		if (drop.getAmount() <= 0) { return true; }
		if ((source.getType() != Material.DISPENSER) && (source.getType() != Material.DROPPER)) { return false; }
		if (remove == null) { remove = new ItemStack(Material.AIR); }
		if (remove.getType() == Material.AIR) { slot = -1; }
		drop = drop.clone(); remove = remove.clone(); //Make sure drop and remove are not the same item

		BlockPos blockPos = new BlockPos(source.getX(), source.getY(), source.getZ());
		ServerLevel serverLevel = getWorld(source.getLocation().getWorld());
		BlockState blockState = serverLevel.getBlockState(blockPos);
		DispenserBlockEntity tileentitydispenser = serverLevel.getBlockEntity(blockPos, BlockEntityType.DISPENSER).orElse(null);
		BlockSource blockSource = new BlockSource(serverLevel, blockPos, blockState, tileentitydispenser);

		DispenseItemBehavior dispenseItemBehavior = (source.getType() == Material.DISPENSER) ? DispenserBlock.DISPENSER_REGISTRY.get(asNMSCopy(drop).getItem()) : new DefaultDispenseItemBehavior(true);
		net.minecraft.world.item.ItemStack result = dispenseItemBehavior.dispense(blockSource, asNMSCopy(drop));
		if (result.getCount() == drop.getAmount()) { return false; } //It failed to dispense or item was modified inside called event

		org.bukkit.block.Container container = (org.bukkit.block.Container) source.getState();
		remove.setAmount(1); //We always remove by 1 item
		drop.setAmount(result.getCount()); //Update drop amount after dispense

		//IDC, I will not check if item doesn't exist, I will just remove it
		if (slot < 0) {
			Utils.removeItem(container.getInventory(), remove); //Fuck you spigot, can't even make simple method to remove items
		} else {
			ItemStack itemStack = container.getInventory().getItem(slot);
			if (itemStack == null) { itemStack = new ItemStack(Material.AIR); }
			itemStack.setAmount(itemStack.getAmount()-1);
			if (itemStack.getAmount() <= 0) { itemStack = new ItemStack(Material.AIR); }
			container.getInventory().setItem(slot, itemStack);
		}

		//Sadly, but dispense only can dispense by 1 item
		return dispenseDispenser(source, drop, remove, slot);
	}

	private static boolean dispenseDropper(org.bukkit.block.Block source, ItemStack drop, ItemStack remove, int slot) {
		if (drop.getAmount() <= 0) { return true; }
		if (source.getType() != Material.DROPPER) { return false; }
		if (remove == null) { remove = new ItemStack(Material.AIR); }
		if (remove.getType() == Material.AIR) { slot = -1; }
		drop = drop.clone(); remove = remove.clone();

		BlockPos blockPos = new BlockPos(source.getX(), source.getY(), source.getZ());
		ServerLevel serverLevel = getWorld(source.getLocation().getWorld());
		DispenserBlockEntity tileentitydispenser = serverLevel.getBlockEntity(blockPos, BlockEntityType.DROPPER).orElse(null);
		Direction enumdirection = serverLevel.getBlockState(blockPos).getValue(DropperBlock.FACING);
		Container iinventory = HopperBlockEntity.getContainerAt(serverLevel, blockPos.relative(enumdirection));
		BlockFace blockFace = ((Directional) source.getBlockData()).getFacing();
		org.bukkit.block.Block dblock = source.getRelative(blockFace);

		//If there is no container in front then just act like dispenser
		if (!(dblock.getState() instanceof org.bukkit.block.Container container)) {
			return dispenseDispenser(source, drop, remove, slot);
		}

		ItemStack event_item = Utils.cloneItem(drop, 1); //We always move by 1 item
		Inventory destinationInventory = (dblock.getState() instanceof DoubleChest) ? ((DoubleChest) dblock.getState()).getInventory() : container.getInventory();
		InventoryMoveItemEvent event = new InventoryMoveItemEvent(tileentitydispenser.getOwner().getInventory(), event_item, destinationInventory, true);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) { return false; }

		net.minecraft.world.item.ItemStack itemstack1 = HopperBlockEntity.addItem(tileentitydispenser, iinventory, asNMSCopy(event.getItem()), enumdirection.getOpposite());
		if (!(event.getItem().equals(event_item) && itemstack1.isEmpty())) { return false; } //If item was modified or was not moved successfully return

		org.bukkit.block.Container dropper = (org.bukkit.block.Container) source.getState();
		remove.setAmount(1); //We always remove by 1 item
		drop.setAmount(drop.getAmount()-1); //Update drop amount after moving

		//IDC, I will not check if item doesn't exist, I will just remove it
		if (slot < 0) {
			Utils.removeItem(dropper.getInventory(), remove); //Fuck you spigot, can't even make simple method to remove items
		} else {
			ItemStack itemStack = dropper.getInventory().getItem(slot);
			if (itemStack == null) { itemStack = new ItemStack(Material.AIR); }
			itemStack.setAmount(itemStack.getAmount()-1);
			if (itemStack.getAmount() <= 0) { itemStack = new ItemStack(Material.AIR); }
			dropper.getInventory().setItem(slot, itemStack);
		}

		return dispenseDropper(source, drop, remove, slot);
	}

	public static Packet<?> editSpawnPacket(Object packet, @Nullable Boolean isFlat, @Nullable World.Environment env) {
		if (packet instanceof ClientboundLoginPacket lPacket) {
			int playerId = lPacket.playerId();
			boolean hardcore = lPacket.hardcore();
			Set<ResourceKey<Level>> levels = lPacket.levels();
			int maxPlayers = lPacket.maxPlayers();
			int chunkRadius = lPacket.chunkRadius();
			int simulationDistance = lPacket.simulationDistance();
			boolean reducedDebugInfo = lPacket.reducedDebugInfo();
			boolean showDeathScreen = lPacket.showDeathScreen();
			boolean doLimitedCrafting = lPacket.doLimitedCrafting();
			boolean enforcesSecureChat = lPacket.enforcesSecureChat();

			CommonPlayerSpawnInfo commonPlayerSpawnInfo = editCommonPlayerSpawnInfo(lPacket.commonPlayerSpawnInfo(), isFlat, env);
			return new ClientboundLoginPacket(playerId, hardcore, levels, maxPlayers, chunkRadius, simulationDistance, reducedDebugInfo, showDeathScreen, doLimitedCrafting, commonPlayerSpawnInfo, enforcesSecureChat);
		}

		if (packet instanceof ClientboundRespawnPacket rPacket) {
			CommonPlayerSpawnInfo commonPlayerSpawnInfo = editCommonPlayerSpawnInfo(rPacket.commonPlayerSpawnInfo(), isFlat, env);
			return new ClientboundRespawnPacket(commonPlayerSpawnInfo, rPacket.dataToKeep());
		}

		return null;
	}

	public static World getSpawnPacketWorld(Object packet) {
		if (packet instanceof ClientboundLoginPacket lPacket) {
			ResourceKey<Level> dimension = lPacket.commonPlayerSpawnInfo().dimension();
			return MinecraftServer.getServer().getLevel(dimension).getWorld();
		}

		if (packet instanceof ClientboundRespawnPacket rPacket) {
			ResourceKey<Level> dimension = rPacket.commonPlayerSpawnInfo().dimension();
			return MinecraftServer.getServer().getLevel(dimension).getWorld();
		}

		return null;
	}

	public static ClientboundLevelChunkWithLightPacket setPacketChunkBiome(World world, Object packet, Object customNmsBiome, @Nullable String customBiomeName, @Nullable HashMap<String, Object> customBiomeMap) {
		ServerLevel level = getWorld(world);
		int chunkX = ((ClientboundLevelChunkWithLightPacket) packet).getX();
		int chunkZ = ((ClientboundLevelChunkWithLightPacket) packet).getZ();
		LevelChunk levelChunk = level.getChunk(chunkX, chunkZ);
		List<Biome> saved_biomes = new ArrayList<>();

		//Replace with custom biome and save old biomes
		//Remember for each custom biome we register copy of that biome for every ingame biomes
		//So we first get section biome name and then check if there is custom biome for that biome
		for (LevelChunkSection section : levelChunk.getSections()) {
			for (int x = 0; x < 4; ++x) {
				for (int z = 0; z < 4; ++z) {
					for (int y = 0; y < 4; ++y) {
						Holder<Biome> saved_biome = section.getNoiseBiome(x, y, z);
						saved_biomes.add(saved_biome.value());
						String biomeName = saved_biome.unwrapKey().get().location().toString().replaceAll("[:/]", "_");

						Object adaptedCustomBiome = ((customBiomeName != null) && (customBiomeMap != null)) ? customBiomeMap.get(customBiomeName + "_" + biomeName) : null;
						section.setBiome(x, y, z, Holder.direct((Biome) ((adaptedCustomBiome != null) ? adaptedCustomBiome : customNmsBiome)));
					}
				}
			}
		}

		//Create packet with custom biomes
		ClientboundLevelChunkWithLightPacket npacket = new ClientboundLevelChunkWithLightPacket(levelChunk, levelChunk.getLevel().getLightEngine(), null, null);

		//Restore old biomes, so that we don't lose them
		for (LevelChunkSection section : levelChunk.getSections()) {
			for (int x = 0; x < 4; ++x) {
				for (int z = 0; z < 4; ++z) {
					for (int y = 0; y < 4; ++y) {
						section.setBiome(x, y, z, Holder.direct(saved_biomes.remove(0)));
					}
				}
			}
		}

		return npacket;
	}

	public static CommonPlayerSpawnInfo editCommonPlayerSpawnInfo(CommonPlayerSpawnInfo commonPlayerSpawnInfo, @Nullable Boolean isFlat, @Nullable World.Environment env) {
		Holder<DimensionType> dimensionType = commonPlayerSpawnInfo.dimensionType();
		if (env == World.Environment.NORMAL) { dimensionType = getRegistry(Registries.LEVEL_STEM).get(LevelStem.OVERWORLD).type(); }
		if (env == World.Environment.NETHER) { dimensionType = getRegistry(Registries.LEVEL_STEM).get(LevelStem.NETHER).type(); }
		if (env == World.Environment.THE_END) { dimensionType = getRegistry(Registries.LEVEL_STEM).get(LevelStem.END).type(); }

		ResourceKey<Level> dimension = commonPlayerSpawnInfo.dimension();
		long seed = commonPlayerSpawnInfo.seed();
		GameType gameType = commonPlayerSpawnInfo.gameType();
		GameType previousGameType = commonPlayerSpawnInfo.previousGameType();
		boolean isDebug = commonPlayerSpawnInfo.isDebug();
		isFlat = (isFlat != null) ? isFlat : commonPlayerSpawnInfo.isFlat();
		Optional<GlobalPos> lastDeathLocation = commonPlayerSpawnInfo.lastDeathLocation();
		int portalCooldown = commonPlayerSpawnInfo.portalCooldown();
		return new CommonPlayerSpawnInfo(dimensionType, dimension, seed, gameType, previousGameType, isDebug, isFlat, lastDeathLocation, portalCooldown);
	}

	//This is very unstable and can produce server crash, use only in WorldInitEvent
	public static void setCustomDimension(World world, @Nullable DimensionType copy, @Nullable World.Environment env, @Nullable Long fixedTime, @Nullable Boolean hasSkyLight, @Nullable Boolean hasCeiling, @Nullable Boolean ultraWarm, @Nullable Boolean natural, @Nullable Double coordinateScale, @Nullable Boolean bedWorks, @Nullable Boolean respawnAnchorWorks, @Nullable Integer minY, @Nullable Integer height, @Nullable Integer logicalHeight, @Nullable TagKey<Block> infiniburn, @Nullable ResourceLocation effectsLocation, @Nullable Float ambientLight, @Nullable DimensionType.MonsterSettings monsterSettings) {
		ServerLevel level = getWorld(world);
		DimensionType original = level.dimensionType();

		Field CraftWorld_environment = getField(CraftWorld, World.Environment.class, null, true);
		if (env != null) { setValue(CraftWorld_environment, level.getWorld(), env); }

		//Select dimension data if environment parameter is used and others are not
		boolean b1 = (fixedTime == null) && (hasSkyLight == null) && (hasCeiling == null) && (ultraWarm == null) && (natural == null) && (coordinateScale == null) && (bedWorks == null) && (respawnAnchorWorks == null) && (minY == null) && (height == null) && (logicalHeight == null) && (infiniburn == null) && (effectsLocation == null) && (ambientLight == null) && (monsterSettings == null);
		if (b1 && (env == World.Environment.NORMAL)) { copy = getRegistry(Registries.LEVEL_STEM).get(LevelStem.OVERWORLD.location()).type().value(); }
		if (b1 && (env == World.Environment.NETHER)) { copy = getRegistry(Registries.LEVEL_STEM).get(LevelStem.NETHER.location()).type().value(); }
		if (b1 && (env == World.Environment.THE_END)) { copy = getRegistry(Registries.LEVEL_STEM).get(LevelStem.END.location()).type().value(); }

		//Create new dimension data from given parameters
		DimensionType type = new DimensionType(
				(fixedTime == null) ? ((copy == null) ? original.fixedTime() : copy.fixedTime()) : OptionalLong.of(fixedTime),
				(hasSkyLight == null) ? ((copy == null) ? original.hasSkyLight() : copy.hasSkyLight()) : hasSkyLight,
				(hasCeiling == null) ? ((copy == null) ? original.hasCeiling() : copy.hasCeiling()) : hasCeiling,
				(ultraWarm == null) ? ((copy == null) ? original.ultraWarm() : copy.ultraWarm()) : ultraWarm,
				(natural == null) ? ((copy == null) ? original.natural() : copy.natural()) : natural,
				(coordinateScale == null) ? ((copy == null) ? original.coordinateScale() : copy.coordinateScale()) : coordinateScale,
				(bedWorks == null) ? ((copy == null) ? original.bedWorks() : copy.bedWorks()) : bedWorks,
				(respawnAnchorWorks == null) ? ((copy == null) ? original.respawnAnchorWorks() : copy.respawnAnchorWorks()) : respawnAnchorWorks,
				(minY == null) ? ((copy == null) ? original.minY() : copy.minY()) : minY,
				(height == null) ? ((copy == null) ? original.height() : copy.height()) : height,
				(logicalHeight == null) ? ((copy == null) ? original.logicalHeight() : copy.logicalHeight()) : logicalHeight,
				(infiniburn == null) ? ((copy == null) ? original.infiniburn() : copy.infiniburn()) : infiniburn,
				(effectsLocation == null) ? ((copy == null) ? original.effectsLocation() : copy.effectsLocation()) : effectsLocation,
				(ambientLight == null) ? ((copy == null) ? original.ambientLight() : copy.ambientLight()) : ambientLight,
				(monsterSettings == null) ? ((copy == null) ? original.monsterSettings() : copy.monsterSettings()) : monsterSettings
		);

		//Create holder and replace dimension data inside level
		Holder<DimensionType> holder = (Holder<DimensionType>) getValue(Level_dimensionTypeRegistration, level); //Get original holder
		HolderOwner<DimensionType> owner = (HolderOwner<DimensionType>) getValue(Holder_owner, holder); //Get original holder owner
		Holder<DimensionType> newHolder = Holder.Reference.createIntrusive(owner, type); //Create new holder, so that we don't affect all worlds
		setValue(Level_dimensionTypeRegistration, level, newHolder); //Replace old holder with new one

		//Replace stupid paper EntityLookup system, because it initializes and uses old minY and height variables
		//FUCK YOU PAPER -> io.papermc.paper.chunk.system.entity.EntityLookup -> minSection = WorldUtil.getMinSection(world)
		if (PaperUtils.isPaper()) {
			Field Level_entityLookup = getField(Level.class, PaperUtils.EntityLookup, null, true);
			Field EntityLookup_levelCallback = getField(PaperUtils.EntityLookup, net.minecraft.world.level.entity.LevelCallback.class, null, true);
			LevelCallback<?> callback = (LevelCallback<net.minecraft.world.entity.Entity>) getValue(EntityLookup_levelCallback, level.moonrise$getEntityLookup());
			Object entityLookup = newInstance(PaperUtils.ServerEntityLookup, new Class[]{ ServerLevel.class, LevelCallback.class }, new Object[]{ level, callback }, true, true);
			setValue(Level_entityLookup, level, entityLookup);
		}
	}

	public static List<String> getBiomes(String exnamespace) {
		ArrayList<ResourceLocation> resourceLocations = new ArrayList<>(getRegistry(Registries.BIOME).keySet());
		if (!exnamespace.isEmpty()) { resourceLocations.removeIf(e -> e.getNamespace().equalsIgnoreCase(exnamespace)); }
		return resourceLocations.stream().map(ResourceLocation::toString).toList();
	}

	//This will crash client if he will not rejoin and receive custom biome, because client only receives biomes when joining,
	// but if we create new biome and send it to client, he won't know what to do with it and crash
	public static Biome registerBiome(String basename, String namespace, String name, int skyColor, int fogColor, int waterColor, int waterFogColor, int foliageColor, int grassColor, boolean effectsEnabled) {
		ResourceKey<Biome> minecraftKey = ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(basename.split(":")[0], basename.split(":")[1]));
		ResourceKey<Biome> customKey = ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(namespace, name));
		WritableRegistry<Biome> biomeRegirsty = (WritableRegistry<Biome>) getRegistry(Registries.BIOME);
		Biome minecraftBiome = biomeRegirsty.get(minecraftKey);

		BiomeSpecialEffects.Builder biomeSpecialEffects = new BiomeSpecialEffects.Builder();
		biomeSpecialEffects.grassColorModifier(minecraftBiome.getSpecialEffects().getGrassColorModifier());
		biomeSpecialEffects.skyColor((skyColor != -1) ? skyColor : minecraftBiome.getSkyColor());
		biomeSpecialEffects.fogColor((fogColor != -1) ? fogColor : minecraftBiome.getFogColor());
		biomeSpecialEffects.waterColor((waterColor != -1) ? waterColor : minecraftBiome.getWaterColor());
		biomeSpecialEffects.waterFogColor((waterFogColor != -1) ? waterFogColor : minecraftBiome.getWaterFogColor());

		//Default override color can be empty and our color can be -1,
		//If our color is not -1 set our color
		//If our color is -1 and override color is not empty set override color
		//If both are -1 and empty do nothing

		int foliageColorOverride = (foliageColor == -1) ? minecraftBiome.getSpecialEffects().getFoliageColorOverride().orElse(-1) : foliageColor;
		if (foliageColorOverride != -1) { biomeSpecialEffects.foliageColorOverride(foliageColorOverride); }

		int grassColorOverride = (grassColor == -1) ? minecraftBiome.getSpecialEffects().getGrassColorOverride().orElse(-1) : grassColor;
		if (grassColorOverride != -1) { biomeSpecialEffects.grassColorOverride(grassColorOverride); }

		if (effectsEnabled) {
			minecraftBiome.getAmbientAdditions().ifPresent(biomeSpecialEffects::ambientAdditionsSound);
			minecraftBiome.getAmbientLoop().ifPresent(biomeSpecialEffects::ambientLoopSound);
			minecraftBiome.getAmbientMood().ifPresent(biomeSpecialEffects::ambientMoodSound);
			minecraftBiome.getAmbientParticle().ifPresent(biomeSpecialEffects::ambientParticle);
			minecraftBiome.getBackgroundMusic().ifPresent(biomeSpecialEffects::backgroundMusic);
		}

		Biome.BiomeBuilder biomeBuilder = new Biome.BiomeBuilder();
		biomeBuilder.downfall(minecraftBiome.climateSettings.downfall());
		biomeBuilder.mobSpawnSettings(minecraftBiome.getMobSettings());
		biomeBuilder.generationSettings(minecraftBiome.getGenerationSettings());
		biomeBuilder.temperature(minecraftBiome.climateSettings.temperature());
		biomeBuilder.downfall(minecraftBiome.climateSettings.downfall());
		biomeBuilder.temperatureAdjustment(minecraftBiome.climateSettings.temperatureModifier());
		biomeBuilder.specialEffects(biomeSpecialEffects.build());
		Biome customBiome = biomeBuilder.build();

		setRegistryFrozen(biomeRegirsty, false);
		biomeRegirsty.register(customKey, customBiome, RegistrationInfo.BUILT_IN);
		setRegistryFrozen(biomeRegirsty, true);
		fixHolder(biomeRegirsty, customBiome);

		return customBiome;
	}

	public static void fillChunk(Chunk chunk, Material material, boolean removeEntity, boolean refresh) {
		if (!chunk.isLoaded()) { return; }

		if (removeEntity) {
			for (Entity entity : chunk.getEntities()) {
				if (entity.getType() != EntityType.PLAYER) {
					try { entity.remove(); } catch (Exception ignored) {}
				}
			}
		}

		int maxY = chunk.getWorld().getMaxHeight();
		int minY = chunk.getWorld().getMinHeight();
		BlockState state = getBlock(material).defaultBlockState();
		LevelChunk levelChunk = getWorld(chunk.getWorld()).getChunk(chunk.getX(), chunk.getZ());

		for (int x = 0; x < 16; x++) {
			for (int y = minY; y < maxY; y++) {
				for (int z = 0; z < 16 ; z++) {
					BlockPos blockPos = new BlockPos(x, y, z);
					if (levelChunk.getBlockEntity(blockPos) != null) { levelChunk.removeBlockEntity(blockPos); }
					levelChunk.setBlockState(blockPos, state, false, true);
				}
			}
		}

		if (refresh) {
			ClientboundLevelChunkWithLightPacket npacket = new ClientboundLevelChunkWithLightPacket(levelChunk, levelChunk.getLevel().getLightEngine(), null, null, true);
			Collection<Entity> players = Utils.getNearbyEntities(chunk.getBlock(8, 0, 8).getLocation(), EntityType.PLAYER, Bukkit.getViewDistance()*16, true);
			players.stream().map(Player.class::cast).forEach(e -> sendPacket(e, npacket));
		}
	}

	public static float getLocalDifficulty(Player player, boolean real) {
		Difficulty difficulty = player.getWorld().getDifficulty();
		float timeOfDay = getWorld(player.getWorld()).getLevelData().getDayTime();
		float moonSize = getWorld(player.getWorld()).getMoonBrightness();
		long inhabitedTime = real ? player.getChunk().getInhabitedTime() : 0L;
		if (difficulty == Difficulty.PEACEFUL) { return 0.0F; }

		float daytimeFactor = Utils.clamp((timeOfDay - 72000.0F) / 1440000.0F, 0.0F, 1.0F) * 0.25F;
		float chunkFactor = Utils.clamp((float) inhabitedTime / 3600000.0F, 0.0F, 1.0F) * ((difficulty == Difficulty.HARD) ? 1.0F : 0.75F);
		chunkFactor += Utils.clamp(moonSize * 0.25F, 0.0F, daytimeFactor);
		if (difficulty == Difficulty.EASY) { chunkFactor *= 0.5F; }
		return (float) difficulty.getValue() * (0.75F + daytimeFactor + chunkFactor);
	}

	public static int[] getEntityLinkPacketIds(Object packet) {
		if (!(packet instanceof ClientboundSetEntityLinkPacket)) { return new int[] { -1, -1 }; }
		int sourceId = ((ClientboundSetEntityLinkPacket) packet).getSourceId();
		int destId = ((ClientboundSetEntityLinkPacket) packet).getDestId();
		return new int[] { sourceId, destId };
	}

	public static void createServerChannelHandler(Consumer<Channel> consumer) {
		ServerConnectionListener connection = MinecraftServer.getServer().getConnection();

		ChannelInitializer<Channel> endInitProtocol = new ChannelInitializer<>() {
			@Override
			protected void initChannel(Channel channel) {
				synchronized (connection.getConnections()) {
					channel.eventLoop().submit(() -> consumer.accept(channel));
				}
			}
		};

		ChannelInitializer<Channel> beginInitProtocol = new ChannelInitializer<>() {
			@Override
			protected void initChannel(Channel channel) {
				channel.pipeline().addLast(endInitProtocol);
			}
		};

		ChannelInboundHandlerAdapter serverChannelHandler = new ChannelInboundHandlerAdapter() {
			@Override
			public void channelRead(ChannelHandlerContext ctx, @NotNull Object msg) {
				Channel channel = (Channel) msg;
				channel.pipeline().addFirst(beginInitProtocol);
				ctx.fireChannelRead(msg);
			}
		};

		List<ChannelFuture> channels = (List<ChannelFuture>) getValue(ServerConnectionListener_channels, connection);
		channels.get(0).channel().pipeline().addFirst(serverChannelHandler);
	}
}
