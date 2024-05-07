package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.level.storage.loot.LootTable;

public class CriterionTriggerPlayerGeneratesContainerLoot extends CriterionTriggerAbstract<CriterionTriggerPlayerGeneratesContainerLoot.a> {

    public CriterionTriggerPlayerGeneratesContainerLoot() {}

    @Override
    public Codec<CriterionTriggerPlayerGeneratesContainerLoot.a> codec() {
        return CriterionTriggerPlayerGeneratesContainerLoot.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, ResourceKey<LootTable> resourcekey) {
        this.trigger(entityplayer, (criteriontriggerplayergeneratescontainerloot_a) -> {
            return criteriontriggerplayergeneratescontainerloot_a.matches(resourcekey);
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, ResourceKey<LootTable> lootTable) implements CriterionTriggerAbstract.a {

        public static final Codec<CriterionTriggerPlayerGeneratesContainerLoot.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CriterionTriggerPlayerGeneratesContainerLoot.a::player), ResourceKey.codec(Registries.LOOT_TABLE).fieldOf("loot_table").forGetter(CriterionTriggerPlayerGeneratesContainerLoot.a::lootTable)).apply(instance, CriterionTriggerPlayerGeneratesContainerLoot.a::new);
        });

        public static Criterion<CriterionTriggerPlayerGeneratesContainerLoot.a> lootTableUsed(ResourceKey<LootTable> resourcekey) {
            return CriterionTriggers.GENERATE_LOOT.createCriterion(new CriterionTriggerPlayerGeneratesContainerLoot.a(Optional.empty(), resourcekey));
        }

        public boolean matches(ResourceKey<LootTable> resourcekey) {
            return this.lootTable == resourcekey;
        }
    }
}
