package me.brynview.navidrohim.jmws.client.ui.screen;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.JMWSClientCommon;
import me.brynview.navidrohim.jmws.client.ui.RenderUtils;
import me.brynview.navidrohim.jmws.client.ui.UIConstants;
import me.brynview.navidrohim.jmws.client.ui.elements.Checkbox;
import me.brynview.navidrohim.jmws.client.ui.elements.IconButton;
import me.brynview.navidrohim.jmws.client.ui.generic.screen.HasScrollableList;
import me.brynview.navidrohim.jmws.client.ui.generic.screen.NotificationAlertScreen;
import me.brynview.navidrohim.jmws.client.ui.list.IncomingShareRequestsList;
import me.brynview.navidrohim.jmws.client.utils.PlayerUtils;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import org.joml.Vector2i;
import org.jspecify.annotations.NonNull;

import java.util.Set;

public class ShareRequestScreen extends NotificationAlertScreen implements HasScrollableList {

    private static final Identifier ACCEPT = Identifier.fromNamespaceAndPath(Constants.MODID, "accept");
    private static final Identifier DECLINE = Identifier.fromNamespaceAndPath(Constants.MODID, "decline");

    private static final Tooltip ACCEPT_TOOLTIP = Tooltip.create(Component.translatable("jmws.ui.requests.accept_selected"));
    private static final Tooltip DECLINE_TOOLTIP = Tooltip.create(Component.translatable("jmws.ui.requests.decline_selected"));

    private IncomingShareRequestsList incomingShareRequestsList;
    private Checkbox checkbox;

    public ShareRequestScreen(Screen parent) {
        super(parent, true);
    }

    @Override
    protected void init() {
        super.init();

        LinearLayout listActionButtonColumn = LinearLayout.vertical().spacing(4);
        LinearLayout iconButtonColumn = LinearLayout.vertical().spacing(2);
        LinearLayout parentHorizontalLayout = LinearLayout.horizontal();

        incomingShareRequestsList = new IncomingShareRequestsList(JMWSCommon.minecraftClientInstance, 0, 0, 0, 0, 50, this);
        RenderUtils.setDimensionsForList(incomingShareRequestsList, this.width, this.height);

        iconButtonColumn.addChild(IconButton.buildGenericButton(ACCEPT, (button -> this.acceptAll()), UIConstants.GREEN_COLOUR, ACCEPT_TOOLTIP, null));
        iconButtonColumn.addChild(IconButton.buildGenericButton(DECLINE, (button -> this.declineAll()), UIConstants.RED_COLOUR, DECLINE_TOOLTIP, null));
        iconButtonColumn.addChild(IconButton.buildGenericButton(UIConstants.REFRESH, (button -> this.incomingShareRequestsList.refresh()), UIConstants.YELLOW_COLOUR, UIConstants.REFRESH_TOOLTIP, null));
        this.checkbox = Checkbox.buildSelectAllCheckbox(button -> {
            if (button.isChecked) {
                this.incomingShareRequestsList.selectAll();
            } else {
                this.incomingShareRequestsList.unselectAll();
            }
        });
        iconButtonColumn.addChild(this.checkbox, settings -> settings.paddingLeft(4).paddingRight(4));

        parentHorizontalLayout.addChild(iconButtonColumn, settings -> settings.paddingRight(4).paddingLeft(6));
        parentHorizontalLayout.addChild(incomingShareRequestsList, settings -> settings.paddingRight(4));
        parentHorizontalLayout.addChild(listActionButtonColumn, settings -> settings.paddingRight(10) );

        parentHorizontalLayout.arrangeElements();
        FrameLayout.centerInRectangle(parentHorizontalLayout, 0, 0, this.width, this.height);
        parentHorizontalLayout.visitWidgets(this::addRenderableWidget);

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
            MutableComponent text = Component.translatable("jmws.ui.requests.requests_amount", this.incomingShareRequestsList.getAmount());
            graphics.text(this.font, text, incomingShareRequestsList.getX() + incomingShareRequestsList.getWidth() / 2 - (this.font.width(text) / 2), incomingShareRequestsList.getY() - 15, -1);
        }
    }

    @Override
    public void entryPressed()
    {
        if (incomingShareRequestsList.getSelectedEntries().isEmpty())
        {
            this.checkbox.isChecked = false;
            incomingShareRequestsList.isSelectingAll = false;
        } else if (incomingShareRequestsList.getSelectedEntries().size() == incomingShareRequestsList.children().size())
        {
            this.checkbox.isChecked = true;
            incomingShareRequestsList.isSelectingAll = true;
        }
    }

    public static void open()
    {
        JMWSClientCommon.setCurrentUIScreen(new ShareRequestScreen(JMWSClientCommon.currentNotificationScreen));
    }
}
