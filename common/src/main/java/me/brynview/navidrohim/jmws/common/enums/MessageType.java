package me.brynview.navidrohim.jmws.common.enums;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Different colour types for action bar alerts.
 */
public class MessageType {

    private static final Map<String, MessageType> REGISTRY = new HashMap<>();

    public static final MessageType FAILURE = register("§C", 0xFFAA0000); // Red
    public static final MessageType SUCCESS = register("§2", 0xFF00AA00); // Green
    public static final MessageType WARNING = register("§e", 0xFFFFFF55);
    public static final MessageType PENDING = register("§6", 0xFFFFA500);

    public static final MessageType GREY = register("§7", 0xFF555555);

    public static final MessageType ONE_TIME_WARNING = register(WARNING.text, WARNING.numericalColour, true);
    public static final MessageType NEUTRAL = register("", 0xFFFFFFFF);

    private final String text;
    private final int numericalColour;
    public final boolean oneTimeOnly;

    private MessageType(final String text, int numericalColour, boolean oneTimeOnly)
    {
        this.text = text;
        this.numericalColour = numericalColour;
        this.oneTimeOnly = oneTimeOnly;
    }

    public int getNumericalColour()
    {
        return this.numericalColour;
    }

    @Override
    public String toString() {
        return text;
    }

    public static MessageType register(String id, int numericalColour, boolean oneTimeOnly)
    {
        if (REGISTRY.containsKey(id))
        {
            return REGISTRY.get(id);
        } else {
            MessageType type = new MessageType(id, numericalColour, oneTimeOnly);
            REGISTRY.put(id, type);
            return type;
        }
    }

    public static MessageType register(String id, int numericalColour)
    {
        return register(id, numericalColour, false);
    }

    public static MessageType valueOf(String id)
    {
        if (!REGISTRY.containsKey(id))
        {
            throw new IllegalArgumentException("MessageType with id '" + id + "' does not exist.");
        }
        return REGISTRY.get(id);
    }

    public static MessageType of(@Nullable String id, int numericalColour)
    {
        if (id == null)
        {
            id = "";
        }

        return new MessageType(id, numericalColour, false);
    }
}

