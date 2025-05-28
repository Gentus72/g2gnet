package org.geooo.dto;

public class ClientDTO {
    String uuid;

    public ClientDTO(String uuid) {
        this.uuid = uuid;
    }

        public String getUUID() {
        return this.uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }
}
