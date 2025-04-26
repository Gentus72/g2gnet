package org.geooo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.geooo.util.Logger;

// TODO add server ownership of ressources
public class ClientFile {

    public static final String CLIENTFILE_NAME = "clientfile.g2gclient";

    private static ClientFile clientFile;
    private final File file;

    private ArrayList<EmptyRessource> ressources;

    public ClientFile(String filePath) {
        this.file = new File(filePath);

        try {
            if (file.createNewFile()) {
                Logger.warn("No clientfile found, creating a new one!");
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

            try (BufferedReader reader = new BufferedReader(new FileReader(getInstance().file))) {
                // erste Zeile lesen, da sie die "Überschrift" ist
                reader.readLine();

                // Ressource-Informationen lesen
                while (true) {
                    String line = reader.readLine();

                    if (line == null) {
                        break;
                    }

                    String[] values = line.split(",");

                    if (values.length != 5) {
                        Logger.error("Malformed clientfile! Mismatching number of arguments: " + values.length + " -> should be 5!");
                    }

                    // new EmptyRessource with title, uuid, hashSum and blockAmount
                    getInstance().ressources.add(new EmptyRessource(values[1], values[0], values[4], Integer.parseInt(values[2])));
                }
            }
        } catch (IOException | NumberFormatException e) {
            Logger.error("Error while parsing ressources to serverfile instance!");
            Logger.exception(e);
        }
    }
}
