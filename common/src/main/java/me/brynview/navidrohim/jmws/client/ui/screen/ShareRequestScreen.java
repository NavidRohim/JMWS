package me.brynview.navidrohim.jmws.client.ui.screen;

import me.brynview.navidrohim.jmws.client.JMWSClientCommon;
import me.brynview.navidrohim.jmws.client.ui.generic.screen.NotificationAlertScreen;
import me.brynview.navidrohim.jmws.client.ui.requests_screen.IncomingShareRequestsList;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

public class ShareRequestScreen extends NotificationAlertScreen {

    private @Nullable IncomingShareRequestsList incomingShareRequestsList;

    private static final int WIDTH = 200;
    private static final int HEIGHT = 100;

    public ShareRequestScreen(Screen parent) {
        super(parent);
    }

    @Override
    protected void init() {
        super.init();

        this.incomingShareRequestsList = new IncomingShareRequestsList(JMWSCommon.minecraftClientInstance, WIDTH, HEIGHT, this.width / 2 - WIDTH / 2, this.height / 2 - HEIGHT / 2, 64);
        this.addRenderableWidget(this.incomingShareRequestsList);

    }

    public static void open()
    {
        JMWSClientCommon.setCurrentUIScreen(new ShareRequestScreen(JMWSClientCommon.currentShareScreen));
    }
}
