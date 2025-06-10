package org.geooo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Scanner;
import java.util.function.Consumer;

import org.geooo.dto.ClientDTO;
import org.geooo.dto.RessourceBlockDTO;
import org.geooo.dto.RessourceDTO;
import org.geooo.dto.ServerDTO;
import org.geooo.metadata.ClientFile;
import org.geooo.metadata.NetworkFile;
import org.geooo.metadata.RessourceFile;
import org.geooo.util.ClientCommand;
import org.geooo.util.FilesRemote;
import org.geooo.util.G2GUtil;
import org.geooo.util.Logger;
import org.geooo.util.ServerCommand;
import org.geooo.util.ServerResponse;

public final class Client extends ClientDTO {

    public static String RESSOURCE_DIRECTORY = "client/res/";
    public static String HOST_ADDRESS = "localhost";
    public static int HOST_PORT = 7000;

    public static void main(String[] args) {
        // Ressource.reassemble(RESSOURCE_DIRECTORY, "b438d41d25de4bc3a6c043a6431fb0df", new File(RESSOURCE_DIRECTORY + "out.mp4"));
        Client client = new Client();
        Ressource.disassemble(RESSOURCE_DIRECTORY, new File(RESSOURCE_DIRECTORY + "test3.mp4"), "myTitle", client.getPublicKeyBase64());
        client.startClient();
    }

    Socket socket;
    DataOutputStream outputStream;
    DataInputStream inputStream;
    Scanner userInputScanner;

    ClientFile clientFile;
    boolean isConnected = false;
    String[] currentClientInput;
    HashMap<ClientCommand, Consumer<String[]>> registeredClientCommands = new HashMap<>();
    HashMap<ServerResponse, Consumer<String[]>> registeredServerResponses = new HashMap<>();

    public Client() {
        this.clientFile = new ClientFile(RESSOURCE_DIRECTORY + "clientFile.g2gclient");
        this.clientFile.readFromFile(this);

        if (this.getUUID() == null) {
            this.setUUID(G2GUtil.getRandomUUID());
            this.clientFile.writeToFile(this);
        }

        registeredClientCommands.put(ClientCommand.CONNECT, this::handleClientCommandCONNECT);
        registeredClientCommands.put(ClientCommand.INFO, this::handleClientCommandINFO);
        registeredClientCommands.put(ClientCommand.AUTOGET, this::handleClientCommandAUTOGET);

        registeredServerResponses.put(ServerResponse.INFO, this::handleServerResponseINFO);
        registeredServerResponses.put(ServerResponse.REDIRECT, this::handleServerResponseREDIRECT);
        registeredServerResponses.put(ServerResponse.DOWNLOAD, this::handleServerResponseDOWNLOAD);
        registeredServerResponses.put(ServerResponse.ERROR, (String[] args) -> Logger.error("Error response from server: " + String.join(" ", args)));
        registeredServerResponses.put(ServerResponse.CLOSE, (String[] args) -> disconnect());
    }

    public void startClient() {
        this.userInputScanner = new Scanner(System.in);

        while (true) {
            String consolePrefix = this.isConnected ? String.format("%s[%s] $> ", Logger.ANSI_CYAN, this.socket.getInetAddress().getHostAddress()) : Logger.ANSI_RESET + "[CLIENT] $> ";
            System.out.print(consolePrefix);
            currentClientInput = this.userInputScanner.nextLine().split(" ");

            if (this.isConnected) {
                handleServerInteraction(currentClientInput);
            } else {
                try {
                    ClientCommand command = ClientCommand.valueOf(currentClientInput[0]);
                    if (!command.hasCorrectArgsAmount(currentClientInput.length)) {
                        Logger.error("Wrong number of arguments!");
                        continue;
                    }

                    for (var entry : registeredClientCommands.entrySet()) {
                        if (command.equals(entry.getKey())) {
                            entry.getValue().accept(currentClientInput);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    Logger.error(String.format("Unknown client-command %s! Try again...", currentClientInput[0]));
                }
            }
        }
    }

    public void handleServerInteraction(String[] args) {
        try {
            ServerCommand command = ServerCommand.valueOf(args[0]);
            if (!command.hasCorrectArgsAmount(args.length)) {
                Logger.error("Wrong number of arguments!");
                return;
            }

            sendCommand(args);

            String resPayload = this.inputStream.readUTF();
            String[] resArgs = resPayload.split(" ");
            Logger.info(String.format("-->: %s", HOST_ADDRESS, resPayload));
            ServerResponse response = ServerResponse.valueOf(resArgs[0]);

            for (var entry : registeredServerResponses.entrySet()) {
                if (response.equals(entry.getKey())) {
                    entry.getValue().accept(resArgs);
                }
            }
        } catch (IllegalArgumentException e) {
            Logger.error(String.format("Unknown server-command %s! Try again...", args[0]));
        } catch (IOException e) {
            Logger.error("Error while receiving server response!");
            Logger.exception(e);
        }
    }

    public void handleServerResponseREDIRECT(String[] args) {
        HOST_ADDRESS = args[1];
        Logger.info(String.format("Being redirected to: %s", args[1]));
        disconnect();
        handleClientCommandCONNECT(new String[]{"CONNECT", HOST_ADDRESS, String.valueOf(HOST_PORT)});

        // redirect command to ccServer
        handleServerInteraction(currentClientInput);
    }

    public void handleServerResponseINFO(String[] args) {
        switch (args[1]) {
            case "NETWORK" -> {
                FilesRemote.receiveFile(String.format("%s%s.g2gnet", RESSOURCE_DIRECTORY, args[2]), inputStream);
                Logger.info("Received networkfile!");
            }
            case "RESSOURCE" -> {
                FilesRemote.receiveFile(String.format("%s%s.g2g", RESSOURCE_DIRECTORY, args[2]), inputStream);
                // update clientfile
                Logger.info("Received ressourcefile!");
            }
        }
    }

    public void handleServerResponseDOWNLOAD(String[] args) {
        try {
            // create directory
            String ressourceDirectoryPath = String.format("%s%s/", RESSOURCE_DIRECTORY, args[1]);
            File ressourceDirectory = new File(ressourceDirectoryPath);

            if (!ressourceDirectory.exists()) {
                Logger.info("Creating ressource directory! Is this the first block download?");
                Files.createDirectory(Path.of(ressourceDirectoryPath));
            }

            FilesRemote.receiveFile(String.format("%s%s.g2gblock", ressourceDirectoryPath, args[2]), inputStream);
            Logger.success(String.format("Download for block %s successful!", args[2]));
            this.clientFile.writeToFile(this);
        } catch (IOException e) {
            Logger.error("Error while creating client ressource directory!");
            Logger.exception(e);
        }
    }

    public void handleClientCommandCONNECT(String[] args) {
        HOST_ADDRESS = args[1];
        HOST_PORT = 7000;
        if (args.length >= 3) {
            HOST_PORT = Integer.parseInt(args[2]);
        }

        try {
            this.socket = new Socket(HOST_ADDRESS, HOST_PORT);
            this.socket.setSoTimeout(30000); // 30 sec
            this.outputStream = new DataOutputStream(this.socket.getOutputStream());
            this.inputStream = new DataInputStream(this.socket.getInputStream());
            Logger.success(String.format("Successfully connected to Server [%s:%d]!", HOST_ADDRESS, HOST_PORT));

            this.isConnected = true;
        } catch (IOException e) {
            Logger.error("Error while connecting to server!");
            Logger.exception(e);
        }
    }

    public void handleClientCommandINFO(String[] args) {
        switch (args[1]) {
            case "NETWORK" -> {
                if (args[2].equals("ALL")) {
                    Logger.info("Known networks (uuid):");
                    File[] networkFiles = new File(RESSOURCE_DIRECTORY).listFiles((dir, name) -> name.endsWith(".g2gnet"));

                    for (File file : networkFiles) {
                        Logger.info(file.getName());
                    }
                } else {
                    NetworkFile networkFile = new NetworkFile(RESSOURCE_DIRECTORY + args[2] + ".g2gnet");

                    if (!networkFile.getFile().exists()) {
                        Logger.error("Network doesn't exist! Try again...");
                        return;
                    }

                    CCServer networkInfo = new CCServer();
                    networkFile.readFromFile(networkInfo);

                    Logger.info("Info on network:");
                    Logger.info(String.format("UUID: %s", networkInfo.getNetworkUUID()));

                    Logger.info(" - Servers (uuid, address):");
                    for (ServerDTO server : networkInfo.getServers()) {
                        Logger.info(String.format("   - [%s, %s]", server.getUUID(), server.getAddress()));
                    }

                    Logger.info(" - Ressources (uuid, blockAmount):");
                    for (RessourceDTO ressource : networkInfo.getRessources()) {
                        Logger.info(String.format("   - [%s, %d]", ressource.getUUID(), ressource.getBlockAmount()));
                    }
                }
            }
            case "RESSOURCE" -> {
                if (args[2].equals("ALL")) {
                    Logger.info("Known ressources (uuid):");
                    File[] ressourceFiles = new File(RESSOURCE_DIRECTORY).listFiles((dir, name) -> name.endsWith(".g2g"));

                    for (File file : ressourceFiles) {
                        Logger.info(file.getName());
                    }
                } else {
                    RessourceFile ressourceFile = new RessourceFile(RESSOURCE_DIRECTORY + args[2] + ".g2g");

                    if (!ressourceFile.getFile().exists()) {
                        Logger.error("Ressource doesn't exist! Try again...");
                        return;
                    }

                    HashMap<String, String> ressourceInfo = ressourceFile.getConfigContent();

                    Logger.info("Info on ressource:");
                    Logger.info(String.format("UUID: %s", ressourceInfo.get("UUID")));

                    Logger.info(" - Blocks (uuid, location):");
                    for (RessourceBlockDTO block : ressourceFile.getBlocks()) {
                        Logger.info(String.format("   - [%s, %s]", block.getUUID(), block.getLocation()));
                    }
                }
            }
            default -> {
                Logger.error("Wrong argument for command INFO! Should be NETWORK or RESSOURCE");
            }
        }
    }

    public void handleClientCommandAUTOGET(String[] args) {
        RessourceFile ressourceFile = new RessourceFile(RESSOURCE_DIRECTORY + args[1] + ".g2g");

        if (!ressourceFile.getFile().exists()) {
            Logger.error("Ressourcefile doesn't exist!");
            return;
        }

        HashMap<String[], String> commands = ressourceFile.getGETBLOCKCommands();

        if (commands.size() != Integer.parseInt(ressourceFile.getConfigContent().get("AmountOfBlocks"))) {
            Logger.error("Block amount mismatch between ressourcefile entry and amount of GETBLOCK commands!");
        }

        Logger.info(String.format("Downloading %d blocks...", commands.size()));

        for (var entry : commands.entrySet()) {
            String serverIP = entry.getValue();
            Logger.info(String.format("Downloading block from %s... ", entry.getValue()));
            handleClientCommandCONNECT(new String[]{"CONNECT", serverIP});
            handleServerInteraction(entry.getKey()); // send GETBLOCK
            handleServerInteraction(new String[]{"DISCONNECT"});
        }

        Logger.success("All blocks downloaded!");
    }

    public void disconnect() {
        try {
            Logger.info("Disconnecting...");

            this.socket.close();
            this.outputStream.close();
            this.inputStream.close();

            this.isConnected = false;
        } catch (IOException e) {
            Logger.error("Error while closing connection!");
            Logger.exception(e);
        }
    }

    public void sendCommand(String[] args) {
        String payload = String.join(" ", args);

        try {
            this.outputStream.writeUTF(payload);
            this.outputStream.flush();
        } catch (IOException e) {
            Logger.error("Error while sending command to server!");
            Logger.exception(e);
        }
    }
}
