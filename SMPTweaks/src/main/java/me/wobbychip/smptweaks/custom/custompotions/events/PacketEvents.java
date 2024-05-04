package me.wobbychip.smptweaks.custom.custompotions.events;

import me.wobbychip.smptweaks.custom.custompotions.CustomPotions;
import me.wobbychip.smptweaks.library.tinyprotocol.PacketEvent;
import me.wobbychip.smptweaks.library.tinyprotocol.PacketType;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class PacketEvents implements Listener {
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPacketEvent(PacketEvent event) {
        if (event.getType() == PacketType.CONTAINER_SET_SLOT) {
            ClientboundContainerSetSlotPacket packet = (ClientboundContainerSetSlotPacket) event.getPacket();
            ItemStack itemStack = ReflectionUtils.asBukkitMirror(packet.getItem());
            if (CustomPotions.manager.getCustomPotion(itemStack) == null) { return; }
            net.minecraft.world.item.ItemStack nmsStack = ReflectionUtils.asNMSCopy(ReflectionUtils.setPotionTag(itemStack, "minecraft:water"));
            packet = new ClientboundContainerSetSlotPacket(packet.getContainerId(), packet.getStateId(), packet.getSlot(), Objects.requireNonNull(nmsStack));
            event.setPacket(packet);
        }

        if (event.getType() == PacketType.CONTAINER_SET_CONTENT) {
            ClientboundContainerSetContentPacket packet = (ClientboundContainerSetContentPacket) event.getPacket();

            net.minecraft.world.item.ItemStack[] collect = packet.getItems().stream().map(e -> {
                ItemStack itemStack = ReflectionUtils.asBukkitMirror(e);
                if (CustomPotions.manager.getCustomPotion(itemStack) == null) { return e; }
                return ReflectionUtils.asNMSCopy(ReflectionUtils.setPotionTag(itemStack, "minecraft:water"));
            }).toArray(net.minecraft.world.item.ItemStack[]::new);

            ItemStack itemStack = ReflectionUtils.setPotionTag(ReflectionUtils.asBukkitMirror(packet.getCarriedItem()), "minecraft:water");
            net.minecraft.world.item.ItemStack nmsStack = Objects.requireNonNull(ReflectionUtils.asNMSCopy(itemStack));
            NonNullList<net.minecraft.world.item.ItemStack> contents = NonNullList.of(net.minecraft.world.item.ItemStack.EMPTY, collect);
            packet = new ClientboundContainerSetContentPacket(packet.getContainerId(), packet.getStateId(), contents, nmsStack);
            event.setPacket(packet);
        }
    }
}
