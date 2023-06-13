package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriterionInstance;
import net.minecraft.resources.MinecraftKey;

public abstract class CriterionInstanceAbstract implements CriterionInstance {

    private final MinecraftKey criterion;
    private final ContextAwarePredicate player;

    public CriterionInstanceAbstract(MinecraftKey minecraftkey, ContextAwarePredicate contextawarepredicate) {
        this.criterion = minecraftkey;
        this.player = contextawarepredicate;
    }

    @Override
    public MinecraftKey getCriterion() {
        return this.criterion;
    }

    protected ContextAwarePredicate getPlayerPredicate() {
        return this.player;
    }

    @Override
    public JsonObject serializeToJson(LootSerializationContext lootserializationcontext) {
        JsonObject jsonobject = new JsonObject();

        jsonobject.add("player", this.player.toJson(lootserializationcontext));
        return jsonobject;
    }

    public String toString() {
        return "AbstractCriterionInstance{criterion=" + this.criterion + "}";
    }
}
