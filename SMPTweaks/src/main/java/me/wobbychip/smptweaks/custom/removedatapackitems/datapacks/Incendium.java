package me.wobbychip.smptweaks.custom.removedatapackitems.datapacks;

import me.wobbychip.smptweaks.custom.removedatapackitems.RemoveDatapackItems;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

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

    public static void onEntitySpawnEvent(EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof PiglinBrute piglinBrute)) { return; }
        if (piglinBrute.getScoreboardTags().stream().noneMatch(e -> e.equalsIgnoreCase("in.flying"))) { return; }
        piglinBrute.remove();
    }

    public static void onPlayerJoinEvent(PlayerJoinEvent event) {
        PlayerInventory inventory = event.getPlayer().getInventory();

        for (int i = 0; i < inventory.getSize(); i++) {
            if (!isIncendiumItem(inventory.getItem(i))) { continue; }
            inventory.setItem(i, RemoveDatapackItems.normalizeDatapackItem(inventory.getItem(i)));
        }
    }

    public static void onInventoryClickEvent(InventoryClickEvent event) {
        if (!isIncendiumItem(event.getCurrentItem())) { return; }
        ItemStack itemStack = RemoveDatapackItems.normalizeDatapackItem(event.getCurrentItem());
        event.setCurrentItem(itemStack);
    }

    public static void onPlayerInteractAtEntityEvent(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand stand)) { return; }

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!isIncendiumItem(stand.getItem(slot))) { continue; }
            stand.setItem(slot, RemoveDatapackItems.normalizeDatapackItem(stand.getItem(slot)));
        }
    }

    public static void onChunkLoadEvent(ChunkLoadEvent event) {
        if (event.getWorld().getEnvironment() != World.Environment.NETHER) { return; }

        for (Entity entity : event.getChunk().getEntities()) {
            if (entity instanceof Item item) {
                if (isIncendiumItem(item.getItemStack())) { item.remove(); }
            }

            if (entity instanceof ItemFrame itemFrame) {
                if (!isIncendiumItem(itemFrame.getItem())) { continue; }
                itemFrame.setItem(RemoveDatapackItems.normalizeDatapackItem(itemFrame.getItem()));
            }

            if (entity instanceof ArmorStand stand) {
                onPlayerInteractAtEntityEvent(new PlayerInteractAtEntityEvent(null, stand, new Vector()));
            }
        }
    }

    public static boolean isIncendiumItem(ItemStack itemStack) {
        if (itemStack == null) { return false; }
        if (itemStack.getItemMeta() == null) { return false; }
        if (itemStack.getItemMeta().getLore() == null) { return false; }
        return itemStack.getItemMeta().getLore().stream().anyMatch(e -> e.toLowerCase().contains("incendium"));
    }
}
