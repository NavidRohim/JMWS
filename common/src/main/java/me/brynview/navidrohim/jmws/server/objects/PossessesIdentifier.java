package me.brynview.navidrohim.jmws.server.objects;

import me.brynview.navidrohim.jmws.common.enums.ObjectType;

public interface PossessesIdentifier {
    String getName();
    String getSyncedCustomData();
    String getGroupIdentifier();
    ObjectType getObjectType();
}
