package net.minecraft.world.level.block.entity.vault;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.trialspawner.PlayerDetector;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;

public record VaultConfig(ResourceKey<LootTable> lootTable, double activationRange, double deactivationRange, ItemStack keyItem, Optional<ResourceKey<LootTable>> overrideLootTableToDisplay, PlayerDetector playerDetector, PlayerDetector.a entitySelector) {

    static final String TAG_NAME = "config";
    static VaultConfig DEFAULT = new VaultConfig();
    static Codec<VaultConfig> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(ResourceKey.codec(Registries.LOOT_TABLE).lenientOptionalFieldOf("loot_table", VaultConfig.DEFAULT.lootTable()).forGetter(VaultConfig::lootTable), Codec.DOUBLE.lenientOptionalFieldOf("activation_range", VaultConfig.DEFAULT.activationRange()).forGetter(VaultConfig::activationRange), Codec.DOUBLE.lenientOptionalFieldOf("deactivation_range", VaultConfig.DEFAULT.deactivationRange()).forGetter(VaultConfig::deactivationRange), ItemStack.lenientOptionalFieldOf("key_item").forGetter(VaultConfig::keyItem), ResourceKey.codec(Registries.LOOT_TABLE).lenientOptionalFieldOf("override_loot_table_to_display").forGetter(VaultConfig::overrideLootTableToDisplay)).apply(instance, VaultConfig::new);
    }).validate(VaultConfig::validate);

    private VaultConfig() {
        this(LootTables.TRIAL_CHAMBERS_REWARD, 4.0D, 4.5D, new ItemStack(Items.TRIAL_KEY), Optional.empty(), PlayerDetector.INCLUDING_CREATIVE_PLAYERS, PlayerDetector.a.SELECT_FROM_LEVEL);
    }

    public VaultConfig(ResourceKey<LootTable> resourcekey, double d0, double d1, ItemStack itemstack, Optional<ResourceKey<LootTable>> optional) {
        this(resourcekey, d0, d1, itemstack, optional, VaultConfig.DEFAULT.playerDetector(), VaultConfig.DEFAULT.entitySelector());
    }

    private DataResult<VaultConfig> validate() {
        return this.activationRange > this.deactivationRange ? DataResult.error(() -> {
            return "Activation range must (" + this.activationRange + ") be less or equal to deactivation range (" + this.deactivationRange + ")";
        }) : DataResult.success(this);
    }
}
