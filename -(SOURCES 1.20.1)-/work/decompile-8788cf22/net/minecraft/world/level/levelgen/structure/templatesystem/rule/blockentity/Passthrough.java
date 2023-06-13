package net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity;

import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.RandomSource;

public class Passthrough implements RuleBlockEntityModifier {

    public static final Passthrough INSTANCE = new Passthrough();
    public static final Codec<Passthrough> CODEC = Codec.unit(Passthrough.INSTANCE);

    public Passthrough() {}

    @Nullable
    @Override
    public NBTTagCompound apply(RandomSource randomsource, @Nullable NBTTagCompound nbttagcompound) {
        return nbttagcompound;
    }

    @Override
    public RuleBlockEntityModifierType<?> getType() {
        return RuleBlockEntityModifierType.PASSTHROUGH;
    }
}
