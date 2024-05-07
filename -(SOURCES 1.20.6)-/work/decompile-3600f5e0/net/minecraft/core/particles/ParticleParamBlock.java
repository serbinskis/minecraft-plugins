package net.minecraft.core.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.IBlockData;

public class ParticleParamBlock implements ParticleParam {

    private static final Codec<IBlockData> BLOCK_STATE_CODEC = Codec.withAlternative(IBlockData.CODEC, BuiltInRegistries.BLOCK.byNameCodec(), Block::defaultBlockState);
    private final Particle<ParticleParamBlock> type;
    private final IBlockData state;

    public static MapCodec<ParticleParamBlock> codec(Particle<ParticleParamBlock> particle) {
        return ParticleParamBlock.BLOCK_STATE_CODEC.xmap((iblockdata) -> {
            return new ParticleParamBlock(particle, iblockdata);
        }, (particleparamblock) -> {
            return particleparamblock.state;
        }).fieldOf("block_state");
    }

    public static StreamCodec<? super RegistryFriendlyByteBuf, ParticleParamBlock> streamCodec(Particle<ParticleParamBlock> particle) {
        return ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY).map((iblockdata) -> {
            return new ParticleParamBlock(particle, iblockdata);
        }, (particleparamblock) -> {
            return particleparamblock.state;
        });
    }

    public ParticleParamBlock(Particle<ParticleParamBlock> particle, IBlockData iblockdata) {
        this.type = particle;
        this.state = iblockdata;
    }

    @Override
    public Particle<ParticleParamBlock> getType() {
        return this.type;
    }

    public IBlockData getState() {
        return this.state;
    }
}
