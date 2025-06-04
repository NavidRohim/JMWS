package me.brynview.navidrohim.jmws.client.helpers;

import me.brynview.navidrohim.jmws.JMWS;
import net.minecraft.util.Identifier;

public interface AssetHelper {
    Identifier onOffButtonAsset = Identifier.of(JMWS.MODID, "textures/gui/on_off_button.png");
    Identifier enableButtonAsset = Identifier.of(JMWS.MODID, "textures/gui/update_button.png");
}
