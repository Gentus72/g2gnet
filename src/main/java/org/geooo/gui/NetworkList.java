package org.geooo.gui;

import java.util.ArrayList;
import java.util.List;

import org.geooo.dto.NetworkDTO;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class NetworkList extends VBox {
    public NetworkList(ArrayList<NetworkDTO> availableNetworks) {
        this.setMinWidth(300);
        this.setMinHeight(2000);
        this.setBackground(new Background(new BackgroundFill(new Color(0.8, 0.8, 0.8, 1), null, null)));

        Label label = new Label("Available Networks");
        label.setPrefWidth(300);
        label.setPrefHeight(40);
        label.setFont(Fonts.headerFont);
        // label.setTextAlignment(TextAlignment.CENTER);
        // label.setContentDisplay(ContentDisplay.CENTER);
        label.setAlignment(Pos.CENTER);

        ListView<NetworkListItem> list = new ListView<>();
        ObservableList<NetworkListItem> items = FXCollections.observableList(generateListItems(availableNetworks));

        list.setItems(items);
        this.getChildren().addAll(label, list);
    }

    private List<NetworkListItem> generateListItems(ArrayList<NetworkDTO> availableNetworks) {
        List<NetworkListItem> listItems = new ArrayList<>();

        for (NetworkDTO network : availableNetworks) {
            listItems.add(new NetworkListItem(network));
        }

        return listItems;
    }
}
