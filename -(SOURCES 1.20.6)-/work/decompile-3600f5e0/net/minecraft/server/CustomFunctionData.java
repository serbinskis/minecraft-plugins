package net.minecraft.server;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.FunctionInstantiationException;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.commands.functions.InstantiatedFunction;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.profiling.GameProfilerFiller;
import org.slf4j.Logger;

public class CustomFunctionData {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final MinecraftKey TICK_FUNCTION_TAG = new MinecraftKey("tick");
    private static final MinecraftKey LOAD_FUNCTION_TAG = new MinecraftKey("load");
    private final MinecraftServer server;
    private List<CommandFunction<CommandListenerWrapper>> ticking = ImmutableList.of();
    private boolean postReload;
    private CustomFunctionManager library;

    public CustomFunctionData(MinecraftServer minecraftserver, CustomFunctionManager customfunctionmanager) {
        this.server = minecraftserver;
        this.library = customfunctionmanager;
        this.postReload(customfunctionmanager);
    }

    public CommandDispatcher<CommandListenerWrapper> getDispatcher() {
        return this.server.getCommands().getDispatcher();
    }

    public void tick() {
        if (this.server.tickRateManager().runsNormally()) {
            if (this.postReload) {
                this.postReload = false;
                Collection<CommandFunction<CommandListenerWrapper>> collection = this.library.getTag(CustomFunctionData.LOAD_FUNCTION_TAG);

                this.executeTagFunctions(collection, CustomFunctionData.LOAD_FUNCTION_TAG);
            }

            this.executeTagFunctions(this.ticking, CustomFunctionData.TICK_FUNCTION_TAG);
        }
    }

    private void executeTagFunctions(Collection<CommandFunction<CommandListenerWrapper>> collection, MinecraftKey minecraftkey) {
        GameProfilerFiller gameprofilerfiller = this.server.getProfiler();

        Objects.requireNonNull(minecraftkey);
        gameprofilerfiller.push(minecraftkey::toString);
        Iterator iterator = collection.iterator();

        while (iterator.hasNext()) {
            CommandFunction<CommandListenerWrapper> commandfunction = (CommandFunction) iterator.next();

            this.execute(commandfunction, this.getGameLoopSender());
        }

        this.server.getProfiler().pop();
    }

    public void execute(CommandFunction<CommandListenerWrapper> commandfunction, CommandListenerWrapper commandlistenerwrapper) {
        GameProfilerFiller gameprofilerfiller = this.server.getProfiler();

        gameprofilerfiller.push(() -> {
            return "function " + String.valueOf(commandfunction.id());
        });

        try {
            InstantiatedFunction<CommandListenerWrapper> instantiatedfunction = commandfunction.instantiate((NBTTagCompound) null, this.getDispatcher());

            net.minecraft.commands.CommandDispatcher.executeCommandInContext(commandlistenerwrapper, (executioncontext) -> {
                ExecutionContext.queueInitialFunctionCall(executioncontext, instantiatedfunction, commandlistenerwrapper, CommandResultCallback.EMPTY);
            });
        } catch (FunctionInstantiationException functioninstantiationexception) {
            ;
        } catch (Exception exception) {
            CustomFunctionData.LOGGER.warn("Failed to execute function {}", commandfunction.id(), exception);
        } finally {
            gameprofilerfiller.pop();
        }

    }

    public void replaceLibrary(CustomFunctionManager customfunctionmanager) {
        this.library = customfunctionmanager;
        this.postReload(customfunctionmanager);
    }

    private void postReload(CustomFunctionManager customfunctionmanager) {
        this.ticking = ImmutableList.copyOf(customfunctionmanager.getTag(CustomFunctionData.TICK_FUNCTION_TAG));
        this.postReload = true;
    }

    public CommandListenerWrapper getGameLoopSender() {
        return this.server.createCommandSourceStack().withPermission(2).withSuppressedOutput();
    }

    public Optional<CommandFunction<CommandListenerWrapper>> get(MinecraftKey minecraftkey) {
        return this.library.getFunction(minecraftkey);
    }

    public Collection<CommandFunction<CommandListenerWrapper>> getTag(MinecraftKey minecraftkey) {
        return this.library.getTag(minecraftkey);
    }

    public Iterable<MinecraftKey> getFunctionNames() {
        return this.library.getFunctions().keySet();
    }

    public Iterable<MinecraftKey> getTagNames() {
        return this.library.getAvailableTags();
    }
}
