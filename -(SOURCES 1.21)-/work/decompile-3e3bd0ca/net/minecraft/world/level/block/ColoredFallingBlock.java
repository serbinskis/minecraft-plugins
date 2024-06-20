package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPosition;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;

public class ColoredFallingBlock extends BlockFalling {

    public static final MapCodec<ColoredFallingBlock> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(ColorRGBA.CODEC.fieldOf("falling_dust_color").forGetter((coloredfallingblock) -> {
            return coloredfallingblock.dustColor;
        }), propertiesCodec()).apply(instance, ColoredFallingBlock::new);
    });
    private final ColorRGBA dustColor;

    @Override
    public MapCodec<ColoredFallingBlock> codec() {
        return ColoredFallingBlock.CODEC;
    }

    public ColoredFallingBlock(ColorRGBA colorrgba, BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.dustColor = colorrgba;
    }

    @Override
    public int getDustColor(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return this.dustColor.rgba();
    }
}
