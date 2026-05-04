package me.brynview.navidrohim.jmws.client.ui.screen;

import com.mojang.datafixers.kinds.IdF;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.JMWSClientCommon;
import me.brynview.navidrohim.jmws.client.share.request.OutgoingShareRequest;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientBaseObjectWrapper;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientObjectWrapper;
import me.brynview.navidrohim.jmws.client.syncing.rules.ClientShareRule;
import me.brynview.navidrohim.jmws.client.ui.elements.IconButton;
import me.brynview.navidrohim.jmws.client.ui.generic.screen.HasScrollableList;
import me.brynview.navidrohim.jmws.client.ui.list.entry.PlayerEntry;
import me.brynview.navidrohim.jmws.client.ui.RenderUtils;
import me.brynview.navidrohim.jmws.client.ui.UIConstants;
import me.brynview.navidrohim.jmws.client.ui.elements.Checkbox;
import me.brynview.navidrohim.jmws.client.ui.generic.screen.NotificationAlertScreen;
import me.brynview.navidrohim.jmws.client.ui.list.ObjectSharePanel;
import me.brynview.navidrohim.jmws.client.utils.PlayerUtils;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.ServerReconfigScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import org.joml.Vector2i;
import org.jspecify.annotations.NonNull;

import javax.swing.*;
import java.util.Set;
import java.util.function.Consumer;

import static me.brynview.navidrohim.jmws.common.JMWSCommon.minecraftClientInstance;

public class ShareScreen extends NotificationAlertScreen implements HasScrollableList {

    private static final Identifier SEND = Identifier.fromNamespaceAndPath(Constants.MODID, "send");
    private static final Identifier STOP_SHARE = Identifier.fromNamespaceAndPath(Constants.MODID, "stop_share");

    private static final Tooltip SEND_TOOLTIP = Tooltip.create(Component.translatable("jmws.ui.sharing.send.tooltip"));
    private static final Tooltip STOP_SHARE_TOOLTIP = Tooltip.create(Component.translatable("jmws.ui.sharing.stop_share.tooltip"));
    private static final Tooltip CANNOT_SEND_TOOLTIP = Tooltip.create(Component.translatable("jmws.ui.sharing.send_disabled"));
    private static final Tooltip CANNOT_STOP_SHARE_TOOLTIP = Tooltip.create(Component.translatable("jmws.ui.sharing.cannot_revoke_send"));

    private final String displayName;
    final ClientObjectWrapper<?> object;

    private Checkbox checkbox;
    private IconButton sendButton;
    private IconButton stopShareButton;

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
        RenderUtils.setDimensionsForList(this.sharePanel, this.width, this.height, 0.85, 0.75, 10, 2);
        this.checkbox = Checkbox.buildSelectAllCheckbox(button -> {
            if (button.isChecked) {
                this.sharePanel.selectAll();
            } else {
                this.sharePanel.unselectAll();
            }

            entryPressed();
        });

        this.sendButton = IconButton.buildGenericButton(SEND, (btn) -> this.sendRequests(), UIConstants.GREEN_COLOUR, SEND_TOOLTIP, CANNOT_SEND_TOOLTIP);
        this.stopShareButton = IconButton.buildGenericButton(STOP_SHARE, (btn) -> this.stopSharing(), UIConstants.RED_COLOUR, STOP_SHARE_TOOLTIP, CANNOT_STOP_SHARE_TOOLTIP);

        LinearLayout buttonIcnColumn = LinearLayout.vertical().spacing(4);
        LinearLayout mainRow = LinearLayout.horizontal().spacing(0);
        LinearLayout centralColumn = LinearLayout.vertical();

        this.addChildToLayout(buttonIcnColumn, this.sendButton);
        this.addChildToLayout(buttonIcnColumn, this.stopShareButton);
        this.addChildToLayout(buttonIcnColumn, IconButton.buildGenericButton(UIConstants.REFRESH, (btn) -> this.refresh(), UIConstants.YELLOW_COLOUR, UIConstants.REFRESH_TOOLTIP, null));
        this.addChildToLayout(buttonIcnColumn, IconButton.buildGenericButton(UIConstants.SETTINGS, (btn) -> {
            JMWSClientCommon.setCurrentUIScreen(new ShareSettingsScreen(true));
        }, 0xFFFFFFFF, UIConstants.SETTINGS_TOOLTIP, null));
        this.addChildToLayout(buttonIcnColumn, IconButton.buildGenericButton(UIConstants.SETTINGS, (btn) -> {
            JMWSClientCommon.setCurrentUIScreen(new DatePicker(this, btn));
        }, 0xFF0000FF, UIConstants.SETTINGS_TOOLTIP, null));
        this.addChildToLayout(buttonIcnColumn, this.checkbox, settings -> settings.paddingRight(4).paddingLeft(4));

        this.addChildToLayout(centralColumn, this.sharePanel, settings -> settings.paddingBottom(4));
        //centralColumn.addChild(rulePanel);

        mainRow.addChild(buttonIcnColumn, layoutSettings -> layoutSettings.paddingRight(6).paddingLeft(6));
        mainRow.addChild(centralColumn, layoutSettings -> layoutSettings.paddingRight(4));

        //this.addChildToLayout(mainRow, this.sharePanel, layoutSettings -> layoutSettings.paddingRight(11));
        //mainRow.addChild(rulePanel, layoutSettings -> layoutSettings.paddingRight(10));

        mainRow.arrangeElements();
        FrameLayout.centerInRectangle(mainRow, 0, 0, this.width, this.height);
        mainRow.visitWidgets(this::addRenderableWidget);

        sharePanel.addWidgets();
     //   rulePanel.setMaxHeight(width - rulePanel.getY());
    }

    private void setCheckboxState()
    {
        if (sharePanel.getSelectedEntries().isEmpty())
        {
            this.checkbox.uncheck();
            this.sharePanel.isSelectingAll = false;
        } else if (sharePanel.getSelectedEntries().size() == this.sharePanel.children().size())
        {
            this.checkbox.check();
            this.sharePanel.isSelectingAll = true;
        }
    }

    private void sendRequests()
    {
        Set<? extends PlayerEntry<?>> players = this.sharePanel.getSelectedEntries();
        if (players.isEmpty())
        {
            PlayerUtils.sendUserAlert(Component.translatable("jmws.ui.sharing.no_selected_players"), true, true, MessageType.PENDING);
        } else {
            minecraftClientInstance.setScreen(null);
            for (PlayerEntry<?> selectedPlayer : players) {
                if (selectedPlayer.isOnline)
                {
                    OutgoingShareRequest.sendShareRequest(this.object, selectedPlayer.user.getProfile());
                }
            }
            this.refresh();
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
        for (PlayerEntry<ClientObjectWrapper<?>> offlineEntry : this.sharePanel.offlineEntries) {
            if (offlineEntry.isSelected)
            {
                this.sendButton.setEnabled(false);
                break;
            }
            this.sendButton.setEnabled(true);
        }

        for (PlayerEntry<ClientObjectWrapper<?>> onlineEntry : this.sharePanel.onlineEntries) {
            if (onlineEntry.isSelected && onlineEntry.getState() == PlayerEntry.EntryState.NOT_SHARED)
            {
                this.stopShareButton.setEnabled(false);
                break;
            }
            this.stopShareButton.setEnabled(true);
        }

        for (PlayerEntry<ClientObjectWrapper<?>> child : this.sharePanel.children())
        {
            if (child.isSelected && child.getState() == PlayerEntry.EntryState.SHARED)
            {
                this.sendButton.setEnabled(false);
                break;
            }

            this.sendButton.setEnabled(true);
        }
        setCheckboxState();
    }

    public static void open(ClientBaseObjectWrapper<?> object)
    {
        ShareScreen shareScreen = new ShareScreen(minecraftClientInstance.screen, object);
        JMWSClientCommon.currentShareScreen = shareScreen;
        JMWSClientCommon.setCurrentUIScreen(shareScreen);
    }

    private class ShareSettingsScreen extends NotificationAlertScreen
    {

        private static final Component TITLE = Component.literal("Share Settings");

        private class RuleCheckbox extends Checkbox
        {

            private final ClientShareRule rule;

            public RuleCheckbox(ClientShareRule rule)
            {
                super(0, 0, 12, 12, rule.getDisplayName(), checkboxPressed -> {}, Button.DEFAULT_NARRATION, Tooltip.create(rule.getDescription()), Tooltip.create(rule.getDescription()));
                this.rule = rule;
            }

            @Override
            protected void extractContents(@NonNull GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, float v)
            {
                super.extractContents(guiGraphicsExtractor, i, i1, v);
                int yDescription = this.getY() + 13;
                int xDescription = this.getX() + width + 3;

                // Due to limited space, description will have a newline added if over.
                //guiGraphicsExtractor.text(minecraftClientInstance.font, rule.getDescription(), xDescription, yDescription, 0x80FFFFFF);
            }
        }

        private ScrollableLayout scrollableLayout;

        public ShareSettingsScreen(boolean renderCloseButton)
        {
            super(ShareScreen.this, renderCloseButton);
        }

        @Override
        protected void init()
        {
            super.init();

            // Initialise vertical layout, which will hold all scrollable checkbox elements for each rule
            LinearLayout verticalLayoutForRules = LinearLayout.vertical();

            for (ClientShareRule rule : JMWSClientCommon.clientShareRegistry.values())
            {
                MultiLineTextWidget descriptionLabel = new MultiLineTextWidget(rule.getDescription().plainCopy().withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY), minecraft.font);
                descriptionLabel.setMaxWidth(250);

                verticalLayoutForRules.addChild(new RuleCheckbox(rule), settings -> settings.paddingTop(10));
                verticalLayoutForRules.addChild(descriptionLabel, settings -> settings.paddingLeft(16));
            }

            scrollableLayout = new ScrollableLayout(minecraftClientInstance, verticalLayoutForRules, (int) (height * 0.8)); // Height of scrollable area will be 80% of screen height
            scrollableLayout.arrangeElements();
            scrollableLayout.setMinWidth((int) (width * 0.7));

            MultiLineEditBox milis = MultiLineEditBox.builder().setPlaceholder(Component.literal(String.valueOf(System.currentTimeMillis()))).build(minecraft.font, 70, 15, Component.literal("Hours"));

            LinearLayout masterHorizontalLayout = LinearLayout.horizontal().spacing(4);
            masterHorizontalLayout.addChild(scrollableLayout);
            masterHorizontalLayout.addChild(milis);

            masterHorizontalLayout.arrangeElements();
            FrameLayout.centerInRectangle(masterHorizontalLayout, 0, 0, width, height);
            masterHorizontalLayout.visitWidgets(this::addRenderableWidget);
        }

        @Override
        public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a)
        {
            super.extractRenderState(graphics, mouseX, mouseY, a);
            RenderUtils.renderBorderForList(graphics, scrollableLayout);
            graphics.centeredText(this.font, TITLE, this.width / 2, 7, 0xFFFFFFFF);
        }

        @Override
        protected Vector2i getDrawLocationForAlert()
        {
            return new Vector2i(this.scrollableLayout.getX(), this.scrollableLayout.getY() + this.scrollableLayout.getHeight() + 8);
        }

        @Override
        public void onClose()
        {
            JMWSClientCommon.setCurrentUIScreen(ShareScreen.this);
        }
    }
}
