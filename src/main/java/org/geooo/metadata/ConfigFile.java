package org.geooo.metadata;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

import org.geooo.util.Logger;

public abstract class ConfigFile {
    public File file;
    public HashMap<String, String> configContent;

    public ConfigFile(String filePath) {
        this.file = new File(filePath);
    }

    public <T, K> void addSection(BufferedWriter writer, ArrayList<T> list, String header, Function<T, K>... extractors) {
        try {
            writer.write(header);

            for (T element : list) {
                String line = "";

                for (Function<T, K> extractor : extractors) {
                    line += extractor.apply(element).toString() + ",";
                }

                line += "\n";
                writer.write(line);
            }
        } catch (IOException e) {
            Logger.error("Error while writing config section!");
            Logger.exception(e);
        }
    }
    
    public HashMap<String, String> getConfigContentFromFile(File configFile) {
        ensureConfigFile(true);
        HashMap<String, String> newContent = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();

            while (line != null) {
                if (line.contains(":")) {
                    String[] components = line.split(" ");

                    configContent.put(components[0].replace(":", ""), components[1]);
                }

                line = reader.readLine();
            }

            return newContent;
        } catch (IOException e) {
            Logger.error("Error while reading config file (values)!");
            Logger.exception(e);
        }

        return null;
    }

    public void setConfigContentFromFile() {
        ensureConfigFile(true);
        configContent = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();

            while (line != null) {
                if (line.contains(":")) {
                    String[] components = line.split(" ");

                    configContent.put(components[0].replace(":", ""), components[1]);
                }

                line = reader.readLine();
            }
        } catch (IOException e) {
            Logger.error("Error while reading config file (values)!");
            Logger.exception(e);
        }
    }

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
}
