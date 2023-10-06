package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.util.ChatDeserializer;
import net.minecraft.world.item.alchemy.PotionRegistry;

public class CriterionTriggerBrewedPotion extends CriterionTriggerAbstract<CriterionTriggerBrewedPotion.a> {

    public CriterionTriggerBrewedPotion() {}

    @Override
    public CriterionTriggerBrewedPotion.a createInstance(JsonObject jsonobject, Optional<ContextAwarePredicate> optional, LootDeserializationContext lootdeserializationcontext) {
        PotionRegistry potionregistry = null;

        if (jsonobject.has("potion")) {
            MinecraftKey minecraftkey = new MinecraftKey(ChatDeserializer.getAsString(jsonobject, "potion"));

            potionregistry = (PotionRegistry) BuiltInRegistries.POTION.getOptional(minecraftkey).orElseThrow(() -> {
                return new JsonSyntaxException("Unknown potion '" + minecraftkey + "'");
            });
        }

        return new CriterionTriggerBrewedPotion.a(optional, potionregistry);
    }

    public void trigger(EntityPlayer entityplayer, PotionRegistry potionregistry) {
        this.trigger(entityplayer, (criteriontriggerbrewedpotion_a) -> {
            return criteriontriggerbrewedpotion_a.matches(potionregistry);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        @Nullable
        private final PotionRegistry potion;

        public a(Optional<ContextAwarePredicate> optional, @Nullable PotionRegistry potionregistry) {
            super(optional);
            this.potion = potionregistry;
        }

        public static Criterion<CriterionTriggerBrewedPotion.a> brewedPotion() {
            return CriterionTriggers.BREWED_POTION.createCriterion(new CriterionTriggerBrewedPotion.a(Optional.empty(), (PotionRegistry) null));
        }

        public boolean matches(PotionRegistry potionregistry) {
            return this.potion == null || this.potion == potionregistry;
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject jsonobject = super.serializeToJson();

            if (this.potion != null) {
                jsonobject.addProperty("potion", BuiltInRegistries.POTION.getKey(this.potion).toString());
            }

            return jsonobject;
        }
    }
}
