package wobbychip.main.spawnproofer.mixins;

import wobbychip.main.spawnproofer.SpawnProoferHelper;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionManager {

    @Inject(at = @At("HEAD"), method = "tick")
    private void init(CallbackInfo ci) {
        if (SpawnProoferHelper.isEnabled()) { SpawnProoferHelper.tick(); }
    }
}