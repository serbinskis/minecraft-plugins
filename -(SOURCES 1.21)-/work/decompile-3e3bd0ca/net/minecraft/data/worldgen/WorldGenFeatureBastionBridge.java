package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.pools.WorldGenFeatureDefinedStructurePoolStructure;
import net.minecraft.world.level.levelgen.structure.pools.WorldGenFeatureDefinedStructurePoolTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorList;

public class WorldGenFeatureBastionBridge {

    public WorldGenFeatureBastionBridge() {}

    public static void bootstrap(BootstrapContext<WorldGenFeatureDefinedStructurePoolTemplate> bootstrapcontext) {
        HolderGetter<ProcessorList> holdergetter = bootstrapcontext.lookup(Registries.PROCESSOR_LIST);
        Holder<ProcessorList> holder = holdergetter.getOrThrow(ProcessorLists.ENTRANCE_REPLACEMENT);
        Holder<ProcessorList> holder1 = holdergetter.getOrThrow(ProcessorLists.BASTION_GENERIC_DEGRADATION);
        Holder<ProcessorList> holder2 = holdergetter.getOrThrow(ProcessorLists.BRIDGE);
        Holder<ProcessorList> holder3 = holdergetter.getOrThrow(ProcessorLists.RAMPART_DEGRADATION);
        HolderGetter<WorldGenFeatureDefinedStructurePoolTemplate> holdergetter1 = bootstrapcontext.lookup(Registries.TEMPLATE_POOL);
        Holder<WorldGenFeatureDefinedStructurePoolTemplate> holder4 = holdergetter1.getOrThrow(WorldGenFeaturePieces.EMPTY);

        WorldGenFeaturePieces.register(bootstrapcontext, "bastion/bridge/starting_pieces", new WorldGenFeatureDefinedStructurePoolTemplate(holder4, ImmutableList.of(Pair.of(WorldGenFeatureDefinedStructurePoolStructure.single("bastion/bridge/starting_pieces/entrance", (Holder) holder), 1), Pair.of(WorldGenFeatureDefinedStructurePoolStructure.single("bastion/bridge/starting_pieces/entrance_face", (Holder) holder1), 1)), WorldGenFeatureDefinedStructurePoolTemplate.Matching.RIGID));
        WorldGenFeaturePieces.register(bootstrapcontext, "bastion/bridge/bridge_pieces", new WorldGenFeatureDefinedStructurePoolTemplate(holder4, ImmutableList.of(Pair.of(WorldGenFeatureDefinedStructurePoolStructure.single("bastion/bridge/bridge_pieces/bridge", (Holder) holder2), 1)), WorldGenFeatureDefinedStructurePoolTemplate.Matching.RIGID));
        WorldGenFeaturePieces.register(bootstrapcontext, "bastion/bridge/legs", new WorldGenFeatureDefinedStructurePoolTemplate(holder4, ImmutableList.of(Pair.of(WorldGenFeatureDefinedStructurePoolStructure.single("bastion/bridge/legs/leg_0", (Holder) holder1), 1), Pair.of(WorldGenFeatureDefinedStructurePoolStructure.single("bastion/bridge/legs/leg_1", (Holder) holder1), 1)), WorldGenFeatureDefinedStructurePoolTemplate.Matching.RIGID));
        WorldGenFeaturePieces.register(bootstrapcontext, "bastion/bridge/walls", new WorldGenFeatureDefinedStructurePoolTemplate(holder4, ImmutableList.of(Pair.of(WorldGenFeatureDefinedStructurePoolStructure.single("bastion/bridge/walls/wall_base_0", (Holder) holder3), 1), Pair.of(WorldGenFeatureDefinedStructurePoolStructure.single("bastion/bridge/walls/wall_base_1", (Holder) holder3), 1)), WorldGenFeatureDefinedStructurePoolTemplate.Matching.RIGID));
        WorldGenFeaturePieces.register(bootstrapcontext, "bastion/bridge/ramparts", new WorldGenFeatureDefinedStructurePoolTemplate(holder4, ImmutableList.of(Pair.of(WorldGenFeatureDefinedStructurePoolStructure.single("bastion/bridge/ramparts/rampart_0", (Holder) holder3), 1), Pair.of(WorldGenFeatureDefinedStructurePoolStructure.single("bastion/bridge/ramparts/rampart_1", (Holder) holder3), 1)), WorldGenFeatureDefinedStructurePoolTemplate.Matching.RIGID));
        WorldGenFeaturePieces.register(bootstrapcontext, "bastion/bridge/rampart_plates", new WorldGenFeatureDefinedStructurePoolTemplate(holder4, ImmutableList.of(Pair.of(WorldGenFeatureDefinedStructurePoolStructure.single("bastion/bridge/rampart_plates/plate_0", (Holder) holder3), 1)), WorldGenFeatureDefinedStructurePoolTemplate.Matching.RIGID));
        WorldGenFeaturePieces.register(bootstrapcontext, "bastion/bridge/connectors", new WorldGenFeatureDefinedStructurePoolTemplate(holder4, ImmutableList.of(Pair.of(WorldGenFeatureDefinedStructurePoolStructure.single("bastion/bridge/connectors/back_bridge_top", (Holder) holder1), 1), Pair.of(WorldGenFeatureDefinedStructurePoolStructure.single("bastion/bridge/connectors/back_bridge_bottom", (Holder) holder1), 1)), WorldGenFeatureDefinedStructurePoolTemplate.Matching.RIGID));
    }
}
