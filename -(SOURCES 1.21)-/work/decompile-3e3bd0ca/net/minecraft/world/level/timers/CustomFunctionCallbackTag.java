package net.minecraft.world.level.timers;

import java.util.Collection;
import java.util.Iterator;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.CustomFunctionData;
import net.minecraft.server.MinecraftServer;

public class CustomFunctionCallbackTag implements CustomFunctionCallbackTimer<MinecraftServer> {

    final MinecraftKey tagId;

    public CustomFunctionCallbackTag(MinecraftKey minecraftkey) {
        this.tagId = minecraftkey;
    }

    public void handle(MinecraftServer minecraftserver, CustomFunctionCallbackTimerQueue<MinecraftServer> customfunctioncallbacktimerqueue, long i) {
        CustomFunctionData customfunctiondata = minecraftserver.getFunctions();
        Collection<CommandFunction<CommandListenerWrapper>> collection = customfunctiondata.getTag(this.tagId);
        Iterator iterator = collection.iterator();

        while (iterator.hasNext()) {
            CommandFunction<CommandListenerWrapper> commandfunction = (CommandFunction) iterator.next();

            customfunctiondata.execute(commandfunction, customfunctiondata.getGameLoopSender());
        }

    }

    public static class a extends CustomFunctionCallbackTimer.a<MinecraftServer, CustomFunctionCallbackTag> {

        public a() {
            super(MinecraftKey.withDefaultNamespace("function_tag"), CustomFunctionCallbackTag.class);
        }

        public void serialize(NBTTagCompound nbttagcompound, CustomFunctionCallbackTag customfunctioncallbacktag) {
            nbttagcompound.putString("Name", customfunctioncallbacktag.tagId.toString());
        }

        @Override
        public CustomFunctionCallbackTag deserialize(NBTTagCompound nbttagcompound) {
            MinecraftKey minecraftkey = MinecraftKey.parse(nbttagcompound.getString("Name"));

            return new CustomFunctionCallbackTag(minecraftkey);
        }
    }
}
