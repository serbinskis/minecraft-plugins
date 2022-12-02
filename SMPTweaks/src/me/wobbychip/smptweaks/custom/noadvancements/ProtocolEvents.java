package me.wobbychip.smptweaks.custom.noadvancements;

import org.bukkit.Sound;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

public class ProtocolEvents extends PacketAdapter {
	public ProtocolEvents(Plugin plugin) {
		super(plugin, new PacketType[] {
			PacketType.Play.Server.EXPERIENCE,
			PacketType.Play.Server.NAMED_SOUND_EFFECT,
		});

		ProtocolLibrary.getProtocolManager().addPacketListener(this);
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		if (!Events.chats.contains(event.getPlayer())) { return; }

		if (event.getPacketType() == PacketType.Play.Server.EXPERIENCE) {
			event.setCancelled(true);
		}

		if (event.getPacketType() == PacketType.Play.Server.NAMED_SOUND_EFFECT) {
			Sound sound = event.getPacket().getSoundEffects().read(0);
			if (sound.equals(Sound.ENTITY_PLAYER_LEVELUP)) { event.setCancelled(true); }
		}
	}
}
