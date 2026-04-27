package me.brynview.navidrohim.jmws.client.ui.screen;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.JMWSClientCommon;
import me.brynview.navidrohim.jmws.client.share.request.OutgoingShareRequest;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientBaseObjectWrapper;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientObjectWrapper;
import me.brynview.navidrohim.jmws.client.ui.generic.screen.HasScrollableList;
import me.brynview.navidrohim.jmws.client.ui.list.entry.PlayerEntry;
import me.brynview.navidrohim.jmws.client.ui.RenderUtils;
import me.brynview.navidrohim.jmws.client.ui.UIConstants;
import me.brynview.navidrohim.jmws.client.ui.elements.Checkbox;
import me.brynview.navidrohim.jmws.client.ui.generic.screen.NotificationAlertScreen;
import me.brynview.navidrohim.jmws.client.ui.list.ObjectSharePanel;
import me.brynview.navidrohim.jmws.client.utils.PlayerUtils;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.joml.Vector2i;
import org.jspecify.annotations.NonNull;

import java.util.Set;

import static me.brynview.navidrohim.jmws.common.JMWSCommon.minecraftClientInstance;

public class ShareScreen extends NotificationAlertScreen implements HasScrollableList {

    private final String displayName;
    private final ClientObjectWrapper<?> object;

    private Checkbox sACheckbox;
    private ObjectSharePanel<ClientObjectWrapper<?>> sharePanel;

    private static final Tooltip SELECT_ALL_TOGGLE = Tooltip.create(Component.translatable("jmws.ui.sharing.toggle_select_all.tooltip"));
    private static final Tooltip SEND_TO_SELECTED = Tooltip.create(Component.translatable("jmws.ui.sharing.send_request.tooltip"));

    public ShareScreen(Screen parent, ClientBaseObjectWrapper<?> object) {
        super(parent);
        this.object = object;
        this.displayName = RenderUtils.shortenObjectName(object.getName(), 20);
    }

    @Override
    protected void init()
    {
        // Define the sharing panel and add all shared objects on this client to panel
        this.sharePanel = new ObjectSharePanel<>(minecraftClientInstance,  0, 0, 0, 0, 50, object, this);
        RenderUtils.setDimensionsForList(this.sharePanel, this.width, this.height);

        LinearLayout buttonIcnColumb = LinearLayout.vertical().spacing(4);
        LinearLayout buttonColumn = LinearLayout.vertical().spacing(4);
        LinearLayout mainRow = LinearLayout.horizontal();

        buttonColumn.addChild(Button.builder(CommonComponents.GUI_DONE, (bnt) -> this.onClose()).width(UIConstants.NAMED_BUTTON_WIDTH).build());
        buttonColumn.addChild(Button.builder(Component.translatable("jmws.ui.sharing.reload"), (bnt) -> this.refresh()).width(UIConstants.NAMED_BUTTON_WIDTH).build());
        buttonColumn.addChild(Button.builder(Component.translatable("jmws.ui.sharing.send_requests"), (bnt) -> this.sendRequests()).width(UIConstants.NAMED_BUTTON_WIDTH).tooltip(SEND_TO_SELECTED).build());

        this.sACheckbox = Checkbox.buildCheckbox(Component.empty(), button -> {
            if (button.isChecked) {
                this.sharePanel.selectAll();
            } else {
                this.sharePanel.unselectAll();
            }
        });
        this.sACheckbox.setTooltip(SELECT_ALL_TOGGLE);

        buttonIcnColumb.addChild(this.sACheckbox);

        mainRow.addChild(buttonIcnColumb, layoutSettings -> layoutSettings.paddingRight(6).paddingLeft(6));
        mainRow.addChild(this.sharePanel, layoutSettings -> layoutSettings.paddingRight(20));
        mainRow.addChild(buttonColumn, layoutSettings -> layoutSettings.paddingRight(20));

        mainRow.arrangeElements();
        FrameLayout.centerInRectangle(mainRow, 0, 0, this.width, this.height);
        mainRow.visitWidgets(this::addRenderableWidget);

        sharePanel.addWidgets();
    }

    private void refresh()
    {
        this.sharePanel.refresh();
        this.sACheckbox.isChecked = false;
    }

    private void sendRequests()
    {
        Set<? extends PlayerEntry<?>> players = this.sharePanel.getSelectedEntries();
        if (players.isEmpty())
        {
            PlayerUtils.sendUserAlert(Component.translatable("jmws.ui.sharing.no_selected_players"), true, true, MessageType.PENDING);
        } else {
            for (PlayerEntry<?> selectedPlayer : players) {
                OutgoingShareRequest.sendShareRequest(this.object, selectedPlayer.user.getProfile());
            }
            this.sharePanel.unselectAll();
        }

    }

    @Override
    protected Vector2i getDrawLocationForAlert() {
        return RenderUtils.getPositionRelativeToList(this.sharePanel);
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        if (!this.sharePanel.isEmpty())
        {
            graphics.text(this.font, Component.translatable("jmws.ui.sharing.share_object", this.object.getType().getReadableName(), this.displayName), sharePanel.getX(), sharePanel.getY() - 15, -1);
        }
    }

    @Override
    public void entryPressed()
    {
        if (sharePanel.getSelectedEntries().isEmpty())
        {
            this.sACheckbox.isChecked = false;
            this.sharePanel.isSelectingAll = false;
        } else if (sharePanel.getSelectedEntries().size() == this.sharePanel.children().size())
        {
            this.sACheckbox.isChecked = true;
            this.sharePanel.isSelectingAll = true;
        }
    }

    public static void open(ClientBaseObjectWrapper<?> object)
    {
       JMWSClientCommon.setCurrentUIScreen(new ShareScreen(minecraftClientInstance.screen, object));
    }
}
