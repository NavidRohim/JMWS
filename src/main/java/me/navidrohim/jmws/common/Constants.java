package me.navidrohim.jmws.common;

import me.navidrohim.jmws.JMWS;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Constants {

    public static Logger getLogger()
    {
        return LOGGER;
    }

    public static final String MODID = JMWS.MODID;
    public static final Logger LOGGER = JMWS.logger;
}
