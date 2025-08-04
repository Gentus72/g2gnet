package org.geooo.gui;

import org.geooo.dto.NetworkDTO;

import javafx.beans.property.ObjectProperty;
import javafx.scene.layout.VBox;

public class NetworkContent extends VBox {
    // TODO add RessourceList
    // TODO add Upload / Download

    private NetworkOverview overview;

    public NetworkContent(ObjectProperty<NetworkDTO> network) {
        this.setWidth(800);
        this.setHeight(Double.MAX_VALUE);

        this.overview = new NetworkOverview(network);

        this.getChildren().addAll(this.overview);
    }
}
