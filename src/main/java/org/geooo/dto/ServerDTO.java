package org.geooo.dto;

public class ServerDTO {

    private String uuid;
    private String address;

    public ServerDTO(String uuid, String address) {
        this.uuid = uuid;
        this.address = address;
    }

    public ServerDTO(String address) {
        this.address = address;
    }

    public ServerDTO() {

    }

    public String getUUID() {
        return this.uuid;
    }

    public String getAddress() {
        return this.address;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
