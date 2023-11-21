package me.wobbychip.smptweaks.custom.customsky;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
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
		PacketContainer packet = event.getPacket();
		PacketType packetType = event.getPacket().getType();
		if (packetType != PacketType.Play.Server.LOGIN) { return; }

		//WrapperPlayServerLogin wrapperPlayServerLogin = new WrapperPlayServerLogin(event.getPacket());
		//wrapperPlayServerLogin.setLevelType(WorldType.FLAT);
		//wrapperPlayServerLogin.setDimension(1); //Set -1: nether, 0: overworld, 1: end.
		packet.getIntegers().write(0, -1);
		//net.minecraft.network.protocol.game.CommonPlayerSpawnInfo
		ClientboundLoginPacket loginPacket = (ClientboundLoginPacket) event.getPacket().getHandle();
		loginPacket.commonPlayerSpawnInfo().dimension();
		//Utils.sendMessage(event.getPacket().getMinecraftKeys().getField(0)); //out of bounds
	}
}
