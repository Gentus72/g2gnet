package org.geooo.metadata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.geooo.util.Logger;

/*
 * Eine übergeordnete Klasse für alle Metadaten-Dateien
 */
public abstract class ConfigFile {

    public File file;
    public HashMap<String, String> configContent;

    public ConfigFile(String filePath) {
        this.file = new File(filePath);
    }

    /**
     * @return Gibt alle Infos aus der Datei im Key-Value-Format aus
     */
    public HashMap<String, String> getConfigContent() {
        if (this.configContent == null) {
            setConfigContentFromFile();
        }

        return this.configContent;
    }

    /**
     * Liest alle Informationen aus der Datei und schreibt sie in die HashMap im Key-Value-Format
     */
    public void setConfigContentFromFile() {
        ensureConfigFile(true);
        this.configContent = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(this.file))) {
            String line = reader.readLine();

            while (line != null) {
                if (line.contains(":")) {
                    String[] components = line.split(" ");

                    if (components.length == 1) { // value is empty
                        this.configContent.put(components[0].replace(":", ""), "");
                        continue;
                    }

                    this.configContent.put(components[0].replace(":", ""), components[1]);
                }

                line = reader.readLine();
            }
        } catch (IOException e) {
            Logger.error("Error while reading config file (values)!");
            Logger.exception(e);
        }
    }

    /**
     * Stellt sicher, dass die Datei existiert und übernimmt Fehlerbehandlung, falss das nicht der Fall ist
     * @param isNeeded
     */
    public void ensureConfigFile(boolean isNeeded) {
        if (this.file == null) {
            Logger.error("Configfile object is null! Should have been initialized on creation...");
            System.exit(1);
        }

        if (!this.file.exists()) {
            if (isNeeded) {
                Logger.error("Configfile is required but not present! ");
                System.exit(1);
            }

            Logger.warn("Config file doesn't exist! May crash now...");

            try {
                this.file.createNewFile();
            } catch (IOException e) {
                Logger.error("Error while creating configfile!");
                Logger.exception(e);
            }
        }
    }

    /**
     * @return Gibt die Datei aus
     */
    public File getFile() {
        return this.file;
    }
}
