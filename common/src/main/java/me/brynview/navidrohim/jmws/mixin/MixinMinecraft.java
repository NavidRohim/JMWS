package me.brynview.navidrohim.jmws.mixin;

import me.brynview.navidrohim.jmws.Constants;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Inject(at = @At("TAIL"), method = "<init>")
    private void init(CallbackInfo info) {
        Constants.LOGGER.info("This line is printed by the JMWS common mixin!");
        Constants.LOGGER.info("MC Version: {}", Minecraft.getInstance().getVersionType());
    }
}
