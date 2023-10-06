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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.IBlockData;

public class CriterionTriggerEnterBlock extends CriterionTriggerAbstract<CriterionTriggerEnterBlock.a> {

    public CriterionTriggerEnterBlock() {}

    @Override
    public CriterionTriggerEnterBlock.a createInstance(JsonObject jsonobject, Optional<ContextAwarePredicate> optional, LootDeserializationContext lootdeserializationcontext) {
        Block block = deserializeBlock(jsonobject);
        Optional<CriterionTriggerProperties> optional1 = CriterionTriggerProperties.fromJson(jsonobject.get("state"));

        if (block != null) {
            optional1.ifPresent((criteriontriggerproperties) -> {
                criteriontriggerproperties.checkState(block.getStateDefinition(), (s) -> {
                    throw new JsonSyntaxException("Block " + block + " has no property " + s);
                });
            });
        }

        return new CriterionTriggerEnterBlock.a(optional, block, optional1);
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

    public void trigger(EntityPlayer entityplayer, IBlockData iblockdata) {
        this.trigger(entityplayer, (criteriontriggerenterblock_a) -> {
            return criteriontriggerenterblock_a.matches(iblockdata);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        @Nullable
        private final Block block;
        private final Optional<CriterionTriggerProperties> state;

        public a(Optional<ContextAwarePredicate> optional, @Nullable Block block, Optional<CriterionTriggerProperties> optional1) {
            super(optional);
            this.block = block;
            this.state = optional1;
        }

        public static Criterion<CriterionTriggerEnterBlock.a> entersBlock(Block block) {
            return CriterionTriggers.ENTER_BLOCK.createCriterion(new CriterionTriggerEnterBlock.a(Optional.empty(), block, Optional.empty()));
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject jsonobject = super.serializeToJson();

            if (this.block != null) {
                jsonobject.addProperty("block", BuiltInRegistries.BLOCK.getKey(this.block).toString());
            }

            this.state.ifPresent((criteriontriggerproperties) -> {
                jsonobject.add("state", criteriontriggerproperties.serializeToJson());
            });
            return jsonobject;
        }

        public boolean matches(IBlockData iblockdata) {
            return this.block != null && !iblockdata.is(this.block) ? false : !this.state.isPresent() || ((CriterionTriggerProperties) this.state.get()).matches(iblockdata);
        }
    }
}
