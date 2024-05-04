package net.minecraft.world.level.block;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.util.INamable;
import net.minecraft.world.level.block.state.IBlockData;

public interface WeatheringCopper extends ChangeOverTimeBlock<WeatheringCopper.a> {

    Supplier<BiMap<Block, Block>> NEXT_BY_BLOCK = Suppliers.memoize(() -> {
        return ImmutableBiMap.builder().put(Blocks.COPPER_BLOCK, Blocks.EXPOSED_COPPER).put(Blocks.EXPOSED_COPPER, Blocks.WEATHERED_COPPER).put(Blocks.WEATHERED_COPPER, Blocks.OXIDIZED_COPPER).put(Blocks.CUT_COPPER, Blocks.EXPOSED_CUT_COPPER).put(Blocks.EXPOSED_CUT_COPPER, Blocks.WEATHERED_CUT_COPPER).put(Blocks.WEATHERED_CUT_COPPER, Blocks.OXIDIZED_CUT_COPPER).put(Blocks.CHISELED_COPPER, Blocks.EXPOSED_CHISELED_COPPER).put(Blocks.EXPOSED_CHISELED_COPPER, Blocks.WEATHERED_CHISELED_COPPER).put(Blocks.WEATHERED_CHISELED_COPPER, Blocks.OXIDIZED_CHISELED_COPPER).put(Blocks.CUT_COPPER_SLAB, Blocks.EXPOSED_CUT_COPPER_SLAB).put(Blocks.EXPOSED_CUT_COPPER_SLAB, Blocks.WEATHERED_CUT_COPPER_SLAB).put(Blocks.WEATHERED_CUT_COPPER_SLAB, Blocks.OXIDIZED_CUT_COPPER_SLAB).put(Blocks.CUT_COPPER_STAIRS, Blocks.EXPOSED_CUT_COPPER_STAIRS).put(Blocks.EXPOSED_CUT_COPPER_STAIRS, Blocks.WEATHERED_CUT_COPPER_STAIRS).put(Blocks.WEATHERED_CUT_COPPER_STAIRS, Blocks.OXIDIZED_CUT_COPPER_STAIRS).put(Blocks.COPPER_DOOR, Blocks.EXPOSED_COPPER_DOOR).put(Blocks.EXPOSED_COPPER_DOOR, Blocks.WEATHERED_COPPER_DOOR).put(Blocks.WEATHERED_COPPER_DOOR, Blocks.OXIDIZED_COPPER_DOOR).put(Blocks.COPPER_TRAPDOOR, Blocks.EXPOSED_COPPER_TRAPDOOR).put(Blocks.EXPOSED_COPPER_TRAPDOOR, Blocks.WEATHERED_COPPER_TRAPDOOR).put(Blocks.WEATHERED_COPPER_TRAPDOOR, Blocks.OXIDIZED_COPPER_TRAPDOOR).put(Blocks.COPPER_GRATE, Blocks.EXPOSED_COPPER_GRATE).put(Blocks.EXPOSED_COPPER_GRATE, Blocks.WEATHERED_COPPER_GRATE).put(Blocks.WEATHERED_COPPER_GRATE, Blocks.OXIDIZED_COPPER_GRATE).put(Blocks.COPPER_BULB, Blocks.EXPOSED_COPPER_BULB).put(Blocks.EXPOSED_COPPER_BULB, Blocks.WEATHERED_COPPER_BULB).put(Blocks.WEATHERED_COPPER_BULB, Blocks.OXIDIZED_COPPER_BULB).build();
    });
    Supplier<BiMap<Block, Block>> PREVIOUS_BY_BLOCK = Suppliers.memoize(() -> {
        return ((BiMap) WeatheringCopper.NEXT_BY_BLOCK.get()).inverse();
    });

    static Optional<Block> getPrevious(Block block) {
        return Optional.ofNullable((Block) ((BiMap) WeatheringCopper.PREVIOUS_BY_BLOCK.get()).get(block));
    }

    static Block getFirst(Block block) {
        Block block1 = block;

        for (Block block2 = (Block) ((BiMap) WeatheringCopper.PREVIOUS_BY_BLOCK.get()).get(block); block2 != null; block2 = (Block) ((BiMap) WeatheringCopper.PREVIOUS_BY_BLOCK.get()).get(block2)) {
            block1 = block2;
        }

        return block1;
    }

    static Optional<IBlockData> getPrevious(IBlockData iblockdata) {
        return getPrevious(iblockdata.getBlock()).map((block) -> {
            return block.withPropertiesOf(iblockdata);
        });
    }

    static Optional<Block> getNext(Block block) {
        return Optional.ofNullable((Block) ((BiMap) WeatheringCopper.NEXT_BY_BLOCK.get()).get(block));
    }

    static IBlockData getFirst(IBlockData iblockdata) {
        return getFirst(iblockdata.getBlock()).withPropertiesOf(iblockdata);
    }

    @Override
    default Optional<IBlockData> getNext(IBlockData iblockdata) {
        return getNext(iblockdata.getBlock()).map((block) -> {
            return block.withPropertiesOf(iblockdata);
        });
    }

    @Override
    default float getChanceModifier() {
        return this.getAge() == WeatheringCopper.a.UNAFFECTED ? 0.75F : 1.0F;
    }

    public static enum a implements INamable {

        UNAFFECTED("unaffected"), EXPOSED("exposed"), WEATHERED("weathered"), OXIDIZED("oxidized");

        public static final Codec<WeatheringCopper.a> CODEC = INamable.fromEnum(WeatheringCopper.a::values);
        private final String name;

        private a(final String s) {
            this.name = s;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
