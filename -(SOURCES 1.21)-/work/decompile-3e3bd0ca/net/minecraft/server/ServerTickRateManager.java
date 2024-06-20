package net.minecraft.server;

import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.ClientboundTickingStatePacket;
import net.minecraft.network.protocol.game.ClientboundTickingStepPacket;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.util.TimeRange;
import net.minecraft.world.TickRateManager;

public class ServerTickRateManager extends TickRateManager {

    private long remainingSprintTicks = 0L;
    private long sprintTickStartTime = 0L;
    private long sprintTimeSpend = 0L;
    private long scheduledCurrentSprintTicks = 0L;
    private boolean previousIsFrozen = false;
    private final MinecraftServer server;

    public ServerTickRateManager(MinecraftServer minecraftserver) {
        this.server = minecraftserver;
    }

    public boolean isSprinting() {
        return this.scheduledCurrentSprintTicks > 0L;
    }

    @Override
    public void setFrozen(boolean flag) {
        super.setFrozen(flag);
        this.updateStateToClients();
    }

    private void updateStateToClients() {
        this.server.getPlayerList().broadcastAll(ClientboundTickingStatePacket.from(this));
    }

    private void updateStepTicks() {
        this.server.getPlayerList().broadcastAll(ClientboundTickingStepPacket.from(this));
    }

    public boolean stepGameIfPaused(int i) {
        if (!this.isFrozen()) {
            return false;
        } else {
            this.frozenTicksToRun = i;
            this.updateStepTicks();
            return true;
        }
    }

    public boolean stopStepping() {
        if (this.frozenTicksToRun > 0) {
            this.frozenTicksToRun = 0;
            this.updateStepTicks();
            return true;
        } else {
            return false;
        }
    }

    public boolean stopSprinting() {
        if (this.remainingSprintTicks > 0L) {
            this.finishTickSprint();
            return true;
        } else {
            return false;
        }
    }

    public boolean requestGameToSprint(int i) {
        boolean flag = this.remainingSprintTicks > 0L;

        this.sprintTimeSpend = 0L;
        this.scheduledCurrentSprintTicks = (long) i;
        this.remainingSprintTicks = (long) i;
        this.previousIsFrozen = this.isFrozen();
        this.setFrozen(false);
        return flag;
    }

    private void finishTickSprint() {
        long i = this.scheduledCurrentSprintTicks - this.remainingSprintTicks;
        double d0 = Math.max(1.0D, (double) this.sprintTimeSpend) / (double) TimeRange.NANOSECONDS_PER_MILLISECOND;
        int j = (int) ((double) (TimeRange.MILLISECONDS_PER_SECOND * i) / d0);
        String s = String.format("%.2f", i == 0L ? (double) this.millisecondsPerTick() : d0 / (double) i);

        this.scheduledCurrentSprintTicks = 0L;
        this.sprintTimeSpend = 0L;
        this.server.createCommandSourceStack().sendSuccess(() -> {
            return IChatBaseComponent.translatable("commands.tick.sprint.report", j, s);
        }, true);
        this.remainingSprintTicks = 0L;
        this.setFrozen(this.previousIsFrozen);
        this.server.onTickRateChanged();
    }

    public boolean checkShouldSprintThisTick() {
        if (!this.runGameElements) {
            return false;
        } else if (this.remainingSprintTicks > 0L) {
            this.sprintTickStartTime = System.nanoTime();
            --this.remainingSprintTicks;
            return true;
        } else {
            this.finishTickSprint();
            return false;
        }
    }

    public void endTickWork() {
        this.sprintTimeSpend += System.nanoTime() - this.sprintTickStartTime;
    }

    @Override
    public void setTickRate(float f) {
        super.setTickRate(f);
        this.server.onTickRateChanged();
        this.updateStateToClients();
    }

    public void updateJoiningPlayer(EntityPlayer entityplayer) {
        entityplayer.connection.send(ClientboundTickingStatePacket.from(this));
        entityplayer.connection.send(ClientboundTickingStepPacket.from(this));
    }
}
