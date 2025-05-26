package org.geooo.dto;

import java.io.File;

public class ServerDTO {

    private String uuid;
    private String address;
    private File serverFile;

    public ServerDTO(String uuid, String address, File serverFile) {
        this.uuid = uuid;
        this.address = address;
        this.serverFile = serverFile;
    }

    public ServerDTO() {

    }

    public String getUUID() {
        return this.uuid;
    }

    public String getAddress() {
        return this.address;
    }

    public File getFile() {
        return this.serverFile;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setFile(File serverFile) {
        this.serverFile = serverFile;
    }

    public String toString() {
        return this.uuid + "," + this.address + "," + this.serverFile.getName();
    }
}
