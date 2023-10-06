package net.minecraft.world.level.pathfinder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3D;

public class PathEntity {

    private final List<PathPoint> nodes;
    @Nullable
    private PathEntity.a debugData;
    private int nextNodeIndex;
    private final BlockPosition target;
    private final float distToTarget;
    private final boolean reached;

    public PathEntity(List<PathPoint> list, BlockPosition blockposition, boolean flag) {
        this.nodes = list;
        this.target = blockposition;
        this.distToTarget = list.isEmpty() ? Float.MAX_VALUE : ((PathPoint) this.nodes.get(this.nodes.size() - 1)).distanceManhattan(this.target);
        this.reached = flag;
    }

    public void advance() {
        ++this.nextNodeIndex;
    }

    public boolean notStarted() {
        return this.nextNodeIndex <= 0;
    }

    public boolean isDone() {
        return this.nextNodeIndex >= this.nodes.size();
    }

    @Nullable
    public PathPoint getEndNode() {
        return !this.nodes.isEmpty() ? (PathPoint) this.nodes.get(this.nodes.size() - 1) : null;
    }

    public PathPoint getNode(int i) {
        return (PathPoint) this.nodes.get(i);
    }

    public void truncateNodes(int i) {
        if (this.nodes.size() > i) {
            this.nodes.subList(i, this.nodes.size()).clear();
        }

    }

    public void replaceNode(int i, PathPoint pathpoint) {
        this.nodes.set(i, pathpoint);
    }

    public int getNodeCount() {
        return this.nodes.size();
    }

    public int getNextNodeIndex() {
        return this.nextNodeIndex;
    }

    public void setNextNodeIndex(int i) {
        this.nextNodeIndex = i;
    }

    public Vec3D getEntityPosAtNode(Entity entity, int i) {
        PathPoint pathpoint = (PathPoint) this.nodes.get(i);
        double d0 = (double) pathpoint.x + (double) ((int) (entity.getBbWidth() + 1.0F)) * 0.5D;
        double d1 = (double) pathpoint.y;
        double d2 = (double) pathpoint.z + (double) ((int) (entity.getBbWidth() + 1.0F)) * 0.5D;

        return new Vec3D(d0, d1, d2);
    }

    public BlockPosition getNodePos(int i) {
        return ((PathPoint) this.nodes.get(i)).asBlockPos();
    }

    public Vec3D getNextEntityPos(Entity entity) {
        return this.getEntityPosAtNode(entity, this.nextNodeIndex);
    }

    public BlockPosition getNextNodePos() {
        return ((PathPoint) this.nodes.get(this.nextNodeIndex)).asBlockPos();
    }

    public PathPoint getNextNode() {
        return (PathPoint) this.nodes.get(this.nextNodeIndex);
    }

    @Nullable
    public PathPoint getPreviousNode() {
        return this.nextNodeIndex > 0 ? (PathPoint) this.nodes.get(this.nextNodeIndex - 1) : null;
    }

    public boolean sameAs(@Nullable PathEntity pathentity) {
        if (pathentity == null) {
            return false;
        } else if (pathentity.nodes.size() != this.nodes.size()) {
            return false;
        } else {
            for (int i = 0; i < this.nodes.size(); ++i) {
                PathPoint pathpoint = (PathPoint) this.nodes.get(i);
                PathPoint pathpoint1 = (PathPoint) pathentity.nodes.get(i);

                if (pathpoint.x != pathpoint1.x || pathpoint.y != pathpoint1.y || pathpoint.z != pathpoint1.z) {
                    return false;
                }
            }

            return true;
        }
    }

    public boolean canReach() {
        return this.reached;
    }

    @VisibleForDebug
    void setDebug(PathPoint[] apathpoint, PathPoint[] apathpoint1, Set<PathDestination> set) {
        this.debugData = new PathEntity.a(apathpoint, apathpoint1, set);
    }

    @Nullable
    public PathEntity.a debugData() {
        return this.debugData;
    }

    public void writeToStream(PacketDataSerializer packetdataserializer) {
        if (this.debugData != null && !this.debugData.targetNodes.isEmpty()) {
            packetdataserializer.writeBoolean(this.reached);
            packetdataserializer.writeInt(this.nextNodeIndex);
            packetdataserializer.writeBlockPos(this.target);
            packetdataserializer.writeCollection(this.nodes, (packetdataserializer1, pathpoint) -> {
                pathpoint.writeToStream(packetdataserializer1);
            });
            this.debugData.write(packetdataserializer);
        }
    }

    public static PathEntity createFromStream(PacketDataSerializer packetdataserializer) {
        boolean flag = packetdataserializer.readBoolean();
        int i = packetdataserializer.readInt();
        BlockPosition blockposition = packetdataserializer.readBlockPos();
        List<PathPoint> list = packetdataserializer.readList(PathPoint::createFromStream);
        PathEntity.a pathentity_a = PathEntity.a.read(packetdataserializer);
        PathEntity pathentity = new PathEntity(list, blockposition, flag);

        pathentity.debugData = pathentity_a;
        pathentity.nextNodeIndex = i;
        return pathentity;
    }

    public String toString() {
        return "Path(length=" + this.nodes.size() + ")";
    }

    public BlockPosition getTarget() {
        return this.target;
    }

    public float getDistToTarget() {
        return this.distToTarget;
    }

    static PathPoint[] readNodeArray(PacketDataSerializer packetdataserializer) {
        PathPoint[] apathpoint = new PathPoint[packetdataserializer.readVarInt()];

        for (int i = 0; i < apathpoint.length; ++i) {
            apathpoint[i] = PathPoint.createFromStream(packetdataserializer);
        }

        return apathpoint;
    }

    static void writeNodeArray(PacketDataSerializer packetdataserializer, PathPoint[] apathpoint) {
        packetdataserializer.writeVarInt(apathpoint.length);
        PathPoint[] apathpoint1 = apathpoint;
        int i = apathpoint.length;

        for (int j = 0; j < i; ++j) {
            PathPoint pathpoint = apathpoint1[j];

            pathpoint.writeToStream(packetdataserializer);
        }

    }

    public PathEntity copy() {
        PathEntity pathentity = new PathEntity(this.nodes, this.target, this.reached);

        pathentity.debugData = this.debugData;
        pathentity.nextNodeIndex = this.nextNodeIndex;
        return pathentity;
    }

    public static record a(PathPoint[] openSet, PathPoint[] closedSet, Set<PathDestination> targetNodes) {

        public void write(PacketDataSerializer packetdataserializer) {
            packetdataserializer.writeCollection(this.targetNodes, (packetdataserializer1, pathdestination) -> {
                pathdestination.writeToStream(packetdataserializer1);
            });
            PathEntity.writeNodeArray(packetdataserializer, this.openSet);
            PathEntity.writeNodeArray(packetdataserializer, this.closedSet);
        }

        public static PathEntity.a read(PacketDataSerializer packetdataserializer) {
            HashSet<PathDestination> hashset = (HashSet) packetdataserializer.readCollection(HashSet::new, PathDestination::createFromStream);
            PathPoint[] apathpoint = PathEntity.readNodeArray(packetdataserializer);
            PathPoint[] apathpoint1 = PathEntity.readNodeArray(packetdataserializer);

            return new PathEntity.a(apathpoint, apathpoint1, hashset);
        }
    }
}
