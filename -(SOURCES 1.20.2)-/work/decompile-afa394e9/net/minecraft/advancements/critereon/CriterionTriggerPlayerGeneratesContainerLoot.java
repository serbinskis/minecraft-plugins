package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.util.ChatDeserializer;

public class CriterionTriggerPlayerGeneratesContainerLoot extends CriterionTriggerAbstract<CriterionTriggerPlayerGeneratesContainerLoot.a> {

    public CriterionTriggerPlayerGeneratesContainerLoot() {}

    @Override
    protected CriterionTriggerPlayerGeneratesContainerLoot.a createInstance(JsonObject jsonobject, Optional<ContextAwarePredicate> optional, LootDeserializationContext lootdeserializationcontext) {
        MinecraftKey minecraftkey = new MinecraftKey(ChatDeserializer.getAsString(jsonobject, "loot_table"));

        return new CriterionTriggerPlayerGeneratesContainerLoot.a(optional, minecraftkey);
    }

    public void trigger(EntityPlayer entityplayer, MinecraftKey minecraftkey) {
        this.trigger(entityplayer, (criteriontriggerplayergeneratescontainerloot_a) -> {
            return criteriontriggerplayergeneratescontainerloot_a.matches(minecraftkey);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        private final MinecraftKey lootTable;

        public a(Optional<ContextAwarePredicate> optional, MinecraftKey minecraftkey) {
            super(optional);
            this.lootTable = minecraftkey;
        }

        public static Criterion<CriterionTriggerPlayerGeneratesContainerLoot.a> lootTableUsed(MinecraftKey minecraftkey) {
            return CriterionTriggers.GENERATE_LOOT.createCriterion(new CriterionTriggerPlayerGeneratesContainerLoot.a(Optional.empty(), minecraftkey));
        }

        public boolean matches(MinecraftKey minecraftkey) {
            return this.lootTable.equals(minecraftkey);
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject jsonobject = super.serializeToJson();

            jsonobject.addProperty("loot_table", this.lootTable.toString());
            return jsonobject;
        }
    }
}
