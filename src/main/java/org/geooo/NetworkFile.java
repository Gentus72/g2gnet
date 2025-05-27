package org.geooo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.geooo.dto.RessourceDTO;
import org.geooo.dto.ServerDTO;
import org.geooo.util.Logger;

public abstract class NetworkFile {
    public static final String FILENAME = "networkFile.g2gnet";

    public static File file;

    public static void writeToFile(CCServer ccServer) {
        file = new File(FILENAME);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            if (file.createNewFile()) {
                Logger.warn("New networkfile created! Is the network new?");
            } else {
                Logger.info("Existing networkfile will be overwritten!");
            }

            writer.write(String.format("UUID: %s\n", ccServer.getNetworkUUID()));

            writer.write("Servers (uuid, address):\n");
            for (ServerDTO server : ccServer.getServers()) {
                writer.write(String.format("%s, %s\n", server.getUUID(), server.getAddress()));
            }

            writer.write("Ressources (uuid, title, size):\n");
            for (RessourceDTO ressource : ccServer.getRessources()) {
                writer.write(String.format("%s, %s, %d", ressource.getTitle(), ressource.getUUID(), ressource.getSizeMiB()));
            }
        } catch (IOException e) {
            Logger.error("Error while writing to networkfile!");
            Logger.exception(e);
        }
    }

    // get ressource metadata based on available ressourcefiles
    public static void updateRessources() {
        File currentDir = new File(".");

        File[] matchingFiles = currentDir.listFiles((dir, name) -> name.endsWith(".g2g"));

        if (matchingFiles == null || matchingFiles.length == 0) {
            Logger.error("Error while fetching local files or no ressource files found! Ressources in networkfile can't be updated!");
            return;
        }

        for (File ressourceFile : matchingFiles) {
            Logger.info(ressourceFile.getName());
        }
    }
}
