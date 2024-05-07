package net.minecraft.world;

public enum EnumInteractionResult {

    SUCCESS, SUCCESS_NO_ITEM_USED, CONSUME, CONSUME_PARTIAL, PASS, FAIL;

    private EnumInteractionResult() {}

    public boolean consumesAction() {
        return this == EnumInteractionResult.SUCCESS || this == EnumInteractionResult.CONSUME || this == EnumInteractionResult.CONSUME_PARTIAL || this == EnumInteractionResult.SUCCESS_NO_ITEM_USED;
    }

    public boolean shouldSwing() {
        return this == EnumInteractionResult.SUCCESS || this == EnumInteractionResult.SUCCESS_NO_ITEM_USED;
    }

    public boolean indicateItemUse() {
        return this == EnumInteractionResult.SUCCESS || this == EnumInteractionResult.CONSUME;
    }

    public static EnumInteractionResult sidedSuccess(boolean flag) {
        return flag ? EnumInteractionResult.SUCCESS : EnumInteractionResult.CONSUME;
    }
}
