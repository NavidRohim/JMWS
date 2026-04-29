package me.brynview.navidrohim.jmws.client.ui.screen;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.JMWSClientCommon;
import me.brynview.navidrohim.jmws.client.share.request.OutgoingShareRequest;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientBaseObjectWrapper;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientObjectWrapper;
import me.brynview.navidrohim.jmws.client.ui.elements.IconButton;
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

import static me.brynview.navidrohim.jmws.common.JMWSCommon.minecraftClientInstance;

public class ShareScreen extends NotificationAlertScreen implements HasScrollableList {

    private static final Identifier SEND = Identifier.fromNamespaceAndPath(Constants.MODID, "send");
    private static final Identifier STOP_SHARE = Identifier.fromNamespaceAndPath(Constants.MODID, "stop_share");
    private static final Tooltip SEND_TOOLTIP = Tooltip.create(Component.translatable("jmws.ui.sharing.send.tooltip"));
    private static final Tooltip STOP_SHARE_TOOLTIP = Tooltip.create(Component.translatable("jmws.ui.sharing.stop_share.tooltip"));

    private final String displayName;
    private final ClientObjectWrapper<?> object;

    private Checkbox sACheckbox;
    private ObjectSharePanel<ClientObjectWrapper<?>> sharePanel;

    public ShareScreen(Screen parent, ClientBaseObjectWrapper<?> object) {
        super(parent, true);
        this.object = object;
        this.displayName = RenderUtils.shortenObjectName(object.getName(), 20);
    }

    @Override
    protected void init()
    {
        super.init();
        // Define the sharing panel and add all shared objects on this client to panel
        this.sharePanel = new ObjectSharePanel<>(minecraftClientInstance,  0, 0, 0, 0, 50, object, this);
        RenderUtils.setDimensionsForList(this.sharePanel, this.width, this.height);

        this.sACheckbox = Checkbox.buildSelectAllCheckbox(button -> {
            if (button.isChecked) {
                this.sharePanel.selectAll();
            } else {
                this.sharePanel.unselectAll();
            }
        });

        LinearLayout buttonIcnColumn = LinearLayout.vertical().spacing(4);
        LinearLayout mainRow = LinearLayout.horizontal();
        //LinearLayout systemColumn = LinearLayout.vertical().spacing(6);

        buttonIcnColumn.addChild(IconButton.buildGenericButton(SEND, (btn) -> this.sendRequests(), MessageType.SUCCESS.getNumericalColour(), SEND_TOOLTIP));
        buttonIcnColumn.addChild(IconButton.buildGenericButton(STOP_SHARE, (bnt) -> this.stopSharing(), MessageType.FAILURE.getNumericalColour(), STOP_SHARE_TOOLTIP));
        buttonIcnColumn.addChild(IconButton.buildGenericButton(UIConstants.REFRESH, (btn) -> this.refresh(true), MessageType.PENDING.getNumericalColour(), UIConstants.REFRESH_TOOLTIP));

        buttonIcnColumn.addChild(this.sACheckbox, settings -> settings.paddingRight(4).paddingLeft(4));

        mainRow.addChild(buttonIcnColumn, layoutSettings -> layoutSettings.paddingRight(6).paddingLeft(6));
        mainRow.addChild(this.sharePanel, layoutSettings -> layoutSettings.paddingRight(14));
        //mainRow.addChild(systemColumn, layoutSettings -> layoutSettings.paddingRight(6));

        mainRow.arrangeElements();
        FrameLayout.centerInRectangle(mainRow, 0, 0, this.width, this.height);
        mainRow.visitWidgets(this::addRenderableWidget);

        sharePanel.addWidgets();
    }

    private void refresh(boolean sendAlert)
    {
        if (sendAlert)
        {
            PlayerUtils.sendUserAlert(UIConstants.REFRESHING, true, true, MessageType.NEUTRAL);
        }
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
                if (selectedPlayer.isOnline)
                {
                    OutgoingShareRequest.sendShareRequest(this.object, selectedPlayer.user.getProfile());
                }
            }
            this.sharePanel.unselectAll();
        }
    }

    private void stopSharing()
    {
        Set<? extends PlayerEntry<?>> players = this.sharePanel.getSelectedEntries();
        if (players.isEmpty())
        {
            PlayerUtils.sendUserAlert(Component.translatable("jmws.ui.sharing.no_selected_players"), true, true, MessageType.PENDING);
        } else {
            for (PlayerEntry<?> selectedPlayer : players) {
                String playerDisplayName = selectedPlayer.isOnline ? selectedPlayer.user.getProfile().name() : selectedPlayer.userUuid.toString();
                if (object.getSharedTo().contains(selectedPlayer.userUuid))
                {
                    selectedPlayer.setStoppedSharing();
                } else {
                    PlayerUtils.sendUserAlert(Component.literal("Not sharing with %s!".formatted(playerDisplayName)), true, true, MessageType.WARNING);
                }
            }
            //this.refresh(false);
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
            MutableComponent sharingText = Component.translatable("jmws.ui.sharing.share_object", this.object.getType().getReadableName(), this.displayName);
            graphics.text(this.font, sharingText, sharePanel.getX() + sharePanel.getWidth() / 2 - (this.font.width(sharingText) / 2), sharePanel.getY() - 15, -1);
        }
    }

    @Override
    public void entryPressed()
    {
        if (sharePanel.getSelectedEntries().isEmpty())
        {
            this.sACheckbox.uncheck();
            this.sharePanel.isSelectingAll = false;
        } else if (sharePanel.getSelectedEntries().size() == this.sharePanel.children().size())
        {
            this.sACheckbox.check();
            this.sharePanel.isSelectingAll = true;
        }
    }

    public static void open(ClientBaseObjectWrapper<?> object)
    {
       JMWSClientCommon.setCurrentUIScreen(new ShareScreen(minecraftClientInstance.screen, object));
    }
}
