package me.brynview.navidrohim.jmws.common.api;

import me.brynview.navidrohim.jmws.common.syncing.SyncInformation;

public interface PossessesIdentifier {
    String getName();
    String getGuid();
    String getRegistryTypeName();
    SyncInformation getInfo();

}
