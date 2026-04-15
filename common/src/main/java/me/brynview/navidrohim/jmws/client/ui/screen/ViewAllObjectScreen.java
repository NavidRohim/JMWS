package me.brynview.navidrohim.jmws.client.ui.screen;

import me.brynview.navidrohim.jmws.client.JMWSClientCommon;
import net.minecraft.client.gui.screens.Screen;

public class ViewAllObjectScreen extends NotificationAlertScreen
{
    public ViewAllObjectScreen(Screen parent) {
        super(parent);
    }

    public static void open()
    {
        JMWSClientCommon.setCurrentUIScreen(new ViewAllObjectScreen(JMWSClientCommon.currentShareScreen));
    }
}
