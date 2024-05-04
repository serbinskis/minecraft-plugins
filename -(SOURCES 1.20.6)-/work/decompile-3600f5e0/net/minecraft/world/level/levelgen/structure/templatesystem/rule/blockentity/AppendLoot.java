package net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.LootTable;
import org.slf4j.Logger;

public class AppendLoot implements RuleBlockEntityModifier {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<AppendLoot> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(ResourceKey.codec(Registries.LOOT_TABLE).fieldOf("loot_table").forGetter((appendloot) -> {
            return appendloot.lootTable;
        })).apply(instance, AppendLoot::new);
    });
    private final ResourceKey<LootTable> lootTable;

    public AppendLoot(ResourceKey<LootTable> resourcekey) {
        this.lootTable = resourcekey;
    }

    @Override
    public NBTTagCompound apply(RandomSource randomsource, @Nullable NBTTagCompound nbttagcompound) {
        NBTTagCompound nbttagcompound1 = nbttagcompound == null ? new NBTTagCompound() : nbttagcompound.copy();
        DataResult dataresult = ResourceKey.codec(Registries.LOOT_TABLE).encodeStart(DynamicOpsNBT.INSTANCE, this.lootTable);
        Logger logger = AppendLoot.LOGGER;

        Objects.requireNonNull(logger);
        dataresult.resultOrPartial(logger::error).ifPresent((nbtbase) -> {
            nbttagcompound1.put("LootTable", nbtbase);
        });
        nbttagcompound1.putLong("LootTableSeed", randomsource.nextLong());
        return nbttagcompound1;
    }

    @Override
    public RuleBlockEntityModifierType<?> getType() {
        return RuleBlockEntityModifierType.APPEND_LOOT;
    }
}
