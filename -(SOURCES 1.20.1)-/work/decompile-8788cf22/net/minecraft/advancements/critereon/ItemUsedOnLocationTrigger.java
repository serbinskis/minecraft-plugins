package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.Arrays;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.BlockPosition;
import net.minecraft.resources.MinecraftKey;
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

    final MinecraftKey id;

    public ItemUsedOnLocationTrigger(MinecraftKey minecraftkey) {
        this.id = minecraftkey;
    }

    @Override
    public MinecraftKey getId() {
        return this.id;
    }

    @Override
    public ItemUsedOnLocationTrigger.a createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, LootDeserializationContext lootdeserializationcontext) {
        ContextAwarePredicate contextawarepredicate1 = ContextAwarePredicate.fromElement("location", lootdeserializationcontext, jsonobject.get("location"), LootContextParameterSets.ADVANCEMENT_LOCATION);

        if (contextawarepredicate1 == null) {
            throw new JsonParseException("Failed to parse 'location' field");
        } else {
            return new ItemUsedOnLocationTrigger.a(this.id, contextawarepredicate, contextawarepredicate1);
        }
    }

    public void trigger(EntityPlayer entityplayer, BlockPosition blockposition, ItemStack itemstack) {
        WorldServer worldserver = entityplayer.serverLevel();
        IBlockData iblockdata = worldserver.getBlockState(blockposition);
        LootParams lootparams = (new LootParams.a(worldserver)).withParameter(LootContextParameters.ORIGIN, blockposition.getCenter()).withParameter(LootContextParameters.THIS_ENTITY, entityplayer).withParameter(LootContextParameters.BLOCK_STATE, iblockdata).withParameter(LootContextParameters.TOOL, itemstack).create(LootContextParameterSets.ADVANCEMENT_LOCATION);
        LootTableInfo loottableinfo = (new LootTableInfo.Builder(lootparams)).create((MinecraftKey) null);

        this.trigger(entityplayer, (itemusedonlocationtrigger_a) -> {
            return itemusedonlocationtrigger_a.matches(loottableinfo);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        private final ContextAwarePredicate location;

        public a(MinecraftKey minecraftkey, ContextAwarePredicate contextawarepredicate, ContextAwarePredicate contextawarepredicate1) {
            super(minecraftkey, contextawarepredicate);
            this.location = contextawarepredicate1;
        }

        public static ItemUsedOnLocationTrigger.a placedBlock(Block block) {
            ContextAwarePredicate contextawarepredicate = ContextAwarePredicate.create(LootItemConditionBlockStateProperty.hasBlockStateProperties(block).build());

            return new ItemUsedOnLocationTrigger.a(CriterionTriggers.PLACED_BLOCK.id, ContextAwarePredicate.ANY, contextawarepredicate);
        }

        public static ItemUsedOnLocationTrigger.a placedBlock(LootItemCondition.a... alootitemcondition_a) {
            ContextAwarePredicate contextawarepredicate = ContextAwarePredicate.create((LootItemCondition[]) Arrays.stream(alootitemcondition_a).map(LootItemCondition.a::build).toArray((i) -> {
                return new LootItemCondition[i];
            }));

            return new ItemUsedOnLocationTrigger.a(CriterionTriggers.PLACED_BLOCK.id, ContextAwarePredicate.ANY, contextawarepredicate);
        }

        private static ItemUsedOnLocationTrigger.a itemUsedOnLocation(CriterionConditionLocation.a criterionconditionlocation_a, CriterionConditionItem.a criterionconditionitem_a, MinecraftKey minecraftkey) {
            ContextAwarePredicate contextawarepredicate = ContextAwarePredicate.create(LootItemConditionLocationCheck.checkLocation(criterionconditionlocation_a).build(), LootItemConditionMatchTool.toolMatches(criterionconditionitem_a).build());

            return new ItemUsedOnLocationTrigger.a(minecraftkey, ContextAwarePredicate.ANY, contextawarepredicate);
        }

        public static ItemUsedOnLocationTrigger.a itemUsedOnBlock(CriterionConditionLocation.a criterionconditionlocation_a, CriterionConditionItem.a criterionconditionitem_a) {
            return itemUsedOnLocation(criterionconditionlocation_a, criterionconditionitem_a, CriterionTriggers.ITEM_USED_ON_BLOCK.id);
        }

        public static ItemUsedOnLocationTrigger.a allayDropItemOnBlock(CriterionConditionLocation.a criterionconditionlocation_a, CriterionConditionItem.a criterionconditionitem_a) {
            return itemUsedOnLocation(criterionconditionlocation_a, criterionconditionitem_a, CriterionTriggers.ALLAY_DROP_ITEM_ON_BLOCK.id);
        }

        public boolean matches(LootTableInfo loottableinfo) {
            return this.location.matches(loottableinfo);
        }

        @Override
        public JsonObject serializeToJson(LootSerializationContext lootserializationcontext) {
            JsonObject jsonobject = super.serializeToJson(lootserializationcontext);

            jsonobject.add("location", this.location.toJson(lootserializationcontext));
            return jsonobject;
        }
    }
}
