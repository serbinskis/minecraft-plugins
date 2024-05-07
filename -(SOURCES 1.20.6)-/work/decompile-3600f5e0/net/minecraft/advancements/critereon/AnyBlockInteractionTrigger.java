package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameterSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;

public class AnyBlockInteractionTrigger extends CriterionTriggerAbstract<AnyBlockInteractionTrigger.a> {

    public AnyBlockInteractionTrigger() {}

    @Override
    public Codec<AnyBlockInteractionTrigger.a> codec() {
        return AnyBlockInteractionTrigger.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, BlockPosition blockposition, ItemStack itemstack) {
        WorldServer worldserver = entityplayer.serverLevel();
        IBlockData iblockdata = worldserver.getBlockState(blockposition);
        LootParams lootparams = (new LootParams.a(worldserver)).withParameter(LootContextParameters.ORIGIN, blockposition.getCenter()).withParameter(LootContextParameters.THIS_ENTITY, entityplayer).withParameter(LootContextParameters.BLOCK_STATE, iblockdata).withParameter(LootContextParameters.TOOL, itemstack).create(LootContextParameterSets.ADVANCEMENT_LOCATION);
        LootTableInfo loottableinfo = (new LootTableInfo.Builder(lootparams)).create(Optional.empty());

        this.trigger(entityplayer, (anyblockinteractiontrigger_a) -> {
            return anyblockinteractiontrigger_a.matches(loottableinfo);
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> location) implements CriterionTriggerAbstract.a {

        public static final Codec<AnyBlockInteractionTrigger.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(AnyBlockInteractionTrigger.a::player), ContextAwarePredicate.CODEC.optionalFieldOf("location").forGetter(AnyBlockInteractionTrigger.a::location)).apply(instance, AnyBlockInteractionTrigger.a::new);
        });

        public boolean matches(LootTableInfo loottableinfo) {
            return this.location.isEmpty() || ((ContextAwarePredicate) this.location.get()).matches(loottableinfo);
        }

        @Override
        public void validate(CriterionValidator criterionvalidator) {
            CriterionTriggerAbstract.a.super.validate(criterionvalidator);
            this.location.ifPresent((contextawarepredicate) -> {
                criterionvalidator.validate(contextawarepredicate, LootContextParameterSets.ADVANCEMENT_LOCATION, ".location");
            });
        }
    }
}
