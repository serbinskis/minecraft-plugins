package me.serbinskis.smptweaks.library.customitems.items;

import me.serbinskis.smptweaks.Main;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.io.InputStream;

public class CustomItem {
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

    public void setCustomName(String name) {
        this.name = name;
    }

    public String getCustomName() {
        return name;
    }

    public void setTexture(String texture) {
        this.texture = texture;
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

    public ItemStack getItemStack(int textureIndex) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);

        if (this.texture != null) {
            meta.setItemModel(new NamespacedKey("smptweaks", this.id + "_" + textureIndex));
        }

        item.setItemMeta(meta);
        return item;
    }
}
