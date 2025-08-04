package org.geooo.gui;

import org.geooo.dto.NetworkDTO;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public class NetworkList extends VBox {

    private final ListProperty<NetworkDTO> networks;
    private final ListProperty<NetworkListItem> listItems;
    private final ListView<NetworkListItem> listView;

    public NetworkList(ListProperty<NetworkDTO> networkProp) {
        this.networks = networkProp;
        this.listItems = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.listView = new ListView<>(listItems);
        this.listView.itemsProperty().bind(listItems);

        this.setPrefWidth(300);
        this.setPrefHeight(670);
        VBox.setVgrow(listView, Priority.ALWAYS);
        listView.setPrefHeight(620);

        Label header = new Label("Available Networks");
        header.setFont(Fonts.headerFont);
        header.setPrefWidth(300);
        header.setPrefHeight(50);
        header.setTextAlignment(TextAlignment.CENTER);
        header.setAlignment(Pos.CENTER);

        getChildren().addAll(header, listView);

        updateListItems();

        networks.addListener((ListChangeListener<NetworkDTO>) change -> {
            updateListItems();
        });
    }

    private void updateListItems() {
        listItems.clear();
        for (NetworkDTO dto : networks) {
            listItems.add(new NetworkListItem(dto));
        }
    }
}
