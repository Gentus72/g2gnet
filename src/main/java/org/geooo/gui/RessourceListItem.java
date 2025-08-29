package org.geooo.gui;

import org.geooo.ClientHelper;
import org.geooo.dto.RessourceDTO;
import org.geooo.util.Logger;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/* 
 * JavaFX-Komponente für jedes Element von org.geooo.gui.RessourceList.
 * Zeigt den Titel und Größe an und gibt die Möglichkeit, die
 * Ressource / Datei herunter zu laden.
 */
public class RessourceListItem extends HBox {
    private RessourceDTO ressource;
    private Button button;

    public RessourceListItem(RessourceDTO ressource) {
        this.ressource = ressource;

        this.setWidth(600);
        this.setHeight(100);

        // Titel der Ressource
        Label label = new Label(ressource.getTitle());
        label.setPrefWidth(400);

        // Größe der Ressource in MiB
        String ressourceFileSize = "max. " + String.valueOf(this.ressource.getBlockAmount() * 16) + " MiB";
        Label fileSizeLabel = new Label(ressourceFileSize);
        fileSizeLabel.setPrefWidth(200);

        // Downloadknopf
        this.button = new Button("Download");
        this.button.setPrefWidth(100);
        this.button.setOnMousePressed(mouseEvent -> {
            if (mouseEvent.isPrimaryButtonDown()) {
                Logger.info(String.format("Downloading ressource [%s]...", this.ressource.getUUID()));
                downloadRessource();
            }
        });

        this.getChildren().addAll(label, fileSizeLabel, button);
    }

    // Logik für das Herunterladen der Ressource
    public void downloadRessource() {
        if (G2GUI.client.isConnected) {
            ClientHelper.handleServerInteraction(G2GUI.client, new String[] { "DISCONNECT" });
        }

        ClientHelper.handleClientCommandAUTOGET(G2GUI.client, new String[] { "AUTOGET", this.ressource.getUUID() });
    }
}
