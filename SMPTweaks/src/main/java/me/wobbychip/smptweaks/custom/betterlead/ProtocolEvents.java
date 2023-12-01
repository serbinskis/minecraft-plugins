package me.wobbychip.smptweaks.custom.betterlead;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

public class ProtocolEvents extends PacketAdapter {
	public ProtocolEvents(Plugin plugin) {
		super(plugin, PacketType.Play.Server.ATTACH_ENTITY);
		ProtocolLibrary.getProtocolManager().addPacketListener(this);
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		if (event.getPacketType() == PacketType.Play.Server.ATTACH_ENTITY) {
			PacketContainer packet = event.getPacket();
			Entity player = packet.getEntityModifier(event.getPlayer().getWorld()).read(1);
			if (player != null) { return; }

			Entity entity = packet.getEntityModifier(event.getPlayer().getWorld()).read(0);
			if ((entity == null) || !BetterLead.preventPacket.contains(entity.getUniqueId())) { return; }
			event.setCancelled(true);
		}
	}
}
