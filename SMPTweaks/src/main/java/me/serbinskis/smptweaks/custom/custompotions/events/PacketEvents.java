package me.serbinskis.smptweaks.custom.custompotions.events;

import me.serbinskis.smptweaks.custom.custompotions.CustomPotions;
import me.serbinskis.smptweaks.custom.custompotions.potions.CustomPotion;
import me.serbinskis.smptweaks.library.tinyprotocol.PacketEvent;
import me.serbinskis.smptweaks.library.tinyprotocol.PacketType;
import me.serbinskis.smptweaks.utils.ReflectionUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Objects;

public class PacketEvents implements Listener {
    //FUCK THE FUCKING MOJANG, IF YOU SEND CUSTOM POTION TO CLIENT, HE WILL SHIT HIMSELF,
    //BECAUSE CLIENT DOESN'T KNOW WHAT THE FUCK IS A CUSTOM POTION.
    //THIS WILL NOT WORK ON ITEMS THAT ARE CONTAINERS, LIKE SHULKERS BOXES
    //SO IT IS BETTER TO CONVERT TO FAKE TAG AND BACK INSIDE INVENTORY EVENTS

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPacketEvent(PacketEvent event) {
        if (event.getPacketType() == PacketType.CONTAINER_SET_SLOT) {
            event.setPacket(ReflectionUtils.fakePotionTags(event.getPacket(), CustomPotion.PLACEHOLDER_POTION, itemStack -> {
                return Objects.nonNull(CustomPotions.manager.getCustomPotion(itemStack));
            }));
        }

        if (event.getPacketType() == PacketType.CONTAINER_SET_CONTENT) {
            event.setPacket(ReflectionUtils.fakePotionTags(event.getPacket(), CustomPotion.PLACEHOLDER_POTION, itemStack -> {
                return Objects.nonNull(CustomPotions.manager.getCustomPotion(itemStack));
            }));
        }
    }
}
