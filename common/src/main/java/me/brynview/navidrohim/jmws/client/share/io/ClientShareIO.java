package me.brynview.navidrohim.jmws.client.share.io;

import me.brynview.navidrohim.jmws.common.enums.FetchType;
import me.brynview.navidrohim.jmws.common.share.io.CommonShareIO;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;

public class ClientShareIO extends CommonShareIO {
    public ClientShareIO() {
        super(JMWSServerIO.getPathLocationPrefix(FetchType.SHARED) + "shared.json");
    }
}
