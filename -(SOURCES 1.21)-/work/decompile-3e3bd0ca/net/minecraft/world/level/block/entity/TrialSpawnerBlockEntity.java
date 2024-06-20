package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import java.util.Objects;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.protocol.game.PacketPlayOutTileEntityData;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.TrialSpawnerBlock;
import net.minecraft.world.level.block.entity.trialspawner.PlayerDetector;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import org.slf4j.Logger;

public class TrialSpawnerBlockEntity extends TileEntity implements Spawner, TrialSpawner.b {

    private static final Logger LOGGER = LogUtils.getLogger();
    public TrialSpawner trialSpawner;

    public TrialSpawnerBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        super(TileEntityTypes.TRIAL_SPAWNER, blockposition, iblockdata);
        PlayerDetector playerdetector = PlayerDetector.NO_CREATIVE_PLAYERS;
        PlayerDetector.a playerdetector_a = PlayerDetector.a.SELECT_FROM_LEVEL;

        this.trialSpawner = new TrialSpawner(this, playerdetector, playerdetector_a);
    }

    @Override
    protected void loadAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        super.loadAdditional(nbttagcompound, holderlookup_a);
        if (nbttagcompound.contains("normal_config")) {
            NBTTagCompound nbttagcompound1 = nbttagcompound.getCompound("normal_config").copy();

            nbttagcompound.put("ominous_config", nbttagcompound1.merge(nbttagcompound.getCompound("ominous_config")));
        }

        DataResult dataresult = this.trialSpawner.codec().parse(DynamicOpsNBT.INSTANCE, nbttagcompound);
        Logger logger = TrialSpawnerBlockEntity.LOGGER;

        Objects.requireNonNull(logger);
        dataresult.resultOrPartial(logger::error).ifPresent((trialspawner) -> {
            this.trialSpawner = trialspawner;
        });
        if (this.level != null) {
            this.markUpdated();
        }

    }

    @Override
    protected void saveAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        super.saveAdditional(nbttagcompound, holderlookup_a);
        this.trialSpawner.codec().encodeStart(DynamicOpsNBT.INSTANCE, this.trialSpawner).ifSuccess((nbtbase) -> {
            nbttagcompound.merge((NBTTagCompound) nbtbase);
        }).ifError((error) -> {
            TrialSpawnerBlockEntity.LOGGER.warn("Failed to encode TrialSpawner {}", error.message());
        });
    }

    @Override
    public PacketPlayOutTileEntityData getUpdatePacket() {
        return PacketPlayOutTileEntityData.create(this);
    }

    @Override
    public NBTTagCompound getUpdateTag(HolderLookup.a holderlookup_a) {
        return this.trialSpawner.getData().getUpdateTag((TrialSpawnerState) this.getBlockState().getValue(TrialSpawnerBlock.STATE));
    }

    @Override
    public boolean onlyOpCanSetNbt() {
        return true;
    }

    @Override
    public void setEntityId(EntityTypes<?> entitytypes, RandomSource randomsource) {
        this.trialSpawner.getData().setEntityId(this.trialSpawner, randomsource, entitytypes);
        this.setChanged();
    }

    public TrialSpawner getTrialSpawner() {
        return this.trialSpawner;
    }

    @Override
    public TrialSpawnerState getState() {
        return !this.getBlockState().hasProperty(BlockProperties.TRIAL_SPAWNER_STATE) ? TrialSpawnerState.INACTIVE : (TrialSpawnerState) this.getBlockState().getValue(BlockProperties.TRIAL_SPAWNER_STATE);
    }

    @Override
    public void setState(World world, TrialSpawnerState trialspawnerstate) {
        this.setChanged();
        world.setBlockAndUpdate(this.worldPosition, (IBlockData) this.getBlockState().setValue(BlockProperties.TRIAL_SPAWNER_STATE, trialspawnerstate));
    }

    @Override
    public void markUpdated() {
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }

    }
}
