package org.geooo.gui;

import java.io.File;
import java.util.ArrayList;

import org.geooo.dto.ServerDTO;
import org.geooo.util.Logger;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class FXTest extends Application {

    @Override
    public void start(Stage primaryStage) {
        HBox topSearchBox = new HBox();
        topSearchBox.setBackground(new Background(new BackgroundFill(new Color(0.9d, 0.9d, 0.9d, 1), null, null)));
        topSearchBox.setMaxHeight(50);
        topSearchBox.setMinHeight(50);
        topSearchBox.setAlignment(Pos.CENTER);

        HBox currentConnection = new HBox();
        currentConnection.setMinWidth(200);
        currentConnection.setAlignment(Pos.CENTER_LEFT);
        Text currentConnectionTitle = new Text("conn lol");
        currentConnection.getChildren().add(currentConnectionTitle);

        ChoiceBox<ServerDTO> choiceBox = new ChoiceBox<ServerDTO>();
        choiceBox.getItems().add(new ServerDTO("1", "1.1.1.1", new File("")));
        choiceBox.getItems().add(new ServerDTO("3", "2.2.2.2", new File("")));
        choiceBox.getItems().add(new ServerDTO("4", "3.3.3.3", new File("")));

        choiceBox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                Logger.info("New Server chosen: " + choiceBox.getValue());
            }
        });

        TextField searchField = new TextField();
        searchField.setMinWidth(500);
        Button searchButton = new Button();
        searchButton.setText("Connect");

        topSearchBox.getChildren().addAll(choiceBox, searchField, searchButton);

        // Button btn = new Button();
        // btn.setText("Say 'Hello World'");
        // btn.setOnAction(new EventHandler<ActionEvent>() {
        // @Override
        // public void handle(ActionEvent event) {
        // System.out.println("Hello World!");
        // }
        // });

        VBox root = new VBox();
        root.setAlignment(Pos.TOP_CENTER);
        root.getChildren().add(topSearchBox);

        VBox serverChoice = new VBox();
        serverChoice.setMinWidth(300);
        serverChoice.setMinHeight(1000);
        serverChoice.setBackground(new Background(new BackgroundFill(new Color(0.8, 0.8, 0.8, 1), null, null)));

        HBox content = new HBox();
        content.getChildren().add(serverChoice);
        root.getChildren().add(content);

        ArrayList<ServerDTO> servers = new ArrayList<>();
        servers.add(new ServerDTO("1", "1.1.1.1", new File("")));
        servers.add(new ServerDTO("2", "2.2.2.2", new File("")));
        servers.add(new ServerDTO("3", "3.3.3.3", new File("")));

        for (ServerDTO server : servers) {
            Button serverButton = new Button();
            serverButton.setText(server.getAddress());
            serverButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    System.out.println(server.getUUID());
                }
            });

            serverChoice.getChildren().add(serverButton);
        }

        Scene scene = new Scene(root, 1100, 720);

        primaryStage.setTitle("Hello World!");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
