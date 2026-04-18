package me.brynview.navidrohim.jmws.client.ui.requests_screen;

import me.brynview.navidrohim.jmws.client.JMWSClientCommon;
import me.brynview.navidrohim.jmws.client.share.request.ShareRequest;
import me.brynview.navidrohim.jmws.client.ui.generic.Subtitle;
import me.brynview.navidrohim.jmws.client.ui.generic.entry.PlayerHeadEntryWithTitle;
import me.brynview.navidrohim.jmws.client.ui.generic.entry.TitleLabelEntry;
import me.brynview.navidrohim.jmws.client.ui.generic.selection_list.CheckableSelectionList;
import me.brynview.navidrohim.jmws.client.ui.share_panel.ObjectSharePanel;
import me.brynview.navidrohim.jmws.client.utils.PlayerUtils;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class IncomingShareRequestsList extends CheckableSelectionList<IncomingShareRequestsList> {

    private static final Component NO_PLAYERS_TEXT = Component.translatable("jmws.ui.requests.no_requests");

    public IncomingShareRequestsList(Minecraft minecraft, int width, int height, int x, int y, int itemHeight) {
        super(minecraft, width, height, x, y, itemHeight);

        for (ShareRequest request : JMWSClientCommon.incomingShareRequests.values())
        {
            @Nullable PlayerInfo sender = PlayerUtils.getPlayerInfoFromUUID(request.originalSender);
            if (sender != null)
            {
                this.addEntryToTop(new IncomingRequestFromPlayerEntry(this, sender, request));
            }
        }
    }

    @Override
    public Component getEmptyStateText()
    {
        return NO_PLAYERS_TEXT;
    }

    public static class IncomingRequestFromPlayerEntry extends PlayerHeadEntryWithTitle
    {
        private final ShareRequest request;
        private boolean isExpired = false;
        private boolean didAccept = false;

        private static final Subtitle EXPIRED = new Subtitle(Component.translatable("jmws.ui.requests.expired"), MessageType.GREY);
        private static final Subtitle ACCEPTED = new Subtitle(Component.translatable("jmws.ui.requests.accepted"), MessageType.GREY);

        public IncomingRequestFromPlayerEntry(@NotNull IncomingShareRequestsList listOwner, PlayerInfo player, ShareRequest request) {
            super(listOwner, player);
            this.request = request;
        }

        @Override
        public void extractContent(@NonNull GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v) {
            super.extractContent(guiGraphicsExtractor, i, i1, b, v);
            long time = request.timeout.getDelay(TimeUnit.SECONDS);
            MutableComponent timeLeft = Component.literal(time + " ");

            if (time >= 0)
            {
                if (!didAccept)
                {
                    Component toDisplay = !this.selectedEntry ? timeLeft : timeLeft.append(Component.translatable("jmws.ui.generic.selected"));
                    setSubtitle(new Subtitle(toDisplay, MessageType.GREY));
                } else {
                    setSubtitle(ACCEPTED);
                }
            } else if (!didAccept){
                isExpired = true;
                setSubtitle(EXPIRED);
            }
        }


        @Override
        public boolean canSelect() {
            return !isExpired && !didAccept;
        }

        @Override
        public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick)
        {
            boolean isEnabled = super.mouseClicked(event, doubleClick);
            if (doubleClick && canSelect())
            {
                didAccept = true;
                this.request.accept();
                this.setSelected(false);

                PlayerUtils.sendUserAlert(Component.translatable("sharing.jmws.sharing_child"), true, false, MessageType.NEUTRAL);
            }

            return isEnabled;
        }
    }
}
