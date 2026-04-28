package me.brynview.navidrohim.jmws.client.ui.screen;

import me.brynview.navidrohim.jmws.client.JMWSClientCommon;
import me.brynview.navidrohim.jmws.client.ui.RenderUtils;
import me.brynview.navidrohim.jmws.client.ui.UIConstants;
import me.brynview.navidrohim.jmws.client.ui.elements.Checkbox;
import me.brynview.navidrohim.jmws.client.ui.generic.screen.HasScrollableList;
import me.brynview.navidrohim.jmws.client.ui.generic.screen.NotificationAlertScreen;
import me.brynview.navidrohim.jmws.client.ui.list.IncomingShareRequestsList;
import me.brynview.navidrohim.jmws.client.utils.PlayerUtils;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.joml.Vector2i;
import org.jspecify.annotations.NonNull;

import java.util.Set;

public class ShareRequestScreen extends NotificationAlertScreen implements HasScrollableList {

    private IncomingShareRequestsList incomingShareRequestsList;
    private Checkbox SDAllCheckbox;

    public ShareRequestScreen(Screen parent) {
        super(parent, true);
    }

    @Override
    protected void init() {
        super.init();

        LinearLayout verticalButtonColumnSpacer = LinearLayout.vertical().spacing(4);
        LinearLayout verticalButtonColumnSpacerForSaDa = LinearLayout.vertical().spacing(4);
        LinearLayout horizontalButtonColumnSpacer = LinearLayout.horizontal();

        incomingShareRequestsList = new IncomingShareRequestsList(JMWSCommon.minecraftClientInstance, 0, 0, 0, 0, 50, this);
        RenderUtils.setDimensionsForList(incomingShareRequestsList, this.width, this.height);

        verticalButtonColumnSpacer.addChild(Button.builder(Component.translatable("jmws.ui.requests.accept"), (button) -> this.acceptAll()).width(UIConstants.NAMED_BUTTON_WIDTH).build());
        verticalButtonColumnSpacer.addChild(Button.builder(Component.translatable("jmws.ui.requests.decline"), (button) -> this.declineAll()).width(UIConstants.NAMED_BUTTON_WIDTH).build());

        this.SDAllCheckbox = Checkbox.buildCheckbox(button -> {
            if (button.isChecked) {
                this.incomingShareRequestsList.selectAll();
            } else {
                this.incomingShareRequestsList.unselectAll();
            }
        });

        verticalButtonColumnSpacerForSaDa.addChild(this.SDAllCheckbox);

        horizontalButtonColumnSpacer.addChild(verticalButtonColumnSpacerForSaDa, settings -> settings.paddingRight(4).paddingLeft(6));
        horizontalButtonColumnSpacer.addChild(incomingShareRequestsList, settings -> settings.paddingRight(20));
        horizontalButtonColumnSpacer.addChild(verticalButtonColumnSpacer, settings -> settings.paddingRight(10) );

        horizontalButtonColumnSpacer.arrangeElements();
        FrameLayout.centerInRectangle(horizontalButtonColumnSpacer, 0, 0, this.width, this.height);
        horizontalButtonColumnSpacer.visitWidgets(this::addRenderableWidget);

        incomingShareRequestsList.addWidgets();
    }

    private Set<? extends IncomingShareRequestsList.IncomingRequestFromPlayerEntry> getEntries()
    {
        return this.incomingShareRequestsList.getSelectedEntries();
    }

    private void acceptAll()
    {
        Set<? extends IncomingShareRequestsList.IncomingRequestFromPlayerEntry> entries = getEntries();
        if (entries.isEmpty())
        {
            PlayerUtils.sendUserAlert(Component.translatable("jmws.ui.requests.no_selected_requests"), true, true, MessageType.PENDING);
        }
        for (IncomingShareRequestsList.IncomingRequestFromPlayerEntry entry : entries) {
            entry.accept();
        }
    }

    private void declineAll()
    {
        Set<? extends IncomingShareRequestsList.IncomingRequestFromPlayerEntry> entries = getEntries();
        if (entries.isEmpty())
        {
            PlayerUtils.sendUserAlert(Component.translatable("jmws.ui.requests.no_selected_requests"), true, true, MessageType.PENDING);
        }
        for (IncomingShareRequestsList.IncomingRequestFromPlayerEntry entry : entries) {
            entry.decline();
        }
    }

    @Override
    protected Vector2i getDrawLocationForAlert() {
        return RenderUtils.getPositionRelativeToList(incomingShareRequestsList);
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        if (!this.incomingShareRequestsList.isEmpty())
        {
            graphics.text(this.font, Component.translatable("jmws.ui.requests.requests_amount", this.incomingShareRequestsList.getAmount()), incomingShareRequestsList.getX(), incomingShareRequestsList.getY() - 15, -1);
        }
    }

    @Override
    public void entryPressed()
    {
        if (incomingShareRequestsList.getSelectedEntries().isEmpty())
        {
            SDAllCheckbox.isChecked = false;
            incomingShareRequestsList.isSelectingAll = false;
        } else if (incomingShareRequestsList.getSelectedEntries().size() == incomingShareRequestsList.children().size())
        {
            SDAllCheckbox.isChecked = true;
            incomingShareRequestsList.isSelectingAll = true;
        }
    }

    public static void open()
    {
        JMWSClientCommon.setCurrentUIScreen(new ShareRequestScreen(JMWSClientCommon.currentShareScreen));
    }
}
