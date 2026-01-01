package me.brynview.navidrohim.jmws.client.helper;

import me.brynview.navidrohim.jmws.Constants;
import net.minecraft.resources.Identifier;

/**
 * ResourceLocations for image assets
 */
public interface AssetHelper {

    Identifier onOffButtonAsset = Identifier.fromNamespaceAndPath(Constants.MODID, "textures/gui/on_off_button.png");
    Identifier enableButtonAsset = Identifier.fromNamespaceAndPath(Constants.MODID, "textures/gui/update_button.png");

    Identifier globalObjectAsset = Identifier.fromNamespaceAndPath(Constants.MODID, "textures/gui/global_object.png");
    Identifier sharedObjectAsset = Identifier.fromNamespaceAndPath(Constants.MODID, "textures/gui/shared_object.png");
}
