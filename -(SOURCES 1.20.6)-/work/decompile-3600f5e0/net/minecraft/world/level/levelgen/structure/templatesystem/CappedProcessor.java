package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.WorldAccess;

public class CappedProcessor extends DefinedStructureProcessor {

    public static final MapCodec<CappedProcessor> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(DefinedStructureStructureProcessorType.SINGLE_CODEC.fieldOf("delegate").forGetter((cappedprocessor) -> {
            return cappedprocessor.delegate;
        }), IntProvider.POSITIVE_CODEC.fieldOf("limit").forGetter((cappedprocessor) -> {
            return cappedprocessor.limit;
        })).apply(instance, CappedProcessor::new);
    });
    private final DefinedStructureProcessor delegate;
    private final IntProvider limit;

    public CappedProcessor(DefinedStructureProcessor definedstructureprocessor, IntProvider intprovider) {
        this.delegate = definedstructureprocessor;
        this.limit = intprovider;
    }

    @Override
    protected DefinedStructureStructureProcessorType<?> getType() {
        return DefinedStructureStructureProcessorType.CAPPED;
    }

    @Override
    public final List<DefinedStructure.BlockInfo> finalizeProcessing(WorldAccess worldaccess, BlockPosition blockposition, BlockPosition blockposition1, List<DefinedStructure.BlockInfo> list, List<DefinedStructure.BlockInfo> list1, DefinedStructureInfo definedstructureinfo) {
        if (this.limit.getMaxValue() != 0 && !list1.isEmpty()) {
            if (list.size() != list1.size()) {
                int i = list.size();

                SystemUtils.logAndPauseIfInIde("Original block info list not in sync with processed list, skipping processing. Original size: " + i + ", Processed size: " + list1.size());
                return list1;
            } else {
                RandomSource randomsource = RandomSource.create(worldaccess.getLevel().getSeed()).forkPositional().at(blockposition);
                int j = Math.min(this.limit.sample(randomsource), list1.size());

                if (j < 1) {
                    return list1;
                } else {
                    IntArrayList intarraylist = SystemUtils.toShuffledList(IntStream.range(0, list1.size()), randomsource);
                    IntIterator intiterator = intarraylist.intIterator();
                    int k = 0;

                    while (intiterator.hasNext() && k < j) {
                        int l = intiterator.nextInt();
                        DefinedStructure.BlockInfo definedstructure_blockinfo = (DefinedStructure.BlockInfo) list.get(l);
                        DefinedStructure.BlockInfo definedstructure_blockinfo1 = (DefinedStructure.BlockInfo) list1.get(l);
                        DefinedStructure.BlockInfo definedstructure_blockinfo2 = this.delegate.processBlock(worldaccess, blockposition, blockposition1, definedstructure_blockinfo, definedstructure_blockinfo1, definedstructureinfo);

                        if (definedstructure_blockinfo2 != null && !definedstructure_blockinfo1.equals(definedstructure_blockinfo2)) {
                            ++k;
                            list1.set(l, definedstructure_blockinfo2);
                        }
                    }

                    return list1;
                }
            }
        } else {
            return list1;
        }
    }
}
