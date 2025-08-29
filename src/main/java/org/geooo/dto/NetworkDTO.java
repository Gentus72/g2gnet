package org.geooo.dto;

import org.geooo.metadata.NetworkFile;

/*
 * NetworkDTo ist ein einfaches Objekt, was wesentliche
 * Informationen über ein Netzwerk enthält, dabei aber
 * keine wichtige Logik ausführt, sondern nur Konstruktor,
 * Getter und Setter enthält.
 */
public class NetworkDTO {
    private String networkUUID;
    private String networkLabel;
    private String ccServerIPv4;
    private NetworkFile networkFile;

    /**
     * @param networkUUID
     * @param networkLabel
     * @param ccServerIPv4
     * @param networkFile
     */
    public NetworkDTO(String networkUUID, String networkLabel, String ccServerIPv4, NetworkFile networkFile) {
        this.networkUUID = networkUUID;
        this.networkLabel = networkLabel;
        this.ccServerIPv4 = ccServerIPv4;
        this.networkFile = networkFile;
    }

    /**
     * @return Gibt die UUID des Netzwerks aus
     */
    public String getNetworkUUID() {
        return this.networkUUID;
    }

    /**
     * Setzt die UUID des Netzwerks
     * @param networkUUID
     */
    public void setNetworkUUID(String networkUUID) {
        this.networkUUID = networkUUID;
    }

    /**
     * @return Gibt das Label des Netzwerks aus
     */
    public String getNetworkLabel() {
        return this.networkLabel;
    }

    /**
     * Setzt das Label des Netzwerks
     * @param networkLabel
     */
    public void setNetworkLabel(String networkLabel) {
        this.networkLabel = networkLabel;
    }

    /**
     * @return Gibt die IPv4-Adresse des Command and Control Servers aus
     */
    public String getCCServerIPv4() {
        return this.ccServerIPv4;
    }

    /**
     * Setzt die IPv4-Adresse des Command and Control Servers
     * @param ccServerIPv4
     */
    public void setCCServerIPv4(String ccServerIPv4) {
        this.ccServerIPv4 = ccServerIPv4;
    }

    /**
     * @return Gibt die zugehörige Netzwerkdaite zum Netzwerk aus
     */
    public NetworkFile getNetworkFile() {
        return this.networkFile;
    }

    /**
     * Setzt die Netzwerkdatei
     * @param networkFile
     */
    public void setNetworkFile(NetworkFile networkFile) {
        this.networkFile = networkFile;
    }

}
