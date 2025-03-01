package me.serbinskis.smptweaks.library.customitems.custom;

import me.serbinskis.smptweaks.library.customitems.items.CustomItem;
import org.bukkit.Material;

public class EmptyItem extends CustomItem {
    public EmptyItem() {
        super("empty_item", Material.KNOWLEDGE_BOOK);
        this.setTexture("empty_item.png");
        this.setCustomName(" ");
    }
}
