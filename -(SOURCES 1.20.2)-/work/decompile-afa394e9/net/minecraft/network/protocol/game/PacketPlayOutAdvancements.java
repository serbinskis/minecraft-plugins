package net.minecraft.network.protocol.game;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.MinecraftKey;

public class PacketPlayOutAdvancements implements Packet<PacketListenerPlayOut> {

    private final boolean reset;
    private final List<AdvancementHolder> added;
    private final Set<MinecraftKey> removed;
    private final Map<MinecraftKey, AdvancementProgress> progress;

    public PacketPlayOutAdvancements(boolean flag, Collection<AdvancementHolder> collection, Set<MinecraftKey> set, Map<MinecraftKey, AdvancementProgress> map) {
        this.reset = flag;
        this.added = List.copyOf(collection);
        this.removed = Set.copyOf(set);
        this.progress = Map.copyOf(map);
    }

    public PacketPlayOutAdvancements(PacketDataSerializer packetdataserializer) {
        this.reset = packetdataserializer.readBoolean();
        this.added = packetdataserializer.readList(AdvancementHolder::read);
        this.removed = (Set) packetdataserializer.readCollection(Sets::newLinkedHashSetWithExpectedSize, PacketDataSerializer::readResourceLocation);
        this.progress = packetdataserializer.readMap(PacketDataSerializer::readResourceLocation, AdvancementProgress::fromNetwork);
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeBoolean(this.reset);
        packetdataserializer.writeCollection(this.added, (packetdataserializer1, advancementholder) -> {
            advancementholder.write(packetdataserializer1);
        });
        packetdataserializer.writeCollection(this.removed, PacketDataSerializer::writeResourceLocation);
        packetdataserializer.writeMap(this.progress, PacketDataSerializer::writeResourceLocation, (packetdataserializer1, advancementprogress) -> {
            advancementprogress.serializeToNetwork(packetdataserializer1);
        });
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleUpdateAdvancementsPacket(this);
    }

    public List<AdvancementHolder> getAdded() {
        return this.added;
    }

    public Set<MinecraftKey> getRemoved() {
        return this.removed;
    }

    public Map<MinecraftKey, AdvancementProgress> getProgress() {
        return this.progress;
    }

    public boolean shouldReset() {
        return this.reset;
    }
}
