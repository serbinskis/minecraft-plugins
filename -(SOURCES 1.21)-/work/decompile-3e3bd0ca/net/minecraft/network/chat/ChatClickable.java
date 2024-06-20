package net.minecraft.network.chat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.INamable;

public class ChatClickable {

    public static final Codec<ChatClickable> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(ChatClickable.EnumClickAction.CODEC.forGetter((chatclickable) -> {
            return chatclickable.action;
        }), Codec.STRING.fieldOf("value").forGetter((chatclickable) -> {
            return chatclickable.value;
        })).apply(instance, ChatClickable::new);
    });
    private final ChatClickable.EnumClickAction action;
    private final String value;

    public ChatClickable(ChatClickable.EnumClickAction chatclickable_enumclickaction, String s) {
        this.action = chatclickable_enumclickaction;
        this.value = s;
    }

    public ChatClickable.EnumClickAction getAction() {
        return this.action;
    }

    public String getValue() {
        return this.value;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (object != null && this.getClass() == object.getClass()) {
            ChatClickable chatclickable = (ChatClickable) object;

            return this.action == chatclickable.action && this.value.equals(chatclickable.value);
        } else {
            return false;
        }
    }

    public String toString() {
        String s = String.valueOf(this.action);

        return "ClickEvent{action=" + s + ", value='" + this.value + "'}";
    }

    public int hashCode() {
        int i = this.action.hashCode();

        i = 31 * i + this.value.hashCode();
        return i;
    }

    public static enum EnumClickAction implements INamable {

        OPEN_URL("open_url", true), OPEN_FILE("open_file", false), RUN_COMMAND("run_command", true), SUGGEST_COMMAND("suggest_command", true), CHANGE_PAGE("change_page", true), COPY_TO_CLIPBOARD("copy_to_clipboard", true);

        public static final MapCodec<ChatClickable.EnumClickAction> UNSAFE_CODEC = INamable.fromEnum(ChatClickable.EnumClickAction::values).fieldOf("action");
        public static final MapCodec<ChatClickable.EnumClickAction> CODEC = ChatClickable.EnumClickAction.UNSAFE_CODEC.validate(ChatClickable.EnumClickAction::filterForSerialization);
        private final boolean allowFromServer;
        private final String name;

        private EnumClickAction(final String s, final boolean flag) {
            this.name = s;
            this.allowFromServer = flag;
        }

        public boolean isAllowedFromServer() {
            return this.allowFromServer;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public static DataResult<ChatClickable.EnumClickAction> filterForSerialization(ChatClickable.EnumClickAction chatclickable_enumclickaction) {
            return !chatclickable_enumclickaction.isAllowedFromServer() ? DataResult.error(() -> {
                return "Action not allowed: " + String.valueOf(chatclickable_enumclickaction);
            }) : DataResult.success(chatclickable_enumclickaction, Lifecycle.stable());
        }
    }
}
