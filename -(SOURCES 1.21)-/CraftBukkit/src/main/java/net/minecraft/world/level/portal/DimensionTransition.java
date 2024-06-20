package net.minecraft.world.level.portal;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.game.PacketPlayOutWorldEvent;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3D;

// CraftBukkit start
import org.bukkit.event.player.PlayerTeleportEvent;

public record DimensionTransition(WorldServer newLevel, Vec3D pos, Vec3D speed, float yRot, float xRot, boolean missingRespawnBlock, DimensionTransition.a postDimensionTransition, PlayerTeleportEvent.TeleportCause cause) {

    public DimensionTransition(WorldServer newLevel, Vec3D pos, Vec3D speed, float yRot, float xRot, boolean missingRespawnBlock, DimensionTransition.a postDimensionTransition) {
        this(newLevel, pos, speed, yRot, xRot, missingRespawnBlock, postDimensionTransition, PlayerTeleportEvent.TeleportCause.UNKNOWN);
    }

    public DimensionTransition(PlayerTeleportEvent.TeleportCause cause) {
        this(null, Vec3D.ZERO, Vec3D.ZERO, 0.0F, 0.0F, false, DO_NOTHING, cause);
    }
    // CraftBukkit end

    public static final DimensionTransition.a DO_NOTHING = (entity) -> {
    };
    public static final DimensionTransition.a PLAY_PORTAL_SOUND = DimensionTransition::playPortalSound;
    public static final DimensionTransition.a PLACE_PORTAL_TICKET = DimensionTransition::placePortalTicket;

    public DimensionTransition(WorldServer worldserver, Vec3D vec3d, Vec3D vec3d1, float f, float f1, DimensionTransition.a dimensiontransition_a) {
        // CraftBukkit start
        this(worldserver, vec3d, vec3d1, f, f1, dimensiontransition_a, PlayerTeleportEvent.TeleportCause.UNKNOWN);
    }

    public DimensionTransition(WorldServer worldserver, Vec3D vec3d, Vec3D vec3d1, float f, float f1, DimensionTransition.a dimensiontransition_a, PlayerTeleportEvent.TeleportCause cause) {
        this(worldserver, vec3d, vec3d1, f, f1, false, dimensiontransition_a, cause);
    }

    public DimensionTransition(WorldServer worldserver, Entity entity, DimensionTransition.a dimensiontransition_a) {
        this(worldserver, entity, dimensiontransition_a, PlayerTeleportEvent.TeleportCause.UNKNOWN);
    }

    public DimensionTransition(WorldServer worldserver, Entity entity, DimensionTransition.a dimensiontransition_a, PlayerTeleportEvent.TeleportCause cause) {
        this(worldserver, findAdjustedSharedSpawnPos(worldserver, entity), Vec3D.ZERO, 0.0F, 0.0F, false, dimensiontransition_a, cause);
        // CraftBukkit end
    }

    private static void playPortalSound(Entity entity) {
        if (entity instanceof EntityPlayer entityplayer) {
            entityplayer.connection.send(new PacketPlayOutWorldEvent(1032, BlockPosition.ZERO, 0, false));
        }

    }

    private static void placePortalTicket(Entity entity) {
        entity.placePortalTicket(BlockPosition.containing(entity.position()));
    }

    public static DimensionTransition missingRespawnBlock(WorldServer worldserver, Entity entity, DimensionTransition.a dimensiontransition_a) {
        return new DimensionTransition(worldserver, findAdjustedSharedSpawnPos(worldserver, entity), Vec3D.ZERO, 0.0F, 0.0F, true, dimensiontransition_a);
    }

    private static Vec3D findAdjustedSharedSpawnPos(WorldServer worldserver, Entity entity) {
        return entity.adjustSpawnLocation(worldserver, worldserver.getSharedSpawnPos()).getBottomCenter();
    }

    @FunctionalInterface
    public interface a {

        void onTransition(Entity entity);

        default DimensionTransition.a then(DimensionTransition.a dimensiontransition_a) {
            return (entity) -> {
                this.onTransition(entity);
                dimensiontransition_a.onTransition(entity);
            };
        }
    }
}
