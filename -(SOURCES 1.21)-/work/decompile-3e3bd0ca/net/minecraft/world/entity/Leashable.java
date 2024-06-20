package net.minecraft.world.entity;

import com.mojang.datafixers.util.Either;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.GameProfileSerializer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.protocol.game.PacketPlayOutAttachEntity;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.decoration.EntityLeash;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.IMaterial;
import net.minecraft.world.level.World;

public interface Leashable {

    String LEASH_TAG = "leash";
    double LEASH_TOO_FAR_DIST = 10.0D;
    double LEASH_ELASTIC_DIST = 6.0D;

    @Nullable
    Leashable.a getLeashData();

    void setLeashData(@Nullable Leashable.a leashable_a);

    default boolean isLeashed() {
        return this.getLeashData() != null && this.getLeashData().leashHolder != null;
    }

    default boolean mayBeLeashed() {
        return this.getLeashData() != null;
    }

    default boolean canHaveALeashAttachedToIt() {
        return this.canBeLeashed() && !this.isLeashed();
    }

    default boolean canBeLeashed() {
        return true;
    }

    default void setDelayedLeashHolderId(int i) {
        this.setLeashData(new Leashable.a(i));
        dropLeash((Entity) this, false, false);
    }

    @Nullable
    default Leashable.a readLeashData(NBTTagCompound nbttagcompound) {
        if (nbttagcompound.contains("leash", 10)) {
            return new Leashable.a(Either.left(nbttagcompound.getCompound("leash").getUUID("UUID")));
        } else {
            if (nbttagcompound.contains("leash", 11)) {
                Either<UUID, BlockPosition> either = (Either) GameProfileSerializer.readBlockPos(nbttagcompound, "leash").map(Either::right).orElse((Object) null);

                if (either != null) {
                    return new Leashable.a(either);
                }
            }

            return null;
        }
    }

    default void writeLeashData(NBTTagCompound nbttagcompound, @Nullable Leashable.a leashable_a) {
        if (leashable_a != null) {
            Either<UUID, BlockPosition> either = leashable_a.delayedLeashInfo;
            Entity entity = leashable_a.leashHolder;

            if (entity instanceof EntityLeash) {
                EntityLeash entityleash = (EntityLeash) entity;

                either = Either.right(entityleash.getPos());
            } else if (leashable_a.leashHolder != null) {
                either = Either.left(leashable_a.leashHolder.getUUID());
            }

            if (either != null) {
                nbttagcompound.put("leash", (NBTBase) either.map((uuid) -> {
                    NBTTagCompound nbttagcompound1 = new NBTTagCompound();

                    nbttagcompound1.putUUID("UUID", uuid);
                    return nbttagcompound1;
                }, GameProfileSerializer::writeBlockPos));
            }
        }
    }

    private static <E extends Entity & Leashable> void restoreLeashFromSave(E e0, Leashable.a leashable_a) {
        if (leashable_a.delayedLeashInfo != null) {
            World world = e0.level();

            if (world instanceof WorldServer) {
                WorldServer worldserver = (WorldServer) world;
                Optional<UUID> optional = leashable_a.delayedLeashInfo.left();
                Optional<BlockPosition> optional1 = leashable_a.delayedLeashInfo.right();

                if (optional.isPresent()) {
                    Entity entity = worldserver.getEntity((UUID) optional.get());

                    if (entity != null) {
                        setLeashedTo(e0, entity, true);
                        return;
                    }
                } else if (optional1.isPresent()) {
                    setLeashedTo(e0, EntityLeash.getOrCreateKnot(worldserver, (BlockPosition) optional1.get()), true);
                    return;
                }

                if (e0.tickCount > 100) {
                    e0.spawnAtLocation((IMaterial) Items.LEAD);
                    ((Leashable) e0).setLeashData((Leashable.a) null);
                }
            }
        }

    }

    default void dropLeash(boolean flag, boolean flag1) {
        dropLeash((Entity) this, flag, flag1);
    }

    private static <E extends Entity & Leashable> void dropLeash(E e0, boolean flag, boolean flag1) {
        Leashable.a leashable_a = ((Leashable) e0).getLeashData();

        if (leashable_a != null && leashable_a.leashHolder != null) {
            ((Leashable) e0).setLeashData((Leashable.a) null);
            if (!e0.level().isClientSide && flag1) {
                e0.spawnAtLocation((IMaterial) Items.LEAD);
            }

            if (flag) {
                World world = e0.level();

                if (world instanceof WorldServer) {
                    WorldServer worldserver = (WorldServer) world;

                    worldserver.getChunkSource().broadcast(e0, new PacketPlayOutAttachEntity(e0, (Entity) null));
                }
            }
        }

    }

    static <E extends Entity & Leashable> void tickLeash(E e0) {
        Leashable.a leashable_a = ((Leashable) e0).getLeashData();

        if (leashable_a != null && leashable_a.delayedLeashInfo != null) {
            restoreLeashFromSave(e0, leashable_a);
        }

        if (leashable_a != null && leashable_a.leashHolder != null) {
            if (!e0.isAlive() || !leashable_a.leashHolder.isAlive()) {
                dropLeash(e0, true, true);
            }

            Entity entity = ((Leashable) e0).getLeashHolder();

            if (entity != null && entity.level() == e0.level()) {
                float f = e0.distanceTo(entity);

                if (!((Leashable) e0).handleLeashAtDistance(entity, f)) {
                    return;
                }

                if ((double) f > 10.0D) {
                    ((Leashable) e0).leashTooFarBehaviour();
                } else if ((double) f > 6.0D) {
                    ((Leashable) e0).elasticRangeLeashBehaviour(entity, f);
                    e0.checkSlowFallDistance();
                } else {
                    ((Leashable) e0).closeRangeLeashBehaviour(entity);
                }
            }

        }
    }

    default boolean handleLeashAtDistance(Entity entity, float f) {
        return true;
    }

    default void leashTooFarBehaviour() {
        this.dropLeash(true, true);
    }

    default void closeRangeLeashBehaviour(Entity entity) {}

    default void elasticRangeLeashBehaviour(Entity entity, float f) {
        legacyElasticRangeLeashBehaviour((Entity) this, entity, f);
    }

    private static <E extends Entity & Leashable> void legacyElasticRangeLeashBehaviour(E e0, Entity entity, float f) {
        double d0 = (entity.getX() - e0.getX()) / (double) f;
        double d1 = (entity.getY() - e0.getY()) / (double) f;
        double d2 = (entity.getZ() - e0.getZ()) / (double) f;

        e0.setDeltaMovement(e0.getDeltaMovement().add(Math.copySign(d0 * d0 * 0.4D, d0), Math.copySign(d1 * d1 * 0.4D, d1), Math.copySign(d2 * d2 * 0.4D, d2)));
    }

    default void setLeashedTo(Entity entity, boolean flag) {
        setLeashedTo((Entity) this, entity, flag);
    }

    private static <E extends Entity & Leashable> void setLeashedTo(E e0, Entity entity, boolean flag) {
        Leashable.a leashable_a = ((Leashable) e0).getLeashData();

        if (leashable_a == null) {
            leashable_a = new Leashable.a(entity);
            ((Leashable) e0).setLeashData(leashable_a);
        } else {
            leashable_a.setLeashHolder(entity);
        }

        if (flag) {
            World world = e0.level();

            if (world instanceof WorldServer) {
                WorldServer worldserver = (WorldServer) world;

                worldserver.getChunkSource().broadcast(e0, new PacketPlayOutAttachEntity(e0, entity));
            }
        }

        if (e0.isPassenger()) {
            e0.stopRiding();
        }

    }

    @Nullable
    default Entity getLeashHolder() {
        return getLeashHolder((Entity) this);
    }

    @Nullable
    private static <E extends Entity & Leashable> Entity getLeashHolder(E e0) {
        Leashable.a leashable_a = ((Leashable) e0).getLeashData();

        if (leashable_a == null) {
            return null;
        } else {
            if (leashable_a.delayedLeashHolderId != 0 && e0.level().isClientSide) {
                Entity entity = e0.level().getEntity(leashable_a.delayedLeashHolderId);

                if (entity instanceof Entity) {
                    leashable_a.setLeashHolder(entity);
                }
            }

            return leashable_a.leashHolder;
        }
    }

    public static final class a {

        int delayedLeashHolderId;
        @Nullable
        public Entity leashHolder;
        @Nullable
        public Either<UUID, BlockPosition> delayedLeashInfo;

        a(Either<UUID, BlockPosition> either) {
            this.delayedLeashInfo = either;
        }

        a(Entity entity) {
            this.leashHolder = entity;
        }

        a(int i) {
            this.delayedLeashHolderId = i;
        }

        public void setLeashHolder(Entity entity) {
            this.leashHolder = entity;
            this.delayedLeashInfo = null;
            this.delayedLeashHolderId = 0;
        }
    }
}
