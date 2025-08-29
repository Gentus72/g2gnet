package org.geooo.gui;

import java.io.File;

import org.geooo.Client;
import org.geooo.Ressource;
import org.geooo.util.Logger;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

/*
 * Ein eigenes Fenster zum Spezifizieren der hochzuladenden Datei.
 * Gibt die Möglichkeit Datei und Titel der Ressource zu wählen und
 * enthält Logik für das "Ressource-ifizieren" der Datei.
 */
public class UploadModal {
    private String ressourceUUID;
    private File selectedFile;
    private StringProperty selectedFileName;

    public UploadModal() {
        this.selectedFileName = new SimpleStringProperty();
    }

    // Hauptmethode dieser Klasse. Gibt die UUID der erstellten Ressource zurück.
    public String display() {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Upload a File");
        window.setMinWidth(250);

        // Dateiwahl
        Label label = new Label("Choose a file to upload:");
        FileChooser fileChooser = new FileChooser();

        Button fileButton = new Button("Select File");
        fileButton.setOnMousePressed(mouseEvent -> {
            if (mouseEvent.isPrimaryButtonDown()) {
                this.selectedFile = fileChooser.showOpenDialog(window);
                this.selectedFileName.set(this.selectedFile.getName());
            }
        });

        // Titelwahl
        TextField titleInput = new TextField();
        titleInput.setPromptText("Enter a title...");

        // Eingabeknopf
        Button submitButton = new Button("Submit");

        // Label zum Anzeigen der ausgewählten Datei
        Label selectedFileLabel = new Label();
        this.selectedFileName.addListener(change -> {
            selectedFileLabel.setText(this.selectedFileName.get());
        });

        // Logik zum Erstellen der Ressource
        submitButton.setOnAction(e -> {
            if (titleInput.getText().isEmpty()) {
                Logger.warn("Tried to upload but title field was empty!");
                return;
            }

            if (this.selectedFile == null) {
                Logger.error("Selected file for upload was not selected!");
                return;
            }

            String ressourceTitle = titleInput.getText();
            Ressource ressource = Ressource.disassemble(Client.RESSOURCE_DIRECTORY, this.selectedFile, ressourceTitle, G2GUI.client.getPublicKeyBase64());
            this.ressourceUUID = ressource.getUUID();

            window.close();
        });

        VBox layout = new VBox(10);
        HBox selectFileBox = new HBox(10);
        selectFileBox.getChildren().addAll(fileButton, selectedFileLabel);
        layout.getChildren().addAll(label, selectFileBox, titleInput, submitButton);
        layout.setStyle("-fx-padding: 10;");
        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.showAndWait();

        return this.ressourceUUID;
    }
}
