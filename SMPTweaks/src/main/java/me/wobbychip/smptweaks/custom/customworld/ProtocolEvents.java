package me.wobbychip.smptweaks.custom.customworld;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import me.wobbychip.smptweaks.utils.PersistentUtils;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import net.minecraft.network.protocol.Packet;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

public class ProtocolEvents extends PacketAdapter {
	public ProtocolEvents(Plugin plugin) {
		super(new AdapterParameteters().plugin(plugin).types(PacketType.Play.Server.LOGIN, PacketType.Play.Server.RESPAWN).listenerPriority(ListenerPriority.HIGHEST));
		ProtocolLibrary.getProtocolManager().addPacketListener(this);
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		PacketType packetType = event.getPacket().getType();
		if (((packetType != PacketType.Play.Server.LOGIN) && (packetType != PacketType.Play.Server.RESPAWN)) || (event.getPlayer() == null)) { return; }
		if (!PersistentUtils.hasPersistentDataString(event.getPlayer().getWorld(), CustomWorld.CUSTOM_WORLD_TAG)) { return; }

		CustomWorld.Type type = CustomWorld.getCustomType(PersistentUtils.getPersistentDataString(event.getPlayer().getWorld(), CustomWorld.CUSTOM_WORLD_TAG));
		if (type != CustomWorld.Type.END) { return; }

		Packet<?> packet = ReflectionUtils.editSpawnPacket((Packet<?>) event.getPacket().getHandle(), true, World.Environment.THE_END);
		event.setPacket(PacketContainer.fromPacket(packet)); //PS: This will not work with overworld because end minY is 0 while overworld minY is -64
	}
}
