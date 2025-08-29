package org.geooo.dto;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;

import org.geooo.util.G2GUtil;
import org.geooo.util.Logger;

/*
 * Helfer-Klasse für HostServer und CCServer.
 * enthält wichtige Informationen und Methoden, die
 * für einen Server jeglicher ARt wichtig sind.
 */
public class ServerDTO {

    public static final int SERVER_PORT = 7000;

    private String uuid;
    private String address;
    private ArrayList<PublicKey> clientPublicKeys; // erlaubte Publickeys von Clients, die gerade hochladen dürfen
    private ArrayList<String> allowedBlockUUIDs;

    /**
     * @param uuid
     * @param address
     */
    public ServerDTO(String uuid, String address) {
        this.uuid = uuid;
        this.address = address;
    }

    /**
     * @param address
     */
    public ServerDTO(String address) {
        this.address = address;
    }

    public ServerDTO() {

    }

    /**
     * @return Gibt die UUID des Servers aus
     */
    public String getUUID() {
        return this.uuid;
    }

    /**
     * @return gibt die IPv4-Adresse des Servers aus
     */
    public String getAddress() {
        if (this.address == null) {
            this.address = G2GUtil.getLocalIPv4Address();
        }

        return this.address;
    }

    /**
     * Setzt die UUID des Servers
     * @param uuid
     */
    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    /**
     * Setzt die IPv4-Adresse des Servers
     * @param address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return Gibt die öffentlichen Schlüssel der Clients aus, die gerade auf diesen Server hochladen dürfen
     */
    public ArrayList<PublicKey> getClientPublicKeys() {
        return this.clientPublicKeys;
    }

    /**
     * @return Gibt die öffentlichen Schlüssel in Base64-Kodierung der Clients aus, die gerade auf diesen Server hochladen dürfen
     */
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

    /**
     * Setzt öffentlichen Schlüssel der Clients aus, die gerade auf diesen Server hochladen dürfen
     * @param clientPublicKeys
     */
    public void setClientPublicKeys(ArrayList<PublicKey> clientPublicKeys) {
        if (this.clientPublicKeys == null) {
            this.clientPublicKeys = new ArrayList<>();
        }

        this.clientPublicKeys = clientPublicKeys;
    }

    // Diese Warnung wird unterdrückt, da VS-Code hier einen Fehler macht. Weitere Exceptions können auftreten.
    @SuppressWarnings("UseSpecificCatch")
    /**
     * Fügt einen öffentlichen Schlüssel eines Clients hinzu, der jetzt hochladen darf
     * @param clientPublicKeyBase64
     */
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

    /**
     * @return Gibt die UUIDs aller Blöcöke aus, die gerade hochgeladen werden dürfen
     */
    public ArrayList<String> getAllowedBlockUUIDs() {
        if (this.allowedBlockUUIDs == null) {
            this.allowedBlockUUIDs = new ArrayList<>();
        }

        return this.allowedBlockUUIDs;
    }

    /**
     * Setzt die UUids aller Blöcke, die gerade hochgeladen werden dürfen
     * @param allowedBlockUUIDs
     */
    public void setAllowedBlockUUIDs(ArrayList<String> allowedBlockUUIDs) {
        if (this.allowedBlockUUIDs == null) {
            this.allowedBlockUUIDs = new ArrayList<>();
        }

        this.allowedBlockUUIDs = allowedBlockUUIDs;
    }

    /**
     * Fügt eine UUID eines Blocks hinzu, der jetzt hochgeladen werden darf
     * @param allowedBlockUUID
     */
    public void addAllowedBlockUUID(String allowedBlockUUID) {
        if (this.allowedBlockUUIDs == null) {
            this.allowedBlockUUIDs = new ArrayList<>();
        }

        this.allowedBlockUUIDs.add(allowedBlockUUID);
    }

    /**
     * @return Gibt den Pfad des Ordners an, in dem Ressourcen / Blöcke gespeichert werden
     */
    public static String getRessourceDirectory() {
        return "unimplemented/";
    }
}
