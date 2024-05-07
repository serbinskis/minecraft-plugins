package net.minecraft.core.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public class ParticleParamItem implements ParticleParam {

    private static final Codec<ItemStack> ITEM_CODEC = Codec.withAlternative(ItemStack.SINGLE_ITEM_CODEC, ItemStack.ITEM_NON_AIR_CODEC, ItemStack::new);
    private final Particle<ParticleParamItem> type;
    private final ItemStack itemStack;

    public static MapCodec<ParticleParamItem> codec(Particle<ParticleParamItem> particle) {
        return ParticleParamItem.ITEM_CODEC.xmap((itemstack) -> {
            return new ParticleParamItem(particle, itemstack);
        }, (particleparamitem) -> {
            return particleparamitem.itemStack;
        }).fieldOf("item");
    }

    public static StreamCodec<? super RegistryFriendlyByteBuf, ParticleParamItem> streamCodec(Particle<ParticleParamItem> particle) {
        return ItemStack.STREAM_CODEC.map((itemstack) -> {
            return new ParticleParamItem(particle, itemstack);
        }, (particleparamitem) -> {
            return particleparamitem.itemStack;
        });
    }

    public ParticleParamItem(Particle<ParticleParamItem> particle, ItemStack itemstack) {
        if (itemstack.isEmpty()) {
            throw new IllegalArgumentException("Empty stacks are not allowed");
        } else {
            this.type = particle;
            this.itemStack = itemstack;
        }
    }

    @Override
    public Particle<ParticleParamItem> getType() {
        return this.type;
    }

    public ItemStack getItem() {
        return this.itemStack;
    }
}
