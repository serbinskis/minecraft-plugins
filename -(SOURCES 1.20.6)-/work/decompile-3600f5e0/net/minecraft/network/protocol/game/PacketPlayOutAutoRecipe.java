package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.item.crafting.RecipeHolder;

public class PacketPlayOutAutoRecipe implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayOutAutoRecipe> STREAM_CODEC = Packet.codec(PacketPlayOutAutoRecipe::write, PacketPlayOutAutoRecipe::new);
    private final int containerId;
    private final MinecraftKey recipe;

    public PacketPlayOutAutoRecipe(int i, RecipeHolder<?> recipeholder) {
        this.containerId = i;
        this.recipe = recipeholder.id();
    }

    private PacketPlayOutAutoRecipe(PacketDataSerializer packetdataserializer) {
        this.containerId = packetdataserializer.readByte();
        this.recipe = packetdataserializer.readResourceLocation();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeByte(this.containerId);
        packetdataserializer.writeResourceLocation(this.recipe);
    }

    @Override
    public PacketType<PacketPlayOutAutoRecipe> type() {
        return GamePacketTypes.CLIENTBOUND_PLACE_GHOST_RECIPE;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handlePlaceRecipe(this);
    }

    public MinecraftKey getRecipe() {
        return this.recipe;
    }

    public int getContainerId() {
        return this.containerId;
    }
}
