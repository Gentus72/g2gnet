package org.geooo.dto;

import org.geooo.metadata.NetworkFile;

public class NetworkDTO {
    private String networkUUID;
    private String networkLabel;
    private String ccServerIPv4;
    private NetworkFile networkFile;

    public NetworkDTO(String networkUUID, String networkLabel, String ccServerIPv4, NetworkFile networkFile) {
        this.networkUUID = networkUUID;
        this.networkLabel = networkLabel;
        this.ccServerIPv4 = ccServerIPv4;
        this.networkFile = networkFile;
    }

    public String getNetworkUUID() {
        return this.networkUUID;
    }

    public void setNetworkUUID(String networkUUID) {
        this.networkUUID = networkUUID;
    }

    public String getNetworkLabel() {
        return this.networkLabel;
    }

    public void setNetworkLabel(String networkLabel) {
        this.networkLabel = networkLabel;
    }

    public String getCCServerIPv4() {
        return this.ccServerIPv4;
    }

    public void setCCServerIPv4(String ccServerIPv4) {
        this.ccServerIPv4 = ccServerIPv4;
    }

    public NetworkFile getNetworkFile() {
        return this.networkFile;
    }

    public void setNetworkFile(NetworkFile networkFile) {
        this.networkFile = networkFile;
    }

}
