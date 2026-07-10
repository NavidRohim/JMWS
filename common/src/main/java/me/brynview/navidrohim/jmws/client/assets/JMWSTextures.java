package me.brynview.navidrohim.jmws.client.assets;

import me.brynview.navidrohim.jmws.Constants;
import net.minecraft.resources.ResourceLocation;

/**
 * ResourceLocations for image assets
 */
public interface JMWSTextures {

   ResourceLocation onOffButtonAsset = ResourceLocation.fromNamespaceAndPath(Constants.MODID, "textures/gui/toggle.png");
   ResourceLocation enableButtonAsset = ResourceLocation.fromNamespaceAndPath(Constants.MODID, "textures/gui/sync.png");

   ResourceLocation globalObjectAsset = ResourceLocation.fromNamespaceAndPath(Constants.MODID, "textures/gui/global.png");
   ResourceLocation sharedObjectAsset = ResourceLocation.fromNamespaceAndPath(Constants.MODID, "textures/gui/share.png");
}
