package me.brynview.navidrohim.jmws.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.kinds.Const;
import journeymap.client.render.draw.DrawStep;
import journeymap.client.render.ingame.WaypointDecorationRenderer;
import journeymap.client.waypoint.ClientWaypointImpl;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.plugin.JMWSPlugin;
import me.brynview.navidrohim.jmws.common.CommonClass;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(WaypointDecorationRenderer.class)
public abstract class WaypointDecorationRendererMixin {

    @Shadow
    protected abstract double angleToBeacon(Vec3 waypointVec);

    @Inject(
            method = "renderNameTag",
            at = @At(value = "TAIL")
    )
    private void callWaypointIconRenderEvent(GuiGraphics graphics, ClientWaypointImpl waypoint, double labelX, double labelY, float alpha, double actualDistance, int size, CallbackInfo ci) {
        double angle = this.angleToBeacon(waypoint.getPosition());
        if (angle < 2 && CommonClass.isHoldingTeleportKey) {
            JMWSPlugin.getInstance().teleportPlayer(waypoint.getPosition());
        }
    }
}
