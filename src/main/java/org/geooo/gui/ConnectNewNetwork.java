package org.geooo.gui;

import java.io.File;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

/*
 * JavaFX-Komponente mit Suchleiste und Eingabeknopf
 * zum Verbinden eines neuen Netzwerks
 * Enthält auch die Möglichkeit das Dateisystem zu öffnen.
 */
public class ConnectNewNetwork extends HBox {
    public ConnectNewNetwork() {
        this.setBackground(new Background(new BackgroundFill(new Color(0.9d, 0.9d, 0.9d, 1), null, null)));
        this.setMaxHeight(50);
        this.setMinHeight(50);
        this.setAlignment(Pos.CENTER);

        TextField inputField = new TextField();
        inputField.setMinWidth(500);
        inputField.setPromptText("Connect a new network by IPv4...");

        Button connectButton = new Button();
        connectButton.setText("Connect");
        connectButton.setOnMousePressed((mouseEvent) -> {
            if (mouseEvent.isPrimaryButtonDown()) {
                if (inputField.getText().isEmpty() || !isValidIPv4(inputField.getText())) {
                    return;
                }

                G2GUI.connectNetwork(inputField.getText());
                inputField.clear();
            }
        });

        // Dieser Knopf ermöglicht es dem Nutzer trotz Docker Container,
        // auf dessen Dateisystem zuzugreifen, damit der Nutzer gucken kann,
        // ob heruntergeladene Dateien auch angekommen sind.
        FileChooser fileChooser = new FileChooser();
        Button openFS = new Button("Open Filesystem");
        openFS.setPrefWidth(150);
        openFS.setOnMousePressed(mouseEvent -> {
            if (mouseEvent.isPrimaryButtonDown()) {
                Stage window = new Stage();
                window.initModality(Modality.APPLICATION_MODAL);
                window.setTitle("Upload a File");
                window.setMinWidth(250);

                File f = fileChooser.showOpenDialog(window);
            }
        });

        this.getChildren().addAll(openFS, inputField, connectButton);
    }

    /*
     * Kopiert von
     * https://www.geeksforgeeks.org/java/validating-ipv4-string-in-java/
     * Letzter Zugriff: 03.08.2025, 20:12 Uhr
     */
    public static boolean isValidIPv4(String ip) {
        String[] parts = ip.split("\\.");

        if (parts.length != 4) {
            return false;
        }

        for (String part : parts) {
            try {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return true;
    }
}
