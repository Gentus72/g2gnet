package org.geooo;

import java.io.File;
import java.util.ArrayList;

public class NetworkFile {
    public static NetworkFile networkFile;

    private File file;
    private int version = -1;

    public NetworkFile(String filePath) {
        this.file = new File(filePath);
    }

    public static NetworkFile getInstance() {
        if (networkFile == null) {
            return new NetworkFile(ServerNetwork.NETWORK_FILE_PATH);
        }

        return networkFile;
    }

    public static void addServer() {

    }

    public static ArrayList<ServerDTO> getServers() {
        return null;
    }

    public static int getVersion() {
        return getInstance().version;
    }

    public static void updateVersion() {

    }

    public static void initializeNetworkFile(ServerDTO initServer) {

    }
}
