package org.geooo.gui;

import java.util.ArrayList;

import org.geooo.dto.NetworkDTO;
import org.geooo.dto.RessourceDTO;
import org.geooo.util.Logger;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public class RessourceList extends VBox {
    private Label header;
    private Button uploadButton;
    private ArrayList<RessourceDTO> ressources;
    private ListProperty<RessourceListItem> listItems;
    private ListView<RessourceListItem> listView;

    public RessourceList(ObjectProperty<NetworkDTO> connectedNetworkProp) {
        this.ressources = connectedNetworkProp.get() != null ? connectedNetworkProp.get().getNetworkFile().getRessources() : new ArrayList<>();
        this.listItems = new SimpleListProperty<>(FXCollections.observableArrayList());

        header = new Label("Available Ressources");
        header.setFont(Fonts.headerFont);
        header.setPrefWidth(400);
        header.setPrefHeight(30);
        header.setTextAlignment(TextAlignment.CENTER);
        header.setAlignment(Pos.CENTER);

        uploadButton = new Button("Upload");
        uploadButton.setPrefWidth(400);
        uploadButton.setPrefHeight(30);
        uploadButton.setOnMousePressed(mouseEvent -> {
            if (mouseEvent.isPrimaryButtonDown()) {
                Logger.info("Upload button clicked!");
            }
        });

        HBox headerBox = new HBox();
        headerBox.getChildren().addAll(header, uploadButton);

        updateListItems();

        this.listView = new ListView<>();
        this.listView.itemsProperty().bind(listItems);

        connectedNetworkProp.addListener(change -> {
            this.ressources.clear();
            this.ressources = connectedNetworkProp.get().getNetworkFile().getRessources();
            updateListItems();
        });

        this.setPrefWidth(800);
        this.setPadding(new Insets(20, 0, 0, 0));
        this.getChildren().addAll(headerBox);
    }

    private void updateListItems() {
        listItems.clear();
        for (RessourceDTO dto : this.ressources) {
            listItems.add(new RessourceListItem(dto));
        }
    }
}
