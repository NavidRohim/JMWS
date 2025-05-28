package me.brynview.navidrohim.jmws.common.io;

import java.io.File;

public class CommonIO {
    public static boolean deleteFile(String filename) {
        File waypointFileObj = new File(filename);
        return waypointFileObj.delete();
    }
}
