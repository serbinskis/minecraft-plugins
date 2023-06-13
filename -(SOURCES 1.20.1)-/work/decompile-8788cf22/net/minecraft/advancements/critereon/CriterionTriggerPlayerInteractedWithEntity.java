package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public class CriterionTriggerPlayerInteractedWithEntity extends CriterionTriggerAbstract<CriterionTriggerPlayerInteractedWithEntity.a> {

    static final MinecraftKey ID = new MinecraftKey("player_interacted_with_entity");

    public CriterionTriggerPlayerInteractedWithEntity() {}

    @Override
    public MinecraftKey getId() {
        return CriterionTriggerPlayerInteractedWithEntity.ID;
    }

    @Override
    protected CriterionTriggerPlayerInteractedWithEntity.a createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, LootDeserializationContext lootdeserializationcontext) {
        CriterionConditionItem criterionconditionitem = CriterionConditionItem.fromJson(jsonobject.get("item"));
        ContextAwarePredicate contextawarepredicate1 = CriterionConditionEntity.fromJson(jsonobject, "entity", lootdeserializationcontext);

        return new CriterionTriggerPlayerInteractedWithEntity.a(contextawarepredicate, criterionconditionitem, contextawarepredicate1);
    }

    public void trigger(EntityPlayer entityplayer, ItemStack itemstack, Entity entity) {
        LootTableInfo loottableinfo = CriterionConditionEntity.createContext(entityplayer, entity);

        this.trigger(entityplayer, (criteriontriggerplayerinteractedwithentity_a) -> {
            return criteriontriggerplayerinteractedwithentity_a.matches(itemstack, loottableinfo);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        private final CriterionConditionItem item;
        private final ContextAwarePredicate entity;

        public a(ContextAwarePredicate contextawarepredicate, CriterionConditionItem criterionconditionitem, ContextAwarePredicate contextawarepredicate1) {
            super(CriterionTriggerPlayerInteractedWithEntity.ID, contextawarepredicate);
            this.item = criterionconditionitem;
            this.entity = contextawarepredicate1;
        }

        public static CriterionTriggerPlayerInteractedWithEntity.a itemUsedOnEntity(ContextAwarePredicate contextawarepredicate, CriterionConditionItem.a criterionconditionitem_a, ContextAwarePredicate contextawarepredicate1) {
            return new CriterionTriggerPlayerInteractedWithEntity.a(contextawarepredicate, criterionconditionitem_a.build(), contextawarepredicate1);
        }

        public static CriterionTriggerPlayerInteractedWithEntity.a itemUsedOnEntity(CriterionConditionItem.a criterionconditionitem_a, ContextAwarePredicate contextawarepredicate) {
            return itemUsedOnEntity(ContextAwarePredicate.ANY, criterionconditionitem_a, contextawarepredicate);
        }

        public boolean matches(ItemStack itemstack, LootTableInfo loottableinfo) {
            return !this.item.matches(itemstack) ? false : this.entity.matches(loottableinfo);
        }

        @Override
        public JsonObject serializeToJson(LootSerializationContext lootserializationcontext) {
            JsonObject jsonobject = super.serializeToJson(lootserializationcontext);

            jsonobject.add("item", this.item.serializeToJson());
            jsonobject.add("entity", this.entity.toJson(lootserializationcontext));
            return jsonobject;
        }
    }
}
