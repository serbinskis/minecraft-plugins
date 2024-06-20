package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import org.slf4j.Logger;

public class SculkSensorBlockEntity extends TileEntity implements GameEventListener.b<VibrationSystem.b>, VibrationSystem {

    private static final Logger LOGGER = LogUtils.getLogger();
    private VibrationSystem.a vibrationData;
    private final VibrationSystem.b vibrationListener;
    private final VibrationSystem.d vibrationUser;
    public int lastVibrationFrequency;

    protected SculkSensorBlockEntity(TileEntityTypes<?> tileentitytypes, BlockPosition blockposition, IBlockData iblockdata) {
        super(tileentitytypes, blockposition, iblockdata);
        this.vibrationUser = this.createVibrationUser();
        this.vibrationData = new VibrationSystem.a();
        this.vibrationListener = new VibrationSystem.b(this);
    }

    public SculkSensorBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        this(TileEntityTypes.SCULK_SENSOR, blockposition, iblockdata);
    }

    public VibrationSystem.d createVibrationUser() {
        return new SculkSensorBlockEntity.a(this.getBlockPos());
    }

    @Override
    protected void loadAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        super.loadAdditional(nbttagcompound, holderlookup_a);
        this.lastVibrationFrequency = nbttagcompound.getInt("last_vibration_frequency");
        RegistryOps<NBTBase> registryops = holderlookup_a.createSerializationContext(DynamicOpsNBT.INSTANCE);

        if (nbttagcompound.contains("listener", 10)) {
            VibrationSystem.a.CODEC.parse(registryops, nbttagcompound.getCompound("listener")).resultOrPartial((s) -> {
                SculkSensorBlockEntity.LOGGER.error("Failed to parse vibration listener for Sculk Sensor: '{}'", s);
            }).ifPresent((vibrationsystem_a) -> {
                this.vibrationData = vibrationsystem_a;
            });
        }

    }

    @Override
    protected void saveAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        super.saveAdditional(nbttagcompound, holderlookup_a);
        nbttagcompound.putInt("last_vibration_frequency", this.lastVibrationFrequency);
        RegistryOps<NBTBase> registryops = holderlookup_a.createSerializationContext(DynamicOpsNBT.INSTANCE);

        VibrationSystem.a.CODEC.encodeStart(registryops, this.vibrationData).resultOrPartial((s) -> {
            SculkSensorBlockEntity.LOGGER.error("Failed to encode vibration listener for Sculk Sensor: '{}'", s);
        }).ifPresent((nbtbase) -> {
            nbttagcompound.put("listener", nbtbase);
        });
    }

    @Override
    public VibrationSystem.a getVibrationData() {
        return this.vibrationData;
    }

    @Override
    public VibrationSystem.d getVibrationUser() {
        return this.vibrationUser;
    }

    public int getLastVibrationFrequency() {
        return this.lastVibrationFrequency;
    }

    public void setLastVibrationFrequency(int i) {
        this.lastVibrationFrequency = i;
    }

    @Override
    public VibrationSystem.b getListener() {
        return this.vibrationListener;
    }

    protected class a implements VibrationSystem.d {

        public static final int LISTENER_RANGE = 8;
        protected final BlockPosition blockPos;
        private final PositionSource positionSource;

        public a(final BlockPosition blockposition) {
            this.blockPos = blockposition;
            this.positionSource = new BlockPositionSource(blockposition);
        }

        @Override
        public int getListenerRadius() {
            return 8;
        }

        @Override
        public PositionSource getPositionSource() {
            return this.positionSource;
        }

        @Override
        public boolean canTriggerAvoidVibration() {
            return true;
        }

        @Override
        public boolean canReceiveVibration(WorldServer worldserver, BlockPosition blockposition, Holder<GameEvent> holder, @Nullable GameEvent.a gameevent_a) {
            return blockposition.equals(this.blockPos) && (holder.is((Holder) GameEvent.BLOCK_DESTROY) || holder.is((Holder) GameEvent.BLOCK_PLACE)) ? false : SculkSensorBlock.canActivate(SculkSensorBlockEntity.this.getBlockState());
        }

        @Override
        public void onReceiveVibration(WorldServer worldserver, BlockPosition blockposition, Holder<GameEvent> holder, @Nullable Entity entity, @Nullable Entity entity1, float f) {
            IBlockData iblockdata = SculkSensorBlockEntity.this.getBlockState();

            if (SculkSensorBlock.canActivate(iblockdata)) {
                SculkSensorBlockEntity.this.setLastVibrationFrequency(VibrationSystem.getGameEventFrequency(holder));
                int i = VibrationSystem.getRedstoneStrengthForDistance(f, this.getListenerRadius());
                Block block = iblockdata.getBlock();

                if (block instanceof SculkSensorBlock) {
                    SculkSensorBlock sculksensorblock = (SculkSensorBlock) block;

                    sculksensorblock.activate(entity, worldserver, this.blockPos, iblockdata, i, SculkSensorBlockEntity.this.getLastVibrationFrequency());
                }
            }

        }

        @Override
        public void onDataChanged() {
            SculkSensorBlockEntity.this.setChanged();
        }

        @Override
        public boolean requiresAdjacentChunksToBeTicking() {
            return true;
        }
    }
}
