package org.geooo.gui;

import java.util.function.Consumer;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

public class ConnectNewNetwork extends HBox {
    public ConnectNewNetwork(Consumer<String> connectNetwork) {
        this.setBackground(new Background(new BackgroundFill(new Color(0.9d, 0.9d, 0.9d, 1), null, null)));
        this.setMaxHeight(50);
        this.setMinHeight(50);
        this.setAlignment(Pos.CENTER);

        TextField inputField = new TextField();
        inputField.setMinWidth(500);
        inputField.setPromptText("Connect a new network by IPv4...");

        Button addButton = new Button();
        addButton.setText("Connect");
        addButton.setOnMousePressed((mouseEvent) -> {
            if (mouseEvent.isPrimaryButtonDown()) {
                if (inputField.getText().isEmpty() || !isValidIPv4(inputField.getText())) {
                    return;
                }

                connectNetwork.accept(inputField.getText());
            }
        });

        this.getChildren().addAll(inputField, addButton);
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
