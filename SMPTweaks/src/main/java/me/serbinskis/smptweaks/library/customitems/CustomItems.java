package me.serbinskis.smptweaks.library.customitems;

import me.serbinskis.smptweaks.library.customitems.commands.Commands;
import me.serbinskis.smptweaks.library.customitems.custom.EmptyItem;
import me.serbinskis.smptweaks.library.customitems.items.CustomItem;
import me.serbinskis.smptweaks.library.customtextures.CustomTextures;
import me.serbinskis.smptweaks.tweaks.CustomTweak;
import me.serbinskis.smptweaks.utils.PersistentUtils;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class CustomItems extends CustomTweak {
    public static HashMap<String, CustomItem> REGISTRY_CUSTOM_ITEMS = new HashMap<>();

    public CustomItems() {
        super(CustomItems.class, false, false, true);
        this.setCommand(new Commands(this, "citems"));
        this.setDescription("Library for custom items");
    }

    public static void start() {
        CustomItems.registerItem(new EmptyItem());
        CustomTextures.addCustomItems(REGISTRY_CUSTOM_ITEMS.values());
    }

    public static void registerItem(CustomItem customItem) {
        REGISTRY_CUSTOM_ITEMS.put(customItem.getId().toLowerCase(), customItem);
    }

    public static CustomItem getCustomItem(String name) {
        return REGISTRY_CUSTOM_ITEMS.getOrDefault(name.toLowerCase(), null);
    }

    public static CustomItem getCustomItem(ItemStack itemStack) {
        if (!PersistentUtils.hasPersistentDataString(itemStack, CustomItem.TAG_CUSTOM_ITEM)) { return null; }
        return getCustomItem(PersistentUtils.getPersistentDataString(itemStack, CustomItem.TAG_CUSTOM_ITEM));
    }

    public static ItemStack getItemStack(String name) {
        return getItemStack(name, "", "");
    }

    public static ItemStack getItemStack(String name, String prefix, String suffix) {
        return REGISTRY_CUSTOM_ITEMS.getOrDefault(name.toLowerCase(), null).getItemStack(0, prefix, suffix);
    }
}
