package net.minecraft.world.level.levelgen.structure.templatesystem;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.WorldAccess;

public abstract class DefinedStructureProcessor {

    public DefinedStructureProcessor() {}

    @Nullable
    public DefinedStructure.BlockInfo processBlock(IWorldReader iworldreader, BlockPosition blockposition, BlockPosition blockposition1, DefinedStructure.BlockInfo definedstructure_blockinfo, DefinedStructure.BlockInfo definedstructure_blockinfo1, DefinedStructureInfo definedstructureinfo) {
        return definedstructure_blockinfo1;
    }

    protected abstract DefinedStructureStructureProcessorType<?> getType();

    public List<DefinedStructure.BlockInfo> finalizeProcessing(WorldAccess worldaccess, BlockPosition blockposition, BlockPosition blockposition1, List<DefinedStructure.BlockInfo> list, List<DefinedStructure.BlockInfo> list1, DefinedStructureInfo definedstructureinfo) {
        return list1;
    }
}
