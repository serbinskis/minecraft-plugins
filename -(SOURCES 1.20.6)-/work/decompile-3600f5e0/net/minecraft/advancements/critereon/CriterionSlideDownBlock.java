package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.IBlockData;

public class CriterionSlideDownBlock extends CriterionTriggerAbstract<CriterionSlideDownBlock.a> {

    public CriterionSlideDownBlock() {}

    @Override
    public Codec<CriterionSlideDownBlock.a> codec() {
        return CriterionSlideDownBlock.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, IBlockData iblockdata) {
        this.trigger(entityplayer, (criterionslidedownblock_a) -> {
            return criterionslidedownblock_a.matches(iblockdata);
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, Optional<Holder<Block>> block, Optional<CriterionTriggerProperties> state) implements CriterionTriggerAbstract.a {

        public static final Codec<CriterionSlideDownBlock.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CriterionSlideDownBlock.a::player), BuiltInRegistries.BLOCK.holderByNameCodec().optionalFieldOf("block").forGetter(CriterionSlideDownBlock.a::block), CriterionTriggerProperties.CODEC.optionalFieldOf("state").forGetter(CriterionSlideDownBlock.a::state)).apply(instance, CriterionSlideDownBlock.a::new);
        }).validate(CriterionSlideDownBlock.a::validate);

        private static DataResult<CriterionSlideDownBlock.a> validate(CriterionSlideDownBlock.a criterionslidedownblock_a) {
            return (DataResult) criterionslidedownblock_a.block.flatMap((holder) -> {
                return criterionslidedownblock_a.state.flatMap((criteriontriggerproperties) -> {
                    return criteriontriggerproperties.checkState(((Block) holder.value()).getStateDefinition());
                }).map((s) -> {
                    return DataResult.error(() -> {
                        String s1 = String.valueOf(holder);

                        return "Block" + s1 + " has no property " + s;
                    });
                });
            }).orElseGet(() -> {
                return DataResult.success(criterionslidedownblock_a);
            });
        }

        public static Criterion<CriterionSlideDownBlock.a> slidesDownBlock(Block block) {
            return CriterionTriggers.HONEY_BLOCK_SLIDE.createCriterion(new CriterionSlideDownBlock.a(Optional.empty(), Optional.of(block.builtInRegistryHolder()), Optional.empty()));
        }

        public boolean matches(IBlockData iblockdata) {
            return this.block.isPresent() && !iblockdata.is((Holder) this.block.get()) ? false : !this.state.isPresent() || ((CriterionTriggerProperties) this.state.get()).matches(iblockdata);
        }
    }
}
