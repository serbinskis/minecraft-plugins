package me.wobbychip.smptweaks.custom.removedatapackitems;

import me.wobbychip.smptweaks.utils.ReflectionUtils;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Arrays;

public class Stellarity {
    public static void onLootGenerateEvent(LootGenerateEvent event) {
        for (int i = 0; i < event.getLoot().size(); i++) {
            if (!isStellarityItem(event.getLoot().get(i))) { continue; }
            ItemStack itemStack = RemoveDatapackItems.normalizeDatapackItem(event.getLoot().get(i));
            event.getLoot().set(i, itemStack);
        }
    }

    public static void onItemSpawnEvent(ItemSpawnEvent event) {
        if (!isStellarityItem(event.getEntity().getItemStack())) { return; }
        ItemStack itemStack = RemoveDatapackItems.normalizeDatapackItem(event.getEntity().getItemStack());
        event.getEntity().setItemStack(itemStack);
    }

    public static void onInventoryClickEvent(InventoryClickEvent event) {
        if (!isStellarityItem(event.getCurrentItem())) { return; }
        ItemStack itemStack = RemoveDatapackItems.normalizeDatapackItem(event.getCurrentItem());
        event.setCurrentItem(itemStack);
    }

    public static void onPlayerInteractAtEntityEvent(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand stand)) { return; }

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!isStellarityItem(stand.getItem(slot))) { continue; }
            stand.setItem(slot, RemoveDatapackItems.normalizeDatapackItem(stand.getItem(slot)));
        }
    }

    public static void onChunkLoadEvent(ChunkLoadEvent event) {
        if (event.getWorld().getEnvironment() != World.Environment.THE_END) { return; }

        for (Entity entity : event.getChunk().getEntities()) {
            if (entity instanceof Item item) {
                if (isStellarityItem(item.getItemStack())) { item.remove(); }
            }

            if (entity instanceof ItemFrame itemFrame) {
                if (!isStellarityItem(itemFrame.getItem())) { continue; }
                itemFrame.setItem(RemoveDatapackItems.normalizeDatapackItem(itemFrame.getItem()));
            }

            if (entity instanceof ArmorStand stand) {
                PlayerInteractAtEntityEvent event1 = new PlayerInteractAtEntityEvent(RemoveDatapackItems.fakePlayer, stand, new Vector());
                onPlayerInteractAtEntityEvent(event1);
            }
        }
    }

    public static boolean isStellarityItem(ItemStack itemStack) {
        if (itemStack == null) { return false; }
        return (ReflectionUtils.getItemNbt(itemStack, Arrays.asList("stellarity.special_item")) != null);
    }
}
