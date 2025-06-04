package org.geooo.dto;

import java.security.PublicKey;
import java.util.Base64;

public class ServerDTO {
    public static final int SERVER_PORT = 7000;

    private String uuid;
    private String address;
    private PublicKey[] clientPublicKeys;

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

	public PublicKey[] getClientPublicKeys() {
		return this.clientPublicKeys;
	}

    public String[] getClientPublicKeysBase64() {
        if (this.clientPublicKeys == null) {
            return new String[] {};
        }

        String[] keysBase64 = new String[this.clientPublicKeys.length];

        for (int i = 0; i < this.clientPublicKeys.length; i++) {
            keysBase64[i] = Base64.getEncoder().encodeToString(this.clientPublicKeys[i].getEncoded());
        }

        return keysBase64;
    }

	public void setClientPublicKeys(PublicKey[] clientPublicKeys) {
		this.clientPublicKeys = clientPublicKeys;
	}
}
