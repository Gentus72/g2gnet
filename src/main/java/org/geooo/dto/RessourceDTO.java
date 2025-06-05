package org.geooo.dto;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import org.geooo.Ressource;
import org.geooo.util.Logger;

public class RessourceDTO {

    public String title;
    public String uuid;
    public String totalHashSum; // hash sum of the whole uploaded file
    public int blockAmount;

    public RessourceDTO() {
        // do nothing;
    }

    public RessourceDTO(String uuid) {
        this.uuid = uuid;
    }

    public RessourceDTO(String title, String uuid, String totalHashSum) {
        this.title = title;
        this.uuid = uuid;
        this.totalHashSum = totalHashSum;
    }

    public RessourceDTO(String uuid, String title, int blockAmount) {
        this.title = title;
        this.uuid = uuid;
        this.blockAmount = blockAmount;
    }

    public RessourceDTO(String title, String uuid, String totalHashSum, int blockAmount) {
        this.title = title;
        this.uuid = uuid;
        this.totalHashSum = totalHashSum;
        this.blockAmount = blockAmount;
    }

    public RessourceDTO(File G2GFile) {
        String g2gFilePath = G2GFile.getPath();

        // ist die Ressource-Datei nicht im Ressource-Ordner
        if (!(g2gFilePath.split("/")[0] + "/").equals(Ressource.PARENT_DIRECTORY)) {
            Logger.warn("Creating ressource from a ressource file which is not in the ressource directory! Is this intended?");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(G2GFile))) {
            HashMap<String, String> values = new HashMap<>();

            // Ressource-Informationen lesen
            while (true) {
                String line = reader.readLine();

                if (line == null || line.equals("blocks (uuid, hash):")) {
                    break;
                }

                String[] keyValuePair = line.split(":");
                values.put(keyValuePair[0], keyValuePair[1].strip());
            }

            this.title = values.get("title");
            this.uuid = values.get("uuid");
            this.totalHashSum = values.get("totalHashSum");
            this.blockAmount = 0;

            while (reader.readLine() != null) {
                this.blockAmount += 1;
            }
        } catch (IOException e) {
            Logger.error("Error while parsing existing G2GFile to EmptyRessource object!");
            Logger.exception(e);
        }
    }

    /**
     * @return String return the totalHashSum
     */
    public String getTotalHashSum() {
        return this.totalHashSum;
    }

    /**
     * @return String return the title
     */
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return String return the uuid
     */
    public String getUUID() {
        return uuid;
    }

    public int getBlockAmount() {
        return this.blockAmount;
    }

    public void setBlockAmount(int blockAmount) {
        this.blockAmount = blockAmount;
    }

    public File getRessourceFile() {
        String ressourceFilePath = Ressource.PARENT_DIRECTORY + this.uuid + "/ressourceFile.g2g";

        if (!Files.exists(Path.of(ressourceFilePath))) {
            return null;
        }

        return new File(ressourceFilePath);
    }

    public int getSizeMiB() {
        return (int) (this.blockAmount * Ressource.BLOCK_SIZE) / (1024 * 1024);
    }
}
