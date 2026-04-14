package me.brynview.navidrohim.jmws.common.enums;

/**
 * Different colour types for action bar alerts.
 */
public enum MessageType {
    FAILURE("§C", 0xFFAA0000), // Red
    SUCCESS("§2", 0xFF00AA00), // Green
    WARNING("§e", 0xFFFFFF55), // Orange
    PENDING("§6", 0xFFFFA500),

    // General colours
    GREY("§7", 0xFF555555),

    // Other
    ONE_TIME_WARNING(WARNING.text, WARNING.numericalColour), // Orange, will only show once
    NEUTRAL("", 0xFFFFFFFF), // White
    INVISIBLE("", 0x00000000);

    private final String text;
    private final int numericalColour;

    MessageType(final String text, int numericalColour)
    {
        this.text = text;
        this.numericalColour = numericalColour;
    }

    public int getNumericalColour()
    {
        return this.numericalColour;
    }

    @Override
    public String toString() {
        return text;
    }
}

