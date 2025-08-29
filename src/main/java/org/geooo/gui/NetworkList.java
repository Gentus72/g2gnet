package org.geooo.gui;

import org.geooo.dto.NetworkDTO;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

/*
 * JavaFX-Komponente zum Darstellen von allen bekannten Netzwerken
 */
public class NetworkList extends VBox {

    private final ListProperty<NetworkDTO> networks;
    private final ListProperty<NetworkListItem> listItems;
    private final ListView<NetworkListItem> listView;

    /**
     * Eine Schnellzugriffsliste aller bekannten Netzwerke
     * @param networkProp
     * @param connectedNetworkProp
     */
    public NetworkList(ListProperty<NetworkDTO> networkProp, ObjectProperty<NetworkDTO> connectedNetworkProp) {
        this.networks = networkProp;
        this.listItems = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.listView = new ListView<>(listItems);
        this.listView.itemsProperty().bind(listItems);

        this.setPrefWidth(300);
        this.setPrefHeight(670);
        VBox.setVgrow(listView, Priority.ALWAYS);
        listView.setPrefHeight(620);

        // überschrift
        Label header = new Label("Available Networks");
        header.setFont(Fonts.headerFont);
        header.setPrefWidth(300);
        header.setPrefHeight(50);
        header.setTextAlignment(TextAlignment.CENTER);
        header.setAlignment(Pos.CENTER);

        getChildren().addAll(header, listView);

        // update der UI initialisieren
        updateListItems(connectedNetworkProp);

        networks.addListener((ListChangeListener<NetworkDTO>) change -> {
            updateListItems(connectedNetworkProp);
        });
    }

    /**
     * löst ein update der UI aus, sollte ein Netzwerk hinzugefügt worden sein
     * @param connectedNetworkProp
     */
    private void updateListItems(ObjectProperty<NetworkDTO> connectedNetworkProp) {
        listItems.clear();
        for (NetworkDTO dto : networks) {
            listItems.add(new NetworkListItem(dto, connectedNetworkProp));
        }
    }
}
