package me.navidrohim.jmws.client.mixin;


import journeymap.client.model.Waypoint;

import me.navidrohim.jmws.Constants;
import me.navidrohim.jmws.client.plugin.JMWSPlugin;
import net.minecraft.client.gui.GuiButton;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(journeymap.client.ui.waypoint.WaypointEditor.class) // I hope this isnt against JM TOS
public abstract class MixinMinecraft {

    @Shadow
    private Waypoint editedWaypoint; // matches exactly

    @Final
    @Shadow
    private Waypoint originalWaypoint; // matches exactly

    @Final
    @Shadow
    private boolean isNew;

    @Inject(method = "save()V", at = @At("HEAD"), remap = false)
    private void afterSave(CallbackInfo ci)
    {
        if (!isNew)
        {
            JMWSPlugin.getInstance().updateAction(editedWaypoint, originalWaypoint);
            return;
        }
        JMWSPlugin.createAction(editedWaypoint, false, false);
    }

}
