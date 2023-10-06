package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameterSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionBlockStateProperty;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionLocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionMatchTool;

public class ItemUsedOnLocationTrigger extends CriterionTriggerAbstract<ItemUsedOnLocationTrigger.a> {

    public ItemUsedOnLocationTrigger() {}

    @Override
    public ItemUsedOnLocationTrigger.a createInstance(JsonObject jsonobject, Optional<ContextAwarePredicate> optional, LootDeserializationContext lootdeserializationcontext) {
        Optional<Optional<ContextAwarePredicate>> optional1 = ContextAwarePredicate.fromElement("location", lootdeserializationcontext, jsonobject.get("location"), LootContextParameterSets.ADVANCEMENT_LOCATION);

        if (optional1.isEmpty()) {
            throw new JsonParseException("Failed to parse 'location' field");
        } else {
            return new ItemUsedOnLocationTrigger.a(optional, (Optional) optional1.get());
        }
    }

    public void trigger(EntityPlayer entityplayer, BlockPosition blockposition, ItemStack itemstack) {
        WorldServer worldserver = entityplayer.serverLevel();
        IBlockData iblockdata = worldserver.getBlockState(blockposition);
        LootParams lootparams = (new LootParams.a(worldserver)).withParameter(LootContextParameters.ORIGIN, blockposition.getCenter()).withParameter(LootContextParameters.THIS_ENTITY, entityplayer).withParameter(LootContextParameters.BLOCK_STATE, iblockdata).withParameter(LootContextParameters.TOOL, itemstack).create(LootContextParameterSets.ADVANCEMENT_LOCATION);
        LootTableInfo loottableinfo = (new LootTableInfo.Builder(lootparams)).create(Optional.empty());

        this.trigger(entityplayer, (itemusedonlocationtrigger_a) -> {
            return itemusedonlocationtrigger_a.matches(loottableinfo);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        private final Optional<ContextAwarePredicate> location;

        public a(Optional<ContextAwarePredicate> optional, Optional<ContextAwarePredicate> optional1) {
            super(optional);
            this.location = optional1;
        }

        public static Criterion<ItemUsedOnLocationTrigger.a> placedBlock(Block block) {
            ContextAwarePredicate contextawarepredicate = ContextAwarePredicate.create(LootItemConditionBlockStateProperty.hasBlockStateProperties(block).build());

            return CriterionTriggers.PLACED_BLOCK.createCriterion(new ItemUsedOnLocationTrigger.a(Optional.empty(), Optional.of(contextawarepredicate)));
        }

        public static Criterion<ItemUsedOnLocationTrigger.a> placedBlock(LootItemCondition.a... alootitemcondition_a) {
            ContextAwarePredicate contextawarepredicate = ContextAwarePredicate.create((LootItemCondition[]) Arrays.stream(alootitemcondition_a).map(LootItemCondition.a::build).toArray((i) -> {
                return new LootItemCondition[i];
            }));

            return CriterionTriggers.PLACED_BLOCK.createCriterion(new ItemUsedOnLocationTrigger.a(Optional.empty(), Optional.of(contextawarepredicate)));
        }

        private static ItemUsedOnLocationTrigger.a itemUsedOnLocation(CriterionConditionLocation.a criterionconditionlocation_a, CriterionConditionItem.a criterionconditionitem_a) {
            ContextAwarePredicate contextawarepredicate = ContextAwarePredicate.create(LootItemConditionLocationCheck.checkLocation(criterionconditionlocation_a).build(), LootItemConditionMatchTool.toolMatches(criterionconditionitem_a).build());

            return new ItemUsedOnLocationTrigger.a(Optional.empty(), Optional.of(contextawarepredicate));
        }

        public static Criterion<ItemUsedOnLocationTrigger.a> itemUsedOnBlock(CriterionConditionLocation.a criterionconditionlocation_a, CriterionConditionItem.a criterionconditionitem_a) {
            return CriterionTriggers.ITEM_USED_ON_BLOCK.createCriterion(itemUsedOnLocation(criterionconditionlocation_a, criterionconditionitem_a));
        }

        public static Criterion<ItemUsedOnLocationTrigger.a> allayDropItemOnBlock(CriterionConditionLocation.a criterionconditionlocation_a, CriterionConditionItem.a criterionconditionitem_a) {
            return CriterionTriggers.ALLAY_DROP_ITEM_ON_BLOCK.createCriterion(itemUsedOnLocation(criterionconditionlocation_a, criterionconditionitem_a));
        }

        public boolean matches(LootTableInfo loottableinfo) {
            return this.location.isEmpty() || ((ContextAwarePredicate) this.location.get()).matches(loottableinfo);
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject jsonobject = super.serializeToJson();

            this.location.ifPresent((contextawarepredicate) -> {
                jsonobject.add("location", contextawarepredicate.toJson());
            });
            return jsonobject;
        }
    }
}
