package org.geooo.util;

public enum LoggingType {
    INFO("INFO"),
    SUCCESS("SUCCESS"),
    WARNING("WARNING"),
    ERROR("ERROR");

    String value;

    LoggingType(String value) {
        this.value = value;
    }
}
