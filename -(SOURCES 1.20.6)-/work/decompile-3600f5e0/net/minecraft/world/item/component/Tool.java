package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.IBlockData;

public record Tool(List<Tool.a> rules, float defaultMiningSpeed, int damagePerBlock) {

    public static final Codec<Tool> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(Tool.a.CODEC.listOf().fieldOf("rules").forGetter(Tool::rules), Codec.FLOAT.optionalFieldOf("default_mining_speed", 1.0F).forGetter(Tool::defaultMiningSpeed), ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("damage_per_block", 1).forGetter(Tool::damagePerBlock)).apply(instance, Tool::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, Tool> STREAM_CODEC = StreamCodec.composite(Tool.a.STREAM_CODEC.apply(ByteBufCodecs.list()), Tool::rules, ByteBufCodecs.FLOAT, Tool::defaultMiningSpeed, ByteBufCodecs.VAR_INT, Tool::damagePerBlock, Tool::new);

    public float getMiningSpeed(IBlockData iblockdata) {
        Iterator iterator = this.rules.iterator();

        Tool.a tool_a;

        do {
            if (!iterator.hasNext()) {
                return this.defaultMiningSpeed;
            }

            tool_a = (Tool.a) iterator.next();
        } while (!tool_a.speed.isPresent() || !iblockdata.is(tool_a.blocks));

        return (Float) tool_a.speed.get();
    }

    public boolean isCorrectForDrops(IBlockData iblockdata) {
        Iterator iterator = this.rules.iterator();

        Tool.a tool_a;

        do {
            if (!iterator.hasNext()) {
                return false;
            }

            tool_a = (Tool.a) iterator.next();
        } while (!tool_a.correctForDrops.isPresent() || !iblockdata.is(tool_a.blocks));

        return (Boolean) tool_a.correctForDrops.get();
    }

    public static record a(HolderSet<Block> blocks, Optional<Float> speed, Optional<Boolean> correctForDrops) {

        public static final Codec<Tool.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("blocks").forGetter(Tool.a::blocks), ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("speed").forGetter(Tool.a::speed), Codec.BOOL.optionalFieldOf("correct_for_drops").forGetter(Tool.a::correctForDrops)).apply(instance, Tool.a::new);
        });
        public static final StreamCodec<RegistryFriendlyByteBuf, Tool.a> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.holderSet(Registries.BLOCK), Tool.a::blocks, ByteBufCodecs.FLOAT.apply(ByteBufCodecs::optional), Tool.a::speed, ByteBufCodecs.BOOL.apply(ByteBufCodecs::optional), Tool.a::correctForDrops, Tool.a::new);

        public static Tool.a minesAndDrops(List<Block> list, float f) {
            return forBlocks(list, Optional.of(f), Optional.of(true));
        }

        public static Tool.a minesAndDrops(TagKey<Block> tagkey, float f) {
            return forTag(tagkey, Optional.of(f), Optional.of(true));
        }

        public static Tool.a deniesDrops(TagKey<Block> tagkey) {
            return forTag(tagkey, Optional.empty(), Optional.of(false));
        }

        public static Tool.a overrideSpeed(TagKey<Block> tagkey, float f) {
            return forTag(tagkey, Optional.of(f), Optional.empty());
        }

        public static Tool.a overrideSpeed(List<Block> list, float f) {
            return forBlocks(list, Optional.of(f), Optional.empty());
        }

        private static Tool.a forTag(TagKey<Block> tagkey, Optional<Float> optional, Optional<Boolean> optional1) {
            return new Tool.a(BuiltInRegistries.BLOCK.getOrCreateTag(tagkey), optional, optional1);
        }

        private static Tool.a forBlocks(List<Block> list, Optional<Float> optional, Optional<Boolean> optional1) {
            return new Tool.a(HolderSet.direct((List) list.stream().map(Block::builtInRegistryHolder).collect(Collectors.toList())), optional, optional1);
        }
    }
}
