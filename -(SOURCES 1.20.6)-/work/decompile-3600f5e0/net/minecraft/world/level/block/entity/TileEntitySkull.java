package net.minecraft.world.level.block.entity;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.logging.LogUtils;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutTileEntityData;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.Services;
import net.minecraft.util.UtilColor;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.BlockSkull;
import net.minecraft.world.level.block.state.IBlockData;
import org.slf4j.Logger;

public class TileEntitySkull extends TileEntity {

    private static final String TAG_PROFILE = "profile";
    private static final String TAG_NOTE_BLOCK_SOUND = "note_block_sound";
    private static final String TAG_CUSTOM_NAME = "custom_name";
    private static final Logger LOGGER = LogUtils.getLogger();
    @Nullable
    private static Executor mainThreadExecutor;
    @Nullable
    private static LoadingCache<String, CompletableFuture<Optional<GameProfile>>> profileCacheByName;
    @Nullable
    private static LoadingCache<UUID, CompletableFuture<Optional<GameProfile>>> profileCacheById;
    public static final Executor CHECKED_MAIN_THREAD_EXECUTOR = (runnable) -> {
        Executor executor = TileEntitySkull.mainThreadExecutor;

        if (executor != null) {
            executor.execute(runnable);
        }

    };
    @Nullable
    public ResolvableProfile owner;
    @Nullable
    public MinecraftKey noteBlockSound;
    private int animationTickCount;
    private boolean isAnimating;
    @Nullable
    private IChatBaseComponent customName;

    public TileEntitySkull(BlockPosition blockposition, IBlockData iblockdata) {
        super(TileEntityTypes.SKULL, blockposition, iblockdata);
    }

    public static void setup(final Services services, Executor executor) {
        TileEntitySkull.mainThreadExecutor = executor;
        final BooleanSupplier booleansupplier = () -> {
            return TileEntitySkull.profileCacheById == null;
        };

        TileEntitySkull.profileCacheByName = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(10L)).maximumSize(256L).build(new CacheLoader<String, CompletableFuture<Optional<GameProfile>>>() {
            public CompletableFuture<Optional<GameProfile>> load(String s) {
                return TileEntitySkull.fetchProfileByName(s, services);
            }
        });
        TileEntitySkull.profileCacheById = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(10L)).maximumSize(256L).build(new CacheLoader<UUID, CompletableFuture<Optional<GameProfile>>>() {
            public CompletableFuture<Optional<GameProfile>> load(UUID uuid) {
                return TileEntitySkull.fetchProfileById(uuid, services, booleansupplier);
            }
        });
    }

    static CompletableFuture<Optional<GameProfile>> fetchProfileByName(String s, Services services) {
        return services.profileCache().getAsync(s).thenCompose((optional) -> {
            LoadingCache<UUID, CompletableFuture<Optional<GameProfile>>> loadingcache = TileEntitySkull.profileCacheById;

            return loadingcache != null && !optional.isEmpty() ? ((CompletableFuture) loadingcache.getUnchecked(((GameProfile) optional.get()).getId())).thenApply((optional1) -> {
                return optional1.or(() -> {
                    return optional;
                });
            }) : CompletableFuture.completedFuture(Optional.empty());
        });
    }

    static CompletableFuture<Optional<GameProfile>> fetchProfileById(UUID uuid, Services services, BooleanSupplier booleansupplier) {
        return CompletableFuture.supplyAsync(() -> {
            if (booleansupplier.getAsBoolean()) {
                return Optional.empty();
            } else {
                ProfileResult profileresult = services.sessionService().fetchProfile(uuid, true);

                return Optional.ofNullable(profileresult).map(ProfileResult::profile);
            }
        }, SystemUtils.backgroundExecutor());
    }

    public static void clear() {
        TileEntitySkull.mainThreadExecutor = null;
        TileEntitySkull.profileCacheByName = null;
        TileEntitySkull.profileCacheById = null;
    }

    @Override
    protected void saveAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        super.saveAdditional(nbttagcompound, holderlookup_a);
        if (this.owner != null) {
            nbttagcompound.put("profile", (NBTBase) ResolvableProfile.CODEC.encodeStart(DynamicOpsNBT.INSTANCE, this.owner).getOrThrow());
        }

        if (this.noteBlockSound != null) {
            nbttagcompound.putString("note_block_sound", this.noteBlockSound.toString());
        }

        if (this.customName != null) {
            nbttagcompound.putString("custom_name", IChatBaseComponent.ChatSerializer.toJson(this.customName, holderlookup_a));
        }

    }

    @Override
    protected void loadAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        super.loadAdditional(nbttagcompound, holderlookup_a);
        if (nbttagcompound.contains("profile")) {
            ResolvableProfile.CODEC.parse(DynamicOpsNBT.INSTANCE, nbttagcompound.get("profile")).resultOrPartial((s) -> {
                TileEntitySkull.LOGGER.error("Failed to load profile from player head: {}", s);
            }).ifPresent(this::setOwner);
        }

        if (nbttagcompound.contains("note_block_sound", 8)) {
            this.noteBlockSound = MinecraftKey.tryParse(nbttagcompound.getString("note_block_sound"));
        }

        if (nbttagcompound.contains("custom_name", 8)) {
            this.customName = IChatBaseComponent.ChatSerializer.fromJson(nbttagcompound.getString("custom_name"), holderlookup_a);
        } else {
            this.customName = null;
        }

    }

    public static void animation(World world, BlockPosition blockposition, IBlockData iblockdata, TileEntitySkull tileentityskull) {
        if (iblockdata.hasProperty(BlockSkull.POWERED) && (Boolean) iblockdata.getValue(BlockSkull.POWERED)) {
            tileentityskull.isAnimating = true;
            ++tileentityskull.animationTickCount;
        } else {
            tileentityskull.isAnimating = false;
        }

    }

    public float getAnimation(float f) {
        return this.isAnimating ? (float) this.animationTickCount + f : (float) this.animationTickCount;
    }

    @Nullable
    public ResolvableProfile getOwnerProfile() {
        return this.owner;
    }

    @Nullable
    public MinecraftKey getNoteBlockSound() {
        return this.noteBlockSound;
    }

    @Override
    public PacketPlayOutTileEntityData getUpdatePacket() {
        return PacketPlayOutTileEntityData.create(this);
    }

    @Override
    public NBTTagCompound getUpdateTag(HolderLookup.a holderlookup_a) {
        return this.saveCustomOnly(holderlookup_a);
    }

    public void setOwner(@Nullable ResolvableProfile resolvableprofile) {
        synchronized (this) {
            this.owner = resolvableprofile;
        }

        this.updateOwnerProfile();
    }

    private void updateOwnerProfile() {
        if (this.owner != null && !this.owner.isResolved()) {
            this.owner.resolve().thenAcceptAsync((resolvableprofile) -> {
                this.owner = resolvableprofile;
                this.setChanged();
            }, TileEntitySkull.CHECKED_MAIN_THREAD_EXECUTOR);
        } else {
            this.setChanged();
        }
    }

    public static CompletableFuture<Optional<GameProfile>> fetchGameProfile(String s) {
        LoadingCache<String, CompletableFuture<Optional<GameProfile>>> loadingcache = TileEntitySkull.profileCacheByName;

        return loadingcache != null && UtilColor.isValidPlayerName(s) ? (CompletableFuture) loadingcache.getUnchecked(s) : CompletableFuture.completedFuture(Optional.empty());
    }

    public static CompletableFuture<Optional<GameProfile>> fetchGameProfile(UUID uuid) {
        LoadingCache<UUID, CompletableFuture<Optional<GameProfile>>> loadingcache = TileEntitySkull.profileCacheById;

        return loadingcache != null ? (CompletableFuture) loadingcache.getUnchecked(uuid) : CompletableFuture.completedFuture(Optional.empty());
    }

    @Override
    protected void applyImplicitComponents(TileEntity.b tileentity_b) {
        super.applyImplicitComponents(tileentity_b);
        this.setOwner((ResolvableProfile) tileentity_b.get(DataComponents.PROFILE));
        this.noteBlockSound = (MinecraftKey) tileentity_b.get(DataComponents.NOTE_BLOCK_SOUND);
        this.customName = (IChatBaseComponent) tileentity_b.get(DataComponents.CUSTOM_NAME);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.a datacomponentmap_a) {
        super.collectImplicitComponents(datacomponentmap_a);
        datacomponentmap_a.set(DataComponents.PROFILE, this.owner);
        datacomponentmap_a.set(DataComponents.NOTE_BLOCK_SOUND, this.noteBlockSound);
        datacomponentmap_a.set(DataComponents.CUSTOM_NAME, this.customName);
    }

    @Override
    public void removeComponentsFromTag(NBTTagCompound nbttagcompound) {
        super.removeComponentsFromTag(nbttagcompound);
        nbttagcompound.remove("profile");
        nbttagcompound.remove("note_block_sound");
        nbttagcompound.remove("custom_name");
    }
}
