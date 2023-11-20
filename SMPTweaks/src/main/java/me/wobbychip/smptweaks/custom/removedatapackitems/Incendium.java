package me.wobbychip.smptweaks.custom.removedatapackitems;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;

public class Incendium {
    public static void onLootGenerateEvent(LootGenerateEvent event) {
        for (int i = 0; i < event.getLoot().size(); i++) {
            if (!isIncendiumItem(event.getLoot().get(i))) { continue; }
            ItemStack itemStack = RemoveDatapackItems.normalizeDatapackItem(event.getLoot().get(i));
            event.getLoot().set(i, itemStack);
        }
    }

    public static void onItemSpawnEvent(ItemSpawnEvent event) {
        if (!isIncendiumItem(event.getEntity().getItemStack())) { return; }
        ItemStack itemStack = RemoveDatapackItems.normalizeDatapackItem(event.getEntity().getItemStack());
        event.getEntity().setItemStack(itemStack);
    }

    public static void onInventoryClickEvent(InventoryClickEvent event) {
        if (!isIncendiumItem(event.getCurrentItem())) { return; }
        ItemStack itemStack = RemoveDatapackItems.normalizeDatapackItem(event.getCurrentItem());
        event.setCurrentItem(itemStack);
    }

    public static void onPlayerInteractAtEntityEvent(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand stand)) { return; }
        ItemStack[] armorContents = stand.getEquipment().getArmorContents();
        boolean update = false;

        for (int i = 0; i < armorContents.length; i++) {
            if (!isIncendiumItem(armorContents[i])) { continue; }
            armorContents[i] = RemoveDatapackItems.normalizeDatapackItem(armorContents[i]);
            update = true;
        }

        if (update) { stand.getEquipment().setArmorContents(armorContents); }
    }

    public static void processItemEntity(Item entity) {
        if (!isIncendiumItem(entity.getItemStack())) { return; }
        entity.setItemStack(RemoveDatapackItems.normalizeDatapackItem(entity.getItemStack()));
    }

    public static boolean isIncendiumItem(ItemStack itemStack) {
        if (itemStack == null) { return false; }
        if (itemStack.getItemMeta() == null) { return false; }
        if (itemStack.getItemMeta().getLore() == null) { return false; }
        return itemStack.getItemMeta().getLore().stream().anyMatch(e -> e.toLowerCase().contains("incendium"));
    }
}
