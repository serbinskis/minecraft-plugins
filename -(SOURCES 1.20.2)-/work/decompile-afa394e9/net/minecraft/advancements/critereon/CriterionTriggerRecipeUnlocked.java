package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.util.ChatDeserializer;
import net.minecraft.world.item.crafting.RecipeHolder;

public class CriterionTriggerRecipeUnlocked extends CriterionTriggerAbstract<CriterionTriggerRecipeUnlocked.a> {

    public CriterionTriggerRecipeUnlocked() {}

    @Override
    public CriterionTriggerRecipeUnlocked.a createInstance(JsonObject jsonobject, Optional<ContextAwarePredicate> optional, LootDeserializationContext lootdeserializationcontext) {
        MinecraftKey minecraftkey = new MinecraftKey(ChatDeserializer.getAsString(jsonobject, "recipe"));

        return new CriterionTriggerRecipeUnlocked.a(optional, minecraftkey);
    }

    public void trigger(EntityPlayer entityplayer, RecipeHolder<?> recipeholder) {
        this.trigger(entityplayer, (criteriontriggerrecipeunlocked_a) -> {
            return criteriontriggerrecipeunlocked_a.matches(recipeholder);
        });
    }

    public static Criterion<CriterionTriggerRecipeUnlocked.a> unlocked(MinecraftKey minecraftkey) {
        return CriterionTriggers.RECIPE_UNLOCKED.createCriterion(new CriterionTriggerRecipeUnlocked.a(Optional.empty(), minecraftkey));
    }

    public static class a extends CriterionInstanceAbstract {

        private final MinecraftKey recipe;

        public a(Optional<ContextAwarePredicate> optional, MinecraftKey minecraftkey) {
            super(optional);
            this.recipe = minecraftkey;
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject jsonobject = super.serializeToJson();

            jsonobject.addProperty("recipe", this.recipe.toString());
            return jsonobject;
        }

        public boolean matches(RecipeHolder<?> recipeholder) {
            return this.recipe.equals(recipeholder.id());
        }
    }
}
