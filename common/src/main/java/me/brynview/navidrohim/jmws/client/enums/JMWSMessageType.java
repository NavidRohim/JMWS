package me.brynview.navidrohim.jmws.client.enums;

/**
 * Different colour types for action bar alerts.
 */
public enum JMWSMessageType {
    FAILURE("§C"), // Red
    SUCCESS("§2"), // Green
    WARNING("§e"), // Orange
    ONE_TIME_WARNING(WARNING.text), // Orange, will only show once
    NEUTRAL(""); // White

    private final String text;

    JMWSMessageType(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}

