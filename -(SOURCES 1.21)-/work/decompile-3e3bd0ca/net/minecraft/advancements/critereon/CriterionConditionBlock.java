package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.pattern.ShapeDetectorBlock;

public record CriterionConditionBlock(Optional<HolderSet<Block>> blocks, Optional<CriterionTriggerProperties> properties, Optional<CriterionConditionNBT> nbt) {

    public static final Codec<CriterionConditionBlock> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(RegistryCodecs.homogeneousList(Registries.BLOCK).optionalFieldOf("blocks").forGetter(CriterionConditionBlock::blocks), CriterionTriggerProperties.CODEC.optionalFieldOf("state").forGetter(CriterionConditionBlock::properties), CriterionConditionNBT.CODEC.optionalFieldOf("nbt").forGetter(CriterionConditionBlock::nbt)).apply(instance, CriterionConditionBlock::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, CriterionConditionBlock> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.optional(ByteBufCodecs.holderSet(Registries.BLOCK)), CriterionConditionBlock::blocks, ByteBufCodecs.optional(CriterionTriggerProperties.STREAM_CODEC), CriterionConditionBlock::properties, ByteBufCodecs.optional(CriterionConditionNBT.STREAM_CODEC), CriterionConditionBlock::nbt, CriterionConditionBlock::new);

    public boolean matches(WorldServer worldserver, BlockPosition blockposition) {
        return !worldserver.isLoaded(blockposition) ? false : (!this.matchesState(worldserver.getBlockState(blockposition)) ? false : !this.nbt.isPresent() || matchesBlockEntity(worldserver, worldserver.getBlockEntity(blockposition), (CriterionConditionNBT) this.nbt.get()));
    }

    public boolean matches(ShapeDetectorBlock shapedetectorblock) {
        return !this.matchesState(shapedetectorblock.getState()) ? false : !this.nbt.isPresent() || matchesBlockEntity(shapedetectorblock.getLevel(), shapedetectorblock.getEntity(), (CriterionConditionNBT) this.nbt.get());
    }

    private boolean matchesState(IBlockData iblockdata) {
        return this.blocks.isPresent() && !iblockdata.is((HolderSet) this.blocks.get()) ? false : !this.properties.isPresent() || ((CriterionTriggerProperties) this.properties.get()).matches(iblockdata);
    }

    private static boolean matchesBlockEntity(IWorldReader iworldreader, @Nullable TileEntity tileentity, CriterionConditionNBT criterionconditionnbt) {
        return tileentity != null && criterionconditionnbt.matches((NBTBase) tileentity.saveWithFullMetadata(iworldreader.registryAccess()));
    }

    public boolean requiresNbt() {
        return this.nbt.isPresent();
    }

    public static class a {

        private Optional<HolderSet<Block>> blocks = Optional.empty();
        private Optional<CriterionTriggerProperties> properties = Optional.empty();
        private Optional<CriterionConditionNBT> nbt = Optional.empty();

        private a() {}

        public static CriterionConditionBlock.a block() {
            return new CriterionConditionBlock.a();
        }

        public CriterionConditionBlock.a of(Block... ablock) {
            this.blocks = Optional.of(HolderSet.direct(Block::builtInRegistryHolder, (Object[]) ablock));
            return this;
        }

        public CriterionConditionBlock.a of(Collection<Block> collection) {
            this.blocks = Optional.of(HolderSet.direct(Block::builtInRegistryHolder, collection));
            return this;
        }

        public CriterionConditionBlock.a of(TagKey<Block> tagkey) {
            this.blocks = Optional.of(BuiltInRegistries.BLOCK.getOrCreateTag(tagkey));
            return this;
        }

        public CriterionConditionBlock.a hasNbt(NBTTagCompound nbttagcompound) {
            this.nbt = Optional.of(new CriterionConditionNBT(nbttagcompound));
            return this;
        }

        public CriterionConditionBlock.a setProperties(CriterionTriggerProperties.a criteriontriggerproperties_a) {
            this.properties = criteriontriggerproperties_a.build();
            return this;
        }

        public CriterionConditionBlock build() {
            return new CriterionConditionBlock(this.blocks, this.properties, this.nbt);
        }
    }
}
