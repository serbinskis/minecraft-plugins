package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.item.crafting.RecipeHolder;

public class PacketPlayInAutoRecipe implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayInAutoRecipe> STREAM_CODEC = Packet.codec(PacketPlayInAutoRecipe::write, PacketPlayInAutoRecipe::new);
    private final int containerId;
    private final MinecraftKey recipe;
    private final boolean shiftDown;

    public PacketPlayInAutoRecipe(int i, RecipeHolder<?> recipeholder, boolean flag) {
        this.containerId = i;
        this.recipe = recipeholder.id();
        this.shiftDown = flag;
    }

    private PacketPlayInAutoRecipe(PacketDataSerializer packetdataserializer) {
        this.containerId = packetdataserializer.readByte();
        this.recipe = packetdataserializer.readResourceLocation();
        this.shiftDown = packetdataserializer.readBoolean();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeByte(this.containerId);
        packetdataserializer.writeResourceLocation(this.recipe);
        packetdataserializer.writeBoolean(this.shiftDown);
    }

    @Override
    public PacketType<PacketPlayInAutoRecipe> type() {
        return GamePacketTypes.SERVERBOUND_PLACE_RECIPE;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handlePlaceRecipe(this);
    }

    public int getContainerId() {
        return this.containerId;
    }

    public MinecraftKey getRecipe() {
        return this.recipe;
    }

    public boolean isShiftDown() {
        return this.shiftDown;
    }
}
