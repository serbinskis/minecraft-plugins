package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.inventory.RecipeBookType;

public class PacketPlayInRecipeSettings implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayInRecipeSettings> STREAM_CODEC = Packet.codec(PacketPlayInRecipeSettings::write, PacketPlayInRecipeSettings::new);
    private final RecipeBookType bookType;
    private final boolean isOpen;
    private final boolean isFiltering;

    public PacketPlayInRecipeSettings(RecipeBookType recipebooktype, boolean flag, boolean flag1) {
        this.bookType = recipebooktype;
        this.isOpen = flag;
        this.isFiltering = flag1;
    }

    private PacketPlayInRecipeSettings(PacketDataSerializer packetdataserializer) {
        this.bookType = (RecipeBookType) packetdataserializer.readEnum(RecipeBookType.class);
        this.isOpen = packetdataserializer.readBoolean();
        this.isFiltering = packetdataserializer.readBoolean();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeEnum(this.bookType);
        packetdataserializer.writeBoolean(this.isOpen);
        packetdataserializer.writeBoolean(this.isFiltering);
    }

    @Override
    public PacketType<PacketPlayInRecipeSettings> type() {
        return GamePacketTypes.SERVERBOUND_RECIPE_BOOK_CHANGE_SETTINGS;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleRecipeBookChangeSettingsPacket(this);
    }

    public RecipeBookType getBookType() {
        return this.bookType;
    }

    public boolean isOpen() {
        return this.isOpen;
    }

    public boolean isFiltering() {
        return this.isFiltering;
    }
}
