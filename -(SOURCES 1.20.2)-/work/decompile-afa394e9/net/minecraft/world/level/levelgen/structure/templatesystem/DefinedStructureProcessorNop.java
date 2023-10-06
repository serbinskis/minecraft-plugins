package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;

public class DefinedStructureProcessorNop extends DefinedStructureProcessor {

    public static final Codec<DefinedStructureProcessorNop> CODEC = Codec.unit(() -> {
        return DefinedStructureProcessorNop.INSTANCE;
    });
    public static final DefinedStructureProcessorNop INSTANCE = new DefinedStructureProcessorNop();

    private DefinedStructureProcessorNop() {}

    @Override
    protected DefinedStructureStructureProcessorType<?> getType() {
        return DefinedStructureStructureProcessorType.NOP;
    }
}
