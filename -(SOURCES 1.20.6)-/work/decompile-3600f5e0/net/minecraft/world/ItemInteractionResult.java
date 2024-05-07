package net.minecraft.world;

public enum ItemInteractionResult {

    SUCCESS, CONSUME, CONSUME_PARTIAL, PASS_TO_DEFAULT_BLOCK_INTERACTION, SKIP_DEFAULT_BLOCK_INTERACTION, FAIL;

    private ItemInteractionResult() {}

    public boolean consumesAction() {
        return this.result().consumesAction();
    }

    public static ItemInteractionResult sidedSuccess(boolean flag) {
        return flag ? ItemInteractionResult.SUCCESS : ItemInteractionResult.CONSUME;
    }

    public EnumInteractionResult result() {
        EnumInteractionResult enuminteractionresult;

        switch (this.ordinal()) {
            case 0:
                enuminteractionresult = EnumInteractionResult.SUCCESS;
                break;
            case 1:
                enuminteractionresult = EnumInteractionResult.CONSUME;
                break;
            case 2:
                enuminteractionresult = EnumInteractionResult.CONSUME_PARTIAL;
                break;
            case 3:
            case 4:
                enuminteractionresult = EnumInteractionResult.PASS;
                break;
            case 5:
                enuminteractionresult = EnumInteractionResult.FAIL;
                break;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }

        return enuminteractionresult;
    }
}
