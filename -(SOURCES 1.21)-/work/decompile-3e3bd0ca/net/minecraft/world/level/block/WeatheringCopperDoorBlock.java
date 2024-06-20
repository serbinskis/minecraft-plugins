package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockPropertyDoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.BlockSetType;

public class WeatheringCopperDoorBlock extends BlockDoor implements WeatheringCopper {

    public static final MapCodec<WeatheringCopperDoorBlock> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(BlockSetType.CODEC.fieldOf("block_set_type").forGetter(BlockDoor::type), WeatheringCopper.a.CODEC.fieldOf("weathering_state").forGetter(WeatheringCopperDoorBlock::getAge), propertiesCodec()).apply(instance, WeatheringCopperDoorBlock::new);
    });
    private final WeatheringCopper.a weatherState;

    @Override
    public MapCodec<WeatheringCopperDoorBlock> codec() {
        return WeatheringCopperDoorBlock.CODEC;
    }

    protected WeatheringCopperDoorBlock(BlockSetType blocksettype, WeatheringCopper.a weatheringcopper_a, BlockBase.Info blockbase_info) {
        super(blocksettype, blockbase_info);
        this.weatherState = weatheringcopper_a;
    }

    @Override
    protected void randomTick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        if (iblockdata.getValue(BlockDoor.HALF) == BlockPropertyDoubleBlockHalf.LOWER) {
            this.changeOverTime(iblockdata, worldserver, blockposition, randomsource);
        }

    }

    @Override
    protected boolean isRandomlyTicking(IBlockData iblockdata) {
        return WeatheringCopper.getNext(iblockdata.getBlock()).isPresent();
    }

    @Override
    public WeatheringCopper.a getAge() {
        return this.weatherState;
    }
}
