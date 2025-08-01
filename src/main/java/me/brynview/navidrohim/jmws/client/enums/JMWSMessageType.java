package me.brynview.navidrohim.jmws.client.enums;

public enum JMWSMessageType {
    FAILURE("§C"),
    SUCCESS("§C"),
    WARNING("§C"),
    NEUTRAL("§C");

    private final String text;

    JMWSMessageType(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}

