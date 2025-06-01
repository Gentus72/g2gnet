package org.geooo.metadata;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.geooo.Client;
import org.geooo.util.Logger;

public abstract class ClientFile extends ConfigFile {
    public static File file = new File(Client.RESSOURCE_DIRECTORY + "clientfile.g2gclient");

    public static void writeToFile(Client client) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Logger.error("Error while creating clientfile!");
                Logger.exception(e);
            }

            Logger.warn("New clientfile instanciating! Is this the first time being run?");
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(String.format("UUID: %s\n", client.getUUID()));
            writer.write(String.format("PublicKey: %s\n", client.getPublicKeyBase64()));
            writer.write(String.format("PrivateKey: %s\n", client.getPrivateKeyBase64()));
        } catch (IOException e) {
            Logger.error("Error while writing to ressourcefile!");
            Logger.exception(e);
        }
    }

    public static void readFromFile(Client client) {

    }
}
