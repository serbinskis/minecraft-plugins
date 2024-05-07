package net.minecraft.world.level.gameevent;

import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.Vec3D;

public record GameEvent(int notificationRadius) {

    public static final Holder.c<GameEvent> BLOCK_ACTIVATE = register("block_activate");
    public static final Holder.c<GameEvent> BLOCK_ATTACH = register("block_attach");
    public static final Holder.c<GameEvent> BLOCK_CHANGE = register("block_change");
    public static final Holder.c<GameEvent> BLOCK_CLOSE = register("block_close");
    public static final Holder.c<GameEvent> BLOCK_DEACTIVATE = register("block_deactivate");
    public static final Holder.c<GameEvent> BLOCK_DESTROY = register("block_destroy");
    public static final Holder.c<GameEvent> BLOCK_DETACH = register("block_detach");
    public static final Holder.c<GameEvent> BLOCK_OPEN = register("block_open");
    public static final Holder.c<GameEvent> BLOCK_PLACE = register("block_place");
    public static final Holder.c<GameEvent> CONTAINER_CLOSE = register("container_close");
    public static final Holder.c<GameEvent> CONTAINER_OPEN = register("container_open");
    public static final Holder.c<GameEvent> DRINK = register("drink");
    public static final Holder.c<GameEvent> EAT = register("eat");
    public static final Holder.c<GameEvent> ELYTRA_GLIDE = register("elytra_glide");
    public static final Holder.c<GameEvent> ENTITY_DAMAGE = register("entity_damage");
    public static final Holder.c<GameEvent> ENTITY_DIE = register("entity_die");
    public static final Holder.c<GameEvent> ENTITY_DISMOUNT = register("entity_dismount");
    public static final Holder.c<GameEvent> ENTITY_INTERACT = register("entity_interact");
    public static final Holder.c<GameEvent> ENTITY_MOUNT = register("entity_mount");
    public static final Holder.c<GameEvent> ENTITY_PLACE = register("entity_place");
    public static final Holder.c<GameEvent> ENTITY_ACTION = register("entity_action");
    public static final Holder.c<GameEvent> EQUIP = register("equip");
    public static final Holder.c<GameEvent> EXPLODE = register("explode");
    public static final Holder.c<GameEvent> FLAP = register("flap");
    public static final Holder.c<GameEvent> FLUID_PICKUP = register("fluid_pickup");
    public static final Holder.c<GameEvent> FLUID_PLACE = register("fluid_place");
    public static final Holder.c<GameEvent> HIT_GROUND = register("hit_ground");
    public static final Holder.c<GameEvent> INSTRUMENT_PLAY = register("instrument_play");
    public static final Holder.c<GameEvent> ITEM_INTERACT_FINISH = register("item_interact_finish");
    public static final Holder.c<GameEvent> ITEM_INTERACT_START = register("item_interact_start");
    public static final Holder.c<GameEvent> JUKEBOX_PLAY = register("jukebox_play", 10);
    public static final Holder.c<GameEvent> JUKEBOX_STOP_PLAY = register("jukebox_stop_play", 10);
    public static final Holder.c<GameEvent> LIGHTNING_STRIKE = register("lightning_strike");
    public static final Holder.c<GameEvent> NOTE_BLOCK_PLAY = register("note_block_play");
    public static final Holder.c<GameEvent> PRIME_FUSE = register("prime_fuse");
    public static final Holder.c<GameEvent> PROJECTILE_LAND = register("projectile_land");
    public static final Holder.c<GameEvent> PROJECTILE_SHOOT = register("projectile_shoot");
    public static final Holder.c<GameEvent> SCULK_SENSOR_TENDRILS_CLICKING = register("sculk_sensor_tendrils_clicking");
    public static final Holder.c<GameEvent> SHEAR = register("shear");
    public static final Holder.c<GameEvent> SHRIEK = register("shriek", 32);
    public static final Holder.c<GameEvent> SPLASH = register("splash");
    public static final Holder.c<GameEvent> STEP = register("step");
    public static final Holder.c<GameEvent> SWIM = register("swim");
    public static final Holder.c<GameEvent> TELEPORT = register("teleport");
    public static final Holder.c<GameEvent> UNEQUIP = register("unequip");
    public static final Holder.c<GameEvent> RESONATE_1 = register("resonate_1");
    public static final Holder.c<GameEvent> RESONATE_2 = register("resonate_2");
    public static final Holder.c<GameEvent> RESONATE_3 = register("resonate_3");
    public static final Holder.c<GameEvent> RESONATE_4 = register("resonate_4");
    public static final Holder.c<GameEvent> RESONATE_5 = register("resonate_5");
    public static final Holder.c<GameEvent> RESONATE_6 = register("resonate_6");
    public static final Holder.c<GameEvent> RESONATE_7 = register("resonate_7");
    public static final Holder.c<GameEvent> RESONATE_8 = register("resonate_8");
    public static final Holder.c<GameEvent> RESONATE_9 = register("resonate_9");
    public static final Holder.c<GameEvent> RESONATE_10 = register("resonate_10");
    public static final Holder.c<GameEvent> RESONATE_11 = register("resonate_11");
    public static final Holder.c<GameEvent> RESONATE_12 = register("resonate_12");
    public static final Holder.c<GameEvent> RESONATE_13 = register("resonate_13");
    public static final Holder.c<GameEvent> RESONATE_14 = register("resonate_14");
    public static final Holder.c<GameEvent> RESONATE_15 = register("resonate_15");
    public static final int DEFAULT_NOTIFICATION_RADIUS = 16;

    public static Holder<GameEvent> bootstrap(IRegistry<GameEvent> iregistry) {
        return GameEvent.BLOCK_ACTIVATE;
    }

    private static Holder.c<GameEvent> register(String s) {
        return register(s, 16);
    }

    private static Holder.c<GameEvent> register(String s, int i) {
        return IRegistry.registerForHolder(BuiltInRegistries.GAME_EVENT, new MinecraftKey(s), new GameEvent(i));
    }

    public static final class b implements Comparable<GameEvent.b> {

        private final Holder<GameEvent> gameEvent;
        private final Vec3D source;
        private final GameEvent.a context;
        private final GameEventListener recipient;
        private final double distanceToRecipient;

        public b(Holder<GameEvent> holder, Vec3D vec3d, GameEvent.a gameevent_a, GameEventListener gameeventlistener, Vec3D vec3d1) {
            this.gameEvent = holder;
            this.source = vec3d;
            this.context = gameevent_a;
            this.recipient = gameeventlistener;
            this.distanceToRecipient = vec3d.distanceToSqr(vec3d1);
        }

        public int compareTo(GameEvent.b gameevent_b) {
            return Double.compare(this.distanceToRecipient, gameevent_b.distanceToRecipient);
        }

        public Holder<GameEvent> gameEvent() {
            return this.gameEvent;
        }

        public Vec3D source() {
            return this.source;
        }

        public GameEvent.a context() {
            return this.context;
        }

        public GameEventListener recipient() {
            return this.recipient;
        }
    }

    public static record a(@Nullable Entity sourceEntity, @Nullable IBlockData affectedState) {

        public static GameEvent.a of(@Nullable Entity entity) {
            return new GameEvent.a(entity, (IBlockData) null);
        }

        public static GameEvent.a of(@Nullable IBlockData iblockdata) {
            return new GameEvent.a((Entity) null, iblockdata);
        }

        public static GameEvent.a of(@Nullable Entity entity, @Nullable IBlockData iblockdata) {
            return new GameEvent.a(entity, iblockdata);
        }
    }
}
