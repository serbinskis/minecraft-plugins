package net.minecraft.world.level.saveddata.maps;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.GameProfileSerializer;
import net.minecraft.nbt.NBTTagCompound;

public class WorldMapFrame {

    private final BlockPosition pos;
    private final int rotation;
    private final int entityId;

    public WorldMapFrame(BlockPosition blockposition, int i, int j) {
        this.pos = blockposition;
        this.rotation = i;
        this.entityId = j;
    }

    @Nullable
    public static WorldMapFrame load(NBTTagCompound nbttagcompound) {
        Optional<BlockPosition> optional = GameProfileSerializer.readBlockPos(nbttagcompound, "pos");

        if (optional.isEmpty()) {
            return null;
        } else {
            int i = nbttagcompound.getInt("rotation");
            int j = nbttagcompound.getInt("entity_id");

            return new WorldMapFrame((BlockPosition) optional.get(), i, j);
        }
    }

    public NBTTagCompound save() {
        NBTTagCompound nbttagcompound = new NBTTagCompound();

        nbttagcompound.put("pos", GameProfileSerializer.writeBlockPos(this.pos));
        nbttagcompound.putInt("rotation", this.rotation);
        nbttagcompound.putInt("entity_id", this.entityId);
        return nbttagcompound;
    }

    public BlockPosition getPos() {
        return this.pos;
    }

    public int getRotation() {
        return this.rotation;
    }

    public int getEntityId() {
        return this.entityId;
    }

    public String getId() {
        return frameId(this.pos);
    }

    public static String frameId(BlockPosition blockposition) {
        int i = blockposition.getX();

        return "frame-" + i + "," + blockposition.getY() + "," + blockposition.getZ();
    }
}
