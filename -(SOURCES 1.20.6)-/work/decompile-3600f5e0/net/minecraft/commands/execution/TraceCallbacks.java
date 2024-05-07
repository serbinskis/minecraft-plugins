package net.minecraft.commands.execution;

import net.minecraft.resources.MinecraftKey;

public interface TraceCallbacks extends AutoCloseable {

    void onCommand(int i, String s);

    void onReturn(int i, String s, int j);

    void onError(String s);

    void onCall(int i, MinecraftKey minecraftkey, int j);

    void close();
}
