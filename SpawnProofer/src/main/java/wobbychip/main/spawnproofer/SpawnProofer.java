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
        KeyBinding keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.spawnproofer.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_APOSTROPHE,"category.spawnproofer.mod"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyBinding.wasPressed()) { SpawnProoferHelper.toggle(); }
        });
    }
}
