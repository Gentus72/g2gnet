package org.geooo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.geooo.dto.RessourceBlockDTO;
import org.geooo.util.Logger;

/**
 * Eine Datenstruktur für einen Teil der Daten einer Datei / Ressource
 */
public class RessourceBlock extends RessourceBlockDTO {

    private String parentDirectory;
    private byte[] data;

    public RessourceBlock(String uuid) {
        this.uuid = uuid;
    }

    /**
     * Nimmt alle in diesem Objekt vorhandenen daten und erstellt die Block-Datei
     */
    public void writeToFile() {
        if (data == null) {
            Logger.error("Error while writing RessourceBlock to file: No data supplied!");
            System.exit(1); // maybe change to more advanced error handling, but for now I dont want thousands of error messages if this fails!
        }

        try {
            File blockFile = new File(this.parentDirectory, getUUID() + ".g2gblock");

            if (blockFile.exists()) {
                Logger.error("Blockfile already exists!");
                System.exit(1);
            }

            blockFile.createNewFile();

            try (FileOutputStream outputStream = new FileOutputStream(blockFile)) {
                outputStream.write(this.data);
            }
        } catch (IOException e) {
            Logger.error("Error while handling blockfile!");
            Logger.exception(e);
        }
    }

    /**
     * Setzt die zu speichernden Datein (ein Teil der Originaldatei)
     * @param data
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * @return die Daten die dieser Block speichern soll
     */
    public byte[] getData() {
        return this.data;
    }

    /**
     * Setzt den Ordner, indem dieser Block gespeichert werden soll
     * @param parentDirectory
     */
    public void setParentDirectory(String parentDirectory) {
        this.parentDirectory = parentDirectory;
    }
}
