package net.minecraft.gametest.framework;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.levelgen.structure.StructureBoundingBox;
import net.minecraft.world.phys.AxisAlignedBB;

public class StructureGridSpawner implements GameTestHarnessRunner.c {

    private static final int SPACE_BETWEEN_COLUMNS = 5;
    private static final int SPACE_BETWEEN_ROWS = 6;
    private final int testsPerRow;
    private int currentRowCount;
    private AxisAlignedBB rowBounds;
    private final BlockPosition.MutableBlockPosition nextTestNorthWestCorner;
    private final BlockPosition firstTestNorthWestCorner;
    private final boolean clearOnBatch;
    private float maxX = -1.0F;
    private final Collection<GameTestHarnessInfo> testInLastBatch = new ArrayList();

    public StructureGridSpawner(BlockPosition blockposition, int i, boolean flag) {
        this.testsPerRow = i;
        this.nextTestNorthWestCorner = blockposition.mutable();
        this.rowBounds = new AxisAlignedBB(this.nextTestNorthWestCorner);
        this.firstTestNorthWestCorner = blockposition;
        this.clearOnBatch = flag;
    }

    @Override
    public void onBatchStart(WorldServer worldserver) {
        if (this.clearOnBatch) {
            this.testInLastBatch.forEach((gametestharnessinfo) -> {
                StructureBoundingBox structureboundingbox = GameTestHarnessStructures.getStructureBoundingBox(gametestharnessinfo.getStructureBlockEntity());

                GameTestHarnessStructures.clearSpaceForStructure(structureboundingbox, worldserver);
            });
            this.testInLastBatch.clear();
            this.rowBounds = new AxisAlignedBB(this.firstTestNorthWestCorner);
            this.nextTestNorthWestCorner.set(this.firstTestNorthWestCorner);
        }

    }

    @Override
    public Optional<GameTestHarnessInfo> spawnStructure(GameTestHarnessInfo gametestharnessinfo) {
        BlockPosition blockposition = new BlockPosition(this.nextTestNorthWestCorner);

        gametestharnessinfo.setNorthWestCorner(blockposition);
        gametestharnessinfo.prepareTestStructure();
        AxisAlignedBB axisalignedbb = GameTestHarnessStructures.getStructureBounds(gametestharnessinfo.getStructureBlockEntity());

        this.rowBounds = this.rowBounds.minmax(axisalignedbb);
        this.nextTestNorthWestCorner.move((int) axisalignedbb.getXsize() + 5, 0, 0);
        if ((float) this.nextTestNorthWestCorner.getX() > this.maxX) {
            this.maxX = (float) this.nextTestNorthWestCorner.getX();
        }

        if (++this.currentRowCount >= this.testsPerRow) {
            this.currentRowCount = 0;
            this.nextTestNorthWestCorner.move(0, 0, (int) this.rowBounds.getZsize() + 6);
            this.nextTestNorthWestCorner.setX(this.firstTestNorthWestCorner.getX());
            this.rowBounds = new AxisAlignedBB(this.nextTestNorthWestCorner);
        }

        this.testInLastBatch.add(gametestharnessinfo);
        return Optional.of(gametestharnessinfo);
    }
}
