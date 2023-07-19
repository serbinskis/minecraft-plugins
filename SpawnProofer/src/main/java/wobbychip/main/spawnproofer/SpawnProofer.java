package wobbychip.main.spawnproofer;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class SpawnProofer implements ModInitializer {
    public static final String MOD_ID = "SpawnProofer";

    @Override
    public void onInitialize() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        KeyBinding keyBindingToggle = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.spawnproofer.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_APOSTROPHE,"category.spawnproofer.mod"));
        KeyBinding keyBindingDecreaseDistance = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.spawnproofer.decrease_distance", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_BRACKET,"category.spawnproofer.mod"));
        KeyBinding keyBindingIncreaseDistance = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.spawnproofer.increase_distance", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_BRACKET,"category.spawnproofer.mod"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyBindingToggle.wasPressed()) { SpawnProoferHelper.toggle(); }
            while (keyBindingDecreaseDistance.wasPressed()) { SpawnProoferHelper.changeDistance(-1); }
            while (keyBindingIncreaseDistance.wasPressed()) { SpawnProoferHelper.changeDistance(1); }
        });
    }
}
