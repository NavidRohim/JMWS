package me.brynview.navidrohim.jmws.common.api;

import me.brynview.navidrohim.jmws.common.enums.ObjectType;

public interface PossessesIdentifier {
    String getName();
    String getSyncedCustomData();
    String getGroupIdentifier();
    ObjectType getObjectType();
}
