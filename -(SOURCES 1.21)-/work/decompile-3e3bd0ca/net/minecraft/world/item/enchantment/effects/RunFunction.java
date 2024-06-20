package net.minecraft.world.item.enchantment.effects;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.CustomFunctionData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.phys.Vec3D;
import org.slf4j.Logger;

public record RunFunction(MinecraftKey function) implements EnchantmentEntityEffect {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<RunFunction> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(MinecraftKey.CODEC.fieldOf("function").forGetter(RunFunction::function)).apply(instance, RunFunction::new);
    });

    @Override
    public void apply(WorldServer worldserver, int i, EnchantedItemInUse enchantediteminuse, Entity entity, Vec3D vec3d) {
        MinecraftServer minecraftserver = worldserver.getServer();
        CustomFunctionData customfunctiondata = minecraftserver.getFunctions();
        Optional<CommandFunction<CommandListenerWrapper>> optional = customfunctiondata.get(this.function);

        if (optional.isPresent()) {
            CommandListenerWrapper commandlistenerwrapper = minecraftserver.createCommandSourceStack().withPermission(2).withSuppressedOutput().withEntity(entity).withLevel(worldserver).withPosition(vec3d).withRotation(entity.getRotationVector());

            customfunctiondata.execute((CommandFunction) optional.get(), commandlistenerwrapper);
        } else {
            RunFunction.LOGGER.error("Enchantment run_function effect failed for non-existent function {}", this.function);
        }

    }

    @Override
    public MapCodec<RunFunction> codec() {
        return RunFunction.CODEC;
    }
}
