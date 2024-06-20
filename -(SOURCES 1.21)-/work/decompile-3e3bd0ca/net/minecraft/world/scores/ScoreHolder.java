package net.minecraft.world.scores;

import com.mojang.authlib.GameProfile;
import javax.annotation.Nullable;
import net.minecraft.network.chat.ChatHoverable;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;

public interface ScoreHolder {

    String WILDCARD_NAME = "*";
    ScoreHolder WILDCARD = new ScoreHolder() {
        @Override
        public String getScoreboardName() {
            return "*";
        }
    };

    String getScoreboardName();

    @Nullable
    default IChatBaseComponent getDisplayName() {
        return null;
    }

    default IChatBaseComponent getFeedbackDisplayName() {
        IChatBaseComponent ichatbasecomponent = this.getDisplayName();

        return ichatbasecomponent != null ? ichatbasecomponent.copy().withStyle((chatmodifier) -> {
            return chatmodifier.withHoverEvent(new ChatHoverable(ChatHoverable.EnumHoverAction.SHOW_TEXT, IChatBaseComponent.literal(this.getScoreboardName())));
        }) : IChatBaseComponent.literal(this.getScoreboardName());
    }

    static ScoreHolder forNameOnly(final String s) {
        if (s.equals("*")) {
            return ScoreHolder.WILDCARD;
        } else {
            final IChatMutableComponent ichatmutablecomponent = IChatBaseComponent.literal(s);

            return new ScoreHolder() {
                @Override
                public String getScoreboardName() {
                    return s;
                }

                @Override
                public IChatBaseComponent getFeedbackDisplayName() {
                    return ichatmutablecomponent;
                }
            };
        }
    }

    static ScoreHolder fromGameProfile(GameProfile gameprofile) {
        final String s = gameprofile.getName();

        return new ScoreHolder() {
            @Override
            public String getScoreboardName() {
                return s;
            }
        };
    }
}
