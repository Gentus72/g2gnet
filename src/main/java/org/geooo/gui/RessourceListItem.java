package org.geooo.gui;

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
        label.setPrefWidth(185);
        this.button = new Button("Download");
        this.button.setPrefWidth(100);

        this.button.setOnMousePressed(mouseEvent -> {
            if (mouseEvent.isPrimaryButtonDown()) {
                Logger.info("Download button pressed!");
            }
        });

        this.getChildren().addAll(label, button);
    }
}
