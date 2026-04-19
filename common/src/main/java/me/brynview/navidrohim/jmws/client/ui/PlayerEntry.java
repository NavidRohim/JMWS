package me.brynview.navidrohim.jmws.client.ui;

import me.brynview.navidrohim.jmws.client.JMWSClientCommon;
import me.brynview.navidrohim.jmws.client.share.request.OutgoingShareRequest;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientObjectWrapper;
import me.brynview.navidrohim.jmws.client.ui.generic.Subtitle;
import me.brynview.navidrohim.jmws.client.ui.generic.entry.PlayerHeadEntryWithTitle;
import me.brynview.navidrohim.jmws.client.ui.share_panel.ObjectSharePanel;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

import static me.brynview.navidrohim.jmws.client.ui.UIConstants.PLAYER_HEAD_SIZE_HALVED;

public class PlayerEntry<T extends ClientObjectWrapper<?>> extends PlayerHeadEntryWithTitle {

    private final static @NotNull Subtitle EMPTY = new Subtitle(Component.translatable("jmws.ui.sharing.not_shared"), MessageType.GREY);
    private final static @NotNull Subtitle ALREADY_SHARED = new Subtitle(Component.translatable("jmws.ui.sharing.already_shared"), MessageType.SUCCESS);
    private final static @NotNull Subtitle PENDING_SHARE = new Subtitle(Component.translatable("jmws.ui.sharing.pending"), MessageType.PENDING);

    public final @NonNull PlayerInfo user;
    private final @NotNull UUID userUuid;
    private final @NotNull ClientObjectWrapper<?> sharedObject;

    public PlayerEntry(@NotNull PlayerInfo user, @NonNull ObjectSharePanel<T> owner, @NonNull ClientObjectWrapper<?> sharedObject) {
        super(owner, user);

        this.sharedObject = sharedObject;
        this.user = user;
        this.userUuid = user.getProfile().id();

        setSubtitle(getSubtitleText());
    }

    public Subtitle getSubtitleText() {
        Subtitle display;
        if (JMWSClientCommon.outgoingShareRequests.hasShareRequestFor(userUuid)) {
            display = PENDING_SHARE;
        } else if (sharedObject.getSharedTo().contains(userUuid)) {
            display = ALREADY_SHARED;
        } else {
            display = EMPTY;
        }

        return display;
    }

    public void extractSharingStatusBar(GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v) {
        // minus half-width
        int barWidth = 6;
        int middle = (this.getContentX() + ((4 + PLAYER_HEAD_SIZE_HALVED) / 2));

        int startX = middle - (barWidth / 2);
        int endX = middle + (barWidth / 2);

        int startY = this.getContentYMiddle() - PLAYER_HEAD_SIZE_HALVED;
        int endY = this.getContentYMiddle() + PLAYER_HEAD_SIZE_HALVED;

        guiGraphicsExtractor.fill(startX, startY, endX, endY, this.subtitle.getMessageType().getNumericalColour());
    }

    @Override
    public boolean canSelect() {
        return !JMWSClientCommon.outgoingShareRequests.hasShareRequestFor(userUuid) && !sharedObject.getSharedTo().contains(userUuid);
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
        boolean c = super.mouseClicked(event, doubleClick);

        if (doubleClick) {
            this.setSelected(false);
            OutgoingShareRequest.sendShareRequest(sharedObject, this.user.getProfile());
        }
        return c;
    }

    protected void extractThumbnailImage(GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v) {
        int headPlacementX = this.getContentX() + PLAYER_HEAD_SIZE_HALVED + 4;
        int headPlacementY = this.getContentYMiddle() - PLAYER_HEAD_SIZE_HALVED;
        PlayerFaceExtractor.extractRenderState(guiGraphicsExtractor, user.getSkin(), headPlacementX, headPlacementY, UIConstants.PLAYER_HEAD_SIZE);
    }

    @Override
    public void extractSubtitleText(GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v) {
        super.extractSubtitleText(guiGraphicsExtractor, i, i1, b, v);
        if (!selectedEntry) {
            setSubtitle(getSubtitleText());
            super.extractSubtitleText(guiGraphicsExtractor, i, i1, b, v);
        }
    }

    @Override
    public void extractContent(@NonNull GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v) {
        super.extractContent(guiGraphicsExtractor, i, i1, b, v);
        this.extractSharingStatusBar(guiGraphicsExtractor, i, i1, b, v);
        this.extractThumbnailImage(guiGraphicsExtractor, i, i1, b, v);

        if (this.selectedEntry) {
            this.setSubtitle(new Subtitle(Component.translatable("jmws.ui.generic.selected"), MessageType.of(null, sharedObject.getColour())));
        }
    }
}
