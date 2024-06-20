package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.item.crafting.RecipeHolder;

public class PacketPlayInRecipeDisplayed implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayInRecipeDisplayed> STREAM_CODEC = Packet.codec(PacketPlayInRecipeDisplayed::write, PacketPlayInRecipeDisplayed::new);
    private final MinecraftKey recipe;

    public PacketPlayInRecipeDisplayed(RecipeHolder<?> recipeholder) {
        this.recipe = recipeholder.id();
    }

    private PacketPlayInRecipeDisplayed(PacketDataSerializer packetdataserializer) {
        this.recipe = packetdataserializer.readResourceLocation();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeResourceLocation(this.recipe);
    }

    @Override
    public PacketType<PacketPlayInRecipeDisplayed> type() {
        return GamePacketTypes.SERVERBOUND_RECIPE_BOOK_SEEN_RECIPE;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleRecipeBookSeenRecipePacket(this);
    }

    public MinecraftKey getRecipe() {
        return this.recipe;
    }
}
