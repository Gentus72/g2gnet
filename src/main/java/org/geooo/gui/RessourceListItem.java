package org.geooo.gui;

import org.geooo.ClientHelper;
import org.geooo.dto.RessourceDTO;
import org.geooo.util.Logger;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class RessourceListItem extends HBox {
    private RessourceDTO ressource;
    private Button button;

    public RessourceListItem(RessourceDTO ressource) {
        this.ressource = ressource;

        this.setWidth(600);
        this.setHeight(100);

        Label label = new Label(ressource.getTitle());
        label.setPrefWidth(400);

        String ressourceFileSize = "max. " + String.valueOf(this.ressource.getBlockAmount() * 16) + " MiB";
        Label fileSizeLabel = new Label(ressourceFileSize);
        fileSizeLabel.setPrefWidth(200);

        this.button = new Button("Download");
        this.button.setPrefWidth(100);

        this.button.setOnMousePressed(mouseEvent -> {
            if (mouseEvent.isPrimaryButtonDown()) {
                Logger.info(String.format("Downloading ressource [%s]...", this.ressource.getUUID()));

                if (G2GUI.client.isConnected) {
                    ClientHelper.handleServerInteraction(G2GUI.client, new String[] { "DISCONNECT" });
                }

                ClientHelper.handleClientCommandAUTOGET(G2GUI.client, new String[] { "AUTOGET", this.ressource.getUUID() });
            }
        });

        this.getChildren().addAll(label, fileSizeLabel, button);
    }
}
