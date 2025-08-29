package org.geooo.gui;

import org.geooo.dto.NetworkDTO;

import javafx.beans.property.ObjectProperty;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/*
 * JavaFX-Komponente zum Darstellen von Netzwerkinformationen,
 * sowie allen vorhanenen Ressourcen mit der Möglichkeit für Upload / Download.
 */
public class NetworkContent extends VBox {
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
