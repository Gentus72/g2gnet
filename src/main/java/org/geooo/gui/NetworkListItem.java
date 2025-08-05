package org.geooo.gui;

import org.geooo.dto.NetworkDTO;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class NetworkListItem extends HBox {
    public NetworkDTO network;
    private BooleanProperty isConnected = new SimpleBooleanProperty();

    public NetworkListItem(NetworkDTO network) {
        this.network = network;
        this.isConnected.set(G2GUI.connectedNetwork.get() != null && this.network.getNetworkUUID().equals(G2GUI.connectedNetwork.get().getNetworkUUID()));
        this.isConnected.bind(Bindings.createBooleanBinding(() -> {
            return this.network.getNetworkUUID().equals(G2GUI.connectedNetwork.get().getNetworkUUID());
        }, G2GUI.connectedNetwork));

        this.setWidth(300);
        this.setHeight(100);

        Label _label = new Label(network.getNetworkLabel().equals("<noLabel>") ? network.getCCServerIPv4() : network.getNetworkLabel());
        _label.setPrefWidth(185);
        Button button = new Button(this.isConnected.get() ? "Connected" : "Connect");
        button.setDisable(this.isConnected.get());
        button.setPrefWidth(100);

        button.setOnMousePressed(mouseEvent -> {
            if (mouseEvent.isPrimaryButtonDown() && !this.isConnected.get()) {
                G2GUI.connectNetwork(this.network.getCCServerIPv4());
            }
        });

        this.getChildren().addAll(_label, button);
    }

    public NetworkDTO getNetwork() {
        return this.network;
    }
}
