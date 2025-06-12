package org.geooo.metadata;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.geooo.HostServer;
import org.geooo.util.Logger;

public class ServerFile extends ConfigFile {

    public ServerFile(String filePath) {
        super(filePath);
    }

    public void writeToFile(HostServer server) {
        ensureConfigFile(false);

        if (server.getCCServer() == null) {
            Logger.error("Can't write to serverfile! CCServer is null!");
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.file))) {
            writer.write(String.format("UUID: %s\n", server.getUUID()));
            writer.write(String.format("CCServerAddress: %s\n", server.getCCServer().getAddress()));
            writer.write(String.format("NetworkUUID: %s\n", server.getCCServer().getNetworkUUID()));

            writer.write("Allowed Client-Publickeys (base64):\n");
            for (String publicKey : server.getClientPublicKeysBase64()) {
                writer.write(publicKey + "\n");
            }

            writer.write("Allowed Block-UUIDs:\n");
            for (String blockUUID : server.getAllowedBlockUUIDs()) {
                writer.write(blockUUID + "\n");
            }
        } catch (IOException e) {
            Logger.error("Error while writing blank serverfile!");
            Logger.exception(e);
        }
    }

    public void generateBlankConfig(HostServer server) {
        ensureConfigFile(false);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.file))) {
            writer.write(String.format("UUID: %s\n", server.getUUID()));
            writer.write("CCServerAddress: 172.20.0.10\n");
            writer.write("NetworkUUID: 137f1c666f1b49a887a19bd3a5b45991\n");

            writer.write("Client-Publickeys (base64):\n");
            for (String publicKey : server.getClientPublicKeysBase64()) {
                writer.write(publicKey + "\n");
            }
        } catch (IOException e) {
            Logger.error("Error while writing blank serverfile!");
            Logger.exception(e);
        }
    }
}
