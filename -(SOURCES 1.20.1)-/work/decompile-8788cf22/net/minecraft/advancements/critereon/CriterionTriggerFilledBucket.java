package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.item.ItemStack;

public class CriterionTriggerFilledBucket extends CriterionTriggerAbstract<CriterionTriggerFilledBucket.a> {

    static final MinecraftKey ID = new MinecraftKey("filled_bucket");

    public CriterionTriggerFilledBucket() {}

    @Override
    public MinecraftKey getId() {
        return CriterionTriggerFilledBucket.ID;
    }

    @Override
    public CriterionTriggerFilledBucket.a createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, LootDeserializationContext lootdeserializationcontext) {
        CriterionConditionItem criterionconditionitem = CriterionConditionItem.fromJson(jsonobject.get("item"));

        return new CriterionTriggerFilledBucket.a(contextawarepredicate, criterionconditionitem);
    }

    public void trigger(EntityPlayer entityplayer, ItemStack itemstack) {
        this.trigger(entityplayer, (criteriontriggerfilledbucket_a) -> {
            return criteriontriggerfilledbucket_a.matches(itemstack);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        private final CriterionConditionItem item;

        public a(ContextAwarePredicate contextawarepredicate, CriterionConditionItem criterionconditionitem) {
            super(CriterionTriggerFilledBucket.ID, contextawarepredicate);
            this.item = criterionconditionitem;
        }

        public static CriterionTriggerFilledBucket.a filledBucket(CriterionConditionItem criterionconditionitem) {
            return new CriterionTriggerFilledBucket.a(ContextAwarePredicate.ANY, criterionconditionitem);
        }

        public boolean matches(ItemStack itemstack) {
            return this.item.matches(itemstack);
        }

        @Override
        public JsonObject serializeToJson(LootSerializationContext lootserializationcontext) {
            JsonObject jsonobject = super.serializeToJson(lootserializationcontext);

            jsonobject.add("item", this.item.serializeToJson());
            return jsonobject;
        }
    }
}
