package org.geooo.gui;

import java.io.File;

import org.geooo.Client;
import org.geooo.ClientHelper;
import org.geooo.dto.NetworkDTO;
import org.geooo.metadata.NetworkFile;
import org.geooo.util.Logger;

import javafx.application.Application;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class G2GUI extends Application implements Runnable {

    public static Client client;
    public static ListProperty<NetworkDTO> availableNetworks = new SimpleListProperty<>(
            FXCollections.observableArrayList());
    public static ObjectProperty<NetworkDTO> connectedNetwork = new SimpleObjectProperty<>();

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox();
        root.setAlignment(Pos.TOP_CENTER);
        root.getChildren().add(new ConnectNewNetwork());

        HBox content = new HBox();
        NetworkList networkList = new NetworkList(availableNetworks);
        content.getChildren().addAll(networkList, new NetworkOverview(connectedNetwork));
        root.getChildren().add(content);

        Scene scene = new Scene(root, 1100, 720);

        primaryStage.setTitle("G2GNet Distributed File Storage");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
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

    public static void connectNetwork(String IPv4) {
        // if (connectedNetwork.get().getCCServerIPv4().equals(IPv4)) {
        // Logger.warn("Client tried to connect to the netowrk he's already connected to!");
        // return;
        // }

        if (client.isConnected) {
            ClientHelper.handleServerInteraction(client, new String[] { "DISCONNECT" });
        }

        // this will automatically trigger setConnectedNetwork()
        client.currentClientInput = new String[] { "CONNECT", IPv4 };
        ClientHelper.handleClientCommandCONNECT(client, new String[] { "CONNECT", IPv4 });
    }

    // is only called from ClientHelper.handleClientCommandCONNECT()
    // all connect/disconnect logic is handled there
    public static void setConnectedNetwork(NetworkDTO network) {
        connectedNetwork.set(new NetworkDTO(network.getNetworkUUID(), network.getNetworkLabel(),
                network.getCCServerIPv4(), network.getNetworkFile()));

        boolean isAlreadyInList = false;

        for (NetworkDTO n : availableNetworks.get()) {
            if (n.getNetworkUUID().equals(network.getNetworkUUID()))
                isAlreadyInList = true;
        }

        if (!isAlreadyInList)
            availableNetworks.add(network);

        Logger.success("new network selected!");
    }

    public G2GUI() {
        // empty constructor, needed for JavaFX
    }

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public G2GUI(Client _client) {
        Logger.info("Starting client GUI...");
        client = _client;
    }

    @Override
    public void run() {
        readNetworksFromFiles();

        launch(new String[] {});
    }
}
