package org.geooo.dto;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.geooo.util.G2GUtil;
import org.geooo.util.Logger;

public class ClientDTO {

    String uuid;
    String address;
    PublicKey publicKey;
    PrivateKey privateKey;

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

    public String getPublicKeyBase64() {
        if (this.publicKey == null) {
            Logger.warn("getPublicKeyBase64 was called although publickey is null! Generating keypair...");
            generateRSAKeyPair();
        }

        return Base64.getEncoder().encodeToString(this.publicKey.getEncoded());
    }

    public PublicKey getPublicKey() {
        if (this.publicKey == null) {
            Logger.warn("getPublicKeyBase64 was called although publickey is null! Generating keypair...");
            generateRSAKeyPair();
        }

        return this.publicKey;
    }

    public String getPrivateKeyBase64() {
        if (this.privateKey == null) {
            Logger.warn("getPublicKeyBase64 was called although privatekey is null! Generating keypair...");
            generateRSAKeyPair();
        }

        return Base64.getEncoder().encodeToString(this.privateKey.getEncoded());
    }

    public PrivateKey getPrivateKey() {
        if (this.privateKey == null) {
            Logger.warn("getPublicKeyBase64 was called although privatekey is null! Generating keypair...");
            generateRSAKeyPair();
        }

        return this.privateKey;
    }

    @SuppressWarnings("UseSpecificCatch")
    public void setPublickeyBase64(String publickeyBase64) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(publickeyBase64);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            this.publicKey = keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            Logger.error("Error while setting public key from base64!");
            Logger.exception(e);
        }
    }

    @SuppressWarnings("UseSpecificCatch")
    public void setPrivateKeyBase64(String privateKeyBase64) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyBase64);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            this.privateKey = keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            Logger.error("Error while setting private key from base64!");
            Logger.exception(e);
        }
    }

    public void generateRSAKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048); // Or 4096 for stronger security
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            this.publicKey = keyPair.getPublic();
            this.privateKey = keyPair.getPrivate();
        } catch (NoSuchAlgorithmException e) {
            Logger.error("Error while generating RSA keypair!");
            Logger.exception(e);
        }
    }

    public String getAddress() {
        if (this.address == null) {
            this.address = G2GUtil.getLocalIPv4Address();
        }

        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
