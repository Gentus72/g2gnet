package org.geooo.util;

public enum ServerResponse {
    AUTH,       // AUTH <SUCCESS | FAIL>
    SUCCESS,    // SUCCESS
    REDIRECT,   // REDIRECT <IPv4>
    INFO,       // INFO
    DOWNLOAD,   // DOWNLOAD
    CLOSE,      // CLOSE
    ERROR;      // ERROR <message>
}
