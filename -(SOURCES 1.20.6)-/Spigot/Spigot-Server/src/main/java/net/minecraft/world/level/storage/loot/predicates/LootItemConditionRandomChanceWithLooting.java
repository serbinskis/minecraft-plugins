package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;

public record LootItemConditionRandomChanceWithLooting(float percent, float lootingMultiplier) implements LootItemCondition {

    public static final MapCodec<LootItemConditionRandomChanceWithLooting> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Codec.FLOAT.fieldOf("chance").forGetter(LootItemConditionRandomChanceWithLooting::percent), Codec.FLOAT.fieldOf("looting_multiplier").forGetter(LootItemConditionRandomChanceWithLooting::lootingMultiplier)).apply(instance, LootItemConditionRandomChanceWithLooting::new);
    });

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.RANDOM_CHANCE_WITH_LOOTING;
    }

    @Override
    public Set<LootContextParameter<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParameters.KILLER_ENTITY);
    }

    public boolean test(LootTableInfo loottableinfo) {
        Entity entity = (Entity) loottableinfo.getParamOrNull(LootContextParameters.KILLER_ENTITY);
        int i = 0;

        if (entity instanceof EntityLiving) {
            i = EnchantmentManager.getMobLooting((EntityLiving) entity);
        }
        // CraftBukkit start - only use lootingModifier if set by Bukkit
        if (loottableinfo.hasParam(LootContextParameters.LOOTING_MOD)) {
            i = loottableinfo.getParamOrNull(LootContextParameters.LOOTING_MOD);
        }
        // CraftBukkit end

        return loottableinfo.getRandom().nextFloat() < this.percent + (float) i * this.lootingMultiplier;
    }

    public static LootItemCondition.a randomChanceAndLootingBoost(float f, float f1) {
        return () -> {
            return new LootItemConditionRandomChanceWithLooting(f, f1);
        };
    }
}
