package net.minecraft.commands.functions;

import com.mojang.brigadier.CommandDispatcher;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.commands.FunctionInstantiationException;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.MinecraftKey;

public record PlainTextFunction<T>(MinecraftKey id, List<UnboundEntryAction<T>> entries) implements CommandFunction<T>, InstantiatedFunction<T> {

    @Override
    public InstantiatedFunction<T> instantiate(@Nullable NBTTagCompound nbttagcompound, CommandDispatcher<T> commanddispatcher) throws FunctionInstantiationException {
        return this;
    }
}
