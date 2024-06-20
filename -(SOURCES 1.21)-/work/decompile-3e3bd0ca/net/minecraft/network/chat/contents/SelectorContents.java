package net.minecraft.network.chat.contents;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.arguments.selector.ArgumentParserSelector;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.ChatComponentUtils;
import net.minecraft.network.chat.ChatModifier;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatFormatted;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;

public class SelectorContents implements ComponentContents {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<SelectorContents> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Codec.STRING.fieldOf("selector").forGetter(SelectorContents::getPattern), ComponentSerialization.CODEC.optionalFieldOf("separator").forGetter(SelectorContents::getSeparator)).apply(instance, SelectorContents::new);
    });
    public static final ComponentContents.a<SelectorContents> TYPE = new ComponentContents.a<>(SelectorContents.CODEC, "selector");
    private final String pattern;
    @Nullable
    private final EntitySelector selector;
    protected final Optional<IChatBaseComponent> separator;

    public SelectorContents(String s, Optional<IChatBaseComponent> optional) {
        this.pattern = s;
        this.separator = optional;
        this.selector = parseSelector(s);
    }

    @Nullable
    private static EntitySelector parseSelector(String s) {
        EntitySelector entityselector = null;

        try {
            ArgumentParserSelector argumentparserselector = new ArgumentParserSelector(new StringReader(s));

            entityselector = argumentparserselector.parse();
        } catch (CommandSyntaxException commandsyntaxexception) {
            SelectorContents.LOGGER.warn("Invalid selector component: {}: {}", s, commandsyntaxexception.getMessage());
        }

        return entityselector;
    }

    @Override
    public ComponentContents.a<?> type() {
        return SelectorContents.TYPE;
    }

    public String getPattern() {
        return this.pattern;
    }

    @Nullable
    public EntitySelector getSelector() {
        return this.selector;
    }

    public Optional<IChatBaseComponent> getSeparator() {
        return this.separator;
    }

    @Override
    public IChatMutableComponent resolve(@Nullable CommandListenerWrapper commandlistenerwrapper, @Nullable Entity entity, int i) throws CommandSyntaxException {
        if (commandlistenerwrapper != null && this.selector != null) {
            Optional<? extends IChatBaseComponent> optional = ChatComponentUtils.updateForEntity(commandlistenerwrapper, this.separator, entity, i);

            return ChatComponentUtils.formatList(this.selector.findEntities(commandlistenerwrapper), optional, Entity::getDisplayName);
        } else {
            return IChatBaseComponent.empty();
        }
    }

    @Override
    public <T> Optional<T> visit(IChatFormatted.b<T> ichatformatted_b, ChatModifier chatmodifier) {
        return ichatformatted_b.accept(chatmodifier, this.pattern);
    }

    @Override
    public <T> Optional<T> visit(IChatFormatted.a<T> ichatformatted_a) {
        return ichatformatted_a.accept(this.pattern);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else {
            boolean flag;

            if (object instanceof SelectorContents) {
                SelectorContents selectorcontents = (SelectorContents) object;

                if (this.pattern.equals(selectorcontents.pattern) && this.separator.equals(selectorcontents.separator)) {
                    flag = true;
                    return flag;
                }
            }

            flag = false;
            return flag;
        }
    }

    public int hashCode() {
        int i = this.pattern.hashCode();

        i = 31 * i + this.separator.hashCode();
        return i;
    }

    public String toString() {
        return "pattern{" + this.pattern + "}";
    }
}
