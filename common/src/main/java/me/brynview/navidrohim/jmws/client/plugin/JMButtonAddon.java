package me.brynview.navidrohim.jmws.client.plugin;

import journeymap.api.v2.client.event.FullscreenDisplayEvent;
import journeymap.api.v2.client.fullscreen.IThemeButton;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.JMWSClientCommon;
import me.brynview.navidrohim.jmws.client.config.ConfigInterface;
import me.brynview.navidrohim.jmws.client.ui.screen.ShareRequestScreen;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import me.brynview.navidrohim.jmws.client.assets.JMWSTextures;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Static methods for anything regarding buttons on the fullscreen.
 */
public class JMButtonAddon {

    private static final List<IThemeButton> buttons = new ArrayList<>();
    private static IThemeButton toggleButton;

    private static boolean canUseFromClient()
    {
        return ConfigInterface.getEnabledStatus();
    }

    private static boolean canUseFromServer()
    {
        return JMWSClientCommon.serverHasMod && JMWSClientCommon.serverConfig.jmwsEnabled;
    }

    private static boolean canUseGenerally()
    {
        return canUseFromClient() && canUseFromServer();
    }

    private static void setButtonState(boolean setMaster)
    {
        boolean canUse = canUseGenerally();

        for (IThemeButton button : buttons)
        {
            button.setEnabled(canUse);
            button.setToggled(canUse);
        }

        if (setMaster)
        {
            toggleButton.setEnabled(canUseFromServer());
        }
    }
    /**
     * Event called by ADDON_BUTTON_DISPLAY_EVENT which is used to add custom buttons.
     * @param addonButtonDisplayEvent The event called by ADDON_BUTTON_DISPLAY_EVENT
     */
    public static void addJMButtons(FullscreenDisplayEvent.AddonButtonDisplayEvent addonButtonDisplayEvent) {

        if (!JMWSCommon.isInternalServer()) {

            buttons.clear();

            // Button for enabling and disabling JMWS
            toggleButton = addonButtonDisplayEvent.getThemeButtonDisplay().addThemeToggleButton(
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

            toggleButton.setLabels(
                    Component.translatable("button.jmws.enable_button.enabled").getString(),
                    Component.translatable("button.jmws.enable_button.disabled").getString()
            );

            buttonSync.setTooltip("button.jmws.tooltip.update_button");
            toggleButton.setTooltip("button.jmws.tooltip.enable_button");
            buttonViewObjectsForSharing.setTooltip("button.jmws.incoming_share_requests.tooltip");

            buttons.add(buttonSync);
            buttons.add(buttonViewObjectsForSharing);

            setButtonState(true);
        }
    }

    /**
     * Function that will be called when the JMWS enable / disable button is pressed.
     * @param iThemeButton the button, will be JMWS on / off button (buttonEnabled)
     */
    private static void enableMod(IThemeButton iThemeButton) {

        boolean state = !JMWSClientCommon.config.enabled.get();
        JMWSClientCommon.config.enabled.set(state);
        toggleButton.setToggled(state);

        setButtonState(false);
    }

    /**
     * Called when the manual sync button is pressed.
     * @param iThemeButton the button. Will be the manual sync button
     */
    private static void updateFromButton(IThemeButton iThemeButton)
    {
        JMWSPlugin.sync(true);
    }
}
