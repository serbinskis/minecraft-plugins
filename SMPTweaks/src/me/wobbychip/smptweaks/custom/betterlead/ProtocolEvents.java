package me.wobbychip.smptweaks.custom.betterlead;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

import me.wobbychip.smptweaks.utils.Utils;

public class ProtocolEvents extends PacketAdapter {
	public ProtocolEvents(Plugin plugin) {
		super(plugin, new PacketType[] {
			PacketType.Play.Server.ATTACH_ENTITY
		});
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		PacketContainer packet = event.getPacket();
		Entity entity = packet.getEntityModifier(event.getPlayer().getWorld()).read(1);
		Utils.sendMessage(entity);
	}
}
