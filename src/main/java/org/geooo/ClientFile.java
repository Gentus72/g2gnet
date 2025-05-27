package org.geooo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.geooo.dto.RessourceDTO;
import org.geooo.dto.ServerDTO;
import org.geooo.util.Logger;

public class ClientFile {

    public static final String CLIENTFILE_NAME = "clientfile.g2gclient";

    private static ClientFile clientFile;
    private final File file;

    private ArrayList<RessourceDTO> ressources;
    private ArrayList<ServerDTO> servers;
    // Server: uuid, file, address

    public ClientFile(String filePath) {
        this.file = new File(filePath);

        try {
            if (file.createNewFile()) {
                Logger.warn("No clientfile found, creating a new one!");

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.file))) {
                    writer.append("serverUUID, address, serverfile");
                }
            }
        } catch (IOException e) {
            Logger.error("Error while instantiating clientfile!");
            Logger.exception(e);
        }
    }

    public static ClientFile getInstance() {
        if (clientFile == null) {
            clientFile = new ClientFile(CLIENTFILE_NAME);
        }

        return clientFile;
    }

    public static void reloadRessources() {
        try {
            if (getInstance().ressources != null) {
                getInstance().ressources = null;
            }

            getInstance().ressources = new ArrayList<>();
            getInstance().servers = new ArrayList<>();

            // reload servers
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(getInstance().file))) {
                List<Path> serverFilePaths = Files.list(Path.of(".")).filter((file) -> file.endsWith(".g2gsrv")).toList();

                for (Path path : serverFilePaths) {
                    File serverFile = new File(path.toString());
                    ServerDTO server = new ServerDTO();

                    getInstance().servers.add(server);
                    writer.append(server.toString());
                }
            }
        } catch (IOException | NumberFormatException e) {
            Logger.error("Error while parsing ressources to serverfile instance!");
            Logger.exception(e);
        }
    }
}

// TODO add serverfile verification if server changes
// TODO add removeServer
