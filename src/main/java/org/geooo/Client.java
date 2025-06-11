package org.geooo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
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
        // Ressource.disassemble(RESSOURCE_DIRECTORY, new File(RESSOURCE_DIRECTORY + "test3.mp4"), "myTitle", client.getPublicKeyBase64());
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
        if (this.clientFile.getFile().exists()) {
            this.clientFile.readFromFile(this);
        }

        if (this.getUUID() == null) {
            this.setUUID(G2GUtil.getRandomUUID());
            this.clientFile.writeToFile(this);
        }

        // Connect all commands to an according function
        // Makes development and readability easy
        registeredClientCommands.put(ClientCommand.CONNECT, this::handleClientCommandCONNECT);
        registeredClientCommands.put(ClientCommand.INFO, this::handleClientCommandINFO);
        registeredClientCommands.put(ClientCommand.AUTOGET, this::handleClientCommandAUTOGET);
        registeredClientCommands.put(ClientCommand.AUTOUPLOAD, this::handleClientCommandAUTOUPLOAD);
        registeredClientCommands.put(ClientCommand.EXIT, this::handleClientCommandEXIT);

        // Servercommands don't have to be registered, because they get interpreted by the server
        // -> leading to a Serverresponse (which is handled by the client)

        registeredServerResponses.put(ServerResponse.INFO, this::handleServerResponseINFO);
        registeredServerResponses.put(ServerResponse.REDIRECT, this::handleServerResponseREDIRECT);
        registeredServerResponses.put(ServerResponse.DOWNLOAD, this::handleServerResponseDOWNLOAD);
        registeredServerResponses.put(ServerResponse.AUTH, this::handleServerResponseAUTH);
        registeredServerResponses.put(ServerResponse.SUCCESS, this::handleServerResponseSUCCESS);
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

            if (command.equals(ServerCommand.AUTH)) {
                File[] ressourceFiles = new File(RESSOURCE_DIRECTORY).listFiles((dir, name) -> name.equals(args[1] + ".g2g"));
                if (ressourceFiles == null || ressourceFiles.length == 0) {
                    Logger.error("Ressource to auth not present in filesystem!");
                    return;
                }
            }

            sendCommand(args);

            String resPayload = this.inputStream.readUTF();
            String[] resArgs = resPayload.split(" ");
            Logger.info(String.format("->: %s", resPayload));
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

    // REDIRECT <destinationAddress>
    // Response to any command thats not supported by a normal server
    // The connection will automatically switch to the CCServer and send the original command
    public void handleServerResponseREDIRECT(String[] args) {
        HOST_ADDRESS = args[1];
        Logger.info(String.format("Being redirected to: %s", args[1]));
        disconnect();
        handleClientCommandCONNECT(new String[]{"CONNECT", HOST_ADDRESS, String.valueOf(HOST_PORT)});

        // redirect command to ccServer
        handleServerInteraction(currentClientInput);
    }

    // INFO <RESSOURCE | NETWORK> <? ressourceUUID>
    // Response to the INFO command
    // Gives information about the network or a specific ressource
    // If INFO was called on a non-CCServer it will redirect to the CCServer
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

    // DOWNLOAD <ressourceUUID> <blockUUID>
    // Response to the GETBLOCK command
    // Is followed by the block file
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

    // AUTH <SUCCESS | FAIL>
    // Response to the AUTH (authorizing a ressource) command
    // Signals, whether the client can upload to the network or not
    // Is followed by an exchange of ressourcefiles
    public void handleServerResponseAUTH(String[] args) {
        if (args[1].equals("SUCCESS")) {
            Logger.success("Upload authorization granted! Sending ressourcefile...");
            String ressourceUUID = args[2];
            String ressourceFilePath = String.format("%s%s.g2g", RESSOURCE_DIRECTORY, ressourceUUID);

            FilesRemote.sendFile(new File(ressourceFilePath), outputStream);
            FilesRemote.receiveFile("tmp.g2g", inputStream);
            Logger.success("Received assmebled ressourcefile!");

            // move file to ressource directory and delete tmp file
            try {
                Files.delete(Path.of(ressourceFilePath));
                Files.move(Path.of("./tmp.g2g"), Path.of(ressourceFilePath));
            } catch (IOException e) {
                Logger.error("Error while replacing temporary ressourcefile with serverresponse!");
                Logger.exception(e);
            }
        } else {
            Logger.error("Upload authorization failed! Server responded with:");
            Logger.error(Arrays.toString(args));
        }
    }

    // SUCCESS <ressourceUUID> <decryptedBlockUUID>
    // Response for AUTH (authorizing a block) to a normal server
    // Means the server could verify the clients public key
    // Is followed by the client uploading the according block
    public void handleServerResponseSUCCESS(String[] args) {
        File blockFile = new File(String.format("%s%s/%s.g2gblock", RESSOURCE_DIRECTORY, args[1], args[2]));
        if (!blockFile.exists()) {
            Logger.error(String.format("Blockfile [%s] doesn't exist!", blockFile.getPath()));
            return;
        }

        try {
            Logger.success("Block upload authorized! Uploading...");
            FilesRemote.sendFile(blockFile, outputStream);

            String response = this.inputStream.readUTF();
            if (response.contains("SUCCESS")) {
                Logger.success(String.format("Block [%s] uploaded successfully!", args[2]));
            }
        } catch (IOException e) {
            Logger.error("Error while reading server output to block upload!");
            Logger.exception(e);
        }
    }

    // CONNECT <address>
    // Tries to establish a connection to the given address
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

    // INFO <NETWORK | RESSOURCE> <networkUUID | ressourceUUID | ALL>
    // Gives information about all or a specific network / ressource
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

    // AUTOGET <ressourceUUID>
    // Assembles GETBLOCK commands from the ressourcefile and
    // sends the command to the according server where its
    // hosted - one after another
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
            String serverAddress = entry.getValue();
            Logger.info(String.format("Downloading block from %s... ", entry.getValue()));
            handleClientCommandCONNECT(new String[]{"CONNECT", serverAddress});
            handleServerInteraction(entry.getKey()); // send GETBLOCK
            handleServerInteraction(new String[]{"DISCONNECT"});
        }

        Logger.success("All blocks downloaded!");
    }

    // AUTOUPLOAD <ressourceUUID>
    // Assembles AUTH commands from the ressourcefile and
    // sends the command to the according server where its
    // hosted - one after another
    public void handleClientCommandAUTOUPLOAD(String[] args) {
        RessourceFile ressourceFile = new RessourceFile(RESSOURCE_DIRECTORY + args[1] + ".g2g");

        if (!ressourceFile.getFile().exists()) {
            Logger.error("Ressourcefile doesn't exist!");
            return;
        }

        HashMap<String[], String> commands = ressourceFile.getAUTHCommands(this.getPrivateKey());

        if (commands.size() != Integer.parseInt(ressourceFile.getConfigContent().get("AmountOfBlocks"))) {
            Logger.error("Block amount mismatch between ressourcefile entry and amount of AUTH commands!");
        }

        Logger.info(String.format("Uploading %d blocks...", commands.size()));

        for (var entry : commands.entrySet()) {
            String serverAddress = entry.getValue();
            Logger.info(String.format("Authorizing block to %s... ", entry.getValue()));
            handleClientCommandCONNECT(new String[]{"CONNECT", serverAddress});
            handleServerInteraction(entry.getKey()); // send AUTH
            handleServerInteraction(new String[]{"DISCONNECT"});
        }

        Logger.success(String.format("[%d/%d] blocks uploaded!", commands.size(), commands.size()));
        Logger.success("Ressource uploaded successfully!");
    }

    // EXIT
    // Simply shuts the client down relatively gracefully
    public void handleClientCommandEXIT(String[] args) {
        Logger.info("Shutting down...");
        System.exit(0);
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

