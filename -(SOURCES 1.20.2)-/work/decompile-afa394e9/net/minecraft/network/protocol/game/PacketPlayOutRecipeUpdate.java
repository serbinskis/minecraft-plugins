package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.item.crafting.IRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class PacketPlayOutRecipeUpdate implements Packet<PacketListenerPlayOut> {

    private final List<RecipeHolder<?>> recipes;

    public PacketPlayOutRecipeUpdate(Collection<RecipeHolder<?>> collection) {
        this.recipes = Lists.newArrayList(collection);
    }

    public PacketPlayOutRecipeUpdate(PacketDataSerializer packetdataserializer) {
        this.recipes = packetdataserializer.readList(PacketPlayOutRecipeUpdate::fromNetwork);
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeCollection(this.recipes, PacketPlayOutRecipeUpdate::toNetwork);
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleUpdateRecipes(this);
    }

    public List<RecipeHolder<?>> getRecipes() {
        return this.recipes;
    }

    private static RecipeHolder<?> fromNetwork(PacketDataSerializer packetdataserializer) {
        MinecraftKey minecraftkey = packetdataserializer.readResourceLocation();
        MinecraftKey minecraftkey1 = packetdataserializer.readResourceLocation();
        IRecipe<?> irecipe = ((RecipeSerializer) BuiltInRegistries.RECIPE_SERIALIZER.getOptional(minecraftkey).orElseThrow(() -> {
            return new IllegalArgumentException("Unknown recipe serializer " + minecraftkey);
        })).fromNetwork(packetdataserializer);

        return new RecipeHolder<>(minecraftkey1, irecipe);
    }

    public static <T extends IRecipe<?>> void toNetwork(PacketDataSerializer packetdataserializer, RecipeHolder<?> recipeholder) {
        packetdataserializer.writeResourceLocation(BuiltInRegistries.RECIPE_SERIALIZER.getKey(recipeholder.value().getSerializer()));
        packetdataserializer.writeResourceLocation(recipeholder.id());
        recipeholder.value().getSerializer().toNetwork(packetdataserializer, recipeholder.value());
    }
}
