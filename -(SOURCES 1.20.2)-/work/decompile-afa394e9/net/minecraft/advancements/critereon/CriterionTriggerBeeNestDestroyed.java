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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.IBlockData;

public class CriterionTriggerBeeNestDestroyed extends CriterionTriggerAbstract<CriterionTriggerBeeNestDestroyed.a> {

    public CriterionTriggerBeeNestDestroyed() {}

    @Override
    public CriterionTriggerBeeNestDestroyed.a createInstance(JsonObject jsonobject, Optional<ContextAwarePredicate> optional, LootDeserializationContext lootdeserializationcontext) {
        Block block = deserializeBlock(jsonobject);
        Optional<CriterionConditionItem> optional1 = CriterionConditionItem.fromJson(jsonobject.get("item"));
        CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange = CriterionConditionValue.IntegerRange.fromJson(jsonobject.get("num_bees_inside"));

        return new CriterionTriggerBeeNestDestroyed.a(optional, block, optional1, criterionconditionvalue_integerrange);
    }

    @Nullable
    private static Block deserializeBlock(JsonObject jsonobject) {
        if (jsonobject.has("block")) {
            MinecraftKey minecraftkey = new MinecraftKey(ChatDeserializer.getAsString(jsonobject, "block"));

            return (Block) BuiltInRegistries.BLOCK.getOptional(minecraftkey).orElseThrow(() -> {
                return new JsonSyntaxException("Unknown block type '" + minecraftkey + "'");
            });
        } else {
            return null;
        }
    }

    public void trigger(EntityPlayer entityplayer, IBlockData iblockdata, ItemStack itemstack, int i) {
        this.trigger(entityplayer, (criteriontriggerbeenestdestroyed_a) -> {
            return criteriontriggerbeenestdestroyed_a.matches(iblockdata, itemstack, i);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        @Nullable
        private final Block block;
        private final Optional<CriterionConditionItem> item;
        private final CriterionConditionValue.IntegerRange numBees;

        public a(Optional<ContextAwarePredicate> optional, @Nullable Block block, Optional<CriterionConditionItem> optional1, CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange) {
            super(optional);
            this.block = block;
            this.item = optional1;
            this.numBees = criterionconditionvalue_integerrange;
        }

        public static Criterion<CriterionTriggerBeeNestDestroyed.a> destroyedBeeNest(Block block, CriterionConditionItem.a criterionconditionitem_a, CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange) {
            return CriterionTriggers.BEE_NEST_DESTROYED.createCriterion(new CriterionTriggerBeeNestDestroyed.a(Optional.empty(), block, Optional.of(criterionconditionitem_a.build()), criterionconditionvalue_integerrange));
        }

        public boolean matches(IBlockData iblockdata, ItemStack itemstack, int i) {
            return this.block != null && !iblockdata.is(this.block) ? false : (this.item.isPresent() && !((CriterionConditionItem) this.item.get()).matches(itemstack) ? false : this.numBees.matches(i));
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject jsonobject = super.serializeToJson();

            if (this.block != null) {
                jsonobject.addProperty("block", BuiltInRegistries.BLOCK.getKey(this.block).toString());
            }

            this.item.ifPresent((criterionconditionitem) -> {
                jsonobject.add("item", criterionconditionitem.serializeToJson());
            });
            jsonobject.add("num_bees_inside", this.numBees.serializeToJson());
            return jsonobject;
        }
    }
}
