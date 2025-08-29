package org.geooo.gui;

import java.util.ArrayList;

import org.geooo.ClientHelper;
import org.geooo.dto.NetworkDTO;
import org.geooo.dto.RessourceDTO;

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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/*
 * JavaFX-Komponente zum Anzeigen aller verfügbaren Ressourcen / Dateien
 * des Netzwerks, mit dem man verbunden ist.
 * Ermöglicht auch den Upload einer neuen Datei via Knopfdruck.
 */
public class RessourceList extends VBox {
    private Label header;
    private Button uploadButton;
    private FileChooser uploadFileChooser;
    private ArrayList<RessourceDTO> ressources;
    private ListProperty<RessourceListItem> listItems;
    private ListView<RessourceListItem> listView;

    /**
     * List aller auf diesem Netzwerk verfügbaren Ressourcen mit Upload-Funktionalität
     * @param connectedNetworkProp
     * @param primaryStage
     */
    public RessourceList(ObjectProperty<NetworkDTO> connectedNetworkProp, Stage primaryStage) {
        this.ressources = connectedNetworkProp.get() != null ? connectedNetworkProp.get().getNetworkFile().getRessources() : new ArrayList<>();
        this.listItems = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.uploadFileChooser = new FileChooser();
        this.uploadFileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"));

        // Überschrift
        header = new Label("Available Ressources");
        header.setFont(Fonts.headerFont);
        header.setPrefWidth(400);
        header.setPrefHeight(30);
        header.setTextAlignment(TextAlignment.CENTER);
        header.setAlignment(Pos.CENTER);

        // Uploadknopf
        uploadButton = new Button("Upload");
        uploadButton.setPrefWidth(400);
        uploadButton.setPrefHeight(30);
        uploadButton.setOnMousePressed(mouseEvent -> {
            if (mouseEvent.isPrimaryButtonDown()) {
                if (connectedNetworkProp.get() == null) {
                    return;
                }

                // neues Fenster zum Spezifizieren der hochzuladenden Datei
                UploadModal uploadModal = new UploadModal();
                String ressourceUUID = uploadModal.display();
                String networkUUID = connectedNetworkProp.get().getNetworkUUID();

                NetworkDTO currentNetwork = G2GUI.connectedNetwork.get();
                if (G2GUI.client != null && G2GUI.client.isConnected) {
                    ClientHelper.handleServerInteraction(G2GUI.client, new String[] { "DISCONNECT" });
                }

                ClientHelper.handleClientCommandFULLUPLOAD(G2GUI.client, new String[] { "FULLUPLOAD", networkUUID, ressourceUUID });

                // refresh network overview and ressourcelist
                G2GUI.connectNetwork(currentNetwork.getCCServerIPv4());
            }
        });

        HBox headerBox = new HBox();
        headerBox.getChildren().addAll(header, uploadButton);

        updateListItems();

        this.listView = new ListView<>();
        this.listView.itemsProperty().bind(listItems);

        // verfügbare Ressourcen werden erneuert, sobald sich der Status
        // des verbundenen Netzwerks ändert
        connectedNetworkProp.addListener(change -> {
            this.ressources.clear();
            this.ressources = connectedNetworkProp.get().getNetworkFile().getRessources();
            updateListItems();
        });

        this.setPrefWidth(800);
        this.setPadding(new Insets(20, 0, 0, 0));
        this.getChildren().addAll(headerBox, this.listView);
    }

    /**
     * Logik für das Hochladen der Datei
     */
    public void uploadFile() {
        if (G2GUI.client.isConnected) {
            ClientHelper.handleServerInteraction(G2GUI.client, new String[] { "DISCONNECT" });
        }

        ClientHelper.handleClientCommandFULLUPLOAD(G2GUI.client, new String[] { "FULLUPLOAD" });
    }

    /**
     * Updatet die UI bei Änderungen
     */
    private void updateListItems() {
        listItems.clear();
        for (RessourceDTO dto : this.ressources) {
            listItems.add(new RessourceListItem(dto));
        }
    }
}
