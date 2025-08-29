package org.geooo.metadata;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.geooo.Client;
import org.geooo.util.Logger;

/*
 * Die Clientdatei wird genutzt um Metadaten des Clients zu speichern,
 * sodass diese auch nach einem Neustart verfügbar sind
 */
public class ClientFile extends ConfigFile {
    // public static File file = new File(Client.RESSOURCE_DIRECTORY +
    // "clientfile.g2gclient");

    public ClientFile(String filePath) {
        super(filePath);
    }

    /**
     * Schreibt alle Informationen aus dem Client in die Clientdatei
     * @param client
     */
    public void writeToFile(Client client) {
        ensureConfigFile(false);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(String.format("UUID: %s\n", client.getUUID()));
            writer.write(String.format("PublicKey: %s\n", client.getPublicKeyBase64()));
            writer.write(String.format("PrivateKey: %s\n", client.getPrivateKeyBase64()));
        } catch (IOException e) {
            Logger.error("Error while writing to ressourcefile!");
            Logger.exception(e);
        }
    }

    /**
     * Liest alle Informationen aus der Clientdatei und übergibt die dem Client
     * @param client
     */
    public void readFromFile(Client client) {
        ensureConfigFile(true);
        setConfigContentFromFile();

        client.setUUID(configContent.get("UUID"));
        client.setPublickeyBase64(configContent.get("PublicKey"));
        client.setPrivateKeyBase64(configContent.get("PrivateKey"));
    }
}
