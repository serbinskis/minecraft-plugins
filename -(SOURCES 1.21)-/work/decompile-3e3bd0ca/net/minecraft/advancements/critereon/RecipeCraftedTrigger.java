package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.item.ItemStack;

public class RecipeCraftedTrigger extends CriterionTriggerAbstract<RecipeCraftedTrigger.a> {

    public RecipeCraftedTrigger() {}

    @Override
    public Codec<RecipeCraftedTrigger.a> codec() {
        return RecipeCraftedTrigger.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, MinecraftKey minecraftkey, List<ItemStack> list) {
        this.trigger(entityplayer, (recipecraftedtrigger_a) -> {
            return recipecraftedtrigger_a.matches(minecraftkey, list);
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, MinecraftKey recipeId, List<CriterionConditionItem> ingredients) implements CriterionTriggerAbstract.a {

        public static final Codec<RecipeCraftedTrigger.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(RecipeCraftedTrigger.a::player), MinecraftKey.CODEC.fieldOf("recipe_id").forGetter(RecipeCraftedTrigger.a::recipeId), CriterionConditionItem.CODEC.listOf().optionalFieldOf("ingredients", List.of()).forGetter(RecipeCraftedTrigger.a::ingredients)).apply(instance, RecipeCraftedTrigger.a::new);
        });

        public static Criterion<RecipeCraftedTrigger.a> craftedItem(MinecraftKey minecraftkey, List<CriterionConditionItem.a> list) {
            return CriterionTriggers.RECIPE_CRAFTED.createCriterion(new RecipeCraftedTrigger.a(Optional.empty(), minecraftkey, list.stream().map(CriterionConditionItem.a::build).toList()));
        }

        public static Criterion<RecipeCraftedTrigger.a> craftedItem(MinecraftKey minecraftkey) {
            return CriterionTriggers.RECIPE_CRAFTED.createCriterion(new RecipeCraftedTrigger.a(Optional.empty(), minecraftkey, List.of()));
        }

        public static Criterion<RecipeCraftedTrigger.a> crafterCraftedItem(MinecraftKey minecraftkey) {
            return CriterionTriggers.CRAFTER_RECIPE_CRAFTED.createCriterion(new RecipeCraftedTrigger.a(Optional.empty(), minecraftkey, List.of()));
        }

        boolean matches(MinecraftKey minecraftkey, List<ItemStack> list) {
            if (!minecraftkey.equals(this.recipeId)) {
                return false;
            } else {
                List<ItemStack> list1 = new ArrayList(list);
                Iterator iterator = this.ingredients.iterator();

                while (iterator.hasNext()) {
                    CriterionConditionItem criterionconditionitem = (CriterionConditionItem) iterator.next();
                    boolean flag = false;
                    Iterator<ItemStack> iterator1 = list1.iterator();

                    while (true) {
                        if (iterator1.hasNext()) {
                            if (!criterionconditionitem.test((ItemStack) iterator1.next())) {
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
    }
}
