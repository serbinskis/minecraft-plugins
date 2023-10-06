package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;

public abstract class CriterionInstanceAbstract implements CriterionTriggerAbstract.a {

    private final Optional<ContextAwarePredicate> player;

    public CriterionInstanceAbstract(Optional<ContextAwarePredicate> optional) {
        this.player = optional;
    }

    @Override
    public Optional<ContextAwarePredicate> playerPredicate() {
        return this.player;
    }

    @Override
    public JsonObject serializeToJson() {
        JsonObject jsonobject = new JsonObject();

        this.player.ifPresent((contextawarepredicate) -> {
            jsonobject.add("player", contextawarepredicate.toJson());
        });
        return jsonobject;
    }
}
