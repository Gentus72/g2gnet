package org.geooo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.geooo.dto.ServerDTO;
import org.geooo.util.Logger;

public class RessourceBlock {

    private final String uuid;
    private final String parentDirectoryPath = "res/";
    private String hashSum;
    private int sequenceID;

    private byte[] data;

    /*
     * Standart Konstruktor
     */
    public RessourceBlock(String uuid) {
        this.uuid = uuid;
    }

    public void writeToServer(ServerDTO server) {
        // write block to remote server

        if (data == null) {
            Logger.error("Error while writing RessourceBlock to file: No data supplied!");
            System.exit(1); // maybe change to more advanced error handling, but for now I dont want thousands of error messages if this fails!
        }

        try {
            File blockFile = new File(this.parentDirectoryPath, getUUID() + ".g2gblock");

            if (!blockFile.createNewFile()) {
                Logger.error("Blockfile already exists!");
                System.exit(1);
            }

            try (FileOutputStream outputStream = new FileOutputStream(blockFile)) {
                outputStream.write(this.data);
            }
        } catch (IOException e) {
            Logger.error("Error while handling blockfile!");
            Logger.exception(e);
        }
    }

    public String getUUID() {
        return this.uuid;
    }

    public String getHashSum() {
        return this.hashSum;
    }

    public void setHashSum(String hashSum) {
        this.hashSum = hashSum;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return this.data;
    }

    public int getSequenceID() {
		return this.sequenceID;
	}

	public void setSequenceID(int sequenceID) {
		this.sequenceID = sequenceID;
	}
}
