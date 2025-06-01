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
    public static File file;
    private static HashMap<String, String> configContent;

    public static void writeToFile() {

    }

    public static void readFromFile() {

    }

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

    public static void setConfigContentFromFile() {
        ensureConfigFile();
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

    public static void ensureConfigFile() {
        if (!file.exists()) {
            Logger.warn("Config file doesn't exist! Creating new one...");
            
            try {
                file.createNewFile();
            } catch (IOException e) {
                Logger.error("Error while creating configfile!");
                Logger.exception(e);
            }
        }
    }
}
