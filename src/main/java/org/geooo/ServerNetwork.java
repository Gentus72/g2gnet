package org.geooo;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

public class ServerNetwork {
    public static final String NETWORK_FILE_PATH = "";
    private static ServerNetwork network;

    private File networkFile;
    private ArrayList<ServerDTO> servers;
    private ArrayList<SharedRessource> sharedRessources;
    private String uuid;

    public ServerNetwork(File networkFile) {
        this.networkFile = networkFile;

        // TODO read networkFile for uuid
        this.uuid = UUID.randomUUID().toString().replace("-", "");
    }

    public static ServerNetwork getInstance() {
        if (network == null) {
            return new ServerNetwork(new File(NETWORK_FILE_PATH));
        }

        return network;
    }

    public static void addServer(ServerDTO newServer) {
        getInstance().servers.add(newServer);

        // TODO add to networkFile
    }

    public ArrayList<ServerDTO> getServers() {
        return getInstance().servers;
    }
}
