package net.minecraft.network.chat.contents;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.arguments.ArgumentEntity;
import net.minecraft.commands.arguments.selector.ArgumentParserSelector;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ScoreboardServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.ScoreboardObjective;

public class ScoreContents implements ComponentContents {

    public static final MapCodec<ScoreContents> INNER_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Codec.STRING.fieldOf("name").forGetter(ScoreContents::getName), Codec.STRING.fieldOf("objective").forGetter(ScoreContents::getObjective)).apply(instance, ScoreContents::new);
    });
    public static final MapCodec<ScoreContents> CODEC = ScoreContents.INNER_CODEC.fieldOf("score");
    public static final ComponentContents.a<ScoreContents> TYPE = new ComponentContents.a<>(ScoreContents.CODEC, "score");
    private final String name;
    @Nullable
    private final EntitySelector selector;
    private final String objective;

    @Nullable
    private static EntitySelector parseSelector(String s) {
        try {
            return (new ArgumentParserSelector(new StringReader(s))).parse();
        } catch (CommandSyntaxException commandsyntaxexception) {
            return null;
        }
    }

    public ScoreContents(String s, String s1) {
        this.name = s;
        this.selector = parseSelector(s);
        this.objective = s1;
    }

    @Override
    public ComponentContents.a<?> type() {
        return ScoreContents.TYPE;
    }

    public String getName() {
        return this.name;
    }

    @Nullable
    public EntitySelector getSelector() {
        return this.selector;
    }

    public String getObjective() {
        return this.objective;
    }

    private ScoreHolder findTargetName(CommandListenerWrapper commandlistenerwrapper) throws CommandSyntaxException {
        if (this.selector != null) {
            List<? extends Entity> list = this.selector.findEntities(commandlistenerwrapper);

            if (!list.isEmpty()) {
                if (list.size() != 1) {
                    throw ArgumentEntity.ERROR_NOT_SINGLE_ENTITY.create();
                }

                return (ScoreHolder) list.get(0);
            }
        }

        return ScoreHolder.forNameOnly(this.name);
    }

    private IChatMutableComponent getScore(ScoreHolder scoreholder, CommandListenerWrapper commandlistenerwrapper) {
        MinecraftServer minecraftserver = commandlistenerwrapper.getServer();

        if (minecraftserver != null) {
            ScoreboardServer scoreboardserver = minecraftserver.getScoreboard();
            ScoreboardObjective scoreboardobjective = scoreboardserver.getObjective(this.objective);

            if (scoreboardobjective != null) {
                ReadOnlyScoreInfo readonlyscoreinfo = scoreboardserver.getPlayerScoreInfo(scoreholder, scoreboardobjective);

                if (readonlyscoreinfo != null) {
                    return readonlyscoreinfo.formatValue(scoreboardobjective.numberFormatOrDefault(StyledFormat.NO_STYLE));
                }
            }
        }

        return IChatBaseComponent.empty();
    }

    @Override
    public IChatMutableComponent resolve(@Nullable CommandListenerWrapper commandlistenerwrapper, @Nullable Entity entity, int i) throws CommandSyntaxException {
        if (commandlistenerwrapper == null) {
            return IChatBaseComponent.empty();
        } else {
            ScoreHolder scoreholder = this.findTargetName(commandlistenerwrapper);
            Object object = entity != null && scoreholder.equals(ScoreHolder.WILDCARD) ? entity : scoreholder;

            return this.getScore((ScoreHolder) object, commandlistenerwrapper);
        }
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else {
            boolean flag;

            if (object instanceof ScoreContents) {
                ScoreContents scorecontents = (ScoreContents) object;

                if (this.name.equals(scorecontents.name) && this.objective.equals(scorecontents.objective)) {
                    flag = true;
                    return flag;
                }
            }

            flag = false;
            return flag;
        }
    }

    public int hashCode() {
        int i = this.name.hashCode();

        i = 31 * i + this.objective.hashCode();
        return i;
    }

    public String toString() {
        return "score{name='" + this.name + "', objective='" + this.objective + "'}";
    }
}
