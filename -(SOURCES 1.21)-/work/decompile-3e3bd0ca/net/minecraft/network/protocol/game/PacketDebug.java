package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.GameTestAddMarkerDebugPayload;
import net.minecraft.network.protocol.common.custom.GameTestClearMarkersDebugPayload;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.INamableTileEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.behavior.BehaviorPositionEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorTarget;
import net.minecraft.world.entity.ai.goal.PathfinderGoalSelector;
import net.minecraft.world.entity.ai.memory.ExpirableMemory;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryTarget;
import net.minecraft.world.entity.animal.EntityBee;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.TileEntityBeehive;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.pathfinder.PathEntity;
import net.minecraft.world.phys.Vec3D;
import org.slf4j.Logger;

public class PacketDebug {

    private static final Logger LOGGER = LogUtils.getLogger();

    public PacketDebug() {}

    public static void sendGameTestAddMarker(WorldServer worldserver, BlockPosition blockposition, String s, int i, int j) {
        sendPacketToAllPlayers(worldserver, new GameTestAddMarkerDebugPayload(blockposition, i, s, j));
    }

    public static void sendGameTestClearPacket(WorldServer worldserver) {
        sendPacketToAllPlayers(worldserver, new GameTestClearMarkersDebugPayload());
    }

    public static void sendPoiPacketsForChunk(WorldServer worldserver, ChunkCoordIntPair chunkcoordintpair) {}

    public static void sendPoiAddedPacket(WorldServer worldserver, BlockPosition blockposition) {
        sendVillageSectionsPacket(worldserver, blockposition);
    }

    public static void sendPoiRemovedPacket(WorldServer worldserver, BlockPosition blockposition) {
        sendVillageSectionsPacket(worldserver, blockposition);
    }

    public static void sendPoiTicketCountPacket(WorldServer worldserver, BlockPosition blockposition) {
        sendVillageSectionsPacket(worldserver, blockposition);
    }

    private static void sendVillageSectionsPacket(WorldServer worldserver, BlockPosition blockposition) {}

    public static void sendPathFindingPacket(World world, EntityInsentient entityinsentient, @Nullable PathEntity pathentity, float f) {}

    public static void sendNeighborsUpdatePacket(World world, BlockPosition blockposition) {}

    public static void sendStructurePacket(GeneratorAccessSeed generatoraccessseed, StructureStart structurestart) {}

    public static void sendGoalSelector(World world, EntityInsentient entityinsentient, PathfinderGoalSelector pathfindergoalselector) {}

    public static void sendRaids(WorldServer worldserver, Collection<Raid> collection) {}

    public static void sendEntityBrain(EntityLiving entityliving) {}

    public static void sendBeeInfo(EntityBee entitybee) {}

    public static void sendBreezeInfo(Breeze breeze) {}

    public static void sendGameEventInfo(World world, Holder<GameEvent> holder, Vec3D vec3d) {}

    public static void sendGameEventListenerInfo(World world, GameEventListener gameeventlistener) {}

    public static void sendHiveInfo(World world, BlockPosition blockposition, IBlockData iblockdata, TileEntityBeehive tileentitybeehive) {}

    private static List<String> getMemoryDescriptions(EntityLiving entityliving, long i) {
        Map<MemoryModuleType<?>, Optional<? extends ExpirableMemory<?>>> map = entityliving.getBrain().getMemories();
        List<String> list = Lists.newArrayList();
        Iterator iterator = map.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<MemoryModuleType<?>, Optional<? extends ExpirableMemory<?>>> entry = (Entry) iterator.next();
            MemoryModuleType<?> memorymoduletype = (MemoryModuleType) entry.getKey();
            Optional<? extends ExpirableMemory<?>> optional = (Optional) entry.getValue();
            String s;

            if (optional.isPresent()) {
                ExpirableMemory<?> expirablememory = (ExpirableMemory) optional.get();
                Object object = expirablememory.getValue();

                if (memorymoduletype == MemoryModuleType.HEARD_BELL_TIME) {
                    long j = i - (Long) object;

                    s = "" + j + " ticks ago";
                } else if (expirablememory.canExpire()) {
                    String s1 = getShortDescription((WorldServer) entityliving.level(), object);

                    s = s1 + " (ttl: " + expirablememory.getTimeToLive() + ")";
                } else {
                    s = getShortDescription((WorldServer) entityliving.level(), object);
                }
            } else {
                s = "-";
            }

            String s2 = BuiltInRegistries.MEMORY_MODULE_TYPE.getKey(memorymoduletype).getPath();

            list.add(s2 + ": " + s);
        }

        list.sort(String::compareTo);
        return list;
    }

    private static String getShortDescription(WorldServer worldserver, @Nullable Object object) {
        if (object == null) {
            return "-";
        } else if (object instanceof UUID) {
            return getShortDescription(worldserver, worldserver.getEntity((UUID) object));
        } else {
            Entity entity;

            if (object instanceof EntityLiving) {
                entity = (Entity) object;
                return DebugEntityNameGenerator.getEntityName(entity);
            } else if (object instanceof INamableTileEntity) {
                return ((INamableTileEntity) object).getName().getString();
            } else if (object instanceof MemoryTarget) {
                return getShortDescription(worldserver, ((MemoryTarget) object).getTarget());
            } else if (object instanceof BehaviorPositionEntity) {
                return getShortDescription(worldserver, ((BehaviorPositionEntity) object).getEntity());
            } else if (object instanceof GlobalPos) {
                return getShortDescription(worldserver, ((GlobalPos) object).pos());
            } else if (object instanceof BehaviorTarget) {
                return getShortDescription(worldserver, ((BehaviorTarget) object).currentBlockPosition());
            } else if (object instanceof DamageSource) {
                entity = ((DamageSource) object).getEntity();
                return entity == null ? object.toString() : getShortDescription(worldserver, entity);
            } else if (!(object instanceof Collection)) {
                return object.toString();
            } else {
                List<String> list = Lists.newArrayList();
                Iterator iterator = ((Iterable) object).iterator();

                while (iterator.hasNext()) {
                    Object object1 = iterator.next();

                    list.add(getShortDescription(worldserver, object1));
                }

                return list.toString();
            }
        }
    }

    private static void sendPacketToAllPlayers(WorldServer worldserver, CustomPacketPayload custompacketpayload) {
        Packet<?> packet = new ClientboundCustomPayloadPacket(custompacketpayload);
        Iterator iterator = worldserver.players().iterator();

        while (iterator.hasNext()) {
            EntityPlayer entityplayer = (EntityPlayer) iterator.next();

            entityplayer.connection.send(packet);
        }

    }
}
