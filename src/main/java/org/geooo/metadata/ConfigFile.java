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
    public static HashMap<String, String> configContent;

    public abstract void writeToFile();

    public abstract void readFromFile();

    public static <T, K> void addSection(BufferedWriter writer, ArrayList<T> list, String header, Function<T, K>... extractors) {
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
    
    public static void setConfigContentFromFile(File configFile) {
        File file = ensureConfigFile(configFile, true);
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

    public static File ensureConfigFile(File file, boolean isNeeded) {
        if (file == null) {
            Logger.error("Configfile object is null!");
            return null;
        }

        if (!file.exists()) {
            if (isNeeded) {
                Logger.error("Configfile is required but not present! ");
                return null;
            }

            Logger.warn("Config file doesn't exist! May crash now...");
            
            try {
                file.createNewFile();
                return file;
            } catch (IOException e) {
                Logger.error("Error while creating configfile!");
                Logger.exception(e);
            }
        }

        return file;
    }
}
