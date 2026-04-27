package me.brynview.navidrohim.jmws.client.ui.generic;

import me.brynview.navidrohim.jmws.common.enums.MessageType;
import net.minecraft.network.chat.Component;

public final class Subtitle {
    private final Component displayable;
    private final MessageType messageType;

    public Subtitle(Component component, MessageType messageType) {
        this.displayable = Component.literal(messageType.toString() + "§o" + component.getString());
        this.messageType = messageType;
    }

    public Component getDisplayableComponent() {
        return displayable;
    }

    public MessageType getMessageType() {
        return messageType;
    }
}
