package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.IBlockData;

public record CriterionConditionBlock(Optional<TagKey<Block>> tag, Optional<HolderSet<Block>> blocks, Optional<CriterionTriggerProperties> properties, Optional<CriterionConditionNBT> nbt) {

    private static final Codec<HolderSet<Block>> BLOCKS_CODEC = BuiltInRegistries.BLOCK.holderByNameCodec().listOf().xmap(HolderSet::direct, (holderset) -> {
        return holderset.stream().toList();
    });
    public static final Codec<CriterionConditionBlock> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(ExtraCodecs.strictOptionalField(TagKey.codec(Registries.BLOCK), "tag").forGetter(CriterionConditionBlock::tag), ExtraCodecs.strictOptionalField(CriterionConditionBlock.BLOCKS_CODEC, "blocks").forGetter(CriterionConditionBlock::blocks), ExtraCodecs.strictOptionalField(CriterionTriggerProperties.CODEC, "state").forGetter(CriterionConditionBlock::properties), ExtraCodecs.strictOptionalField(CriterionConditionNBT.CODEC, "nbt").forGetter(CriterionConditionBlock::nbt)).apply(instance, CriterionConditionBlock::new);
    });

    public boolean matches(WorldServer worldserver, BlockPosition blockposition) {
        if (!worldserver.isLoaded(blockposition)) {
            return false;
        } else {
            IBlockData iblockdata = worldserver.getBlockState(blockposition);

            if (this.tag.isPresent() && !iblockdata.is((TagKey) this.tag.get())) {
                return false;
            } else if (this.blocks.isPresent() && !iblockdata.is((HolderSet) this.blocks.get())) {
                return false;
            } else if (this.properties.isPresent() && !((CriterionTriggerProperties) this.properties.get()).matches(iblockdata)) {
                return false;
            } else {
                if (this.nbt.isPresent()) {
                    TileEntity tileentity = worldserver.getBlockEntity(blockposition);

                    if (tileentity == null || !((CriterionConditionNBT) this.nbt.get()).matches((NBTBase) tileentity.saveWithFullMetadata())) {
                        return false;
                    }
                }

                return true;
            }
        }
    }

    public static class a {

        private Optional<HolderSet<Block>> blocks = Optional.empty();
        private Optional<TagKey<Block>> tag = Optional.empty();
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
            this.tag = Optional.of(tagkey);
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
            return new CriterionConditionBlock(this.tag, this.blocks, this.properties, this.nbt);
        }
    }
}
