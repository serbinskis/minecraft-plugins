package net.minecraft.network.protocol.game;

import java.util.Collection;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.item.crafting.RecipeHolder;

public class PacketPlayOutRecipeUpdate implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlayOutRecipeUpdate> STREAM_CODEC = StreamCodec.composite(RecipeHolder.STREAM_CODEC.apply(ByteBufCodecs.list()), (packetplayoutrecipeupdate) -> {
        return packetplayoutrecipeupdate.recipes;
    }, PacketPlayOutRecipeUpdate::new);
    private final List<RecipeHolder<?>> recipes;

    public PacketPlayOutRecipeUpdate(Collection<RecipeHolder<?>> collection) {
        this.recipes = List.copyOf(collection);
    }

    @Override
    public PacketType<PacketPlayOutRecipeUpdate> type() {
        return GamePacketTypes.CLIENTBOUND_UPDATE_RECIPES;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleUpdateRecipes(this);
    }

    public List<RecipeHolder<?>> getRecipes() {
        return this.recipes;
    }
}
