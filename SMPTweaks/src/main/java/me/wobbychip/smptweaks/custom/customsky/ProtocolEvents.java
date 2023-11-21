package me.wobbychip.smptweaks.custom.customsky;

import com.comphenix.packetwrapper.WrapperPlayServerLogin;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ProtocolEvents extends PacketAdapter {
	public ProtocolEvents(Plugin plugin) {
		super(new AdapterParameteters().plugin(plugin).types(PacketType.Play.Server.LOGIN, PacketType.Play.Server.RESPAWN).listenerPriority(ListenerPriority.HIGHEST));
		ProtocolLibrary.getProtocolManager().addPacketListener(this);
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		Player player = event.getPlayer();
		if (player == null) { return; }
		PacketType packetType = event.getPacket().getType();

		if (packetType == WrapperPlayServerLogin.TYPE) {
			WrapperPlayServerLogin wrapperPlayServerLogin = new WrapperPlayServerLogin(event.getPacket());
			wrapperPlayServerLogin.setLevelType(WorldType.FLAT);
		}
	}
}
