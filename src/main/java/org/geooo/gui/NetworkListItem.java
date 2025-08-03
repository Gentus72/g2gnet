package org.geooo.gui;

import org.geooo.dto.NetworkDTO;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class NetworkListItem extends HBox {
    public NetworkDTO network;

    public NetworkListItem(NetworkDTO network) {
        this.network = network;

        this.setWidth(300);
        this.setHeight(100);

        Label _label = new Label(network.getNetworkLabel());
        _label.setPrefWidth(185);
        Button button = new Button("Connect");
        button.setPrefWidth(100);

        this.getChildren().addAll(_label, button);
    }

    public NetworkDTO getNetwork() {
        return this.network;
    }
}
