package net.minecraft.world;

import javax.annotation.Nullable;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameterSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;
import net.minecraft.world.phys.Vec3D;

public interface RandomizableContainer extends IInventory {

    String LOOT_TABLE_TAG = "LootTable";
    String LOOT_TABLE_SEED_TAG = "LootTableSeed";

    @Nullable
    ResourceKey<LootTable> getLootTable();

    void setLootTable(@Nullable ResourceKey<LootTable> resourcekey);

    default void setLootTable(ResourceKey<LootTable> resourcekey, long i) {
        this.setLootTable(resourcekey);
        this.setLootTableSeed(i);
    }

    long getLootTableSeed();

    void setLootTableSeed(long i);

    BlockPosition getBlockPos();

    @Nullable
    World getLevel();

    static void setBlockEntityLootTable(IBlockAccess iblockaccess, RandomSource randomsource, BlockPosition blockposition, ResourceKey<LootTable> resourcekey) {
        TileEntity tileentity = iblockaccess.getBlockEntity(blockposition);

        if (tileentity instanceof RandomizableContainer randomizablecontainer) {
            randomizablecontainer.setLootTable(resourcekey, randomsource.nextLong());
        }

    }

    default boolean tryLoadLootTable(NBTTagCompound nbttagcompound) {
        if (nbttagcompound.contains("LootTable", 8)) {
            this.setLootTable(ResourceKey.create(Registries.LOOT_TABLE, MinecraftKey.parse(nbttagcompound.getString("LootTable"))));
            if (nbttagcompound.contains("LootTableSeed", 4)) {
                this.setLootTableSeed(nbttagcompound.getLong("LootTableSeed"));
            } else {
                this.setLootTableSeed(0L);
            }

            return true;
        } else {
            return false;
        }
    }

    default boolean trySaveLootTable(NBTTagCompound nbttagcompound) {
        ResourceKey<LootTable> resourcekey = this.getLootTable();

        if (resourcekey == null) {
            return false;
        } else {
            nbttagcompound.putString("LootTable", resourcekey.location().toString());
            long i = this.getLootTableSeed();

            if (i != 0L) {
                nbttagcompound.putLong("LootTableSeed", i);
            }

            return true;
        }
    }

    default void unpackLootTable(@Nullable EntityHuman entityhuman) {
        World world = this.getLevel();
        BlockPosition blockposition = this.getBlockPos();
        ResourceKey<LootTable> resourcekey = this.getLootTable();

        if (resourcekey != null && world != null && world.getServer() != null) {
            LootTable loottable = world.getServer().reloadableRegistries().getLootTable(resourcekey);

            if (entityhuman instanceof EntityPlayer) {
                CriterionTriggers.GENERATE_LOOT.trigger((EntityPlayer) entityhuman, resourcekey);
            }

            this.setLootTable((ResourceKey) null);
            LootParams.a lootparams_a = (new LootParams.a((WorldServer) world)).withParameter(LootContextParameters.ORIGIN, Vec3D.atCenterOf(blockposition));

            if (entityhuman != null) {
                lootparams_a.withLuck(entityhuman.getLuck()).withParameter(LootContextParameters.THIS_ENTITY, entityhuman);
            }

            loottable.fill(this, lootparams_a.create(LootContextParameterSets.CHEST), this.getLootTableSeed());
        }

    }
}
