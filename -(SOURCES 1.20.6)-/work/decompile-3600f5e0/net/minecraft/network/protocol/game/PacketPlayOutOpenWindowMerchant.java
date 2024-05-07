package net.minecraft.network.protocol.game;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.item.trading.MerchantRecipeList;

public class PacketPlayOutOpenWindowMerchant implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlayOutOpenWindowMerchant> STREAM_CODEC = Packet.codec(PacketPlayOutOpenWindowMerchant::write, PacketPlayOutOpenWindowMerchant::new);
    private final int containerId;
    private final MerchantRecipeList offers;
    private final int villagerLevel;
    private final int villagerXp;
    private final boolean showProgress;
    private final boolean canRestock;

    public PacketPlayOutOpenWindowMerchant(int i, MerchantRecipeList merchantrecipelist, int j, int k, boolean flag, boolean flag1) {
        this.containerId = i;
        this.offers = merchantrecipelist.copy();
        this.villagerLevel = j;
        this.villagerXp = k;
        this.showProgress = flag;
        this.canRestock = flag1;
    }

    private PacketPlayOutOpenWindowMerchant(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        this.containerId = registryfriendlybytebuf.readVarInt();
        this.offers = (MerchantRecipeList) MerchantRecipeList.STREAM_CODEC.decode(registryfriendlybytebuf);
        this.villagerLevel = registryfriendlybytebuf.readVarInt();
        this.villagerXp = registryfriendlybytebuf.readVarInt();
        this.showProgress = registryfriendlybytebuf.readBoolean();
        this.canRestock = registryfriendlybytebuf.readBoolean();
    }

    private void write(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        registryfriendlybytebuf.writeVarInt(this.containerId);
        MerchantRecipeList.STREAM_CODEC.encode(registryfriendlybytebuf, this.offers);
        registryfriendlybytebuf.writeVarInt(this.villagerLevel);
        registryfriendlybytebuf.writeVarInt(this.villagerXp);
        registryfriendlybytebuf.writeBoolean(this.showProgress);
        registryfriendlybytebuf.writeBoolean(this.canRestock);
    }

    @Override
    public PacketType<PacketPlayOutOpenWindowMerchant> type() {
        return GamePacketTypes.CLIENTBOUND_MERCHANT_OFFERS;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleMerchantOffers(this);
    }

    public int getContainerId() {
        return this.containerId;
    }

    public MerchantRecipeList getOffers() {
        return this.offers;
    }

    public int getVillagerLevel() {
        return this.villagerLevel;
    }

    public int getVillagerXp() {
        return this.villagerXp;
    }

    public boolean showProgress() {
        return this.showProgress;
    }

    public boolean canRestock() {
        return this.canRestock;
    }
}
