package net.minecraft.world;

import net.minecraft.util.TimeRange;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;

public class TickRateManager {

    public static final float MIN_TICKRATE = 1.0F;
    protected float tickrate = 20.0F;
    protected long nanosecondsPerTick;
    protected int frozenTicksToRun;
    protected boolean runGameElements;
    protected boolean isFrozen;

    public TickRateManager() {
        this.nanosecondsPerTick = TimeRange.NANOSECONDS_PER_SECOND / 20L;
        this.frozenTicksToRun = 0;
        this.runGameElements = true;
        this.isFrozen = false;
    }

    public void setTickRate(float f) {
        this.tickrate = Math.max(f, 1.0F);
        this.nanosecondsPerTick = (long) ((double) TimeRange.NANOSECONDS_PER_SECOND / (double) this.tickrate);
    }

    public float tickrate() {
        return this.tickrate;
    }

    public float millisecondsPerTick() {
        return (float) this.nanosecondsPerTick / (float) TimeRange.NANOSECONDS_PER_MILLISECOND;
    }

    public long nanosecondsPerTick() {
        return this.nanosecondsPerTick;
    }

    public boolean runsNormally() {
        return this.runGameElements;
    }

    public boolean isSteppingForward() {
        return this.frozenTicksToRun > 0;
    }

    public void setFrozenTicksToRun(int i) {
        this.frozenTicksToRun = i;
    }

    public int frozenTicksToRun() {
        return this.frozenTicksToRun;
    }

    public void setFrozen(boolean flag) {
        this.isFrozen = flag;
    }

    public boolean isFrozen() {
        return this.isFrozen;
    }

    public void tick() {
        this.runGameElements = !this.isFrozen || this.frozenTicksToRun > 0;
        if (this.frozenTicksToRun > 0) {
            --this.frozenTicksToRun;
        }

    }

    public boolean isEntityFrozen(Entity entity) {
        return !this.runsNormally() && !(entity instanceof EntityHuman) && entity.countPlayerPassengers() <= 0;
    }
}
