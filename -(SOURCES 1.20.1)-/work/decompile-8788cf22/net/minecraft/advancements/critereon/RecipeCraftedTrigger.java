package net.minecraft.advancements.critereon;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.util.ChatDeserializer;
import net.minecraft.world.item.ItemStack;

public class RecipeCraftedTrigger extends CriterionTriggerAbstract<RecipeCraftedTrigger.a> {

    static final MinecraftKey ID = new MinecraftKey("recipe_crafted");

    public RecipeCraftedTrigger() {}

    @Override
    public MinecraftKey getId() {
        return RecipeCraftedTrigger.ID;
    }

    @Override
    protected RecipeCraftedTrigger.a createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, LootDeserializationContext lootdeserializationcontext) {
        MinecraftKey minecraftkey = new MinecraftKey(ChatDeserializer.getAsString(jsonobject, "recipe_id"));
        CriterionConditionItem[] acriterionconditionitem = CriterionConditionItem.fromJsonArray(jsonobject.get("ingredients"));

        return new RecipeCraftedTrigger.a(contextawarepredicate, minecraftkey, List.of(acriterionconditionitem));
    }

    public void trigger(EntityPlayer entityplayer, MinecraftKey minecraftkey, List<ItemStack> list) {
        this.trigger(entityplayer, (recipecraftedtrigger_a) -> {
            return recipecraftedtrigger_a.matches(minecraftkey, list);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        private final MinecraftKey recipeId;
        private final List<CriterionConditionItem> predicates;

        public a(ContextAwarePredicate contextawarepredicate, MinecraftKey minecraftkey, List<CriterionConditionItem> list) {
            super(RecipeCraftedTrigger.ID, contextawarepredicate);
            this.recipeId = minecraftkey;
            this.predicates = list;
        }

        public static RecipeCraftedTrigger.a craftedItem(MinecraftKey minecraftkey, List<CriterionConditionItem> list) {
            return new RecipeCraftedTrigger.a(ContextAwarePredicate.ANY, minecraftkey, list);
        }

        public static RecipeCraftedTrigger.a craftedItem(MinecraftKey minecraftkey) {
            return new RecipeCraftedTrigger.a(ContextAwarePredicate.ANY, minecraftkey, List.of());
        }

        boolean matches(MinecraftKey minecraftkey, List<ItemStack> list) {
            if (!minecraftkey.equals(this.recipeId)) {
                return false;
            } else {
                List<ItemStack> list1 = new ArrayList(list);
                Iterator iterator = this.predicates.iterator();

                while (iterator.hasNext()) {
                    CriterionConditionItem criterionconditionitem = (CriterionConditionItem) iterator.next();
                    boolean flag = false;
                    Iterator iterator1 = list1.iterator();

                    while (true) {
                        if (iterator1.hasNext()) {
                            if (!criterionconditionitem.matches((ItemStack) iterator1.next())) {
                                continue;
                            }

                            iterator1.remove();
                            flag = true;
                        }

                        if (!flag) {
                            return false;
                        }
                        break;
                    }
                }

                return true;
            }
        }

        @Override
        public JsonObject serializeToJson(LootSerializationContext lootserializationcontext) {
            JsonObject jsonobject = super.serializeToJson(lootserializationcontext);

            jsonobject.addProperty("recipe_id", this.recipeId.toString());
            if (this.predicates.size() > 0) {
                JsonArray jsonarray = new JsonArray();
                Iterator iterator = this.predicates.iterator();

                while (iterator.hasNext()) {
                    CriterionConditionItem criterionconditionitem = (CriterionConditionItem) iterator.next();

                    jsonarray.add(criterionconditionitem.serializeToJson());
                }

                jsonobject.add("ingredients", jsonarray);
            }

            return jsonobject;
        }
    }
}
