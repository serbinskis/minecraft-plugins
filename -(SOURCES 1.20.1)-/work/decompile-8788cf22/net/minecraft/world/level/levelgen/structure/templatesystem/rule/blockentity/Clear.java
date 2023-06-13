package net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity;

import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.RandomSource;

public class Clear implements RuleBlockEntityModifier {

    private static final Clear INSTANCE = new Clear();
    public static final Codec<Clear> CODEC = Codec.unit(Clear.INSTANCE);

    public Clear() {}

    @Override
    public NBTTagCompound apply(RandomSource randomsource, @Nullable NBTTagCompound nbttagcompound) {
        return new NBTTagCompound();
    }

    @Override
    public RuleBlockEntityModifierType<?> getType() {
        return RuleBlockEntityModifierType.CLEAR;
    }
}
