package me.brynview.navidrohim.jmws.common.enums;

public enum JMWSMessageType {
    FAILURE("§C"),
    SUCCESS("§2"),
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

