package me.brynview.navidrohim.jmws.common.enums;

/**
 * Different colour types for action bar alerts.
 */
public enum MessageType {
    FAILURE("§C"), // Red
    SUCCESS("§2"), // Green
    WARNING("§e"), // Orange
    PENDING("§6"),
    ONE_TIME_WARNING(WARNING.text), // Orange, will only show once
    NEUTRAL(""); // White

    private final String text;

    MessageType(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}

