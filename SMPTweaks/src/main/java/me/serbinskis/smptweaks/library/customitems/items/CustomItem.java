package me.serbinskis.smptweaks.library.customitems.items;

import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.utils.PersistentUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.io.InputStream;

public class CustomItem {
    public final static String TAG_CUSTOM_ITEM = "SMPTWEAKS_CUSTOM_ITEM";
    private final Material material;
    private final String id;
    private String name;
    private String texture;

    public CustomItem(String id, Material material) {
        this.id = id;
        this.name = id;
        this.material = material;
    }

    public String getId() {
        return id;
    }

    public CustomItem setCustomName(String name) {
        this.name = name;
        return this;
    }

    public String getCustomName() {
        return name;
    }

    public CustomItem setTexture(String texture) {
        this.texture = texture;
        return this;
    }

    public byte[] getCustomTextures() {
        try {
            if (texture == null) { return null; }
            InputStream inputStream = Main.plugin.getResource("textures/items/" + texture);
            if (inputStream == null) { return null; }
            return inputStream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ItemStack getItemStack() {
        return getItemStack(0);
    }

    public ItemStack getItemStack(int textureIndex) {
        return getItemStack(textureIndex, "", "");
    }

    public ItemStack getItemStack(int textureIndex, String prefix, String suffix) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(prefix+name+suffix);

        if (this.texture != null) {
            meta.setItemModel(new NamespacedKey("smptweaks", this.id + "_" + textureIndex));
        }

        item.setItemMeta(meta);
        return PersistentUtils.setPersistentDataString(item, TAG_CUSTOM_ITEM, this.id);
    }
}
