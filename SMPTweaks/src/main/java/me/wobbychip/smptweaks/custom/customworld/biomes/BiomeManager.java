package me.wobbychip.smptweaks.custom.customworld.biomes;

import me.wobbychip.smptweaks.custom.customworld.CustomWorlds;
import me.wobbychip.smptweaks.utils.PersistentUtils;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.HashMap;

public class BiomeManager {
    public static HashMap<String, CustomBiome> cbiomes = new HashMap<>();
    public static HashMap<String, Object> nmsBiomes = new HashMap<>();
    public static int counter = 0;

    public static void registerBiomeAll(CustomBiome cbiome) {
        //Register custom biome for entire world
        registerBiome(cbiome);

        //Register custom biome for each other biome
        for (String biome_name : ReflectionUtils.getBiomes(CustomWorlds.TAG_BIOME_NAMESPACE)) {
            registerBiome(cbiome.clone(biome_name, null, cbiome.getName() + "_" + biome_name.replaceAll("[:/]", "_")));
        }

        counter++;
        Bukkit.getOnlinePlayers().forEach(e -> e.kickPlayer("Custom biome has changed, rejoin is required."));
    }

    private static void registerBiome(CustomBiome cbiome) {
        Object customBiome = ReflectionUtils.registerBiome(
                cbiome.getBiomeBase(),
                cbiome.getNamespace(),
                cbiome.getName() + "_" + counter,
                cbiome.getSkyColor(),
                cbiome.getFogColor(),
                cbiome.getWaterColor(),
                cbiome.getWaterFogColor(),
                cbiome.getFoliageColor(),
                cbiome.getGrassColor(),
                cbiome.getEffectsEnabled()
        );

        cbiome.setNmsBiome(customBiome);
        cbiomes.put(cbiome.getName(), cbiome);
        nmsBiomes.put(cbiome.getName(), customBiome);
    }

    public static void saveBiome(World world, CustomBiome cbiome) {
        PersistentUtils.setPersistentDataInteger(world, CustomBiome.TAG_GRASSCOLOR, cbiome.getGrassColor());
        PersistentUtils.setPersistentDataInteger(world, CustomBiome.TAG_FOGCOLOR, cbiome.getFogColor());
        PersistentUtils.setPersistentDataInteger(world, CustomBiome.TAG_FOLIAGECOLOR, cbiome.getFoliageColor());
        PersistentUtils.setPersistentDataInteger(world, CustomBiome.TAG_SKYCOLOR, cbiome.getSkyColor());
        PersistentUtils.setPersistentDataInteger(world, CustomBiome.TAG_WATERCOLOR, cbiome.getWaterColor());
        PersistentUtils.setPersistentDataInteger(world, CustomBiome.TAG_WATERFOGCOLOR, cbiome.getWaterFogColor());
    }

    public static CustomBiome loadBiome(World world) {
        CustomBiome biome = new CustomBiome(null, null, world.getName());
        if (PersistentUtils.hasPersistentDataInteger(world, CustomBiome.TAG_GRASSCOLOR)) { biome.setGrassColor(PersistentUtils.getPersistentDataInteger(world, CustomBiome.TAG_GRASSCOLOR)); }
        if (PersistentUtils.hasPersistentDataInteger(world, CustomBiome.TAG_FOGCOLOR)) { biome.setFogColor(PersistentUtils.getPersistentDataInteger(world, CustomBiome.TAG_FOGCOLOR)); }
        if (PersistentUtils.hasPersistentDataInteger(world, CustomBiome.TAG_FOLIAGECOLOR)) { biome.setFoliageColor(PersistentUtils.getPersistentDataInteger(world, CustomBiome.TAG_FOLIAGECOLOR)); }
        if (PersistentUtils.hasPersistentDataInteger(world, CustomBiome.TAG_SKYCOLOR)) { biome.setSkyColor(PersistentUtils.getPersistentDataInteger(world, CustomBiome.TAG_SKYCOLOR)); }
        if (PersistentUtils.hasPersistentDataInteger(world, CustomBiome.TAG_WATERCOLOR)) { biome.setWaterColor(PersistentUtils.getPersistentDataInteger(world, CustomBiome.TAG_WATERCOLOR)); }
        if (PersistentUtils.hasPersistentDataInteger(world, CustomBiome.TAG_WATERFOGCOLOR)) { biome.setWaterFogColor(PersistentUtils.getPersistentDataInteger(world, CustomBiome.TAG_WATERFOGCOLOR)); }

        return biome.isEmpty() ? null : biome;
    }

    public static CustomBiome getCustomBiome(String name) {
        return cbiomes.get(name);
    }

    public static HashMap<String, Object> getNmsMap() {
        return nmsBiomes;
    }
}
