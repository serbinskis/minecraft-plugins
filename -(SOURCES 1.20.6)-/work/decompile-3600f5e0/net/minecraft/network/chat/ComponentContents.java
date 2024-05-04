package net.minecraft.network.chat;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.util.INamable;
import net.minecraft.world.entity.Entity;

public interface ComponentContents {

    default <T> Optional<T> visit(IChatFormatted.b<T> ichatformatted_b, ChatModifier chatmodifier) {
        return Optional.empty();
    }

    default <T> Optional<T> visit(IChatFormatted.a<T> ichatformatted_a) {
        return Optional.empty();
    }

    default IChatMutableComponent resolve(@Nullable CommandListenerWrapper commandlistenerwrapper, @Nullable Entity entity, int i) throws CommandSyntaxException {
        return IChatMutableComponent.create(this);
    }

    ComponentContents.a<?> type();

    public static record a<T extends ComponentContents>(MapCodec<T> codec, String id) implements INamable {

        @Override
        public String getSerializedName() {
            return this.id;
        }
    }
}
