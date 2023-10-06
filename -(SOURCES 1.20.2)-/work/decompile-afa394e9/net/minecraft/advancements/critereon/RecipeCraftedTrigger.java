package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.util.ChatDeserializer;
import net.minecraft.world.item.ItemStack;

public class RecipeCraftedTrigger extends CriterionTriggerAbstract<RecipeCraftedTrigger.a> {

    public RecipeCraftedTrigger() {}

    @Override
    protected RecipeCraftedTrigger.a createInstance(JsonObject jsonobject, Optional<ContextAwarePredicate> optional, LootDeserializationContext lootdeserializationcontext) {
        MinecraftKey minecraftkey = new MinecraftKey(ChatDeserializer.getAsString(jsonobject, "recipe_id"));
        List<CriterionConditionItem> list = CriterionConditionItem.fromJsonArray(jsonobject.get("ingredients"));

        return new RecipeCraftedTrigger.a(optional, minecraftkey, list);
    }

    public void trigger(EntityPlayer entityplayer, MinecraftKey minecraftkey, List<ItemStack> list) {
        this.trigger(entityplayer, (recipecraftedtrigger_a) -> {
            return recipecraftedtrigger_a.matches(minecraftkey, list);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        private final MinecraftKey recipeId;
        private final List<CriterionConditionItem> predicates;

        public a(Optional<ContextAwarePredicate> optional, MinecraftKey minecraftkey, List<CriterionConditionItem> list) {
            super(optional);
            this.recipeId = minecraftkey;
            this.predicates = list;
        }

        public static Criterion<RecipeCraftedTrigger.a> craftedItem(MinecraftKey minecraftkey, List<CriterionConditionItem.a> list) {
            return CriterionTriggers.RECIPE_CRAFTED.createCriterion(new RecipeCraftedTrigger.a(Optional.empty(), minecraftkey, list.stream().map(CriterionConditionItem.a::build).toList()));
        }

        public static Criterion<RecipeCraftedTrigger.a> craftedItem(MinecraftKey minecraftkey) {
            return CriterionTriggers.RECIPE_CRAFTED.createCriterion(new RecipeCraftedTrigger.a(Optional.empty(), minecraftkey, List.of()));
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
        public JsonObject serializeToJson() {
            JsonObject jsonobject = super.serializeToJson();

            jsonobject.addProperty("recipe_id", this.recipeId.toString());
            if (!this.predicates.isEmpty()) {
                jsonobject.add("ingredients", CriterionConditionItem.serializeToJsonArray(this.predicates));
            }

            return jsonobject;
        }
    }
}
