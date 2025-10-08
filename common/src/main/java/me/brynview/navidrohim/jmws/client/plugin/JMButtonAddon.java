package me.brynview.navidrohim.jmws.client.plugin;

import journeymap.api.v2.client.event.FullscreenDisplayEvent;
import journeymap.api.v2.client.fullscreen.IThemeButton;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.client.helper.AssetHelper;
import net.minecraft.network.chat.Component;

import static me.brynview.navidrohim.jmws.client.plugin.JMWSPlugin.updateWaypoints;

/**
 * Static methods for anything regarding buttons on the fullscreen.
 */
public class JMButtonAddon {

    /**
     * Event called by ADDON_BUTTON_DISPLAY_EVENT which is used to add custom buttons.
     * @param addonButtonDisplayEvent The event called by ADDON_BUTTON_DISPLAY_EVENT
     */
    public static void addJMButtons(FullscreenDisplayEvent.AddonButtonDisplayEvent addonButtonDisplayEvent) {

        if (!CommonClass.isInternalServer()) {

            // Button for enabling and disabling JMWS
            IThemeButton buttonEnabled = addonButtonDisplayEvent.getThemeButtonDisplay().addThemeToggleButton(
                    "button.jmws.enable_button",
                    AssetHelper.onOffButtonAsset,
                    CommonClass.getEnabledStatus(),
                    JMButtonAddon::enableMod);

            // Manual sync button
            IThemeButton buttonSync = addonButtonDisplayEvent.getThemeButtonDisplay().addThemeToggleButton(
                    "button.jmws.update_button",
                    AssetHelper.enableButtonAsset,
                    true,
                    JMButtonAddon::updateFromButton);

            buttonSync.setEnabled(CommonClass.getEnabledStatus()); // Sync button will only be functional if JMWS is enabled.
            buttonSync.setTooltip(Component.translatable("button.jmws.tooltip.update_button").getString());

            buttonEnabled.setTooltip(Component.translatable("button.jmws.tooltip.enable_button").getString());
        }
    }

    /**
     * Function that will be called when the JMWS enable / disable button is pressed.
     * @param iThemeButton the button, will be JMWS on / off button (buttonEnabled)
     */
    private static void enableMod(IThemeButton iThemeButton) {
        iThemeButton.setLabels(
                Component.translatable("addServer.resourcePack.enabled").getString(),
                Component.translatable("addServer.resourcePack.disabled").getString()
        );

        if (CommonClass.getEnabledStatus()) { // Turn off
            CommonClass.config.enabled.set(false);
            iThemeButton.setToggled(false);
        } else { // Turn on
            CommonClass.config.enabled.set(true);
            iThemeButton.setToggled(true);
        }
    }

    /**
     * Called when manual sync button is pressed.
     * @param iThemeButton the button, will be the manual sync button
     */
    private static void updateFromButton(IThemeButton iThemeButton) {
        updateWaypoints(true);
    }
}
