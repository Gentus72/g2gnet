package org.geooo.gui;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.swing.text.Position;

import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
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

        ChoiceBox<Text> choiceBox = new ChoiceBox<Text>();
        ObservableList<Text> items = javafx.collections.FXCollections.observableArrayList();
        choiceBox.setItems(items);

        TextField searchField = new TextField();
        searchField.setMinWidth(500);
        Button searchButton = new Button();
        searchButton.setText("Connect");

        topSearchBox.getChildren().addAll(choiceBox, searchField, searchButton);

        Button btn = new Button();
        btn.setText("Say 'Hello World'");
        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Hello World!");
            }
        });

        VBox root = new VBox();
        root.setAlignment(Pos.TOP_CENTER);
        root.getChildren().add(topSearchBox);
        root.getChildren().add(btn);

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
