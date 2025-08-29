package org.geooo.gui;

import org.geooo.dto.NetworkDTO;

import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/*
 * JavaFX-Komponenten für jedes bekannte Netzwerk, was angezeigt werden soll.
 * Stellt jedes Element von org.geooo.gui.NetworkList dar.
 */
public class NetworkListItem extends HBox {
    public NetworkDTO network;
    private Button button;
    private boolean isConnected;

    public NetworkListItem(NetworkDTO network, ObjectProperty<NetworkDTO> connectedNetworkProp) {
        this.network = network;
        this.isConnected = connectedNetworkProp.get() != null && this.network.getNetworkUUID().equals(connectedNetworkProp.get().getNetworkUUID());

        connectedNetworkProp.addListener((obs, oldVal, newVal) -> {
            this.isConnected = this.network.getNetworkUUID().equals(connectedNetworkProp.get().getNetworkUUID());
            this.button.setDisable(this.isConnected);
            this.button.setText(this.isConnected ? "Connected" : "Connect");
        });

        this.setWidth(300);
        this.setHeight(100);

        // titel des Netzwerks
        Label label = new Label(network.getNetworkLabel().equals("<noLabel>") ? network.getCCServerIPv4() : network.getNetworkLabel());
        label.setPrefWidth(185);

        // verbindungsknopf
        this.button = new Button(this.isConnected ? "Connected" : "Connect");
        this.button.setDisable(this.isConnected);
        this.button.setPrefWidth(100);

        // mit dem Netzwerk verbinden, sobald der knopf gedrückt wird
        this.button.setOnMousePressed(mouseEvent -> {
            if (mouseEvent.isPrimaryButtonDown() && !this.isConnected) {
                G2GUI.connectNetwork(this.network.getCCServerIPv4());
            }
        });

        this.getChildren().addAll(label, button);
    }

    public NetworkDTO getNetwork() {
        return this.network;
    }
}
