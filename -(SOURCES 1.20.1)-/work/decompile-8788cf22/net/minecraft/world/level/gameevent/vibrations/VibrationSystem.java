package net.minecraft.world.level.gameevent.vibrations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Optional;
import java.util.function.ToIntFunction;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.particles.VibrationParticleOption;
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
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.Vec3D;

public interface VibrationSystem {

    GameEvent[] RESONANCE_EVENTS = new GameEvent[]{GameEvent.RESONATE_1, GameEvent.RESONATE_2, GameEvent.RESONATE_3, GameEvent.RESONATE_4, GameEvent.RESONATE_5, GameEvent.RESONATE_6, GameEvent.RESONATE_7, GameEvent.RESONATE_8, GameEvent.RESONATE_9, GameEvent.RESONATE_10, GameEvent.RESONATE_11, GameEvent.RESONATE_12, GameEvent.RESONATE_13, GameEvent.RESONATE_14, GameEvent.RESONATE_15};
    ToIntFunction<GameEvent> VIBRATION_FREQUENCY_FOR_EVENT = (ToIntFunction) SystemUtils.make(new Object2IntOpenHashMap(), (object2intopenhashmap) -> {
        object2intopenhashmap.defaultReturnValue(0);
        object2intopenhashmap.put(GameEvent.STEP, 1);
        object2intopenhashmap.put(GameEvent.SWIM, 1);
        object2intopenhashmap.put(GameEvent.FLAP, 1);
        object2intopenhashmap.put(GameEvent.PROJECTILE_LAND, 2);
        object2intopenhashmap.put(GameEvent.HIT_GROUND, 2);
        object2intopenhashmap.put(GameEvent.SPLASH, 2);
        object2intopenhashmap.put(GameEvent.ITEM_INTERACT_FINISH, 3);
        object2intopenhashmap.put(GameEvent.PROJECTILE_SHOOT, 3);
        object2intopenhashmap.put(GameEvent.INSTRUMENT_PLAY, 3);
        object2intopenhashmap.put(GameEvent.ENTITY_ROAR, 4);
        object2intopenhashmap.put(GameEvent.ENTITY_SHAKE, 4);
        object2intopenhashmap.put(GameEvent.ELYTRA_GLIDE, 4);
        object2intopenhashmap.put(GameEvent.ENTITY_DISMOUNT, 5);
        object2intopenhashmap.put(GameEvent.EQUIP, 5);
        object2intopenhashmap.put(GameEvent.ENTITY_INTERACT, 6);
        object2intopenhashmap.put(GameEvent.SHEAR, 6);
        object2intopenhashmap.put(GameEvent.ENTITY_MOUNT, 6);
        object2intopenhashmap.put(GameEvent.ENTITY_DAMAGE, 7);
        object2intopenhashmap.put(GameEvent.DRINK, 8);
        object2intopenhashmap.put(GameEvent.EAT, 8);
        object2intopenhashmap.put(GameEvent.CONTAINER_CLOSE, 9);
        object2intopenhashmap.put(GameEvent.BLOCK_CLOSE, 9);
        object2intopenhashmap.put(GameEvent.BLOCK_DEACTIVATE, 9);
        object2intopenhashmap.put(GameEvent.BLOCK_DETACH, 9);
        object2intopenhashmap.put(GameEvent.CONTAINER_OPEN, 10);
        object2intopenhashmap.put(GameEvent.BLOCK_OPEN, 10);
        object2intopenhashmap.put(GameEvent.BLOCK_ACTIVATE, 10);
        object2intopenhashmap.put(GameEvent.BLOCK_ATTACH, 10);
        object2intopenhashmap.put(GameEvent.PRIME_FUSE, 10);
        object2intopenhashmap.put(GameEvent.NOTE_BLOCK_PLAY, 10);
        object2intopenhashmap.put(GameEvent.BLOCK_CHANGE, 11);
        object2intopenhashmap.put(GameEvent.BLOCK_DESTROY, 12);
        object2intopenhashmap.put(GameEvent.FLUID_PICKUP, 12);
        object2intopenhashmap.put(GameEvent.BLOCK_PLACE, 13);
        object2intopenhashmap.put(GameEvent.FLUID_PLACE, 13);
        object2intopenhashmap.put(GameEvent.ENTITY_PLACE, 14);
        object2intopenhashmap.put(GameEvent.LIGHTNING_STRIKE, 14);
        object2intopenhashmap.put(GameEvent.TELEPORT, 14);
        object2intopenhashmap.put(GameEvent.ENTITY_DIE, 15);
        object2intopenhashmap.put(GameEvent.EXPLODE, 15);

        for (int i = 1; i <= 15; ++i) {
            object2intopenhashmap.put(getResonanceEventByFrequency(i), i);
        }

    });

    VibrationSystem.a getVibrationData();

    VibrationSystem.d getVibrationUser();

    static int getGameEventFrequency(GameEvent gameevent) {
        return VibrationSystem.VIBRATION_FREQUENCY_FOR_EVENT.applyAsInt(gameevent);
    }

    static GameEvent getResonanceEventByFrequency(int i) {
        return VibrationSystem.RESONANCE_EVENTS[i - 1];
    }

    static int getRedstoneStrengthForDistance(float f, int i) {
        double d0 = 15.0D / (double) i;

        return Math.max(1, 15 - MathHelper.floor(d0 * (double) f));
    }

    public interface d {

        int getListenerRadius();

        PositionSource getPositionSource();

        boolean canReceiveVibration(WorldServer worldserver, BlockPosition blockposition, GameEvent gameevent, GameEvent.a gameevent_a);

        void onReceiveVibration(WorldServer worldserver, BlockPosition blockposition, GameEvent gameevent, @Nullable Entity entity, @Nullable Entity entity1, float f);

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

        default boolean isValidVibration(GameEvent gameevent, GameEvent.a gameevent_a) {
            if (!gameevent.is(this.getListenableEvents())) {
                return false;
            } else {
                Entity entity = gameevent_a.sourceEntity();

                if (entity != null) {
                    if (entity.isSpectator()) {
                        return false;
                    }

                    if (entity.isSteppingCarefully() && gameevent.is(GameEventTags.IGNORE_VIBRATIONS_SNEAKING)) {
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
            if (world instanceof WorldServer) {
                WorldServer worldserver = (WorldServer) world;

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

            for (int i = chunkcoordintpair.x - 1; i < chunkcoordintpair.x + 1; ++i) {
                for (int j = chunkcoordintpair.z - 1; j < chunkcoordintpair.z + 1; ++j) {
                    Chunk chunk = world.getChunkSource().getChunkNow(i, j);

                    if (chunk == null || !world.shouldTickBlocksAt(chunk.getPos().toLong())) {
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
        public boolean handleGameEvent(WorldServer worldserver, GameEvent gameevent, GameEvent.a gameevent_a, Vec3D vec3d) {
            VibrationSystem.a vibrationsystem_a = this.system.getVibrationData();
            VibrationSystem.d vibrationsystem_d = this.system.getVibrationUser();

            if (vibrationsystem_a.getCurrentVibration() != null) {
                return false;
            } else if (!vibrationsystem_d.isValidVibration(gameevent, gameevent_a)) {
                return false;
            } else {
                Optional<Vec3D> optional = vibrationsystem_d.getPositionSource().getPosition(worldserver);

                if (optional.isEmpty()) {
                    return false;
                } else {
                    Vec3D vec3d1 = (Vec3D) optional.get();

                    if (!vibrationsystem_d.canReceiveVibration(worldserver, BlockPosition.containing(vec3d), gameevent, gameevent_a)) {
                        return false;
                    } else if (isOccluded(worldserver, vec3d, vec3d1)) {
                        return false;
                    } else {
                        this.scheduleVibration(worldserver, vibrationsystem_a, gameevent, gameevent_a, vec3d, vec3d1);
                        return true;
                    }
                }
            }
        }

        public void forceScheduleVibration(WorldServer worldserver, GameEvent gameevent, GameEvent.a gameevent_a, Vec3D vec3d) {
            this.system.getVibrationUser().getPositionSource().getPosition(worldserver).ifPresent((vec3d1) -> {
                this.scheduleVibration(worldserver, this.system.getVibrationData(), gameevent, gameevent_a, vec3d, vec3d1);
            });
        }

        private void scheduleVibration(WorldServer worldserver, VibrationSystem.a vibrationsystem_a, GameEvent gameevent, GameEvent.a gameevent_a, Vec3D vec3d, Vec3D vec3d1) {
            vibrationsystem_a.selectionStrategy.addCandidate(new VibrationInfo(gameevent, (float) vec3d.distanceTo(vec3d1), vec3d, gameevent_a.sourceEntity()), worldserver.getGameTime());
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
            return instance.group(VibrationInfo.CODEC.optionalFieldOf("event").forGetter((vibrationsystem_a) -> {
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
