package me.brynview.navidrohim.jmws.client.enums;

public enum JMWSMessageType {
    FAILURE("§C"),
    SUCCESS("§2"),
    WARNING("§e"),
    ONE_TIME_WARNING(WARNING.text),
    NEUTRAL("");

    private final String text;

    JMWSMessageType(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}

