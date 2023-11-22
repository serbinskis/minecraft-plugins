package me.wobbychip.smptweaks.custom.customworld;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.custom.customworld.commands.Commands;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import me.wobbychip.smptweaks.utils.Utils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.util.Set;

public class CustomWorld extends CustomTweak {
	public static CustomTweak tweak;
	public static String CUSTOM_WORLD_TAG = "SMPTWEAKS_CUSTOM_WORLD";
	public enum Type { END, VOID, NONE }
	public Commands comands;

	public CustomWorld() {
		super(CustomWorld.class, false, true);
		this.comands = new Commands(this, "cworld");
		this.setDescription("Custom world setting for my server.");
	}

	public void onEnable() {
		CustomWorld.tweak = this;
		this.setCommand(this.comands);

		new ProtocolEvents(Main.plugin);
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
		if (true) { return; }

		ServerLevel world = ReflectionUtils.getWorld(Bukkit.getWorlds().get(0));
		MinecraftServer server = MinecraftServer.getServer();
		Field MinecraftServer_registries = ReflectionUtils.getField(MinecraftServer.class, LayeredRegistryAccess.class, RegistryLayer.class, true);
		LayeredRegistryAccess<RegistryLayer> registries = (LayeredRegistryAccess<RegistryLayer>) ReflectionUtils.getValue(MinecraftServer_registries, server);
		Registry<LevelStem> dimensions = registries.compositeAccess().registryOrThrow(Registries.LEVEL_STEM);
		LevelStem worlddimension = (LevelStem) dimensions.get(LevelStem.END.location());

		Utils.sendMessage(worlddimension.type().value());
		DimensionType type = world.dimensionType();
		type = new DimensionType(type.fixedTime(), type.hasSkyLight(), type.hasCeiling(), type.ultraWarm(), type.natural(), type.coordinateScale(), type.bedWorks(), type.respawnAnchorWorks(), -128, type.height(), type.logicalHeight(), type.infiniburn(), type.effectsLocation(), type.ambientLight(), type.monsterSettings());

		Field Level_dimensionTypeRegistration = ReflectionUtils.getField(Level.class, Holder.class, DimensionType.class, true);
		Holder<DimensionType> holder = (Holder<DimensionType>) ReflectionUtils.getValue(Level_dimensionTypeRegistration, world);

		Field Holder_owner = ReflectionUtils.getField(Holder.Reference.class, HolderOwner.class, null, true);
		Field Holder_tags = ReflectionUtils.getField(Holder.Reference.class, Set.class, null, true);
		Field Holder_key = ReflectionUtils.getField(Holder.Reference.class, ResourceKey.class, null, true);
		Field Holder_value = ReflectionUtils.getField(Holder.Reference.class, Object.class, null, true);

		HolderOwner<DimensionType> owner = (HolderOwner<DimensionType>) ReflectionUtils.getValue(Holder_owner, holder);
		Holder<DimensionType> newHolder = Holder.Reference.createIntrusive(owner, type);
		ReflectionUtils.setValue(Level_dimensionTypeRegistration, world, newHolder);

		//ReflectionUtils.setValue(Holder_value, holder, type);
		Utils.sendMessage(world.dimensionType());
	}

	public static Type getCustomType(String string) {
		try {
			return CustomWorld.Type.valueOf(string.toUpperCase());
		} catch (Exception e) { return null; }
	}
}
