package net.minecraft.gametest.framework;

import java.util.Optional;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.phys.AxisAlignedBB;

public class StructureGridSpawner implements GameTestHarnessRunner.c {

    private static final int SPACE_BETWEEN_COLUMNS = 5;
    private static final int SPACE_BETWEEN_ROWS = 6;
    private final int testsPerRow;
    private int currentRowCount;
    private AxisAlignedBB rowBounds;
    private final BlockPosition.MutableBlockPosition nextTestNorthWestCorner;
    private final BlockPosition firstTestNorthWestCorner;

    public StructureGridSpawner(BlockPosition blockposition, int i) {
        this.testsPerRow = i;
        this.nextTestNorthWestCorner = blockposition.mutable();
        this.rowBounds = new AxisAlignedBB(this.nextTestNorthWestCorner);
        this.firstTestNorthWestCorner = blockposition;
    }

    @Override
    public Optional<GameTestHarnessInfo> spawnStructure(GameTestHarnessInfo gametestharnessinfo) {
        BlockPosition blockposition = new BlockPosition(this.nextTestNorthWestCorner);

        gametestharnessinfo.setNorthWestCorner(blockposition);
        gametestharnessinfo.prepareTestStructure();
        AxisAlignedBB axisalignedbb = GameTestHarnessStructures.getStructureBounds(gametestharnessinfo.getStructureBlockEntity());

        this.rowBounds = this.rowBounds.minmax(axisalignedbb);
        this.nextTestNorthWestCorner.move((int) axisalignedbb.getXsize() + 5, 0, 0);
        if (++this.currentRowCount >= this.testsPerRow) {
            this.currentRowCount = 0;
            this.nextTestNorthWestCorner.move(0, 0, (int) this.rowBounds.getZsize() + 6);
            this.nextTestNorthWestCorner.setX(this.firstTestNorthWestCorner.getX());
            this.rowBounds = new AxisAlignedBB(this.nextTestNorthWestCorner);
        }

        return Optional.of(gametestharnessinfo);
    }
}
