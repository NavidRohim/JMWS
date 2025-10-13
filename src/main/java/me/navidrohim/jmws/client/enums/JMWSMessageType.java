package me.navidrohim.jmws.client.enums;

public enum JMWSMessageType {
    FAILURE("\u00A7c"),
    SUCCESS("\u00A72"),
    WARNING("\u00A7e"),
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

