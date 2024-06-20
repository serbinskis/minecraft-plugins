package net.minecraft.world.level.levelgen.structure.pools;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.Optionull;
import net.minecraft.SystemUtils;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnumBlockRotation;
import net.minecraft.world.level.block.state.properties.BlockPropertyStructureMode;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.StructureBoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructureInfo;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructureProcessorBlockIgnore;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructureProcessorJigsawReplacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructureStructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class WorldGenFeatureDefinedStructurePoolSingle extends WorldGenFeatureDefinedStructurePoolStructure {

    private static final Codec<Either<MinecraftKey, DefinedStructure>> TEMPLATE_CODEC = Codec.of(WorldGenFeatureDefinedStructurePoolSingle::encodeTemplate, MinecraftKey.CODEC.map(Either::left));
    public static final MapCodec<WorldGenFeatureDefinedStructurePoolSingle> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(templateCodec(), processorsCodec(), projectionCodec(), overrideLiquidSettingsCodec()).apply(instance, WorldGenFeatureDefinedStructurePoolSingle::new);
    });
    protected final Either<MinecraftKey, DefinedStructure> template;
    protected final Holder<ProcessorList> processors;
    protected final Optional<LiquidSettings> overrideLiquidSettings;

    private static <T> DataResult<T> encodeTemplate(Either<MinecraftKey, DefinedStructure> either, DynamicOps<T> dynamicops, T t0) {
        Optional<MinecraftKey> optional = either.left();

        return optional.isEmpty() ? DataResult.error(() -> {
            return "Can not serialize a runtime pool element";
        }) : MinecraftKey.CODEC.encode((MinecraftKey) optional.get(), dynamicops, t0);
    }

    protected static <E extends WorldGenFeatureDefinedStructurePoolSingle> RecordCodecBuilder<E, Holder<ProcessorList>> processorsCodec() {
        return DefinedStructureStructureProcessorType.LIST_CODEC.fieldOf("processors").forGetter((worldgenfeaturedefinedstructurepoolsingle) -> {
            return worldgenfeaturedefinedstructurepoolsingle.processors;
        });
    }

    protected static <E extends WorldGenFeatureDefinedStructurePoolSingle> RecordCodecBuilder<E, Optional<LiquidSettings>> overrideLiquidSettingsCodec() {
        return LiquidSettings.CODEC.optionalFieldOf("override_liquid_settings").forGetter((worldgenfeaturedefinedstructurepoolsingle) -> {
            return worldgenfeaturedefinedstructurepoolsingle.overrideLiquidSettings;
        });
    }

    protected static <E extends WorldGenFeatureDefinedStructurePoolSingle> RecordCodecBuilder<E, Either<MinecraftKey, DefinedStructure>> templateCodec() {
        return WorldGenFeatureDefinedStructurePoolSingle.TEMPLATE_CODEC.fieldOf("location").forGetter((worldgenfeaturedefinedstructurepoolsingle) -> {
            return worldgenfeaturedefinedstructurepoolsingle.template;
        });
    }

    protected WorldGenFeatureDefinedStructurePoolSingle(Either<MinecraftKey, DefinedStructure> either, Holder<ProcessorList> holder, WorldGenFeatureDefinedStructurePoolTemplate.Matching worldgenfeaturedefinedstructurepooltemplate_matching, Optional<LiquidSettings> optional) {
        super(worldgenfeaturedefinedstructurepooltemplate_matching);
        this.template = either;
        this.processors = holder;
        this.overrideLiquidSettings = optional;
    }

    @Override
    public BaseBlockPosition getSize(StructureTemplateManager structuretemplatemanager, EnumBlockRotation enumblockrotation) {
        DefinedStructure definedstructure = this.getTemplate(structuretemplatemanager);

        return definedstructure.getSize(enumblockrotation);
    }

    private DefinedStructure getTemplate(StructureTemplateManager structuretemplatemanager) {
        Either either = this.template;

        Objects.requireNonNull(structuretemplatemanager);
        return (DefinedStructure) either.map(structuretemplatemanager::getOrCreate, Function.identity());
    }

    public List<DefinedStructure.BlockInfo> getDataMarkers(StructureTemplateManager structuretemplatemanager, BlockPosition blockposition, EnumBlockRotation enumblockrotation, boolean flag) {
        DefinedStructure definedstructure = this.getTemplate(structuretemplatemanager);
        List<DefinedStructure.BlockInfo> list = definedstructure.filterBlocks(blockposition, (new DefinedStructureInfo()).setRotation(enumblockrotation), Blocks.STRUCTURE_BLOCK, flag);
        List<DefinedStructure.BlockInfo> list1 = Lists.newArrayList();
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            DefinedStructure.BlockInfo definedstructure_blockinfo = (DefinedStructure.BlockInfo) iterator.next();
            NBTTagCompound nbttagcompound = definedstructure_blockinfo.nbt();

            if (nbttagcompound != null) {
                BlockPropertyStructureMode blockpropertystructuremode = BlockPropertyStructureMode.valueOf(nbttagcompound.getString("mode"));

                if (blockpropertystructuremode == BlockPropertyStructureMode.DATA) {
                    list1.add(definedstructure_blockinfo);
                }
            }
        }

        return list1;
    }

    @Override
    public List<DefinedStructure.BlockInfo> getShuffledJigsawBlocks(StructureTemplateManager structuretemplatemanager, BlockPosition blockposition, EnumBlockRotation enumblockrotation, RandomSource randomsource) {
        DefinedStructure definedstructure = this.getTemplate(structuretemplatemanager);
        ObjectArrayList<DefinedStructure.BlockInfo> objectarraylist = definedstructure.filterBlocks(blockposition, (new DefinedStructureInfo()).setRotation(enumblockrotation), Blocks.JIGSAW, true);

        SystemUtils.shuffle(objectarraylist, randomsource);
        sortBySelectionPriority(objectarraylist);
        return objectarraylist;
    }

    @VisibleForTesting
    static void sortBySelectionPriority(List<DefinedStructure.BlockInfo> list) {
        list.sort(Comparator.comparingInt((definedstructure_blockinfo) -> {
            return (Integer) Optionull.mapOrDefault(definedstructure_blockinfo.nbt(), (nbttagcompound) -> {
                return nbttagcompound.getInt("selection_priority");
            }, 0);
        }).reversed());
    }

    @Override
    public StructureBoundingBox getBoundingBox(StructureTemplateManager structuretemplatemanager, BlockPosition blockposition, EnumBlockRotation enumblockrotation) {
        DefinedStructure definedstructure = this.getTemplate(structuretemplatemanager);

        return definedstructure.getBoundingBox((new DefinedStructureInfo()).setRotation(enumblockrotation), blockposition);
    }

    @Override
    public boolean place(StructureTemplateManager structuretemplatemanager, GeneratorAccessSeed generatoraccessseed, StructureManager structuremanager, ChunkGenerator chunkgenerator, BlockPosition blockposition, BlockPosition blockposition1, EnumBlockRotation enumblockrotation, StructureBoundingBox structureboundingbox, RandomSource randomsource, LiquidSettings liquidsettings, boolean flag) {
        DefinedStructure definedstructure = this.getTemplate(structuretemplatemanager);
        DefinedStructureInfo definedstructureinfo = this.getSettings(enumblockrotation, structureboundingbox, liquidsettings, flag);

        if (!definedstructure.placeInWorld(generatoraccessseed, blockposition, blockposition1, definedstructureinfo, randomsource, 18)) {
            return false;
        } else {
            List<DefinedStructure.BlockInfo> list = DefinedStructure.processBlockInfos(generatoraccessseed, blockposition, blockposition1, definedstructureinfo, this.getDataMarkers(structuretemplatemanager, blockposition, enumblockrotation, false));
            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                DefinedStructure.BlockInfo definedstructure_blockinfo = (DefinedStructure.BlockInfo) iterator.next();

                this.handleDataMarker(generatoraccessseed, definedstructure_blockinfo, blockposition, enumblockrotation, randomsource, structureboundingbox);
            }

            return true;
        }
    }

    protected DefinedStructureInfo getSettings(EnumBlockRotation enumblockrotation, StructureBoundingBox structureboundingbox, LiquidSettings liquidsettings, boolean flag) {
        DefinedStructureInfo definedstructureinfo = new DefinedStructureInfo();

        definedstructureinfo.setBoundingBox(structureboundingbox);
        definedstructureinfo.setRotation(enumblockrotation);
        definedstructureinfo.setKnownShape(true);
        definedstructureinfo.setIgnoreEntities(false);
        definedstructureinfo.addProcessor(DefinedStructureProcessorBlockIgnore.STRUCTURE_BLOCK);
        definedstructureinfo.setFinalizeEntities(true);
        definedstructureinfo.setLiquidSettings((LiquidSettings) this.overrideLiquidSettings.orElse(liquidsettings));
        if (!flag) {
            definedstructureinfo.addProcessor(DefinedStructureProcessorJigsawReplacement.INSTANCE);
        }

        List list = ((ProcessorList) this.processors.value()).list();

        Objects.requireNonNull(definedstructureinfo);
        list.forEach(definedstructureinfo::addProcessor);
        ImmutableList immutablelist = this.getProjection().getProcessors();

        Objects.requireNonNull(definedstructureinfo);
        immutablelist.forEach(definedstructureinfo::addProcessor);
        return definedstructureinfo;
    }

    @Override
    public WorldGenFeatureDefinedStructurePools<?> getType() {
        return WorldGenFeatureDefinedStructurePools.SINGLE;
    }

    public String toString() {
        return "Single[" + String.valueOf(this.template) + "]";
    }
}
