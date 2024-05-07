package net.minecraft.network.protocol.game;

import java.util.List;
import java.util.Optional;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record PacketPlayInBEdit(int slot, List<String> pages, Optional<String> title) implements Packet<PacketListenerPlayIn> {

    public static final int MAX_BYTES_PER_CHAR = 4;
    private static final int TITLE_MAX_CHARS = 128;
    private static final int PAGE_MAX_CHARS = 8192;
    private static final int MAX_PAGES_COUNT = 200;
    public static final StreamCodec<PacketDataSerializer, PacketPlayInBEdit> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, PacketPlayInBEdit::slot, ByteBufCodecs.stringUtf8(8192).apply(ByteBufCodecs.list(200)), PacketPlayInBEdit::pages, ByteBufCodecs.stringUtf8(128).apply(ByteBufCodecs::optional), PacketPlayInBEdit::title, PacketPlayInBEdit::new);

    public PacketPlayInBEdit(int slot, List<String> pages, Optional<String> title) {
        pages = List.copyOf(pages);
        this.slot = slot;
        this.pages = pages;
        this.title = title;
    }

    @Override
    public PacketType<PacketPlayInBEdit> type() {
        return GamePacketTypes.SERVERBOUND_EDIT_BOOK;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleEditBook(this);
    }
}
