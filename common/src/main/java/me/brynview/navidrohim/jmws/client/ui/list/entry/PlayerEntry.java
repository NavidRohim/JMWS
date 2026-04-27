package me.brynview.navidrohim.jmws.client.ui.list.entry;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.JMWSClientCommon;
import me.brynview.navidrohim.jmws.client.share.request.OutgoingShareRequest;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientObjectWrapper;
import me.brynview.navidrohim.jmws.client.ui.RenderUtils;
import me.brynview.navidrohim.jmws.client.ui.generic.Subtitle;
import me.brynview.navidrohim.jmws.client.ui.generic.list.entry.PlayerHeadEntryWithTitle;
import me.brynview.navidrohim.jmws.client.ui.list.ObjectSharePanel;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public final class PlayerEntry<T extends ClientObjectWrapper<?>> extends PlayerHeadEntryWithTitle<PlayerEntry<T>> {

    private final static @NotNull Subtitle EMPTY = new Subtitle(Component.translatable("jmws.ui.sharing.not_shared"), MessageType.GREY);
    private final static @NotNull Subtitle ALREADY_SHARED = new Subtitle(Component.translatable("jmws.ui.sharing.already_shared"), MessageType.SUCCESS);
    private final static @NotNull Subtitle PENDING_SHARE = new Subtitle(Component.translatable("jmws.ui.sharing.pending"), MessageType.PENDING);
    private final static @NotNull Subtitle SELECTED = new Subtitle(Component.translatable("jmws.ui.generic.selected"), MessageType.SUCCESS);

    public final @NonNull PlayerInfo user;
    private final @NotNull UUID userUuid;
    private final @NotNull T sharedObject;

    public PlayerEntry(@NotNull PlayerInfo user, @NonNull ObjectSharePanel<T> owner, @NonNull T sharedObject) {
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

    @Override
    public boolean canSelect() {
        return !JMWSClientCommon.outgoingShareRequests.hasShareRequestFor(userUuid) && !sharedObject.getSharedTo().contains(userUuid);
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
        boolean c = super.mouseClicked(event, doubleClick);

        if (doubleClick && canSelect()) {
            this.setSelected(false);
            OutgoingShareRequest.sendShareRequest(sharedObject, this.user.getProfile());
        }
        return c;
    }

    @Override
    public void extractSelectedState(GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v)
    {
        setSubtitle(SELECTED);
    }

    @Override
    public void extractUnselectedState(GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v) {
        if (!isSelected) {
            setSubtitle(getSubtitleText());
        }
    }

    @Override
    public void extractContent(@NonNull GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v) {
        super.extractContent(guiGraphicsExtractor, i, i1, b, v);
        RenderUtils.renderStatusBarInEntry(guiGraphicsExtractor, this, this.subtitle.getMessageType().getNumericalColour());
    }
}
