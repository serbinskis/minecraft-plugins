package net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity;

import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.RandomSource;

public interface RuleBlockEntityModifier {

    Codec<RuleBlockEntityModifier> CODEC = BuiltInRegistries.RULE_BLOCK_ENTITY_MODIFIER.byNameCodec().dispatch(RuleBlockEntityModifier::getType, RuleBlockEntityModifierType::codec);

    @Nullable
    NBTTagCompound apply(RandomSource randomsource, @Nullable NBTTagCompound nbttagcompound);

    RuleBlockEntityModifierType<?> getType();
}
