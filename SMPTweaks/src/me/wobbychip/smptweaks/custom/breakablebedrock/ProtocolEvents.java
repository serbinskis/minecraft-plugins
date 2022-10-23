package me.wobbychip.smptweaks.custom.breakablebedrock;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;

import me.wobbychip.smptweaks.utils.Utils;

public class ProtocolEvents extends PacketAdapter {
	public ProtocolEvents(Plugin plugin) {
		super(plugin, new PacketType[] {
			PacketType.Play.Server.BLOCK_BREAK_ANIMATION,
		});

		ProtocolLibrary.getProtocolManager().addPacketListener(this);
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		Utils.sendMessage(event.getPacketType().name());
		/*if (event.getPacketType() == PacketType.Play.Server.BLOCK_BREAK_ANIMATION) {
			PacketContainer packet = event.getPacket();
			World world = event.getPlayer().getWorld();
			BlockPosition position = packet.getBlockPositionModifier().getValues().get(0);
			Block block = world.getBlockAt(position.getX(), position.getY(), position.getZ());	

			if ((block.getType() != Material.BEDROCK) || !BreakableBedrock.preventPacket) { return; }
			event.setCancelled(true);
		}*/

		/*if (event.getPacketType() == PacketType.Play.Server.BLOCK_BREAK_ANIMATION) {
			PacketContainer packet = event.getPacket();
			packet.getBytes().write(0, (byte) 0);
		}*/
	}
}
