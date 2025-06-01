package org.geooo.metadata;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.PublicKey;

import org.geooo.Ressource;
import org.geooo.util.Logger;

public abstract class RessourceFile {
    public static File file;

    public static void writeToFile(Ressource ressource, String ressourceDirectory, PublicKey clientPublicKey) {
        file = new File(ressourceDirectory + ressource.getUUID() + ".g2g");

        if (file.exists()) {
            Logger.error("Ressource file already exists! Won't overwrite existing file!");
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(String.format("UUID: %s\n", ressource.getUUID()));
            writer.write(String.format("Title: %s\n", ressource.getTitle()));
            writer.write(String.format("HashSum: %s\n", ressource.getTotalHashSum()));
            writer.write(String.format("Uploader: %s\n", clientPublicKey.toString()));
            writer.write(String.format("AmountOfBlocks: %d\n", ressource.getBlockAmount()));
            writer.write(String.format("SourceFileName: %s\n", ressource.getSourceFile().getName()));
            
            writer.write("Blocks (uuid, location, hash, sequenceID):\n");
            for (var entry : ressource.getBlockLocations().entrySet()) {
                writer.write(String.format("%s,%s,%s,%d\n", entry.getKey().getUUID(), entry.getValue(), entry.getKey().getHashSum(), entry.getKey().getSequenceID()));
            }
        } catch (IOException e) {
            Logger.error("Error while writing to ressourcefile!");
            Logger.exception(e);
        }
    }
}
