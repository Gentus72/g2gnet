package org.geooo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import org.geooo.dto.ClientDTO;
import org.geooo.dto.RessourceBlockDTO;
import org.geooo.metadata.ClientFile;
import org.geooo.metadata.RessourceFile;
import org.geooo.util.ClientCommand;
import org.geooo.util.FilesRemote;
import org.geooo.util.G2GUUID;
import org.geooo.util.Logger;
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

    public Client() {
        ClientFile.readFromFile(this);

        if (this.getUUID() == null) {
            this.setUUID(G2GUUID.getRandomUUID());
            ClientFile.writeToFile(this);
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
            }
        } catch (IllegalArgumentException e) {
            Logger.error("Unknown command! Try again...");
            return;
        }
    }

    public void handleServerCommand(String[] args) {

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
            this.outputStream = new DataOutputStream(this.socket.getOutputStream());
            this.inputStream = new DataInputStream(this.socket.getInputStream());
            Logger.info(String.format("Successfully connected to Server [%s:%d]!", HOST_ADDRESS, HOST_PORT));

            this.isConnected = true;
        } catch (IOException e) {
            Logger.error(String.format("Error while connecting to [%s:%d]!", HOST_ADDRESS, HOST_PORT));
            Logger.exception(e);
        }
    }

    public void oldStartClient() {

        try {
            this.userInputScanner = new Scanner(System.in);

            Logger.info("Client started and waiting for commands!");

            boolean manualInput = true;
            String clientInput = "";

            while (true) {
                if (manualInput) {
                    System.out.print("$> ");
                    clientInput = this.userInputScanner.nextLine();

                    try {
                        ClientCommand.valueOf(clientInput.split(" ")[0]);

                        // handleClientCommand(clientInput);
                        continue;
                    } catch (IllegalArgumentException e) {
                        if (!this.isConnected) {
                            Logger.warn("Please enter a client command or connect to a network!");
                            continue;
                        }

                        sendCommand(clientInput);
                    }
                } else {
                    sendCommand(clientInput);
                    manualInput = true;
                }

                String response = this.inputStream.readUTF();
                String[] responseArgs = response.split(" ");
                Logger.info(String.format("Received response from server [%s]: %s", HOST_ADDRESS, response));
                ServerResponse responseCommand = ServerResponse.valueOf(responseArgs[0]);

                switch (responseCommand) {
                    // INFO <NETWORK | RESSOURCE> <ressourceUUID?>
                    case INFO -> {
                        switch (responseArgs[1]) {
                            case "NETWORK" -> {
                                FilesRemote.receiveFile(String.format("%s.g2gnet", responseArgs[2]), inputStream);
                                Logger.info("Received networkfile!");
                            }
                            case "RESSOURCE" -> {
                                FilesRemote.receiveFile(String.format("%s.g2gcopy", responseArgs[2]), inputStream);
                                // update clientfile
                                Logger.info("Received ressourcefile!");
                            }
                            default -> {
                                Logger.error("Wrong argument in server response!");
                            }
                        }
                    }
                    // REDIRECT <destinationAddress>
                    case REDIRECT -> {
                        HOST_ADDRESS = responseArgs[1];
                        Logger.info(String.format("Being redirected to: %s", responseArgs[1]));

                        this.socket.close();
                        this.outputStream.close();
                        this.inputStream.close();

                        this.socket = new Socket(HOST_ADDRESS, HOST_PORT);
                        this.outputStream = new DataOutputStream(this.socket.getOutputStream());
                        this.inputStream = new DataInputStream(this.socket.getInputStream());

                        manualInput = false;
                    }
                    // DOWNLOAD <ressourceUUID> <blockUUID>
                    case DOWNLOAD -> {
                        // create directory
                        String ressourceDirectoryPath = String.format("%s/%s/", RESSOURCE_DIRECTORY, responseArgs[1]);
                        File ressourceDirectory = new File(ressourceDirectoryPath);

                        if (!ressourceDirectory.exists()) {
                            Logger.info("Creating ressource directory! Is this the first block download?");
                            Files.createDirectory(Path.of(ressourceDirectoryPath));
                        }

                        FilesRemote.receiveFile(String.format("%s%s.g2gblock", ressourceDirectoryPath, responseArgs[2]),
                                inputStream);
                    }
                    // ERROR <errorMessage>
                    case ERROR -> {
                        Logger.error("Error response from server: " + response);
                        Logger.error("Try again!");
                    }
                    case CLOSE -> {
                        Logger.info("Closing connection and shutting down!");

                        this.socket.close();
                        this.outputStream.close();
                        this.inputStream.close();
                        this.userInputScanner.close();

                        System.exit(0);
                    }
                    default -> {
                        Logger.error("Unknown command!");
                    }
                }
            }
        } catch (IOException e) {
            Logger.error("Error while setting up client socket!");
            Logger.exception(e);
        }
    }

    private void oldhandleClientCommand(String clientInput) {
        String[] args = clientInput.split(" ");

        switch (ClientCommand.valueOf(args[0])) {
            // DISASSEMBLE <inputFilePath>
            case DISASSEMBLE -> {
                Logger.warn("Not implemented yet!");
            }
            // REASSEMBLE <ressourceUUID> <outputFilePath>
            case REASSEMBLE -> {
                Logger.warn("Not implemented yet!");
            }
            // CONNECT <serverAddress> <serverPort | <blank = 7000>>
            case CONNECT -> {

            }
        }
    }

    private void sendCommand(String command) {
        Logger.info(String.format("Sending command: %s", command));

        try {
            this.outputStream.writeUTF(command);
            this.outputStream.flush();
        } catch (IOException e) {
            Logger.error("Error while sending command to server!");
            Logger.exception(e);
        }
    }

    private void receiveRessource(String[] clientArguments) {
        try {
            Path ressourceDirectory = Files.createDirectory(Path.of(RESSOURCE_DIRECTORY + clientArguments[1]));
            File ressourceFile = new File(ressourceDirectory.toString() + "/ressourceFile.g2g");

            // 1. receive response file with metadata or error
            Logger.info("Getting metadata for requested ressource!");
            FilesRemote.receiveFile(ressourceFile, inputStream);

            // 2. read and act on metadata
            RessourceFile.setConfigContentFromFile(ressourceFile);
            HashMap<String, String> metadata = RessourceFile.configContent;
            ArrayList<RessourceBlockDTO> blocks = RessourceFile.getBlocks(ressourceFile);

            Logger.info("Receving block amount: " + metadata.get("total_blocks"));

            if (Integer.parseInt(metadata.get("total_blocks")) != blocks.size()) {
                Logger.error("Block amount mismatch for ressource: " + clientArguments[1]);
            }

            // 3. receive blocks according to metadata
            for (RessourceBlockDTO block : blocks) {
                File blockFile = new File(ressourceDirectory.toString() + "/" + block.getUUID() + ".g2gblock");

                outputStream.writeUTF("GET block " + block.getUUID());

                FilesRemote.receiveFile(blockFile, inputStream);
            }
        } catch (IOException e) {
            Logger.error("Error while creating client ressource directory!");
            Logger.exception(e);
        }
    }
}
