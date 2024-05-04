package net.minecraft.world.level.gameevent.vibrations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.ToIntFunction;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.World;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.Vec3D;

public interface VibrationSystem {

    List<ResourceKey<GameEvent>> RESONANCE_EVENTS = List.of(GameEvent.RESONATE_1.key(), GameEvent.RESONATE_2.key(), GameEvent.RESONATE_3.key(), GameEvent.RESONATE_4.key(), GameEvent.RESONATE_5.key(), GameEvent.RESONATE_6.key(), GameEvent.RESONATE_7.key(), GameEvent.RESONATE_8.key(), GameEvent.RESONATE_9.key(), GameEvent.RESONATE_10.key(), GameEvent.RESONATE_11.key(), GameEvent.RESONATE_12.key(), GameEvent.RESONATE_13.key(), GameEvent.RESONATE_14.key(), GameEvent.RESONATE_15.key());
    int DEFAULT_VIBRATION_FREQUENCY = 0;
    ToIntFunction<ResourceKey<GameEvent>> VIBRATION_FREQUENCY_FOR_EVENT = (ToIntFunction) SystemUtils.make(new Reference2IntOpenHashMap(), (reference2intopenhashmap) -> {
        reference2intopenhashmap.defaultReturnValue(0);
        reference2intopenhashmap.put(GameEvent.STEP.key(), 1);
        reference2intopenhashmap.put(GameEvent.SWIM.key(), 1);
        reference2intopenhashmap.put(GameEvent.FLAP.key(), 1);
        reference2intopenhashmap.put(GameEvent.PROJECTILE_LAND.key(), 2);
        reference2intopenhashmap.put(GameEvent.HIT_GROUND.key(), 2);
        reference2intopenhashmap.put(GameEvent.SPLASH.key(), 2);
        reference2intopenhashmap.put(GameEvent.ITEM_INTERACT_FINISH.key(), 3);
        reference2intopenhashmap.put(GameEvent.PROJECTILE_SHOOT.key(), 3);
        reference2intopenhashmap.put(GameEvent.INSTRUMENT_PLAY.key(), 3);
        reference2intopenhashmap.put(GameEvent.ENTITY_ACTION.key(), 4);
        reference2intopenhashmap.put(GameEvent.ELYTRA_GLIDE.key(), 4);
        reference2intopenhashmap.put(GameEvent.UNEQUIP.key(), 4);
        reference2intopenhashmap.put(GameEvent.ENTITY_DISMOUNT.key(), 5);
        reference2intopenhashmap.put(GameEvent.EQUIP.key(), 5);
        reference2intopenhashmap.put(GameEvent.ENTITY_INTERACT.key(), 6);
        reference2intopenhashmap.put(GameEvent.SHEAR.key(), 6);
        reference2intopenhashmap.put(GameEvent.ENTITY_MOUNT.key(), 6);
        reference2intopenhashmap.put(GameEvent.ENTITY_DAMAGE.key(), 7);
        reference2intopenhashmap.put(GameEvent.DRINK.key(), 8);
        reference2intopenhashmap.put(GameEvent.EAT.key(), 8);
        reference2intopenhashmap.put(GameEvent.CONTAINER_CLOSE.key(), 9);
        reference2intopenhashmap.put(GameEvent.BLOCK_CLOSE.key(), 9);
        reference2intopenhashmap.put(GameEvent.BLOCK_DEACTIVATE.key(), 9);
        reference2intopenhashmap.put(GameEvent.BLOCK_DETACH.key(), 9);
        reference2intopenhashmap.put(GameEvent.CONTAINER_OPEN.key(), 10);
        reference2intopenhashmap.put(GameEvent.BLOCK_OPEN.key(), 10);
        reference2intopenhashmap.put(GameEvent.BLOCK_ACTIVATE.key(), 10);
        reference2intopenhashmap.put(GameEvent.BLOCK_ATTACH.key(), 10);
        reference2intopenhashmap.put(GameEvent.PRIME_FUSE.key(), 10);
        reference2intopenhashmap.put(GameEvent.NOTE_BLOCK_PLAY.key(), 10);
        reference2intopenhashmap.put(GameEvent.BLOCK_CHANGE.key(), 11);
        reference2intopenhashmap.put(GameEvent.BLOCK_DESTROY.key(), 12);
        reference2intopenhashmap.put(GameEvent.FLUID_PICKUP.key(), 12);
        reference2intopenhashmap.put(GameEvent.BLOCK_PLACE.key(), 13);
        reference2intopenhashmap.put(GameEvent.FLUID_PLACE.key(), 13);
        reference2intopenhashmap.put(GameEvent.ENTITY_PLACE.key(), 14);
        reference2intopenhashmap.put(GameEvent.LIGHTNING_STRIKE.key(), 14);
        reference2intopenhashmap.put(GameEvent.TELEPORT.key(), 14);
        reference2intopenhashmap.put(GameEvent.ENTITY_DIE.key(), 15);
        reference2intopenhashmap.put(GameEvent.EXPLODE.key(), 15);

        for (int i = 1; i <= 15; ++i) {
            reference2intopenhashmap.put(getResonanceEventByFrequency(i), i);
        }

    });

    VibrationSystem.a getVibrationData();

    VibrationSystem.d getVibrationUser();

    static int getGameEventFrequency(Holder<GameEvent> holder) {
        return (Integer) holder.unwrapKey().map(VibrationSystem::getGameEventFrequency).orElse(0);
    }

    static int getGameEventFrequency(ResourceKey<GameEvent> resourcekey) {
        return VibrationSystem.VIBRATION_FREQUENCY_FOR_EVENT.applyAsInt(resourcekey);
    }

    static ResourceKey<GameEvent> getResonanceEventByFrequency(int i) {
        return (ResourceKey) VibrationSystem.RESONANCE_EVENTS.get(i - 1);
    }

    static int getRedstoneStrengthForDistance(float f, int i) {
        double d0 = 15.0D / (double) i;

        return Math.max(1, 15 - MathHelper.floor(d0 * (double) f));
    }

    public interface d {

        int getListenerRadius();

        PositionSource getPositionSource();

        boolean canReceiveVibration(WorldServer worldserver, BlockPosition blockposition, Holder<GameEvent> holder, GameEvent.a gameevent_a);

        void onReceiveVibration(WorldServer worldserver, BlockPosition blockposition, Holder<GameEvent> holder, @Nullable Entity entity, @Nullable Entity entity1, float f);

        default TagKey<GameEvent> getListenableEvents() {
            return GameEventTags.VIBRATIONS;
        }

        default boolean canTriggerAvoidVibration() {
            return false;
        }

        default boolean requiresAdjacentChunksToBeTicking() {
            return false;
        }

        default int calculateTravelTimeInTicks(float f) {
            return MathHelper.floor(f);
        }

        default boolean isValidVibration(Holder<GameEvent> holder, GameEvent.a gameevent_a) {
            if (!holder.is(this.getListenableEvents())) {
                return false;
            } else {
                Entity entity = gameevent_a.sourceEntity();

                if (entity != null) {
                    if (entity.isSpectator()) {
                        return false;
                    }

                    if (entity.isSteppingCarefully() && holder.is(GameEventTags.IGNORE_VIBRATIONS_SNEAKING)) {
                        if (this.canTriggerAvoidVibration() && entity instanceof EntityPlayer) {
                            EntityPlayer entityplayer = (EntityPlayer) entity;

                            CriterionTriggers.AVOID_VIBRATION.trigger(entityplayer);
                        }

                        return false;
                    }

                    if (entity.dampensVibrations()) {
                        return false;
                    }
                }

                return gameevent_a.affectedState() != null ? !gameevent_a.affectedState().is(TagsBlock.DAMPENS_VIBRATIONS) : true;
            }
        }

        default void onDataChanged() {}
    }

    public interface c {

        static void tick(World world, VibrationSystem.a vibrationsystem_a, VibrationSystem.d vibrationsystem_d) {
            if (world instanceof WorldServer worldserver) {
                if (vibrationsystem_a.currentVibration == null) {
                    trySelectAndScheduleVibration(worldserver, vibrationsystem_a, vibrationsystem_d);
                }

                if (vibrationsystem_a.currentVibration != null) {
                    boolean flag = vibrationsystem_a.getTravelTimeInTicks() > 0;

                    tryReloadVibrationParticle(worldserver, vibrationsystem_a, vibrationsystem_d);
                    vibrationsystem_a.decrementTravelTime();
                    if (vibrationsystem_a.getTravelTimeInTicks() <= 0) {
                        flag = receiveVibration(worldserver, vibrationsystem_a, vibrationsystem_d, vibrationsystem_a.currentVibration);
                    }

                    if (flag) {
                        vibrationsystem_d.onDataChanged();
                    }

                }
            }
        }

        private static void trySelectAndScheduleVibration(WorldServer worldserver, VibrationSystem.a vibrationsystem_a, VibrationSystem.d vibrationsystem_d) {
            vibrationsystem_a.getSelectionStrategy().chosenCandidate(worldserver.getGameTime()).ifPresent((vibrationinfo) -> {
                vibrationsystem_a.setCurrentVibration(vibrationinfo);
                Vec3D vec3d = vibrationinfo.pos();

                vibrationsystem_a.setTravelTimeInTicks(vibrationsystem_d.calculateTravelTimeInTicks(vibrationinfo.distance()));
                worldserver.sendParticles(new VibrationParticleOption(vibrationsystem_d.getPositionSource(), vibrationsystem_a.getTravelTimeInTicks()), vec3d.x, vec3d.y, vec3d.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                vibrationsystem_d.onDataChanged();
                vibrationsystem_a.getSelectionStrategy().startOver();
            });
        }

        private static void tryReloadVibrationParticle(WorldServer worldserver, VibrationSystem.a vibrationsystem_a, VibrationSystem.d vibrationsystem_d) {
            if (vibrationsystem_a.shouldReloadVibrationParticle()) {
                if (vibrationsystem_a.currentVibration == null) {
                    vibrationsystem_a.setReloadVibrationParticle(false);
                } else {
                    Vec3D vec3d = vibrationsystem_a.currentVibration.pos();
                    PositionSource positionsource = vibrationsystem_d.getPositionSource();
                    Vec3D vec3d1 = (Vec3D) positionsource.getPosition(worldserver).orElse(vec3d);
                    int i = vibrationsystem_a.getTravelTimeInTicks();
                    int j = vibrationsystem_d.calculateTravelTimeInTicks(vibrationsystem_a.currentVibration.distance());
                    double d0 = 1.0D - (double) i / (double) j;
                    double d1 = MathHelper.lerp(d0, vec3d.x, vec3d1.x);
                    double d2 = MathHelper.lerp(d0, vec3d.y, vec3d1.y);
                    double d3 = MathHelper.lerp(d0, vec3d.z, vec3d1.z);
                    boolean flag = worldserver.sendParticles(new VibrationParticleOption(positionsource, i), d1, d2, d3, 1, 0.0D, 0.0D, 0.0D, 0.0D) > 0;

                    if (flag) {
                        vibrationsystem_a.setReloadVibrationParticle(false);
                    }

                }
            }
        }

        private static boolean receiveVibration(WorldServer worldserver, VibrationSystem.a vibrationsystem_a, VibrationSystem.d vibrationsystem_d, VibrationInfo vibrationinfo) {
            BlockPosition blockposition = BlockPosition.containing(vibrationinfo.pos());
            BlockPosition blockposition1 = (BlockPosition) vibrationsystem_d.getPositionSource().getPosition(worldserver).map(BlockPosition::containing).orElse(blockposition);

            if (vibrationsystem_d.requiresAdjacentChunksToBeTicking() && !areAdjacentChunksTicking(worldserver, blockposition1)) {
                return false;
            } else {
                vibrationsystem_d.onReceiveVibration(worldserver, blockposition, vibrationinfo.gameEvent(), (Entity) vibrationinfo.getEntity(worldserver).orElse((Object) null), (Entity) vibrationinfo.getProjectileOwner(worldserver).orElse((Object) null), VibrationSystem.b.distanceBetweenInBlocks(blockposition, blockposition1));
                vibrationsystem_a.setCurrentVibration((VibrationInfo) null);
                return true;
            }
        }

        private static boolean areAdjacentChunksTicking(World world, BlockPosition blockposition) {
            ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(blockposition);

            for (int i = chunkcoordintpair.x - 1; i <= chunkcoordintpair.x + 1; ++i) {
                for (int j = chunkcoordintpair.z - 1; j <= chunkcoordintpair.z + 1; ++j) {
                    if (!world.shouldTickBlocksAt(ChunkCoordIntPair.asLong(i, j)) || world.getChunkSource().getChunkNow(i, j) == null) {
                        return false;
                    }
                }
            }

            return true;
        }
    }

    public static class b implements GameEventListener {

        private final VibrationSystem system;

        public b(VibrationSystem vibrationsystem) {
            this.system = vibrationsystem;
        }

        @Override
        public PositionSource getListenerSource() {
            return this.system.getVibrationUser().getPositionSource();
        }

        @Override
        public int getListenerRadius() {
            return this.system.getVibrationUser().getListenerRadius();
        }

        @Override
        public boolean handleGameEvent(WorldServer worldserver, Holder<GameEvent> holder, GameEvent.a gameevent_a, Vec3D vec3d) {
            VibrationSystem.a vibrationsystem_a = this.system.getVibrationData();
            VibrationSystem.d vibrationsystem_d = this.system.getVibrationUser();

            if (vibrationsystem_a.getCurrentVibration() != null) {
                return false;
            } else if (!vibrationsystem_d.isValidVibration(holder, gameevent_a)) {
                return false;
            } else {
                Optional<Vec3D> optional = vibrationsystem_d.getPositionSource().getPosition(worldserver);

                if (optional.isEmpty()) {
                    return false;
                } else {
                    Vec3D vec3d1 = (Vec3D) optional.get();

                    if (!vibrationsystem_d.canReceiveVibration(worldserver, BlockPosition.containing(vec3d), holder, gameevent_a)) {
                        return false;
                    } else if (isOccluded(worldserver, vec3d, vec3d1)) {
                        return false;
                    } else {
                        this.scheduleVibration(worldserver, vibrationsystem_a, holder, gameevent_a, vec3d, vec3d1);
                        return true;
                    }
                }
            }
        }

        public void forceScheduleVibration(WorldServer worldserver, Holder<GameEvent> holder, GameEvent.a gameevent_a, Vec3D vec3d) {
            this.system.getVibrationUser().getPositionSource().getPosition(worldserver).ifPresent((vec3d1) -> {
                this.scheduleVibration(worldserver, this.system.getVibrationData(), holder, gameevent_a, vec3d, vec3d1);
            });
        }

        private void scheduleVibration(WorldServer worldserver, VibrationSystem.a vibrationsystem_a, Holder<GameEvent> holder, GameEvent.a gameevent_a, Vec3D vec3d, Vec3D vec3d1) {
            vibrationsystem_a.selectionStrategy.addCandidate(new VibrationInfo(holder, (float) vec3d.distanceTo(vec3d1), vec3d, gameevent_a.sourceEntity()), worldserver.getGameTime());
        }

        public static float distanceBetweenInBlocks(BlockPosition blockposition, BlockPosition blockposition1) {
            return (float) Math.sqrt(blockposition.distSqr(blockposition1));
        }

        private static boolean isOccluded(World world, Vec3D vec3d, Vec3D vec3d1) {
            Vec3D vec3d2 = new Vec3D((double) MathHelper.floor(vec3d.x) + 0.5D, (double) MathHelper.floor(vec3d.y) + 0.5D, (double) MathHelper.floor(vec3d.z) + 0.5D);
            Vec3D vec3d3 = new Vec3D((double) MathHelper.floor(vec3d1.x) + 0.5D, (double) MathHelper.floor(vec3d1.y) + 0.5D, (double) MathHelper.floor(vec3d1.z) + 0.5D);
            EnumDirection[] aenumdirection = EnumDirection.values();
            int i = aenumdirection.length;

            for (int j = 0; j < i; ++j) {
                EnumDirection enumdirection = aenumdirection[j];
                Vec3D vec3d4 = vec3d2.relative(enumdirection, 9.999999747378752E-6D);

                if (world.isBlockInLine(new ClipBlockStateContext(vec3d4, vec3d3, (iblockdata) -> {
                    return iblockdata.is(TagsBlock.OCCLUDES_VIBRATION_SIGNALS);
                })).getType() != MovingObjectPosition.EnumMovingObjectType.BLOCK) {
                    return false;
                }
            }

            return true;
        }
    }

    public static final class a {

        public static Codec<VibrationSystem.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(VibrationInfo.CODEC.lenientOptionalFieldOf("event").forGetter((vibrationsystem_a) -> {
                return Optional.ofNullable(vibrationsystem_a.currentVibration);
            }), VibrationSelector.CODEC.fieldOf("selector").forGetter(VibrationSystem.a::getSelectionStrategy), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("event_delay").orElse(0).forGetter(VibrationSystem.a::getTravelTimeInTicks)).apply(instance, (optional, vibrationselector, integer) -> {
                return new VibrationSystem.a((VibrationInfo) optional.orElse((Object) null), vibrationselector, integer, true);
            });
        });
        public static final String NBT_TAG_KEY = "listener";
        @Nullable
        VibrationInfo currentVibration;
        private int travelTimeInTicks;
        final VibrationSelector selectionStrategy;
        private boolean reloadVibrationParticle;

        private a(@Nullable VibrationInfo vibrationinfo, VibrationSelector vibrationselector, int i, boolean flag) {
            this.currentVibration = vibrationinfo;
            this.travelTimeInTicks = i;
            this.selectionStrategy = vibrationselector;
            this.reloadVibrationParticle = flag;
        }

        public a() {
            this((VibrationInfo) null, new VibrationSelector(), 0, false);
        }

        public VibrationSelector getSelectionStrategy() {
            return this.selectionStrategy;
        }

        @Nullable
        public VibrationInfo getCurrentVibration() {
            return this.currentVibration;
        }

        public void setCurrentVibration(@Nullable VibrationInfo vibrationinfo) {
            this.currentVibration = vibrationinfo;
        }

        public int getTravelTimeInTicks() {
            return this.travelTimeInTicks;
        }

        public void setTravelTimeInTicks(int i) {
            this.travelTimeInTicks = i;
        }

        public void decrementTravelTime() {
            this.travelTimeInTicks = Math.max(0, this.travelTimeInTicks - 1);
        }

        public boolean shouldReloadVibrationParticle() {
            return this.reloadVibrationParticle;
        }

        public void setReloadVibrationParticle(boolean flag) {
            this.reloadVibrationParticle = flag;
        }
    }
}
