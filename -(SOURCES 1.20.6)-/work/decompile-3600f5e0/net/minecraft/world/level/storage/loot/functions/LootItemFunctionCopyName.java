package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.INamable;
import net.minecraft.world.INamableTileEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootItemFunctionCopyName extends LootItemFunctionConditional {

    public static final MapCodec<LootItemFunctionCopyName> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(LootItemFunctionCopyName.Source.CODEC.fieldOf("source").forGetter((lootitemfunctioncopyname) -> {
            return lootitemfunctioncopyname.source;
        })).apply(instance, LootItemFunctionCopyName::new);
    });
    private final LootItemFunctionCopyName.Source source;

    private LootItemFunctionCopyName(List<LootItemCondition> list, LootItemFunctionCopyName.Source lootitemfunctioncopyname_source) {
        super(list);
        this.source = lootitemfunctioncopyname_source;
    }

    @Override
    public LootItemFunctionType<LootItemFunctionCopyName> getType() {
        return LootItemFunctions.COPY_NAME;
    }

    @Override
    public Set<LootContextParameter<?>> getReferencedContextParams() {
        return ImmutableSet.of(this.source.param);
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        Object object = loottableinfo.getParamOrNull(this.source.param);

        if (object instanceof INamableTileEntity inamabletileentity) {
            itemstack.set(DataComponents.CUSTOM_NAME, inamabletileentity.getCustomName());
        }

        return itemstack;
    }

    public static LootItemFunctionConditional.a<?> copyName(LootItemFunctionCopyName.Source lootitemfunctioncopyname_source) {
        return simpleBuilder((list) -> {
            return new LootItemFunctionCopyName(list, lootitemfunctioncopyname_source);
        });
    }

    public static enum Source implements INamable {

        THIS("this", LootContextParameters.THIS_ENTITY), KILLER("killer", LootContextParameters.KILLER_ENTITY), KILLER_PLAYER("killer_player", LootContextParameters.LAST_DAMAGE_PLAYER), BLOCK_ENTITY("block_entity", LootContextParameters.BLOCK_ENTITY);

        public static final Codec<LootItemFunctionCopyName.Source> CODEC = INamable.fromEnum(LootItemFunctionCopyName.Source::values);
        private final String name;
        final LootContextParameter<?> param;

        private Source(final String s, final LootContextParameter lootcontextparameter) {
            this.name = s;
            this.param = lootcontextparameter;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
