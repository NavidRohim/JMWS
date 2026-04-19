package me.brynview.navidrohim.jmws.client.ui.screen;

import me.brynview.navidrohim.jmws.client.JMWSClientCommon;
import me.brynview.navidrohim.jmws.client.share.request.ShareRequest;
import me.brynview.navidrohim.jmws.client.ui.UIConstants;
import me.brynview.navidrohim.jmws.client.ui.generic.screen.NotificationAlertScreen;
import me.brynview.navidrohim.jmws.client.ui.requests_screen.IncomingShareRequestsList;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

public class ShareRequestScreen extends NotificationAlertScreen {

    private IncomingShareRequestsList incomingShareRequestsList;

    public ShareRequestScreen(Screen parent) {
        super(parent);
    }

    @Override
    protected void init() {
        //super.init();

        int panelWidth = (int) (this.width * 0.75);
        int panelHeight = (int) (this.height * 0.65);
        int panelX = this.width / 10;
        int panelY = (this.height - panelHeight) / 2;

        LinearLayout verticalButtonColumnSpacer = LinearLayout.vertical().spacing(4);
        LinearLayout horizontalButtonColumnSpacer = LinearLayout.horizontal().spacing(20);

        incomingShareRequestsList = new IncomingShareRequestsList(JMWSCommon.minecraftClientInstance, panelWidth, panelHeight, panelX, panelY, 50);

        verticalButtonColumnSpacer.addChild(Button.builder(Component.literal("Decline all"), (button) -> JMWSClientCommon.incomingShareRequests.values().forEach(ShareRequest::decline)).width(UIConstants.DONE_BUTTON_WIDTH).build());
        verticalButtonColumnSpacer.addChild(Button.builder(Component.literal("Accept all"), (button) -> JMWSClientCommon.incomingShareRequests.values().forEach(ShareRequest::accept)).width(UIConstants.DONE_BUTTON_WIDTH).build());

        horizontalButtonColumnSpacer.addChild(incomingShareRequestsList);
        horizontalButtonColumnSpacer.addChild(verticalButtonColumnSpacer);

        horizontalButtonColumnSpacer.arrangeElements();
        FrameLayout.centerInRectangle(horizontalButtonColumnSpacer, 0, 0, this.width, this.height);
        horizontalButtonColumnSpacer.visitWidgets(this::addRenderableWidget);

        incomingShareRequestsList.addWidgets();
    }

    @Override
    protected Vector2i getDrawLocationForAlert() {
        int y = incomingShareRequestsList.getY() + incomingShareRequestsList.getHeight() + 15;
        return new Vector2i(incomingShareRequestsList.getX(), y);
    }

    public static void open()
    {
        JMWSClientCommon.setCurrentUIScreen(new ShareRequestScreen(JMWSClientCommon.currentShareScreen));
    }
}
