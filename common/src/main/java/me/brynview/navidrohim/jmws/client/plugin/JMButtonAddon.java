package me.brynview.navidrohim.jmws.client.plugin;

import journeymap.api.v2.client.event.FullscreenDisplayEvent;
import journeymap.api.v2.client.fullscreen.IThemeButton;
import me.brynview.navidrohim.jmws.client.JMWSClientCommon;
import me.brynview.navidrohim.jmws.client.config.ConfigInterface;
import me.brynview.navidrohim.jmws.client.ui.screen.ShareRequestScreen;
import me.brynview.navidrohim.jmws.client.ui.screen.ViewAllObjectScreen;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import me.brynview.navidrohim.jmws.client.assets.JMWSTextures;
import net.minecraft.network.chat.Component;

/**
 * Static methods for anything regarding buttons on the fullscreen.
 */
public class JMButtonAddon {

    /**
     * Event called by ADDON_BUTTON_DISPLAY_EVENT which is used to add custom buttons.
     * @param addonButtonDisplayEvent The event called by ADDON_BUTTON_DISPLAY_EVENT
     */
    public static void addJMButtons(FullscreenDisplayEvent.AddonButtonDisplayEvent addonButtonDisplayEvent) {

        if (!JMWSCommon.isInternalServer()) {

            // Button for enabling and disabling JMWS
            IThemeButton buttonEnabled = addonButtonDisplayEvent.getThemeButtonDisplay().addThemeToggleButton(
                    "button.jmws.enable_button",
                    JMWSTextures.onOffButtonAsset,
                    ConfigInterface.getEnabledStatus(),
                    JMButtonAddon::enableMod);

            // Manual sync button
            IThemeButton buttonSync = addonButtonDisplayEvent.getThemeButtonDisplay().addThemeToggleButton(
                    "button.jmws.update_button",
                    JMWSTextures.enableButtonAsset,
                    true,
                    JMButtonAddon::updateFromButton);

            IThemeButton buttonViewObjectsForSharing = addonButtonDisplayEvent.getThemeButtonDisplay().addThemeToggleButton(
                    "button.jmws.incoming_share_requests",
                    JMWSTextures.sharedObjectAsset,
                    ConfigInterface.getEnabledStatus(),
                    (IThemeButton iThemeButton) -> ShareRequestScreen.open()
            );

            buttonSync.setEnabled(ConfigInterface.getEnabledStatus()); // Sync button will only be functional if JMWS is enabled.
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
                Component.translatable("button.jmws.enable_button.enabled").getString(),
                Component.translatable("button.jmws.enable_button.disabled").getString()
        );

        if (ConfigInterface.getEnabledStatus()) { // Turn off
            JMWSClientCommon.config.enabled.set(false);
            iThemeButton.setToggled(false);
        } else { // Turn on
            JMWSClientCommon.config.enabled.set(true);
            iThemeButton.setToggled(true);
        }
    }

    /**
     * Called when the manual sync button is pressed.
     * @param iThemeButton the button. Will be the manual sync button
     */
    private static void updateFromButton(IThemeButton iThemeButton) {
        JMWSPlugin.sync(true);
    }
}
