package me.brynview.navidrohim.jmws.client.assets;

import me.brynview.navidrohim.jmws.Constants;
import net.minecraft.resources.Identifier;

/**
 * ResourceLocations for image assets
 */
public interface JMWSTextures {

    Identifier onOffButtonAsset = Identifier.fromNamespaceAndPath(Constants.MODID, "textures/gui/toggle.png");
    Identifier enableButtonAsset = Identifier.fromNamespaceAndPath(Constants.MODID, "textures/gui/sync.png");

    Identifier globalObjectAsset = Identifier.fromNamespaceAndPath(Constants.MODID, "textures/gui/global.png");
    Identifier sharedObjectAsset = Identifier.fromNamespaceAndPath(Constants.MODID, "textures/gui/share.png");
}
