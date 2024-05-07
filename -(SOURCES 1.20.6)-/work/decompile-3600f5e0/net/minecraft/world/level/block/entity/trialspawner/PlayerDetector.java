package net.minecraft.world.level.block.entity.trialspawner;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.RayTrace;
import net.minecraft.world.level.World;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public interface PlayerDetector {

    PlayerDetector NO_CREATIVE_PLAYERS = (worldserver, playerdetector_a, blockposition, d0, flag) -> {
        return playerdetector_a.getPlayers(worldserver, (entityhuman) -> {
            return entityhuman.blockPosition().closerThan(blockposition, d0) && !entityhuman.isCreative() && !entityhuman.isSpectator();
        }).stream().filter((entityhuman) -> {
            return !flag || inLineOfSight(worldserver, blockposition.getCenter(), entityhuman.getEyePosition());
        }).map(Entity::getUUID).toList();
    };
    PlayerDetector INCLUDING_CREATIVE_PLAYERS = (worldserver, playerdetector_a, blockposition, d0, flag) -> {
        return playerdetector_a.getPlayers(worldserver, (entityhuman) -> {
            return entityhuman.blockPosition().closerThan(blockposition, d0) && !entityhuman.isSpectator();
        }).stream().filter((entityhuman) -> {
            return !flag || inLineOfSight(worldserver, blockposition.getCenter(), entityhuman.getEyePosition());
        }).map(Entity::getUUID).toList();
    };
    PlayerDetector SHEEP = (worldserver, playerdetector_a, blockposition, d0, flag) -> {
        AxisAlignedBB axisalignedbb = (new AxisAlignedBB(blockposition)).inflate(d0);

        return playerdetector_a.getEntities(worldserver, EntityTypes.SHEEP, axisalignedbb, EntityLiving::isAlive).stream().filter((entitysheep) -> {
            return !flag || inLineOfSight(worldserver, blockposition.getCenter(), entitysheep.getEyePosition());
        }).map(Entity::getUUID).toList();
    };

    List<UUID> detect(WorldServer worldserver, PlayerDetector.a playerdetector_a, BlockPosition blockposition, double d0, boolean flag);

    private static boolean inLineOfSight(World world, Vec3D vec3d, Vec3D vec3d1) {
        MovingObjectPositionBlock movingobjectpositionblock = world.clip(new RayTrace(vec3d1, vec3d, RayTrace.BlockCollisionOption.VISUAL, RayTrace.FluidCollisionOption.NONE, VoxelShapeCollision.empty()));

        return movingobjectpositionblock.getBlockPos().equals(BlockPosition.containing(vec3d)) || movingobjectpositionblock.getType() == MovingObjectPosition.EnumMovingObjectType.MISS;
    }

    public interface a {

        PlayerDetector.a SELECT_FROM_LEVEL = new PlayerDetector.a() {
            @Override
            public List<EntityPlayer> getPlayers(WorldServer worldserver, Predicate<? super EntityHuman> predicate) {
                return worldserver.getPlayers(predicate);
            }

            @Override
            public <T extends Entity> List<T> getEntities(WorldServer worldserver, EntityTypeTest<Entity, T> entitytypetest, AxisAlignedBB axisalignedbb, Predicate<? super T> predicate) {
                return worldserver.getEntities(entitytypetest, axisalignedbb, predicate);
            }
        };

        List<? extends EntityHuman> getPlayers(WorldServer worldserver, Predicate<? super EntityHuman> predicate);

        <T extends Entity> List<T> getEntities(WorldServer worldserver, EntityTypeTest<Entity, T> entitytypetest, AxisAlignedBB axisalignedbb, Predicate<? super T> predicate);

        static PlayerDetector.a onlySelectPlayer(EntityHuman entityhuman) {
            return onlySelectPlayers(List.of(entityhuman));
        }

        static PlayerDetector.a onlySelectPlayers(final List<EntityHuman> list) {
            return new PlayerDetector.a() {
                @Override
                public List<EntityHuman> getPlayers(WorldServer worldserver, Predicate<? super EntityHuman> predicate) {
                    return list.stream().filter(predicate).toList();
                }

                @Override
                public <T extends Entity> List<T> getEntities(WorldServer worldserver, EntityTypeTest<Entity, T> entitytypetest, AxisAlignedBB axisalignedbb, Predicate<? super T> predicate) {
                    Stream stream = list.stream();

                    Objects.requireNonNull(entitytypetest);
                    return stream.map(entitytypetest::tryCast).filter(Objects::nonNull).filter(predicate).toList();
                }
            };
        }
    }
}
