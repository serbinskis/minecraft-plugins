package net.minecraft.server.packs;

import net.minecraft.server.packs.repository.ResourcePackLoader;

public record PackSelectionConfig(boolean required, ResourcePackLoader.Position defaultPosition, boolean fixedPosition) {

}
