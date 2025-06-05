package org.geooo.metadata;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.geooo.Server;
import org.geooo.util.Logger;

public class ServerFile extends ConfigFile {

    public ServerFile(String filePath) {
        super(filePath);
    }

    public void writeToFile(Server server) {

    }

    public void generateBlankConfig(Server server) {
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
