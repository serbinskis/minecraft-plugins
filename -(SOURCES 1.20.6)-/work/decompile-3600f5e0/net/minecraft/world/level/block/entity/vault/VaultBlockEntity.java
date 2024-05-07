package net.minecraft.world.level.block.entity.vault;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleParam;
import net.minecraft.core.particles.Particles;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketListenerPlayOut;
import net.minecraft.network.protocol.game.PacketPlayOutTileEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.stats.StatisticList;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameterSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;
import net.minecraft.world.phys.Vec3D;
import org.slf4j.Logger;

public class VaultBlockEntity extends TileEntity {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final VaultServerData serverData = new VaultServerData();
    private final VaultSharedData sharedData = new VaultSharedData();
    private final VaultClientData clientData = new VaultClientData();
    private VaultConfig config;

    public VaultBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        super(TileEntityTypes.VAULT, blockposition, iblockdata);
        this.config = VaultConfig.DEFAULT;
    }

    @Nullable
    @Override
    public Packet<PacketListenerPlayOut> getUpdatePacket() {
        return PacketPlayOutTileEntityData.create(this);
    }

    @Override
    public NBTTagCompound getUpdateTag(HolderLookup.a holderlookup_a) {
        return (NBTTagCompound) SystemUtils.make(new NBTTagCompound(), (nbttagcompound) -> {
            nbttagcompound.put("shared_data", encode(VaultSharedData.CODEC, this.sharedData, holderlookup_a));
        });
    }

    @Override
    protected void saveAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        super.saveAdditional(nbttagcompound, holderlookup_a);
        nbttagcompound.put("config", encode(VaultConfig.CODEC, this.config, holderlookup_a));
        nbttagcompound.put("shared_data", encode(VaultSharedData.CODEC, this.sharedData, holderlookup_a));
        nbttagcompound.put("server_data", encode(VaultServerData.CODEC, this.serverData, holderlookup_a));
    }

    private static <T> NBTBase encode(Codec<T> codec, T t0, HolderLookup.a holderlookup_a) {
        return (NBTBase) codec.encodeStart(holderlookup_a.createSerializationContext(DynamicOpsNBT.INSTANCE), t0).getOrThrow();
    }

    @Override
    protected void loadAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        super.loadAdditional(nbttagcompound, holderlookup_a);
        DynamicOps<NBTBase> dynamicops = holderlookup_a.createSerializationContext(DynamicOpsNBT.INSTANCE);
        DataResult dataresult;
        Logger logger;
        Optional optional;

        if (nbttagcompound.contains("server_data")) {
            dataresult = VaultServerData.CODEC.parse(dynamicops, nbttagcompound.get("server_data"));
            logger = VaultBlockEntity.LOGGER;
            Objects.requireNonNull(logger);
            optional = dataresult.resultOrPartial(logger::error);
            VaultServerData vaultserverdata = this.serverData;

            Objects.requireNonNull(this.serverData);
            optional.ifPresent(vaultserverdata::set);
        }

        if (nbttagcompound.contains("config")) {
            dataresult = VaultConfig.CODEC.parse(dynamicops, nbttagcompound.get("config"));
            logger = VaultBlockEntity.LOGGER;
            Objects.requireNonNull(logger);
            dataresult.resultOrPartial(logger::error).ifPresent((vaultconfig) -> {
                this.config = vaultconfig;
            });
        }

        if (nbttagcompound.contains("shared_data")) {
            dataresult = VaultSharedData.CODEC.parse(dynamicops, nbttagcompound.get("shared_data"));
            logger = VaultBlockEntity.LOGGER;
            Objects.requireNonNull(logger);
            optional = dataresult.resultOrPartial(logger::error);
            VaultSharedData vaultshareddata = this.sharedData;

            Objects.requireNonNull(this.sharedData);
            optional.ifPresent(vaultshareddata::set);
        }

    }

    @Nullable
    public VaultServerData getServerData() {
        return this.level != null && !this.level.isClientSide ? this.serverData : null;
    }

    public VaultSharedData getSharedData() {
        return this.sharedData;
    }

    public VaultClientData getClientData() {
        return this.clientData;
    }

    public VaultConfig getConfig() {
        return this.config;
    }

    @VisibleForTesting
    public void setConfig(VaultConfig vaultconfig) {
        this.config = vaultconfig;
    }

    public static final class a {

        private static final int PARTICLE_TICK_RATE = 20;
        private static final float IDLE_PARTICLE_CHANCE = 0.5F;
        private static final float AMBIENT_SOUND_CHANCE = 0.02F;
        private static final int ACTIVATION_PARTICLE_COUNT = 20;
        private static final int DEACTIVATION_PARTICLE_COUNT = 20;

        public a() {}

        public static void tick(World world, BlockPosition blockposition, IBlockData iblockdata, VaultClientData vaultclientdata, VaultSharedData vaultshareddata) {
            vaultclientdata.updateDisplayItemSpin();
            if (world.getGameTime() % 20L == 0L) {
                emitConnectionParticlesForNearbyPlayers(world, blockposition, iblockdata, vaultshareddata);
            }

            emitIdleParticles(world, blockposition, vaultshareddata, (Boolean) iblockdata.getValue(VaultBlock.OMINOUS) ? Particles.SOUL_FIRE_FLAME : Particles.SMALL_FLAME);
            playIdleSounds(world, blockposition, vaultshareddata);
        }

        public static void emitActivationParticles(World world, BlockPosition blockposition, IBlockData iblockdata, VaultSharedData vaultshareddata, ParticleParam particleparam) {
            emitConnectionParticlesForNearbyPlayers(world, blockposition, iblockdata, vaultshareddata);
            RandomSource randomsource = world.random;

            for (int i = 0; i < 20; ++i) {
                Vec3D vec3d = randomPosInsideCage(blockposition, randomsource);

                world.addParticle(Particles.SMOKE, vec3d.x(), vec3d.y(), vec3d.z(), 0.0D, 0.0D, 0.0D);
                world.addParticle(particleparam, vec3d.x(), vec3d.y(), vec3d.z(), 0.0D, 0.0D, 0.0D);
            }

        }

        public static void emitDeactivationParticles(World world, BlockPosition blockposition, ParticleParam particleparam) {
            RandomSource randomsource = world.random;

            for (int i = 0; i < 20; ++i) {
                Vec3D vec3d = randomPosCenterOfCage(blockposition, randomsource);
                Vec3D vec3d1 = new Vec3D(randomsource.nextGaussian() * 0.02D, randomsource.nextGaussian() * 0.02D, randomsource.nextGaussian() * 0.02D);

                world.addParticle(particleparam, vec3d.x(), vec3d.y(), vec3d.z(), vec3d1.x(), vec3d1.y(), vec3d1.z());
            }

        }

        private static void emitIdleParticles(World world, BlockPosition blockposition, VaultSharedData vaultshareddata, ParticleParam particleparam) {
            RandomSource randomsource = world.getRandom();

            if (randomsource.nextFloat() <= 0.5F) {
                Vec3D vec3d = randomPosInsideCage(blockposition, randomsource);

                world.addParticle(Particles.SMOKE, vec3d.x(), vec3d.y(), vec3d.z(), 0.0D, 0.0D, 0.0D);
                if (shouldDisplayActiveEffects(vaultshareddata)) {
                    world.addParticle(particleparam, vec3d.x(), vec3d.y(), vec3d.z(), 0.0D, 0.0D, 0.0D);
                }
            }

        }

        private static void emitConnectionParticlesForPlayer(World world, Vec3D vec3d, EntityHuman entityhuman) {
            RandomSource randomsource = world.random;
            Vec3D vec3d1 = vec3d.vectorTo(entityhuman.position().add(0.0D, (double) (entityhuman.getBbHeight() / 2.0F), 0.0D));
            int i = MathHelper.nextInt(randomsource, 2, 5);

            for (int j = 0; j < i; ++j) {
                Vec3D vec3d2 = vec3d1.offsetRandom(randomsource, 1.0F);

                world.addParticle(Particles.VAULT_CONNECTION, vec3d.x(), vec3d.y(), vec3d.z(), vec3d2.x(), vec3d2.y(), vec3d2.z());
            }

        }

        private static void emitConnectionParticlesForNearbyPlayers(World world, BlockPosition blockposition, IBlockData iblockdata, VaultSharedData vaultshareddata) {
            Set<UUID> set = vaultshareddata.getConnectedPlayers();

            if (!set.isEmpty()) {
                Vec3D vec3d = keyholePos(blockposition, (EnumDirection) iblockdata.getValue(VaultBlock.FACING));
                Iterator iterator = set.iterator();

                while (iterator.hasNext()) {
                    UUID uuid = (UUID) iterator.next();
                    EntityHuman entityhuman = world.getPlayerByUUID(uuid);

                    if (entityhuman != null && isWithinConnectionRange(blockposition, vaultshareddata, entityhuman)) {
                        emitConnectionParticlesForPlayer(world, vec3d, entityhuman);
                    }
                }

            }
        }

        private static boolean isWithinConnectionRange(BlockPosition blockposition, VaultSharedData vaultshareddata, EntityHuman entityhuman) {
            return entityhuman.blockPosition().distSqr(blockposition) <= MathHelper.square(vaultshareddata.connectedParticlesRange());
        }

        private static void playIdleSounds(World world, BlockPosition blockposition, VaultSharedData vaultshareddata) {
            if (shouldDisplayActiveEffects(vaultshareddata)) {
                RandomSource randomsource = world.getRandom();

                if (randomsource.nextFloat() <= 0.02F) {
                    world.playLocalSound(blockposition, SoundEffects.VAULT_AMBIENT, SoundCategory.BLOCKS, randomsource.nextFloat() * 0.25F + 0.75F, randomsource.nextFloat() + 0.5F, false);
                }

            }
        }

        public static boolean shouldDisplayActiveEffects(VaultSharedData vaultshareddata) {
            return vaultshareddata.hasDisplayItem();
        }

        private static Vec3D randomPosCenterOfCage(BlockPosition blockposition, RandomSource randomsource) {
            return Vec3D.atLowerCornerOf(blockposition).add(MathHelper.nextDouble(randomsource, 0.4D, 0.6D), MathHelper.nextDouble(randomsource, 0.4D, 0.6D), MathHelper.nextDouble(randomsource, 0.4D, 0.6D));
        }

        private static Vec3D randomPosInsideCage(BlockPosition blockposition, RandomSource randomsource) {
            return Vec3D.atLowerCornerOf(blockposition).add(MathHelper.nextDouble(randomsource, 0.1D, 0.9D), MathHelper.nextDouble(randomsource, 0.25D, 0.75D), MathHelper.nextDouble(randomsource, 0.1D, 0.9D));
        }

        private static Vec3D keyholePos(BlockPosition blockposition, EnumDirection enumdirection) {
            return Vec3D.atBottomCenterOf(blockposition).add((double) enumdirection.getStepX() * 0.5D, 1.75D, (double) enumdirection.getStepZ() * 0.5D);
        }
    }

    public static final class b {

        private static final int UNLOCKING_DELAY_TICKS = 14;
        private static final int DISPLAY_CYCLE_TICK_RATE = 20;
        private static final int INSERT_FAIL_SOUND_BUFFER_TICKS = 15;

        public b() {}

        public static void tick(WorldServer worldserver, BlockPosition blockposition, IBlockData iblockdata, VaultConfig vaultconfig, VaultServerData vaultserverdata, VaultSharedData vaultshareddata) {
            VaultState vaultstate = (VaultState) iblockdata.getValue(VaultBlock.STATE);

            if (shouldCycleDisplayItem(worldserver.getGameTime(), vaultstate)) {
                cycleDisplayItemFromLootTable(worldserver, vaultstate, vaultconfig, vaultshareddata, blockposition);
            }

            IBlockData iblockdata1 = iblockdata;

            if (worldserver.getGameTime() >= vaultserverdata.stateUpdatingResumesAt()) {
                iblockdata1 = (IBlockData) iblockdata.setValue(VaultBlock.STATE, vaultstate.tickAndGetNext(worldserver, blockposition, vaultconfig, vaultserverdata, vaultshareddata));
                if (!iblockdata.equals(iblockdata1)) {
                    setVaultState(worldserver, blockposition, iblockdata, iblockdata1, vaultconfig, vaultshareddata);
                }
            }

            if (vaultserverdata.isDirty || vaultshareddata.isDirty) {
                VaultBlockEntity.setChanged(worldserver, blockposition, iblockdata);
                if (vaultshareddata.isDirty) {
                    worldserver.sendBlockUpdated(blockposition, iblockdata, iblockdata1, 2);
                }

                vaultserverdata.isDirty = false;
                vaultshareddata.isDirty = false;
            }

        }

        public static void tryInsertKey(WorldServer worldserver, BlockPosition blockposition, IBlockData iblockdata, VaultConfig vaultconfig, VaultServerData vaultserverdata, VaultSharedData vaultshareddata, EntityHuman entityhuman, ItemStack itemstack) {
            VaultState vaultstate = (VaultState) iblockdata.getValue(VaultBlock.STATE);

            if (canEjectReward(vaultconfig, vaultstate)) {
                if (!isValidToInsert(vaultconfig, itemstack)) {
                    playInsertFailSound(worldserver, vaultserverdata, blockposition);
                } else if (vaultserverdata.hasRewardedPlayer(entityhuman)) {
                    playInsertFailSound(worldserver, vaultserverdata, blockposition);
                } else {
                    List<ItemStack> list = resolveItemsToEject(worldserver, vaultconfig, blockposition, entityhuman);

                    if (!list.isEmpty()) {
                        entityhuman.awardStat(StatisticList.ITEM_USED.get(itemstack.getItem()));
                        if (!entityhuman.isCreative()) {
                            itemstack.shrink(vaultconfig.keyItem().getCount());
                        }

                        unlock(worldserver, iblockdata, blockposition, vaultconfig, vaultserverdata, vaultshareddata, list);
                        vaultserverdata.addToRewardedPlayers(entityhuman);
                        vaultshareddata.updateConnectedPlayersWithinRange(worldserver, blockposition, vaultserverdata, vaultconfig, vaultconfig.deactivationRange());
                    }
                }
            }
        }

        static void setVaultState(WorldServer worldserver, BlockPosition blockposition, IBlockData iblockdata, IBlockData iblockdata1, VaultConfig vaultconfig, VaultSharedData vaultshareddata) {
            VaultState vaultstate = (VaultState) iblockdata.getValue(VaultBlock.STATE);
            VaultState vaultstate1 = (VaultState) iblockdata1.getValue(VaultBlock.STATE);

            worldserver.setBlock(blockposition, iblockdata1, 3);
            vaultstate.onTransition(worldserver, blockposition, vaultstate1, vaultconfig, vaultshareddata, (Boolean) iblockdata1.getValue(VaultBlock.OMINOUS));
        }

        static void cycleDisplayItemFromLootTable(WorldServer worldserver, VaultState vaultstate, VaultConfig vaultconfig, VaultSharedData vaultshareddata, BlockPosition blockposition) {
            if (!canEjectReward(vaultconfig, vaultstate)) {
                vaultshareddata.setDisplayItem(ItemStack.EMPTY);
            } else {
                ItemStack itemstack = getRandomDisplayItemFromLootTable(worldserver, blockposition, (ResourceKey) vaultconfig.overrideLootTableToDisplay().orElse(vaultconfig.lootTable()));

                vaultshareddata.setDisplayItem(itemstack);
            }
        }

        private static ItemStack getRandomDisplayItemFromLootTable(WorldServer worldserver, BlockPosition blockposition, ResourceKey<LootTable> resourcekey) {
            LootTable loottable = worldserver.getServer().reloadableRegistries().getLootTable(resourcekey);
            LootParams lootparams = (new LootParams.a(worldserver)).withParameter(LootContextParameters.ORIGIN, Vec3D.atCenterOf(blockposition)).create(LootContextParameterSets.VAULT);
            List<ItemStack> list = loottable.getRandomItems(lootparams);

            return list.isEmpty() ? ItemStack.EMPTY : (ItemStack) SystemUtils.getRandom((List) list, worldserver.getRandom());
        }

        private static void unlock(WorldServer worldserver, IBlockData iblockdata, BlockPosition blockposition, VaultConfig vaultconfig, VaultServerData vaultserverdata, VaultSharedData vaultshareddata, List<ItemStack> list) {
            vaultserverdata.setItemsToEject(list);
            vaultshareddata.setDisplayItem(vaultserverdata.getNextItemToEject());
            vaultserverdata.pauseStateUpdatingUntil(worldserver.getGameTime() + 14L);
            setVaultState(worldserver, blockposition, iblockdata, (IBlockData) iblockdata.setValue(VaultBlock.STATE, VaultState.UNLOCKING), vaultconfig, vaultshareddata);
        }

        private static List<ItemStack> resolveItemsToEject(WorldServer worldserver, VaultConfig vaultconfig, BlockPosition blockposition, EntityHuman entityhuman) {
            LootTable loottable = worldserver.getServer().reloadableRegistries().getLootTable(vaultconfig.lootTable());
            LootParams lootparams = (new LootParams.a(worldserver)).withParameter(LootContextParameters.ORIGIN, Vec3D.atCenterOf(blockposition)).withLuck(entityhuman.getLuck()).withParameter(LootContextParameters.THIS_ENTITY, entityhuman).create(LootContextParameterSets.VAULT);

            return loottable.getRandomItems(lootparams);
        }

        private static boolean canEjectReward(VaultConfig vaultconfig, VaultState vaultstate) {
            return vaultconfig.lootTable() != LootTables.EMPTY && !vaultconfig.keyItem().isEmpty() && vaultstate != VaultState.INACTIVE;
        }

        private static boolean isValidToInsert(VaultConfig vaultconfig, ItemStack itemstack) {
            return ItemStack.isSameItemSameComponents(itemstack, vaultconfig.keyItem()) && itemstack.getCount() >= vaultconfig.keyItem().getCount();
        }

        private static boolean shouldCycleDisplayItem(long i, VaultState vaultstate) {
            return i % 20L == 0L && vaultstate == VaultState.ACTIVE;
        }

        private static void playInsertFailSound(WorldServer worldserver, VaultServerData vaultserverdata, BlockPosition blockposition) {
            if (worldserver.getGameTime() >= vaultserverdata.getLastInsertFailTimestamp() + 15L) {
                worldserver.playSound((EntityHuman) null, blockposition, SoundEffects.VAULT_INSERT_ITEM_FAIL, SoundCategory.BLOCKS);
                vaultserverdata.setLastInsertFailTimestamp(worldserver.getGameTime());
            }

        }
    }
}
