package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;

public class WeatheringCopperStairBlock extends BlockStairs implements WeatheringCopper {

    public static final MapCodec<WeatheringCopperStairBlock> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(WeatheringCopper.a.CODEC.fieldOf("weathering_state").forGetter(ChangeOverTimeBlock::getAge), IBlockData.CODEC.fieldOf("base_state").forGetter((weatheringcopperstairblock) -> {
            return weatheringcopperstairblock.baseState;
        }), propertiesCodec()).apply(instance, WeatheringCopperStairBlock::new);
    });
    private final WeatheringCopper.a weatherState;

    @Override
    public MapCodec<WeatheringCopperStairBlock> codec() {
        return WeatheringCopperStairBlock.CODEC;
    }

    public WeatheringCopperStairBlock(WeatheringCopper.a weatheringcopper_a, IBlockData iblockdata, BlockBase.Info blockbase_info) {
        super(iblockdata, blockbase_info);
        this.weatherState = weatheringcopper_a;
    }

    @Override
    protected void randomTick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        this.changeOverTime(iblockdata, worldserver, blockposition, randomsource);
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
