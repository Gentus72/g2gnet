package org.geooo.util;

import java.util.UUID;

public abstract class G2GUUID {
    public static String getRandomUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
