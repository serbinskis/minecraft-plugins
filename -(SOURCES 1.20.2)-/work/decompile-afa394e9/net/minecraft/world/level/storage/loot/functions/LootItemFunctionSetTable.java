package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemBlock;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.storage.loot.LootCollector;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootItemFunctionSetTable extends LootItemFunctionConditional {

    public static final Codec<LootItemFunctionSetTable> CODEC = RecordCodecBuilder.create((instance) -> {
        return commonFields(instance).and(instance.group(MinecraftKey.CODEC.fieldOf("name").forGetter((lootitemfunctionsettable) -> {
            return lootitemfunctionsettable.name;
        }), ExtraCodecs.strictOptionalField(Codec.LONG, "seed", 0L).forGetter((lootitemfunctionsettable) -> {
            return lootitemfunctionsettable.seed;
        }), BuiltInRegistries.BLOCK_ENTITY_TYPE.holderByNameCodec().fieldOf("type").forGetter((lootitemfunctionsettable) -> {
            return lootitemfunctionsettable.type;
        }))).apply(instance, LootItemFunctionSetTable::new);
    });
    private final MinecraftKey name;
    private final long seed;
    private final Holder<TileEntityTypes<?>> type;

    private LootItemFunctionSetTable(List<LootItemCondition> list, MinecraftKey minecraftkey, long i, Holder<TileEntityTypes<?>> holder) {
        super(list);
        this.name = minecraftkey;
        this.seed = i;
        this.type = holder;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_LOOT_TABLE;
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        if (itemstack.isEmpty()) {
            return itemstack;
        } else {
            NBTTagCompound nbttagcompound = ItemBlock.getBlockEntityData(itemstack);

            if (nbttagcompound == null) {
                nbttagcompound = new NBTTagCompound();
            }

            nbttagcompound.putString("LootTable", this.name.toString());
            if (this.seed != 0L) {
                nbttagcompound.putLong("LootTableSeed", this.seed);
            }

            ItemBlock.setBlockEntityData(itemstack, (TileEntityTypes) this.type.value(), nbttagcompound);
            return itemstack;
        }
    }

    @Override
    public void validate(LootCollector lootcollector) {
        super.validate(lootcollector);
        LootDataId<LootTable> lootdataid = new LootDataId<>(LootDataType.TABLE, this.name);

        if (lootcollector.resolver().getElementOptional(lootdataid).isEmpty()) {
            lootcollector.reportProblem("Missing loot table used for container: " + this.name);
        }

    }

    public static LootItemFunctionConditional.a<?> withLootTable(TileEntityTypes<?> tileentitytypes, MinecraftKey minecraftkey) {
        return simpleBuilder((list) -> {
            return new LootItemFunctionSetTable(list, minecraftkey, 0L, tileentitytypes.builtInRegistryHolder());
        });
    }

    public static LootItemFunctionConditional.a<?> withLootTable(TileEntityTypes<?> tileentitytypes, MinecraftKey minecraftkey, long i) {
        return simpleBuilder((list) -> {
            return new LootItemFunctionSetTable(list, minecraftkey, i, tileentitytypes.builtInRegistryHolder());
        });
    }
}
