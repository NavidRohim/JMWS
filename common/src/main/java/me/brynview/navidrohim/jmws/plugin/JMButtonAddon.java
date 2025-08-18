package me.brynview.navidrohim.jmws.plugin;

import journeymap.api.v2.client.event.FullscreenDisplayEvent;
import journeymap.api.v2.client.fullscreen.IThemeButton;
import me.brynview.navidrohim.jmws.CommonClass;
import me.brynview.navidrohim.jmws.client.helpers.AssetHelper;
import net.minecraft.network.chat.Component;

import static me.brynview.navidrohim.jmws.plugin.JMWSPlugin.updateWaypoints;

public class JMButtonAddon {
    public static void addJMButtons(FullscreenDisplayEvent.AddonButtonDisplayEvent addonButtonDisplayEvent) {

        if (!CommonClass.minecraftClientInstance.isSingleplayer()) {
            IThemeButton buttonEnabled = addonButtonDisplayEvent.getThemeButtonDisplay().addThemeToggleButton(
                    "button.jmws.enable_button",
                    AssetHelper.onOffButtonAsset,
                    CommonClass.getEnabledStatus(),
                    JMButtonAddon::enableMod);

            IThemeButton buttonSync = addonButtonDisplayEvent.getThemeButtonDisplay().addThemeToggleButton(
                    "button.jmws.update_button",
                    AssetHelper.enableButtonAsset,
                    true,
                    JMButtonAddon::updateFromButton);

            buttonSync.setEnabled(CommonClass.getEnabledStatus());
            buttonSync.setTooltip(Component.translatable("button.jmws.tooltip.update_button").getString());

            buttonEnabled.setTooltip(Component.translatable("button.jmws.tooltip.enable_button").getString());
        }
    }

    private static void enableMod(IThemeButton iThemeButton) {
        iThemeButton.setLabels(
                Component.translatable("addServer.resourcePack.enabled").getString(),
                Component.translatable("addServer.resourcePack.disabled").getString()
        );

        if (CommonClass.getEnabledStatus()) {
            CommonClass.config.enabled.set(false);
            iThemeButton.setToggled(false);
        } else {
            CommonClass.config.enabled.set(true);
            iThemeButton.setToggled(true);
        }
    }

    private static void updateFromButton(IThemeButton iThemeButton) {
        updateWaypoints(true);
    }
}
