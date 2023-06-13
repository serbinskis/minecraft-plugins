package net.minecraft.world.item;

import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.entity.TileEntitySign;

public interface SignApplicator {

    boolean tryApplyToSign(World world, TileEntitySign tileentitysign, boolean flag, EntityHuman entityhuman);

    default boolean canApplyToSign(SignText signtext, EntityHuman entityhuman) {
        return signtext.hasMessage(entityhuman);
    }
}
