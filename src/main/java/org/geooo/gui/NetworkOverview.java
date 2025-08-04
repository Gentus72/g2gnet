package org.geooo.gui;

import org.geooo.dto.NetworkDTO;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class NetworkOverview extends VBox {
    private ObjectProperty<NetworkDTO> network;
    private Label headerLabel, connectedToLabel, serverCountLabel, ressourceCountLabel;

    public NetworkOverview(ObjectProperty<NetworkDTO> networkProp) {
        this.network = networkProp;

        headerLabel = new Label("not connected");
        headerLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            NetworkDTO network = networkProp.get();
            return network != null ? network.getNetworkLabel() : "not connected";
        }, networkProp));
        headerLabel.setFont(Fonts.headerFont);
        headerLabel.setPrefWidth(400);
        headerLabel.setPrefHeight(30);

        connectedToLabel = new Label("-");
        connectedToLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            NetworkDTO network = networkProp.get();
            return "CC: " + (network != null ? network.getCCServerIPv4() : "-");
        }, networkProp));
        connectedToLabel.setFont(Fonts.subHeaderFont);
        connectedToLabel.setPrefWidth(400);
        connectedToLabel.setPrefHeight(30);

        serverCountLabel = new Label("Servers: -");
        serverCountLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            NetworkDTO network = networkProp.get();
            return "Servers: " + (network != null ? String.valueOf(network.getNetworkFile().getServers().size()) : "-");
        }, networkProp));
        serverCountLabel.setFont(Fonts.subHeaderFont);
        serverCountLabel.setPrefWidth(400);
        serverCountLabel.setPrefHeight(20);

        ressourceCountLabel = new Label("Ressources: -");
        ressourceCountLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            NetworkDTO network = networkProp.get();
            return "Ressources: " + (network != null ? String.valueOf(network.getNetworkFile().getRessources().size())
                    : "-");
        }, networkProp));
        ressourceCountLabel.setFont(Fonts.subHeaderFont);
        ressourceCountLabel.setPrefWidth(400);
        ressourceCountLabel.setPrefHeight(20);

        HBox headers = new HBox();
        headers.getChildren().addAll(headerLabel, connectedToLabel);

        HBox headers2 = new HBox();
        headers2.getChildren().addAll(serverCountLabel, ressourceCountLabel);

        this.getChildren().addAll(headers, headers2);
    }
}
