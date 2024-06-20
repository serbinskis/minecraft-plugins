package net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.RandomSource;

public class AppendStatic implements RuleBlockEntityModifier {

    public static final MapCodec<AppendStatic> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(NBTTagCompound.CODEC.fieldOf("data").forGetter((appendstatic) -> {
            return appendstatic.tag;
        })).apply(instance, AppendStatic::new);
    });
    private final NBTTagCompound tag;

    public AppendStatic(NBTTagCompound nbttagcompound) {
        this.tag = nbttagcompound;
    }

    @Override
    public NBTTagCompound apply(RandomSource randomsource, @Nullable NBTTagCompound nbttagcompound) {
        return nbttagcompound == null ? this.tag.copy() : nbttagcompound.merge(this.tag);
    }

    @Override
    public RuleBlockEntityModifierType<?> getType() {
        return RuleBlockEntityModifierType.APPEND_STATIC;
    }
}
