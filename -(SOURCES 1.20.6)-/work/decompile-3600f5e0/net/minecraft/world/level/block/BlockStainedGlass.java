package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.EnumColor;
import net.minecraft.world.level.block.state.BlockBase;

public class BlockStainedGlass extends BlockGlassAbstract implements IBeaconBeam {

    public static final MapCodec<BlockStainedGlass> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(EnumColor.CODEC.fieldOf("color").forGetter(BlockStainedGlass::getColor), propertiesCodec()).apply(instance, BlockStainedGlass::new);
    });
    private final EnumColor color;

    @Override
    public MapCodec<BlockStainedGlass> codec() {
        return BlockStainedGlass.CODEC;
    }

    public BlockStainedGlass(EnumColor enumcolor, BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.color = enumcolor;
    }

    @Override
    public EnumColor getColor() {
        return this.color;
    }
}
