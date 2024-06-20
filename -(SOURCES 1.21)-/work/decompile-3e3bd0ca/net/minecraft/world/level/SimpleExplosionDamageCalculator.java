package net.minecraft.world.level;

import java.util.Optional;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.HolderSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.material.Fluid;

public class SimpleExplosionDamageCalculator extends ExplosionDamageCalculator {

    private final boolean explodesBlocks;
    private final boolean damagesEntities;
    private final Optional<Float> knockbackMultiplier;
    private final Optional<HolderSet<Block>> immuneBlocks;

    public SimpleExplosionDamageCalculator(boolean flag, boolean flag1, Optional<Float> optional, Optional<HolderSet<Block>> optional1) {
        this.explodesBlocks = flag;
        this.damagesEntities = flag1;
        this.knockbackMultiplier = optional;
        this.immuneBlocks = optional1;
    }

    @Override
    public Optional<Float> getBlockExplosionResistance(Explosion explosion, IBlockAccess iblockaccess, BlockPosition blockposition, IBlockData iblockdata, Fluid fluid) {
        return this.immuneBlocks.isPresent() ? (iblockdata.is((HolderSet) this.immuneBlocks.get()) ? Optional.of(3600000.0F) : Optional.empty()) : super.getBlockExplosionResistance(explosion, iblockaccess, blockposition, iblockdata, fluid);
    }

    @Override
    public boolean shouldBlockExplode(Explosion explosion, IBlockAccess iblockaccess, BlockPosition blockposition, IBlockData iblockdata, float f) {
        return this.explodesBlocks;
    }

    @Override
    public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
        return this.damagesEntities;
    }

    @Override
    public float getKnockbackMultiplier(Entity entity) {
        boolean flag;
        label17:
        {
            if (entity instanceof EntityHuman entityhuman) {
                if (entityhuman.getAbilities().flying) {
                    flag = true;
                    break label17;
                }
            }

            flag = false;
        }

        boolean flag1 = flag;

        return flag1 ? 0.0F : (Float) this.knockbackMultiplier.orElseGet(() -> {
            return super.getKnockbackMultiplier(entity);
        });
    }
}
