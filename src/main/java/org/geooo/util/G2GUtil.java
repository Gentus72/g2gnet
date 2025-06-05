package org.geooo.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

public abstract class G2GUtil {
    public static String getRandomUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String getLocalIPv4Address() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            Logger.error("Error while getting local ipv4address!");
            Logger.exception(e);
            System.exit(1);

            return null;
        }
    }
}
