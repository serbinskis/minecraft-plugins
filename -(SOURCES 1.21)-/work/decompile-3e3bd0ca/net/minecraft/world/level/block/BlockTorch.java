package net.minecraft.world.level.block;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.Particles;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;

public class BlockTorch extends BaseTorchBlock {

    protected static final MapCodec<ParticleType> PARTICLE_OPTIONS_FIELD = BuiltInRegistries.PARTICLE_TYPE.byNameCodec().comapFlatMap((particle) -> {
        DataResult dataresult;

        if (particle instanceof ParticleType particletype) {
            dataresult = DataResult.success(particletype);
        } else {
            dataresult = DataResult.error(() -> {
                return "Not a SimpleParticleType: " + String.valueOf(particle);
            });
        }

        return dataresult;
    }, (particletype) -> {
        return particletype;
    }).fieldOf("particle_options");
    public static final MapCodec<BlockTorch> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(BlockTorch.PARTICLE_OPTIONS_FIELD.forGetter((blocktorch) -> {
            return blocktorch.flameParticle;
        }), propertiesCodec()).apply(instance, BlockTorch::new);
    });
    protected final ParticleType flameParticle;

    @Override
    public MapCodec<? extends BlockTorch> codec() {
        return BlockTorch.CODEC;
    }

    protected BlockTorch(ParticleType particletype, BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.flameParticle = particletype;
    }

    @Override
    public void animateTick(IBlockData iblockdata, World world, BlockPosition blockposition, RandomSource randomsource) {
        double d0 = (double) blockposition.getX() + 0.5D;
        double d1 = (double) blockposition.getY() + 0.7D;
        double d2 = (double) blockposition.getZ() + 0.5D;

        world.addParticle(Particles.SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
        world.addParticle(this.flameParticle, d0, d1, d2, 0.0D, 0.0D, 0.0D);
    }
}
