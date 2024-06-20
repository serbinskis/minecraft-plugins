package net.minecraft.references;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public class Items {

    public static final ResourceKey<Item> PUMPKIN_SEEDS = createKey("pumpkin_seeds");
    public static final ResourceKey<Item> MELON_SEEDS = createKey("melon_seeds");

    public Items() {}

    private static ResourceKey<Item> createKey(String s) {
        return ResourceKey.create(Registries.ITEM, MinecraftKey.withDefaultNamespace(s));
    }
}
