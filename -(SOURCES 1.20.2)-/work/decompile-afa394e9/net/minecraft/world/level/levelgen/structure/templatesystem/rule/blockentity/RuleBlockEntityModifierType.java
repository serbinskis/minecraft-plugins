package net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity;

import com.mojang.serialization.Codec;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;

public interface RuleBlockEntityModifierType<P extends RuleBlockEntityModifier> {

    RuleBlockEntityModifierType<Clear> CLEAR = register("clear", Clear.CODEC);
    RuleBlockEntityModifierType<Passthrough> PASSTHROUGH = register("passthrough", Passthrough.CODEC);
    RuleBlockEntityModifierType<AppendStatic> APPEND_STATIC = register("append_static", AppendStatic.CODEC);
    RuleBlockEntityModifierType<AppendLoot> APPEND_LOOT = register("append_loot", AppendLoot.CODEC);

    Codec<P> codec();

    private static <P extends RuleBlockEntityModifier> RuleBlockEntityModifierType<P> register(String s, Codec<P> codec) {
        return (RuleBlockEntityModifierType) IRegistry.register(BuiltInRegistries.RULE_BLOCK_ENTITY_MODIFIER, s, () -> {
            return codec;
        });
    }
}
