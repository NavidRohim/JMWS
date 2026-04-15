package me.brynview.navidrohim.jmws.client.ui.screen;

import me.brynview.navidrohim.jmws.client.JMWSClientCommon;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import net.minecraft.client.gui.screens.Screen;

public class GenericScreen extends NotificationAlertScreen {
    public GenericScreen(Screen parent) {
        super(parent);
    }

    public static void open()
    {
        JMWSClientCommon.setCurrentUIScreen(new GenericScreen(JMWSCommon.minecraftClientInstance.screen));
    }
}
