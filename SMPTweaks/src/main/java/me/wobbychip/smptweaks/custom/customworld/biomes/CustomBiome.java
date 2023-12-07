package me.wobbychip.smptweaks.custom.customworld.biomes;

import me.wobbychip.smptweaks.custom.customworld.CustomWorlds;

import javax.annotation.Nullable;

public class CustomBiome {
    public static String TAG_GRASSCOLOR = CustomWorlds.TAG_BIOME_NAME + "_GRASSCOLOR";
    public static String TAG_FOGCOLOR = CustomWorlds.TAG_BIOME_NAME + "_FOGCOLOR";
    public static String TAG_FOLIAGECOLOR = CustomWorlds.TAG_BIOME_NAME + "_FOLIAGECOLOR";
    public static String TAG_SKYCOLOR = CustomWorlds.TAG_BIOME_NAME + "_SKYCOLOR";
    public static String TAG_WATERCOLOR = CustomWorlds.TAG_BIOME_NAME + "_WATERCOLOR";
    public static String TAG_WATERFOGCOLOR = CustomWorlds.TAG_BIOME_NAME + "_WATERFOGCOLOR";

    private final String biome_base;
    private final String namespace;
    private final String name;
    private int fogColor = -1;
    private int waterColor = -1;
    private int waterFogColor = -1;
    private int skyColor = -1;
    private int foliageColor = -1;
    private int grassColor = -1;
    private boolean effectsEnabled = false;
    private Object nmsBiome;

    public CustomBiome(@Nullable String biome_base, @Nullable String namespace, String name) {
        this.biome_base = (biome_base != null) ? biome_base : "minecraft:plains";
        this.namespace = (namespace != null) ? namespace : CustomWorlds.TAG_BIOME_NAMESPACE.toLowerCase();
        this.name = name;
    }

    public static int fromHex(String hex) {
        if (hex.startsWith("#")) { hex = hex.substring(1); }

        try {
            return Integer.parseInt(hex, 16);
        } catch (Exception e) { return -1; }
    }

    public static String toHex(int value) {
        if (value < 0) { return "NONE"; }
        return String.format("#%06X", value);
    }

    public String getBiomeBase() {
        return biome_base;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getName() {
        return name;
    }

    public int getFogColor() {
        return fogColor;
    }

    public String getFogColorHex() {
        return toHex(fogColor);
    }

    public CustomBiome setFogColor(int fogColor) {
        this.fogColor = fogColor;
        return this;
    }

    public CustomBiome setFogColor(String hex) {
        return setFogColor(fromHex(hex));
    }

    public int getWaterColor() {
        return waterColor;
    }

    public String getWaterColorHex() {
        return toHex(waterColor);
    }

    public CustomBiome setWaterColor(int waterColor) {
        this.waterColor = waterColor;
        return this;
    }

    public CustomBiome setWaterColor(String hex) {
        return setWaterColor(fromHex(hex));
    }

    public int getWaterFogColor() {
        return waterFogColor;
    }

    public String getWaterFogColorHex() {
        return toHex(waterFogColor);
    }

    public CustomBiome setWaterFogColor(int waterFogColor) {
        this.waterFogColor = waterFogColor;
        return this;
    }

    public CustomBiome setWaterFogColor(String hex) {
        return setWaterFogColor(fromHex(hex));
    }

    public int getSkyColor() {
        return skyColor;
    }

    public String getSkyColorHex() {
        return toHex(skyColor);
    }

    public CustomBiome setSkyColor(int skyColor) {
        this.skyColor = skyColor;
        return this;
    }

    public CustomBiome setSkyColor(String hex) {
        return setSkyColor(fromHex(hex));
    }

    public int getFoliageColor() {
        return foliageColor;
    }

    public String getFoliageColorHex() {
        return toHex(foliageColor);
    }

    public CustomBiome setFoliageColor(int foliageColor) {
        this.foliageColor = foliageColor;
        return this;
    }

    public CustomBiome setFoliageColor(String hex) {
        return setFoliageColor(fromHex(hex));
    }

    public int getGrassColor() {
        return grassColor;
    }

    public String getGrassColorHex() {
        return toHex(grassColor);
    }

    public CustomBiome setGrassColor(int grassColor) {
        this.grassColor = grassColor;
        return this;
    }

    public CustomBiome setGrassColor(String hex) {
        return setGrassColor(fromHex(hex));
    }

    public CustomBiome setEffectsEnabled(boolean effectsEnabled) {
        this.effectsEnabled = effectsEnabled;
        return this;
    }

    public boolean getEffectsEnabled() {
        return effectsEnabled;
    }

    public void setNmsBiome(Object nmsBiome) {
        this.nmsBiome = nmsBiome;
    }

    public Object getNmsBiome() {
        return nmsBiome;
    }

    public boolean isEmpty() {
        return (this.skyColor == -1) && (this.fogColor == -1)&& (this.foliageColor == -1) && (this.waterColor == -1) && (this.waterFogColor == -1) && (this.grassColor == -1);
    }

    public CustomBiome clone(@Nullable String biome_base, @Nullable String namespace, String name) {
        CustomBiome cbiome = new CustomBiome(biome_base, namespace, name);

        cbiome.setSkyColor(this.getSkyColor());
        cbiome.setFogColor(this.getFogColor());
        cbiome.setFoliageColor(this.getFoliageColor());
        cbiome.setGrassColor(this.getGrassColor());
        cbiome.setWaterColor(this.getWaterColor());
        cbiome.setWaterFogColor(this.getWaterFogColor());

        return cbiome;
    }

    public CustomBiome clone() {
        return clone(biome_base, namespace, name);
    }
}
