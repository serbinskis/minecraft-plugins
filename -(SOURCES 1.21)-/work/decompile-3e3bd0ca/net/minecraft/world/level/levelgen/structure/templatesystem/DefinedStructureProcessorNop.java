package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.MapCodec;

public class DefinedStructureProcessorNop extends DefinedStructureProcessor {

    public static final MapCodec<DefinedStructureProcessorNop> CODEC = MapCodec.unit(() -> {
        return DefinedStructureProcessorNop.INSTANCE;
    });
    public static final DefinedStructureProcessorNop INSTANCE = new DefinedStructureProcessorNop();

    private DefinedStructureProcessorNop() {}

    @Override
    protected DefinedStructureStructureProcessorType<?> getType() {
        return DefinedStructureStructureProcessorType.NOP;
    }
}
