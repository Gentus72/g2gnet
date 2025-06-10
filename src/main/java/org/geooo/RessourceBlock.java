package org.geooo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.geooo.dto.RessourceBlockDTO;
import org.geooo.util.Logger;

public class RessourceBlock extends RessourceBlockDTO {

    private String parentDirectory;
    private byte[] data;

    /*
     * Standart Konstruktor
     */
    public RessourceBlock(String uuid) {
        this.uuid = uuid;
    }

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

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return this.data;
    }

    public void setParentDirectory(String parentDirectory) {
        this.parentDirectory = parentDirectory;
    }
}
