package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.DataResult;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.ICompletionProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.Particle;
import net.minecraft.core.particles.ParticleParam;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.MojangsonParser;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;

public class ArgumentParticle implements ArgumentType<ParticleParam> {

    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "particle{foo:bar}");
    public static final DynamicCommandExceptionType ERROR_UNKNOWN_PARTICLE = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("particle.notFound", object);
    });
    public static final DynamicCommandExceptionType ERROR_INVALID_OPTIONS = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("particle.invalidOptions", object);
    });
    private final HolderLookup.a registries;

    public ArgumentParticle(CommandBuildContext commandbuildcontext) {
        this.registries = commandbuildcontext;
    }

    public static ArgumentParticle particle(CommandBuildContext commandbuildcontext) {
        return new ArgumentParticle(commandbuildcontext);
    }

    public static ParticleParam getParticle(CommandContext<CommandListenerWrapper> commandcontext, String s) {
        return (ParticleParam) commandcontext.getArgument(s, ParticleParam.class);
    }

    public ParticleParam parse(StringReader stringreader) throws CommandSyntaxException {
        return readParticle(stringreader, this.registries);
    }

    public Collection<String> getExamples() {
        return ArgumentParticle.EXAMPLES;
    }

    public static ParticleParam readParticle(StringReader stringreader, HolderLookup.a holderlookup_a) throws CommandSyntaxException {
        Particle<?> particle = readParticleType(stringreader, holderlookup_a.lookupOrThrow(Registries.PARTICLE_TYPE));

        return readParticle(stringreader, particle, holderlookup_a);
    }

    private static Particle<?> readParticleType(StringReader stringreader, HolderLookup<Particle<?>> holderlookup) throws CommandSyntaxException {
        MinecraftKey minecraftkey = MinecraftKey.read(stringreader);
        ResourceKey<Particle<?>> resourcekey = ResourceKey.create(Registries.PARTICLE_TYPE, minecraftkey);

        return (Particle) ((Holder.c) holderlookup.get(resourcekey).orElseThrow(() -> {
            return ArgumentParticle.ERROR_UNKNOWN_PARTICLE.createWithContext(stringreader, minecraftkey);
        })).value();
    }

    private static <T extends ParticleParam> T readParticle(StringReader stringreader, Particle<T> particle, HolderLookup.a holderlookup_a) throws CommandSyntaxException {
        NBTTagCompound nbttagcompound;

        if (stringreader.canRead() && stringreader.peek() == '{') {
            nbttagcompound = (new MojangsonParser(stringreader)).readStruct();
        } else {
            nbttagcompound = new NBTTagCompound();
        }

        DataResult dataresult = particle.codec().codec().parse(holderlookup_a.createSerializationContext(DynamicOpsNBT.INSTANCE), nbttagcompound);
        DynamicCommandExceptionType dynamiccommandexceptiontype = ArgumentParticle.ERROR_INVALID_OPTIONS;

        Objects.requireNonNull(dynamiccommandexceptiontype);
        return (ParticleParam) dataresult.getOrThrow(dynamiccommandexceptiontype::create);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandcontext, SuggestionsBuilder suggestionsbuilder) {
        HolderLookup.b<Particle<?>> holderlookup_b = this.registries.lookupOrThrow(Registries.PARTICLE_TYPE);

        return ICompletionProvider.suggestResource(holderlookup_b.listElementIds().map(ResourceKey::location), suggestionsbuilder);
    }
}
