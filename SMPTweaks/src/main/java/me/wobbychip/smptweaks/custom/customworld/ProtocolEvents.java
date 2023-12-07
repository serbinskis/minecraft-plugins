package me.wobbychip.smptweaks.custom.customworld;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import me.wobbychip.smptweaks.custom.customworld.biomes.BiomeManager;
import me.wobbychip.smptweaks.custom.customworld.biomes.CustomBiome;
import me.wobbychip.smptweaks.custom.customworld.biomes.CustomWorld;
import me.wobbychip.smptweaks.utils.PersistentUtils;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

public class ProtocolEvents extends PacketAdapter {
	public ProtocolEvents(Plugin plugin) {
		super(new AdapterParameteters().plugin(plugin).types(PacketType.Play.Server.LOGIN, PacketType.Play.Server.RESPAWN, PacketType.Play.Server.MAP_CHUNK, PacketType.Play.Server.CHUNKS_BIOMES).listenerPriority(ListenerPriority.HIGHEST));
		ProtocolLibrary.getProtocolManager().addPacketListener(this);
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		PacketType packetType = event.getPacket().getType();

		if (packetType == PacketType.Play.Server.MAP_CHUNK) {
			World world = event.getPlayer().getWorld();
			CustomBiome cbiome = BiomeManager.getCustomBiome(world.getName());
			if ((cbiome == null) || (cbiome.isEmpty())) { return; }

			Object packet = ReflectionUtils.setPacketChunkBiome(world, event.getPacket().getHandle(), cbiome.getNmsBiome(), cbiome.getName(), BiomeManager.getNmsMap());
			event.setPacket(PacketContainer.fromPacket(packet));
			return;
		}

		if (((packetType != PacketType.Play.Server.LOGIN) && (packetType != PacketType.Play.Server.RESPAWN)) || (event.getPlayer() == null)) { return; }
		if (!PersistentUtils.hasPersistentDataString(event.getPlayer().getWorld(), CustomWorlds.TAG_CUSTOM_WORLD)) { return; }
		CustomWorld type = CustomWorld.getCustomType(PersistentUtils.getPersistentDataString(event.getPlayer().getWorld(), CustomWorlds.TAG_CUSTOM_WORLD));
		if ((type == null) || (type == CustomWorld.NONE)) { return; }

		Object packet = ReflectionUtils.editSpawnPacket(event.getPacket().getHandle(), type.isFlat(), type.getEnvironment());
		event.setPacket(PacketContainer.fromPacket(packet)); //PS: This will not work with overworld because end minY is 0 while overworld minY is -64
	}
}