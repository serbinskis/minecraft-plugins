package net.minecraft.commands.functions;

import java.util.List;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.resources.MinecraftKey;

public interface InstantiatedFunction<T> {

    MinecraftKey id();

    List<UnboundEntryAction<T>> entries();
}
