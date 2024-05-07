package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;

public interface DefinedStructureStructureProcessorType<P extends DefinedStructureProcessor> {

    Codec<DefinedStructureProcessor> SINGLE_CODEC = BuiltInRegistries.STRUCTURE_PROCESSOR.byNameCodec().dispatch("processor_type", DefinedStructureProcessor::getType, DefinedStructureStructureProcessorType::codec);
    Codec<ProcessorList> LIST_OBJECT_CODEC = DefinedStructureStructureProcessorType.SINGLE_CODEC.listOf().xmap(ProcessorList::new, ProcessorList::list);
    Codec<ProcessorList> DIRECT_CODEC = Codec.withAlternative(DefinedStructureStructureProcessorType.LIST_OBJECT_CODEC.fieldOf("processors").codec(), DefinedStructureStructureProcessorType.LIST_OBJECT_CODEC);
    Codec<Holder<ProcessorList>> LIST_CODEC = RegistryFileCodec.create(Registries.PROCESSOR_LIST, DefinedStructureStructureProcessorType.DIRECT_CODEC);
    DefinedStructureStructureProcessorType<DefinedStructureProcessorBlockIgnore> BLOCK_IGNORE = register("block_ignore", DefinedStructureProcessorBlockIgnore.CODEC);
    DefinedStructureStructureProcessorType<DefinedStructureProcessorRotation> BLOCK_ROT = register("block_rot", DefinedStructureProcessorRotation.CODEC);
    DefinedStructureStructureProcessorType<DefinedStructureProcessorGravity> GRAVITY = register("gravity", DefinedStructureProcessorGravity.CODEC);
    DefinedStructureStructureProcessorType<DefinedStructureProcessorJigsawReplacement> JIGSAW_REPLACEMENT = register("jigsaw_replacement", DefinedStructureProcessorJigsawReplacement.CODEC);
    DefinedStructureStructureProcessorType<DefinedStructureProcessorRule> RULE = register("rule", DefinedStructureProcessorRule.CODEC);
    DefinedStructureStructureProcessorType<DefinedStructureProcessorNop> NOP = register("nop", DefinedStructureProcessorNop.CODEC);
    DefinedStructureStructureProcessorType<DefinedStructureProcessorBlockAge> BLOCK_AGE = register("block_age", DefinedStructureProcessorBlockAge.CODEC);
    DefinedStructureStructureProcessorType<DefinedStructureProcessorBlackstoneReplace> BLACKSTONE_REPLACE = register("blackstone_replace", DefinedStructureProcessorBlackstoneReplace.CODEC);
    DefinedStructureStructureProcessorType<DefinedStructureProcessorLavaSubmergedBlock> LAVA_SUBMERGED_BLOCK = register("lava_submerged_block", DefinedStructureProcessorLavaSubmergedBlock.CODEC);
    DefinedStructureStructureProcessorType<ProtectedBlockProcessor> PROTECTED_BLOCKS = register("protected_blocks", ProtectedBlockProcessor.CODEC);
    DefinedStructureStructureProcessorType<CappedProcessor> CAPPED = register("capped", CappedProcessor.CODEC);

    MapCodec<P> codec();

    static <P extends DefinedStructureProcessor> DefinedStructureStructureProcessorType<P> register(String s, MapCodec<P> mapcodec) {
        return (DefinedStructureStructureProcessorType) IRegistry.register(BuiltInRegistries.STRUCTURE_PROCESSOR, s, () -> {
            return mapcodec;
        });
    }
}
