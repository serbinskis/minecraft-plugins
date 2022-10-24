package me.wobbychip.smptweaks.custom.breakablebedrock;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
			PacketType.Play.Client.BLOCK_DIG,
		});

		ProtocolLibrary.getProtocolManager().addPacketListener(this);
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		Utils.sendMessage(event.getPacketType().name());
		event.setCancelled(true);

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

	@Override
	public void onPacketReceiving(PacketEvent event) {
		if (event.getPacketType() == PacketType.Play.Client.BLOCK_DIG) {
			event.setCancelled(true);
			PacketContainer packet = event.getPacket();
			World world = event.getPlayer().getWorld();
			BlockPosition position = packet.getBlockPositionModifier().getValues().get(0);
			Block block = world.getBlockAt(position.getX(), position.getY(), position.getZ());
			Utils.sendMessage(block);

			if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) { return; }
			if (block.getType() != Material.BEDROCK) { return; }

			BedrockBreaker.addPlayer(event.getPlayer(), block);
			event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, Integer.MAX_VALUE, -1, false, false, false));
		}
	}
}
