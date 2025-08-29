package org.geooo.gui;

import org.geooo.dto.NetworkDTO;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/*
 * JavaFX-Komponente zum Anzeigen von allgemeinen Daten zu einem Netzwerk,
 * mit dem man verbunden ist.
 */
public class NetworkOverview extends VBox {
    private Label headerLabel, connectedToLabel, serverCountLabel, ressourceCountLabel;

    public NetworkOverview(ObjectProperty<NetworkDTO> networkProp) {
        // netzwerktitel / label
        headerLabel = new Label("not connected");
        headerLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            NetworkDTO _network = networkProp.get();
            return _network != null ? _network.getNetworkLabel() : "not connected";
        }, networkProp));
        headerLabel.setFont(Fonts.headerFont);
        headerLabel.setPrefWidth(400);
        headerLabel.setPrefHeight(30);

        // IPv4 Adresse des CCServers des Netzwerks
        connectedToLabel = new Label("-");
        connectedToLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            NetworkDTO network = networkProp.get();
            return "CC: " + (network != null ? network.getCCServerIPv4() : "-");
        }, networkProp));
        connectedToLabel.setFont(Fonts.subHeaderFont);
        connectedToLabel.setPrefWidth(400);
        connectedToLabel.setPrefHeight(30);

        // Anzahl der Server im Netzwerk (inkl. CCServer)
        serverCountLabel = new Label("Servers: -");
        serverCountLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            NetworkDTO network = networkProp.get();
            return "Servers: " + (network != null ? String.valueOf(network.getNetworkFile().getServers().size()) : "-");
        }, networkProp));
        serverCountLabel.setFont(Fonts.subHeaderFont);
        serverCountLabel.setPrefWidth(400);
        serverCountLabel.setPrefHeight(20);

        // Anzahl der verfügbaren Ressourcen / Dateien
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

        this.setBackground(new Background(new BackgroundFill(new Color(1d, 1d, 1d, 1), null, null)));
        this.setPadding(new Insets(0, 0, 0, 5));
        this.getChildren().addAll(headers, headers2);
    }
}
