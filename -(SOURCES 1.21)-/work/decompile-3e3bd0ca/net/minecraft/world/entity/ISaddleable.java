package net.minecraft.world.entity;

import javax.annotation.Nullable;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.world.item.ItemStack;

public interface ISaddleable {

    boolean isSaddleable();

    void equipSaddle(ItemStack itemstack, @Nullable SoundCategory soundcategory);

    default SoundEffect getSaddleSoundEvent() {
        return SoundEffects.HORSE_SADDLE;
    }

    boolean isSaddled();
}
