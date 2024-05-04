package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.IBlockData;

public class CriterionTriggerBeeNestDestroyed extends CriterionTriggerAbstract<CriterionTriggerBeeNestDestroyed.a> {

    public CriterionTriggerBeeNestDestroyed() {}

    @Override
    public Codec<CriterionTriggerBeeNestDestroyed.a> codec() {
        return CriterionTriggerBeeNestDestroyed.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, IBlockData iblockdata, ItemStack itemstack, int i) {
        this.trigger(entityplayer, (criteriontriggerbeenestdestroyed_a) -> {
            return criteriontriggerbeenestdestroyed_a.matches(iblockdata, itemstack, i);
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, Optional<Holder<Block>> block, Optional<CriterionConditionItem> item, CriterionConditionValue.IntegerRange beesInside) implements CriterionTriggerAbstract.a {

        public static final Codec<CriterionTriggerBeeNestDestroyed.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CriterionTriggerBeeNestDestroyed.a::player), BuiltInRegistries.BLOCK.holderByNameCodec().optionalFieldOf("block").forGetter(CriterionTriggerBeeNestDestroyed.a::block), CriterionConditionItem.CODEC.optionalFieldOf("item").forGetter(CriterionTriggerBeeNestDestroyed.a::item), CriterionConditionValue.IntegerRange.CODEC.optionalFieldOf("num_bees_inside", CriterionConditionValue.IntegerRange.ANY).forGetter(CriterionTriggerBeeNestDestroyed.a::beesInside)).apply(instance, CriterionTriggerBeeNestDestroyed.a::new);
        });

        public static Criterion<CriterionTriggerBeeNestDestroyed.a> destroyedBeeNest(Block block, CriterionConditionItem.a criterionconditionitem_a, CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange) {
            return CriterionTriggers.BEE_NEST_DESTROYED.createCriterion(new CriterionTriggerBeeNestDestroyed.a(Optional.empty(), Optional.of(block.builtInRegistryHolder()), Optional.of(criterionconditionitem_a.build()), criterionconditionvalue_integerrange));
        }

        public boolean matches(IBlockData iblockdata, ItemStack itemstack, int i) {
            return this.block.isPresent() && !iblockdata.is((Holder) this.block.get()) ? false : (this.item.isPresent() && !((CriterionConditionItem) this.item.get()).test(itemstack) ? false : this.beesInside.matches(i));
        }
    }
}
