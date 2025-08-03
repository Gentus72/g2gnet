package org.geooo.gui;

import java.io.File;
import java.util.ArrayList;

import org.geooo.Client;
import org.geooo.ClientHelper;
import org.geooo.dto.NetworkDTO;
import org.geooo.metadata.NetworkFile;
import org.geooo.util.Logger;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class G2GUI extends Application {

    public static Client client;
    public static ArrayList<NetworkDTO> availableNetworks = new ArrayList<>();
    public static NetworkDTO connectedNetwork;

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox();
        root.setAlignment(Pos.TOP_CENTER);
        root.getChildren().add(new ConnectNewNetwork(this::connectNetwork));

        HBox content = new HBox();
        content.getChildren().addAll(new NetworkList(availableNetworks));
        root.getChildren().add(content);

        Scene scene = new Scene(root, 1100, 720);

        primaryStage.setTitle("G2GNet Distributed File Storage");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void start(Client _client) {
        client = _client;

        readNetworksFromFiles();

        launch(new String[] {});
    }

    public static void readNetworksFromFiles() {
        try {
            File[] networkFiles = new File(Client.RESSOURCE_DIRECTORY)
                    .listFiles((dir, name) -> name.endsWith(".g2gnet"));

            for (File file : networkFiles) {
                NetworkFile networkFile = new NetworkFile(file.getPath());
                availableNetworks.add(networkFile.getNetwork());
            }
        } catch (Exception e) {
            Logger.error("Error while reading networkFiles!");
            Logger.exception(e);
        }
    }

    public void connectNetwork(String IPv4) {
        if (connectedNetwork != null && connectedNetwork.getCCServerIPv4().equals(IPv4)) {
            Logger.warn("Client tried to connect to the netowrk he's already connected to!");
            return;
        }

        if (client.isConnected) {
            ClientHelper.handleServerInteraction(client, new String[] { "DISCONNECT" });
        }

        // this will automatically trigger setConnectedNetwork()
        ClientHelper.handleClientCommandCONNECT(client, new String[] { "CONNECT", IPv4 });
    }

    // is only called from ClientHelper.handleClientCommandCONNECT()
    // all connect/disconnect logic is handled there
    public static void setConnectedNetwork(NetworkDTO network) {
        if (!availableNetworks.contains(network)) {
            availableNetworks.add(network);
        }

        connectedNetwork = network;
        Logger.success("new network selected!");
    }
}
