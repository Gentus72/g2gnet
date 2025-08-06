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

public class UploadModal {
    private String ressourceUUID;
    private File selectedFile;
    private StringProperty selectedFileName;

    public UploadModal() {
        this.selectedFileName = new SimpleStringProperty();
    }

    public String display() {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Upload a File");
        window.setMinWidth(250);

        Label label = new Label("Choose a file to upload:");
        FileChooser fileChooser = new FileChooser();
        TextField titleInput = new TextField();
        titleInput.setPromptText("Enter a title...");
        Button submitButton = new Button("Submit");

        Button fileButton = new Button("Select File");
        fileButton.setOnMousePressed(mouseEvent -> {
            if (mouseEvent.isPrimaryButtonDown()) {
                this.selectedFile = fileChooser.showOpenDialog(window);
                this.selectedFileName.set(this.selectedFile.getName());
            }
        });

        Label selectedFileLabel = new Label();
        this.selectedFileName.addListener(change -> {
            selectedFileLabel.setText(this.selectedFileName.get());
        });

        submitButton.setOnAction(e -> {
            if (titleInput.getText().isEmpty()) {
                Logger.warn("Tried to upload but title field was empty!");
                return;
            }

            if (this.selectedFile == null) {
                Logger.error("Selected file for upload was not selected!");
                return;
            }

            // generate Ressource
            Ressource ressource = Ressource.disassemble(Client.RESSOURCE_DIRECTORY, new File(Client.RESSOURCE_DIRECTORY + "test3.mp4"), "myTitle", G2GUI.client.getPublicKeyBase64());
            this.ressourceUUID = ressource.getUUID();

            Logger.info("Uploading file: " + selectedFile.getAbsolutePath());
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

        return ressourceUUID;
    }
}
