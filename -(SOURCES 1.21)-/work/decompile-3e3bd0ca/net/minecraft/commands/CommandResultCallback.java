package net.minecraft.commands;

@FunctionalInterface
public interface CommandResultCallback {

    CommandResultCallback EMPTY = new CommandResultCallback() {
        @Override
        public void onResult(boolean flag, int i) {}

        public String toString() {
            return "<empty>";
        }
    };

    void onResult(boolean flag, int i);

    default void onSuccess(int i) {
        this.onResult(true, i);
    }

    default void onFailure() {
        this.onResult(false, 0);
    }

    static CommandResultCallback chain(CommandResultCallback commandresultcallback, CommandResultCallback commandresultcallback1) {
        return commandresultcallback == CommandResultCallback.EMPTY ? commandresultcallback1 : (commandresultcallback1 == CommandResultCallback.EMPTY ? commandresultcallback : (flag, i) -> {
            commandresultcallback.onResult(flag, i);
            commandresultcallback1.onResult(flag, i);
        });
    }
}
