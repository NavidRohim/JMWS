package me.brynview.navidrohim.jmws.server.objects;

import me.brynview.navidrohim.jmws.common.enums.FetchType;

public interface PossessesIdentifier {

    public String getName();
    public String getCustomData();
    public String getGroupIdentifier();
    public FetchType getObjectType();

}
