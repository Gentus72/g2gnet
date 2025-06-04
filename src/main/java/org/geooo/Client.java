package org.geooo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import org.geooo.dto.ClientDTO;
import org.geooo.metadata.ClientFile;
import org.geooo.util.ClientCommand;
import org.geooo.util.FilesRemote;
import org.geooo.util.G2GUUID;
import org.geooo.util.Logger;
import org.geooo.util.ServerCommand;
import org.geooo.util.ServerResponse;

public final class Client extends ClientDTO {

    public static String RESSOURCE_DIRECTORY = "client/res/";
    public static String HOST_ADDRESS = "localhost";
    public static int HOST_PORT = 7000;

    public static void main(String[] args) {
        // Ressource res = new Ressource(new File("res/test.jpg"), HOST_ADDRESS,
        // RessourceDistributionStrategy.EVEN_DISTRIBUTION);
        // TemporaryRessourceFile.writeToFile(res, RESSOURCE_DIRECTORY);

        // Ressource.reassembleSourceFile(new
        // File("client/res/f922d9b0e27a41d7b708cf54dfd8e14c.g2g"), new File[] {
        // new
        // File("client/res/f922d9b0e27a41d7b708cf54dfd8e14c/9efac98096e546c6956c462bf3c22f06.g2gblock"),
        // new
        // File("client/res/f922d9b0e27a41d7b708cf54dfd8e14c/0136ba79e6af4fd59697d7b6d65ee99a.g2gblock")
        // }, "client/res/f922d9b0e27a41d7b708cf54dfd8e14c/");

        new Client();
    }

    Socket socket;
    DataOutputStream outputStream;
    DataInputStream inputStream;
    Scanner userInputScanner;
    boolean isConnected = false;
    ClientFile clientFile;

    public Client() {
        this.clientFile = new ClientFile(RESSOURCE_DIRECTORY + "clientFile.g2gclient");
        this.clientFile.readFromFile(this);

        if (this.getUUID() == null) {
            this.setUUID(G2GUUID.getRandomUUID());
            this.clientFile.writeToFile(this);
        }

        startClient();
    }

    public void startClient() {
        this.userInputScanner = new Scanner(System.in);
        
        while (true) { 
            System.out.print("$> ");
            String[] inputArgs = this.userInputScanner.nextLine().split(" ");

            if (this.isConnected) {
                handleServerCommand(inputArgs);
            } else {
                handleClientCommand(inputArgs);
            }
        }
    }

    public void handleClientCommand(String[] args) {
        try {
            ClientCommand command = ClientCommand.valueOf(args[0]);
            if (!command.hasCorrectArgsAmount(args.length)) return;

            switch (command) {
                case CONNECT -> {
                    connectToServer(args);
                }
                case EXIT -> {
                    Logger.info("Shutting down...");
                    System.exit(0);
                }
            }
        } catch (IllegalArgumentException e) {
            Logger.error("Unknown command! Try again...");
        }
    }

    public void handleServerCommand(String[] args) {
        try {
            ServerCommand command = ServerCommand.valueOf(args[0]);
            if (!command.hasCorrectArgsAmount(args.length)) return;
            sendCommand(args);

            String resPayload = this.inputStream.readUTF();
            String[] responseArgs = resPayload.split(" ");
            Logger.info(String.format("Received response from [%s]: %s", HOST_ADDRESS, resPayload));
            ServerResponse response = ServerResponse.valueOf(responseArgs[0]);

            switch (response) {
                // REDIRECT <newServerAddress>
                case REDIRECT -> {
                    HOST_ADDRESS = responseArgs[1];
                    Logger.info(String.format("Being redirected to: %s", responseArgs[1]));
                    disconnect();
                    connectToServer(new String[] { "CONNECT", HOST_ADDRESS, String.valueOf(HOST_PORT) });
                }
                // INFO <NETWORK | RESSOURCE> <? ressourceUUID>
                case INFO -> {
                    switch (responseArgs[1]) {
                        case "NETWORK" -> {
                            FilesRemote.receiveFile(String.format("%s%s.g2gnet", RESSOURCE_DIRECTORY, responseArgs[2]), inputStream);
                            Logger.info("Received networkfile!");
                        }
                        case "RESSOURCE" -> {
                            FilesRemote.receiveFile(String.format("%s%s.g2g", RESSOURCE_DIRECTORY, responseArgs[2]), inputStream);
                            // update clientfile
                            Logger.info("Received ressourcefile!");
                        }
                    }
                }
                // DOWNLOAD <ressourceUUID> <blockUUID>
                case DOWNLOAD -> {
                    // create directory
                    String ressourceDirectoryPath = String.format("%s%s/", RESSOURCE_DIRECTORY, responseArgs[1]);
                    File ressourceDirectory = new File(ressourceDirectoryPath);

                    if (!ressourceDirectory.exists()) {
                        Logger.info("Creating ressource directory! Is this the first block download?");
                        Files.createDirectory(Path.of(ressourceDirectoryPath));
                    }

                    FilesRemote.receiveFile(String.format("%s%s.g2gblock", ressourceDirectoryPath, responseArgs[2]), inputStream);
                }
                // ERROR <errorMessage>
                case ERROR -> {
                    Logger.error("Error response from server: " + response);
                    Logger.error("Try again...");
                }
                case CLOSE -> {
                    Logger.info("Closing connection!");
                    disconnect();
                }
            }
        } catch (IllegalArgumentException e) {
            Logger.error("Unknown command! Try again...");
        } catch (IOException e) {
            Logger.error("Error while sending/receiving server command! Continuing...");
        }
    }

    public void connectToServer(String[] args) {
        if (args.length >= 3) {
            HOST_PORT = Integer.parseInt(args[2]);
        } else {
            HOST_PORT = 7000;
        }

        HOST_ADDRESS = args[1];

        try {
            this.socket = new Socket(HOST_ADDRESS, HOST_PORT);
            this.socket.setSoTimeout(30000); // 30 sec
            this.outputStream = new DataOutputStream(this.socket.getOutputStream());
            this.inputStream = new DataInputStream(this.socket.getInputStream());
            Logger.info(String.format("Successfully connected to Server [%s:%d]!", HOST_ADDRESS, HOST_PORT));

            this.isConnected = true;
        } catch (IOException e) {
            Logger.error(String.format("Error while connecting to [%s:%d]!", HOST_ADDRESS, HOST_PORT));
            Logger.exception(e);
        }
    }

    public void disconnect() {
        try {
            this.socket.close();
            this.outputStream.close();
            this.inputStream.close();
            this.isConnected = false;
        } catch (IOException e) {
            Logger.error("Error while closing connection!");
            Logger.exception(e);
        }
    }

    private void sendCommand(String[] args) {
        String payload = String.join(" ", args);

        Logger.info(String.format("Sending command: %s", payload));

        try {
            this.outputStream.writeUTF(payload);
            this.outputStream.flush();
        } catch (IOException e) {
            Logger.error("Error while sending command to server!");
            Logger.exception(e);
        }
    }
}
