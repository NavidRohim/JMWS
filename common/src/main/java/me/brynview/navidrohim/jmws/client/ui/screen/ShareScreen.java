package me.brynview.navidrohim.jmws.client.ui.screen;

import me.brynview.navidrohim.jmws.client.JMWSClientCommon;
import me.brynview.navidrohim.jmws.client.share.request.OutgoingShareRequest;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientBaseObjectWrapper;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientObjectWrapper;
import me.brynview.navidrohim.jmws.client.ui.PlayerEntry;
import me.brynview.navidrohim.jmws.client.ui.UIConstants;
import me.brynview.navidrohim.jmws.client.ui.generic.screen.NotificationAlertScreen;
import me.brynview.navidrohim.jmws.client.ui.share_panel.ObjectSharePanel;
import me.brynview.navidrohim.jmws.client.ui.generic.entry.SelectableLabelEntry;
import me.brynview.navidrohim.jmws.client.utils.PlayerUtils;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.joml.Vector2i;
import org.jspecify.annotations.NonNull;

import java.util.Set;

import static me.brynview.navidrohim.jmws.common.JMWSCommon.minecraftClientInstance;

public class ShareScreen<T extends ClientObjectWrapper<?>> extends NotificationAlertScreen {

    private final ClientObjectWrapper<?> object;
    private ObjectSharePanel<ClientObjectWrapper<?>> sharePanel;

    public ShareScreen(Screen parent, ClientBaseObjectWrapper<?> object) {
        super(parent);
        this.object = object;
    }

    private int getCornerXWithSpacing(int width, int spacing)
    {
        return this.width - (width + spacing);
    }

    private int getCornerYWithSpacing(int height, int spacing, int row)
    {
        return this.height - (height + spacing) * row;
    }

    @Override
    protected void init()
    {
        // Define the sharing panel and add all shared objects on this client to panel
        this.sharePanel = new ObjectSharePanel<>(minecraftClientInstance,  UIConstants.PLAYER_LIST_WIDTH, UIConstants.PLAYER_LIST_HEIGHT,this.width / 10, (height / 2) - (UIConstants.PLAYER_LIST_HEIGHT / 2), 50, object);
        this.sharePanel.addWidgets();
        // Add close button and share panel
        this.addRenderableWidget(this.sharePanel);

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (bnt) -> this.onClose()).bounds(getCornerXWithSpacing(UIConstants.DONE_BUTTON_WIDTH, UIConstants.ELEMENT_SPACING), getCornerYWithSpacing(UIConstants.DONE_BUTTON_HEIGHT, UIConstants.ELEMENT_SPACING, 1), UIConstants.DONE_BUTTON_WIDTH, UIConstants.DONE_BUTTON_HEIGHT).build());
        this.addRenderableWidget(Button.builder(Component.translatable("jmws.ui.sharing.reload"), (bnt) -> this.sharePanel.refresh()).bounds(getCornerXWithSpacing(UIConstants.DONE_BUTTON_WIDTH, UIConstants.ELEMENT_SPACING), getCornerYWithSpacing(UIConstants.DONE_BUTTON_HEIGHT, UIConstants.ELEMENT_SPACING, 2), UIConstants.DONE_BUTTON_WIDTH, UIConstants.DONE_BUTTON_HEIGHT).build());
        this.addRenderableWidget(Button.builder(Component.translatable("jmws.ui.sharing.send_requests"), (bnt) -> this.sendRequests()).bounds(getCornerXWithSpacing(UIConstants.DONE_BUTTON_WIDTH, UIConstants.ELEMENT_SPACING), getCornerYWithSpacing(UIConstants.DONE_BUTTON_HEIGHT, UIConstants.ELEMENT_SPACING, 3), UIConstants.DONE_BUTTON_WIDTH, UIConstants.DONE_BUTTON_HEIGHT).build());
    }

    private void sendRequests()
    {
        Set<SelectableLabelEntry> players = this.sharePanel.getSelectedEntries();
        if (players.isEmpty())
        {
            PlayerUtils.sendUserAlert(Component.translatable("jmws.ui.sharing.no_selected_players"), true, true, MessageType.PENDING);
        } else {
            for (SelectableLabelEntry selectedPlayer : players) {
                if (selectedPlayer instanceof PlayerEntry playerEntry)
                {
                    OutgoingShareRequest.sendShareRequest(this.object, playerEntry.user.getProfile());
                }
            }
            this.sharePanel.unselectAll();
        }

    }

    @Override
    protected Vector2i getDrawLocation() {
        int alertX = this.sharePanel.getX();
        int sharePanelBottom = this.sharePanel.getY() + UIConstants.PLAYER_LIST_HEIGHT;
        int alertY = sharePanelBottom + ((this.height - sharePanelBottom) / 2) - (this.font.lineHeight / 2);
        return new Vector2i(alertX, alertY);
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);

        int x = (this.width / 10) + UIConstants.PLAYER_LIST_WIDTH + 20;
        graphics.text(this.font, Component.translatable("jmws.ui.sharing.share_object", this.object.getType().getReadableName(), this.object.getName()), x, (height / 2) - (UIConstants.PLAYER_LIST_HEIGHT / 2), -1);
    }

    public static void openShare(ClientBaseObjectWrapper<?> object)
    {
       JMWSClientCommon.setCurrentUIScreen(new ShareScreen<ClientBaseObjectWrapper<?>>(minecraftClientInstance.screen, object));
    }
}
