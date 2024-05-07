package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.level.block.Block;

public class PacketPlayOutBlockAction implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlayOutBlockAction> STREAM_CODEC = Packet.codec(PacketPlayOutBlockAction::write, PacketPlayOutBlockAction::new);
    private final BlockPosition pos;
    private final int b0;
    private final int b1;
    private final Block block;

    public PacketPlayOutBlockAction(BlockPosition blockposition, Block block, int i, int j) {
        this.pos = blockposition;
        this.block = block;
        this.b0 = i;
        this.b1 = j;
    }

    private PacketPlayOutBlockAction(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        this.pos = registryfriendlybytebuf.readBlockPos();
        this.b0 = registryfriendlybytebuf.readUnsignedByte();
        this.b1 = registryfriendlybytebuf.readUnsignedByte();
        this.block = (Block) ByteBufCodecs.registry(Registries.BLOCK).decode(registryfriendlybytebuf);
    }

    private void write(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        registryfriendlybytebuf.writeBlockPos(this.pos);
        registryfriendlybytebuf.writeByte(this.b0);
        registryfriendlybytebuf.writeByte(this.b1);
        ByteBufCodecs.registry(Registries.BLOCK).encode(registryfriendlybytebuf, this.block);
    }

    @Override
    public PacketType<PacketPlayOutBlockAction> type() {
        return GamePacketTypes.CLIENTBOUND_BLOCK_EVENT;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleBlockEvent(this);
    }

    public BlockPosition getPos() {
        return this.pos;
    }

    public int getB0() {
        return this.b0;
    }

    public int getB1() {
        return this.b1;
    }

    public Block getBlock() {
        return this.block;
    }
}
