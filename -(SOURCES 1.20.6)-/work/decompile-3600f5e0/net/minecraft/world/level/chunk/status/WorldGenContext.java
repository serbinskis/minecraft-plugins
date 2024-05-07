package net.minecraft.world.level.chunk.status;

import net.minecraft.server.level.LightEngineThreaded;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public record WorldGenContext(WorldServer level, ChunkGenerator generator, StructureTemplateManager structureManager, LightEngineThreaded lightEngine) {

}
