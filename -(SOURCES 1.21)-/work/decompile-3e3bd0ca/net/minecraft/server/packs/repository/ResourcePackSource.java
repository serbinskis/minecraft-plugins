package net.minecraft.server.packs.repository;

import java.util.function.Consumer;

@FunctionalInterface
public interface ResourcePackSource {

    void loadPacks(Consumer<ResourcePackLoader> consumer);
}
