package net.minecraft.core.dispenser;

public abstract class DispenseBehaviorMaybe extends DispenseBehaviorItem {

    private boolean success = true;

    public DispenseBehaviorMaybe() {}

    public boolean isSuccess() {
        return this.success;
    }

    public void setSuccess(boolean flag) {
        this.success = flag;
    }

    @Override
    protected void playSound(SourceBlock sourceblock) {
        sourceblock.level().levelEvent(this.isSuccess() ? 1000 : 1001, sourceblock.pos(), 0);
    }
}
