package me.serbinskis.smptweaks.custom.cycletrades;

import me.serbinskis.smptweaks.library.customitems.items.CustomItem;
import org.bukkit.Material;

public class CycleItem extends CustomItem {
    public CycleItem() {
        super("cycle_trade", Material.KNOWLEDGE_BOOK);
        this.setTexture("cycle_trade.png");
        this.setCustomName("§r§d§lCycle Trades");
    }
}
