package org.geooo.dto;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.geooo.util.Logger;

public class ClientDTO {
    String uuid;
    KeyPair keyPair;
    String publicKey;
    String privateKey;

    public ClientDTO() {

    }

    public ClientDTO(String uuid) {
        this.uuid = uuid;
    }

    public String getUUID() {
        return this.uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public KeyPair getKeyPair() {
        try {
            if (keyPair == null) {
                return generateRSAKeyPair();
            }

            return keyPair;
        } catch (NoSuchAlgorithmException e) {
            Logger.error("Error while generating / getting client keypair!");
            Logger.exception(e);
        }

        return null;
    }

    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(this.getKeyPair().getPublic().getEncoded());
    }

    public String getPrivateKeyBase64() {
        return Base64.getEncoder().encodeToString(this.getKeyPair().getPrivate().getEncoded());
    }

    public static KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        keyPairGenerator.initialize(256);

        return keyPairGenerator.generateKeyPair();
    }

    public String getPublicKey() {
        return this.publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return this.privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
}
