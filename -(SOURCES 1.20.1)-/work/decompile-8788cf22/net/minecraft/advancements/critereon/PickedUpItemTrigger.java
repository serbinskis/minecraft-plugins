package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public class PickedUpItemTrigger extends CriterionTriggerAbstract<PickedUpItemTrigger.a> {

    private final MinecraftKey id;

    public PickedUpItemTrigger(MinecraftKey minecraftkey) {
        this.id = minecraftkey;
    }

    @Override
    public MinecraftKey getId() {
        return this.id;
    }

    @Override
    protected PickedUpItemTrigger.a createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, LootDeserializationContext lootdeserializationcontext) {
        CriterionConditionItem criterionconditionitem = CriterionConditionItem.fromJson(jsonobject.get("item"));
        ContextAwarePredicate contextawarepredicate1 = CriterionConditionEntity.fromJson(jsonobject, "entity", lootdeserializationcontext);

        return new PickedUpItemTrigger.a(this.id, contextawarepredicate, criterionconditionitem, contextawarepredicate1);
    }

    public void trigger(EntityPlayer entityplayer, ItemStack itemstack, @Nullable Entity entity) {
        LootTableInfo loottableinfo = CriterionConditionEntity.createContext(entityplayer, entity);

        this.trigger(entityplayer, (pickedupitemtrigger_a) -> {
            return pickedupitemtrigger_a.matches(entityplayer, itemstack, loottableinfo);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        private final CriterionConditionItem item;
        private final ContextAwarePredicate entity;

        public a(MinecraftKey minecraftkey, ContextAwarePredicate contextawarepredicate, CriterionConditionItem criterionconditionitem, ContextAwarePredicate contextawarepredicate1) {
            super(minecraftkey, contextawarepredicate);
            this.item = criterionconditionitem;
            this.entity = contextawarepredicate1;
        }

        public static PickedUpItemTrigger.a thrownItemPickedUpByEntity(ContextAwarePredicate contextawarepredicate, CriterionConditionItem criterionconditionitem, ContextAwarePredicate contextawarepredicate1) {
            return new PickedUpItemTrigger.a(CriterionTriggers.THROWN_ITEM_PICKED_UP_BY_ENTITY.getId(), contextawarepredicate, criterionconditionitem, contextawarepredicate1);
        }

        public static PickedUpItemTrigger.a thrownItemPickedUpByPlayer(ContextAwarePredicate contextawarepredicate, CriterionConditionItem criterionconditionitem, ContextAwarePredicate contextawarepredicate1) {
            return new PickedUpItemTrigger.a(CriterionTriggers.THROWN_ITEM_PICKED_UP_BY_PLAYER.getId(), contextawarepredicate, criterionconditionitem, contextawarepredicate1);
        }

        public boolean matches(EntityPlayer entityplayer, ItemStack itemstack, LootTableInfo loottableinfo) {
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
