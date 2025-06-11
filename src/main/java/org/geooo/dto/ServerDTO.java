package org.geooo.dto;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;

import org.geooo.util.G2GUtil;
import org.geooo.util.Logger;

public class ServerDTO {

    public static final int SERVER_PORT = 7000;

    private String uuid;
    private String address;
    private ArrayList<PublicKey> clientPublicKeys;
    private ArrayList<String> allowedBlockUUIDs;

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
        if (this.address == null) {
            this.address = G2GUtil.getLocalIPv4Address();
        }

        return this.address;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public ArrayList<PublicKey> getClientPublicKeys() {
        return this.clientPublicKeys;
    }

    public ArrayList<String> getClientPublicKeysBase64() {
        if (this.clientPublicKeys == null) {
            return new ArrayList<>();
        }

        ArrayList<String> keysBase64 = new ArrayList<>();

        for (PublicKey key : this.clientPublicKeys) {
            keysBase64.add(Base64.getEncoder().encodeToString(key.getEncoded()));
        }

        return keysBase64;
    }

    public void setClientPublicKeys(ArrayList<PublicKey> clientPublicKeys) {
        if (this.clientPublicKeys == null) {
            this.clientPublicKeys = new ArrayList<>();
        }

        this.clientPublicKeys = clientPublicKeys;
    }

    @SuppressWarnings("UseSpecificCatch")
    public void addClientPublicKey(String clientPublicKeyBase64) {
        if (this.clientPublicKeys == null) {
            this.clientPublicKeys = new ArrayList<>();
        }

        try {
            byte[] keyBytes = Base64.getDecoder().decode(clientPublicKeyBase64);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            this.clientPublicKeys.add(keyFactory.generatePublic(keySpec));
        } catch (Exception e) {
            Logger.error("Error while setting public key from base64!");
            Logger.exception(e);
        }
    }

    public ArrayList<String> getAllowedBlockUUIDs() {
        return allowedBlockUUIDs;
    }

    public void setAllowedBlockUUIDs(ArrayList<String> allowedBlockUUIDs) {
        if (this.allowedBlockUUIDs == null) {
            this.allowedBlockUUIDs = new ArrayList<>();
        }

        this.allowedBlockUUIDs = allowedBlockUUIDs;
    }

    public void addAllowedBlockUUID(String allowedBlockUUID) {
        if (this.allowedBlockUUIDs == null) {
            this.allowedBlockUUIDs = new ArrayList<>();
        }

        this.allowedBlockUUIDs.add(allowedBlockUUID);
    }

    public static String getRessourceDirectory() {
        return "unimplemented/";
    }
}
