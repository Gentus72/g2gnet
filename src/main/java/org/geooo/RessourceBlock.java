package org.geooo;

import org.geooo.dto.ServerDTO;

public class RessourceBlock {

    private final String uuid;
    private String hashSum;
    private byte[] data;

    /*
     * Standart Konstruktor
     */
    public RessourceBlock(String uuid) {
        this.uuid = uuid;
    }

    public void writeToServer(ServerDTO server) {
        // write block to remote server
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
}
