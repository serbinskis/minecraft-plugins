package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;

public class CriterionTriggerRecipeUnlocked extends CriterionTriggerAbstract<CriterionTriggerRecipeUnlocked.a> {

    public CriterionTriggerRecipeUnlocked() {}

    @Override
    public Codec<CriterionTriggerRecipeUnlocked.a> codec() {
        return CriterionTriggerRecipeUnlocked.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, RecipeHolder<?> recipeholder) {
        this.trigger(entityplayer, (criteriontriggerrecipeunlocked_a) -> {
            return criteriontriggerrecipeunlocked_a.matches(recipeholder);
        });
    }

    public static Criterion<CriterionTriggerRecipeUnlocked.a> unlocked(MinecraftKey minecraftkey) {
        return CriterionTriggers.RECIPE_UNLOCKED.createCriterion(new CriterionTriggerRecipeUnlocked.a(Optional.empty(), minecraftkey));
    }

    public static record a(Optional<ContextAwarePredicate> player, MinecraftKey recipe) implements CriterionTriggerAbstract.a {

        public static final Codec<CriterionTriggerRecipeUnlocked.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CriterionTriggerRecipeUnlocked.a::player), MinecraftKey.CODEC.fieldOf("recipe").forGetter(CriterionTriggerRecipeUnlocked.a::recipe)).apply(instance, CriterionTriggerRecipeUnlocked.a::new);
        });

        public boolean matches(RecipeHolder<?> recipeholder) {
            return this.recipe.equals(recipeholder.id());
        }
    }
}
