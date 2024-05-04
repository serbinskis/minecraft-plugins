package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemWorldMap;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.WorldMap;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3D;

public class LootItemFunctionExplorationMap extends LootItemFunctionConditional {

    public static final TagKey<Structure> DEFAULT_DESTINATION = StructureTags.ON_TREASURE_MAPS;
    public static final Holder<MapDecorationType> DEFAULT_DECORATION = MapDecorationTypes.WOODLAND_MANSION;
    public static final byte DEFAULT_ZOOM = 2;
    public static final int DEFAULT_SEARCH_RADIUS = 50;
    public static final boolean DEFAULT_SKIP_EXISTING = true;
    public static final MapCodec<LootItemFunctionExplorationMap> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(instance.group(TagKey.codec(Registries.STRUCTURE).optionalFieldOf("destination", LootItemFunctionExplorationMap.DEFAULT_DESTINATION).forGetter((lootitemfunctionexplorationmap) -> {
            return lootitemfunctionexplorationmap.destination;
        }), MapDecorationType.CODEC.optionalFieldOf("decoration", LootItemFunctionExplorationMap.DEFAULT_DECORATION).forGetter((lootitemfunctionexplorationmap) -> {
            return lootitemfunctionexplorationmap.mapDecoration;
        }), Codec.BYTE.optionalFieldOf("zoom", (byte) 2).forGetter((lootitemfunctionexplorationmap) -> {
            return lootitemfunctionexplorationmap.zoom;
        }), Codec.INT.optionalFieldOf("search_radius", 50).forGetter((lootitemfunctionexplorationmap) -> {
            return lootitemfunctionexplorationmap.searchRadius;
        }), Codec.BOOL.optionalFieldOf("skip_existing_chunks", true).forGetter((lootitemfunctionexplorationmap) -> {
            return lootitemfunctionexplorationmap.skipKnownStructures;
        }))).apply(instance, LootItemFunctionExplorationMap::new);
    });
    private final TagKey<Structure> destination;
    private final Holder<MapDecorationType> mapDecoration;
    private final byte zoom;
    private final int searchRadius;
    private final boolean skipKnownStructures;

    LootItemFunctionExplorationMap(List<LootItemCondition> list, TagKey<Structure> tagkey, Holder<MapDecorationType> holder, byte b0, int i, boolean flag) {
        super(list);
        this.destination = tagkey;
        this.mapDecoration = holder;
        this.zoom = b0;
        this.searchRadius = i;
        this.skipKnownStructures = flag;
    }

    @Override
    public LootItemFunctionType<LootItemFunctionExplorationMap> getType() {
        return LootItemFunctions.EXPLORATION_MAP;
    }

    @Override
    public Set<LootContextParameter<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParameters.ORIGIN);
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        if (!itemstack.is(Items.MAP)) {
            return itemstack;
        } else {
            Vec3D vec3d = (Vec3D) loottableinfo.getParamOrNull(LootContextParameters.ORIGIN);

            if (vec3d != null) {
                WorldServer worldserver = loottableinfo.getLevel();
                BlockPosition blockposition = worldserver.findNearestMapStructure(this.destination, BlockPosition.containing(vec3d), this.searchRadius, this.skipKnownStructures);

                if (blockposition != null) {
                    ItemStack itemstack1 = ItemWorldMap.create(worldserver, blockposition.getX(), blockposition.getZ(), this.zoom, true, true);

                    ItemWorldMap.renderBiomePreviewMap(worldserver, itemstack1);
                    WorldMap.addTargetDecoration(itemstack1, blockposition, "+", this.mapDecoration);
                    return itemstack1;
                }
            }

            return itemstack;
        }
    }

    public static LootItemFunctionExplorationMap.a makeExplorationMap() {
        return new LootItemFunctionExplorationMap.a();
    }

    public static class a extends LootItemFunctionConditional.a<LootItemFunctionExplorationMap.a> {

        private TagKey<Structure> destination;
        private Holder<MapDecorationType> mapDecoration;
        private byte zoom;
        private int searchRadius;
        private boolean skipKnownStructures;

        public a() {
            this.destination = LootItemFunctionExplorationMap.DEFAULT_DESTINATION;
            this.mapDecoration = LootItemFunctionExplorationMap.DEFAULT_DECORATION;
            this.zoom = 2;
            this.searchRadius = 50;
            this.skipKnownStructures = true;
        }

        @Override
        protected LootItemFunctionExplorationMap.a getThis() {
            return this;
        }

        public LootItemFunctionExplorationMap.a setDestination(TagKey<Structure> tagkey) {
            this.destination = tagkey;
            return this;
        }

        public LootItemFunctionExplorationMap.a setMapDecoration(Holder<MapDecorationType> holder) {
            this.mapDecoration = holder;
            return this;
        }

        public LootItemFunctionExplorationMap.a setZoom(byte b0) {
            this.zoom = b0;
            return this;
        }

        public LootItemFunctionExplorationMap.a setSearchRadius(int i) {
            this.searchRadius = i;
            return this;
        }

        public LootItemFunctionExplorationMap.a setSkipKnownStructures(boolean flag) {
            this.skipKnownStructures = flag;
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new LootItemFunctionExplorationMap(this.getConditions(), this.destination, this.mapDecoration, this.zoom, this.searchRadius, this.skipKnownStructures);
        }
    }
}
