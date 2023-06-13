package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.blocks.ArgumentBlock;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import org.slf4j.Logger;

public class DefinedStructureProcessorJigsawReplacement extends DefinedStructureProcessor {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<DefinedStructureProcessorJigsawReplacement> CODEC = Codec.unit(() -> {
        return DefinedStructureProcessorJigsawReplacement.INSTANCE;
    });
    public static final DefinedStructureProcessorJigsawReplacement INSTANCE = new DefinedStructureProcessorJigsawReplacement();

    private DefinedStructureProcessorJigsawReplacement() {}

    @Nullable
    @Override
    public DefinedStructure.BlockInfo processBlock(IWorldReader iworldreader, BlockPosition blockposition, BlockPosition blockposition1, DefinedStructure.BlockInfo definedstructure_blockinfo, DefinedStructure.BlockInfo definedstructure_blockinfo1, DefinedStructureInfo definedstructureinfo) {
        IBlockData iblockdata = definedstructure_blockinfo1.state();

        if (iblockdata.is(Blocks.JIGSAW)) {
            if (definedstructure_blockinfo1.nbt() == null) {
                DefinedStructureProcessorJigsawReplacement.LOGGER.warn("Jigsaw block at {} is missing nbt, will not replace", blockposition);
                return definedstructure_blockinfo1;
            } else {
                String s = definedstructure_blockinfo1.nbt().getString("final_state");

                IBlockData iblockdata1;

                try {
                    ArgumentBlock.a argumentblock_a = ArgumentBlock.parseForBlock(iworldreader.holderLookup(Registries.BLOCK), s, true);

                    iblockdata1 = argumentblock_a.blockState();
                } catch (CommandSyntaxException commandsyntaxexception) {
                    throw new RuntimeException(commandsyntaxexception);
                }

                return iblockdata1.is(Blocks.STRUCTURE_VOID) ? null : new DefinedStructure.BlockInfo(definedstructure_blockinfo1.pos(), iblockdata1, (NBTTagCompound) null);
            }
        } else {
            return definedstructure_blockinfo1;
        }
    }

    @Override
    protected DefinedStructureStructureProcessorType<?> getType() {
        return DefinedStructureStructureProcessorType.JIGSAW_REPLACEMENT;
    }
}
