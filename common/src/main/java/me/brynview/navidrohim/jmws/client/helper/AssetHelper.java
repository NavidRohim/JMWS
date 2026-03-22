package me.brynview.navidrohim.jmws.client.helper;

import me.brynview.navidrohim.jmws.Constants;
import net.minecraft.resources.ResourceLocation;

/**
 * ResourceLocations for image assets
 */
public interface AssetHelper {

    ResourceLocation onOffButtonAsset = ResourceLocation.fromNamespaceAndPath(Constants.MODID, "textures/gui/on_off_button.png");
    ResourceLocation enableButtonAsset = ResourceLocation.fromNamespaceAndPath(Constants.MODID, "textures/gui/update_button.png");

    ResourceLocation globalObjectAsset = ResourceLocation.fromNamespaceAndPath(Constants.MODID, "textures/gui/global_object.png");
    ResourceLocation sharedObjectAsset = ResourceLocation.fromNamespaceAndPath(Constants.MODID, "textures/gui/shared_object.png");
}
