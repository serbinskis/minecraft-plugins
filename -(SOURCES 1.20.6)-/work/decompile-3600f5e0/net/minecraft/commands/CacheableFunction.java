package net.minecraft.commands;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.CustomFunctionData;

public class CacheableFunction {

    public static final Codec<CacheableFunction> CODEC = MinecraftKey.CODEC.xmap(CacheableFunction::new, CacheableFunction::getId);
    private final MinecraftKey id;
    private boolean resolved;
    private Optional<CommandFunction<CommandListenerWrapper>> function = Optional.empty();

    public CacheableFunction(MinecraftKey minecraftkey) {
        this.id = minecraftkey;
    }

    public Optional<CommandFunction<CommandListenerWrapper>> get(CustomFunctionData customfunctiondata) {
        if (!this.resolved) {
            this.function = customfunctiondata.get(this.id);
            this.resolved = true;
        }

        return this.function;
    }

    public MinecraftKey getId() {
        return this.id;
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        } else {
            boolean flag;

            if (object instanceof CacheableFunction) {
                CacheableFunction cacheablefunction = (CacheableFunction) object;

                if (this.getId().equals(cacheablefunction.getId())) {
                    flag = true;
                    return flag;
                }
            }

            flag = false;
            return flag;
        }
    }
}
