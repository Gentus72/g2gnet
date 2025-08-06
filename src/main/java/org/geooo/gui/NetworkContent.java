package org.geooo.gui;

import org.geooo.dto.NetworkDTO;

import javafx.beans.property.ObjectProperty;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class NetworkContent extends VBox {
    // TODO add RessourceList
    // TODO add Upload / Download

    private NetworkOverview overview;
    private RessourceList ressourceList;

    public NetworkContent(ObjectProperty<NetworkDTO> network, Stage primaryStage) {
        this.setWidth(900);
        this.setHeight(Double.MAX_VALUE);

        this.overview = new NetworkOverview(network);
        this.ressourceList = new RessourceList(network, primaryStage);

        this.getChildren().addAll(this.overview, this.ressourceList);
    }
}
