package me.brynview.navidrohim.jmws.client.helpers;

import me.brynview.navidrohim.jmws.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public interface AssetHelper {
    ResourceLocation onOffButtonAsset = ResourceLocation.fromNamespaceAndPath(Constants.MODID, "textures/gui/on_off_button.png");
    ResourceLocation enableButtonAsset = ResourceLocation.fromNamespaceAndPath(Constants.MODID, "textures/gui/update_button.png");
}
