package net.minecraft.world.item;

import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.TileEntitySign;

public class GlowInkSacItem extends Item implements SignApplicator {

    public GlowInkSacItem(Item.Info item_info) {
        super(item_info);
    }

    @Override
    public boolean tryApplyToSign(World world, TileEntitySign tileentitysign, boolean flag, EntityHuman entityhuman) {
        if (tileentitysign.updateText((signtext) -> {
            return signtext.setHasGlowingText(true);
        }, flag)) {
            world.playSound((EntityHuman) null, tileentitysign.getBlockPos(), SoundEffects.GLOW_INK_SAC_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
            return true;
        } else {
            return false;
        }
    }
}
