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

import org.geooo.dto.RessourceBlockDTO;
import org.geooo.dto.RessourceDTO;
import org.geooo.dto.ServerDTO;
import org.geooo.metadata.NetworkFile;
import org.geooo.metadata.RessourceFile;
import org.geooo.util.ClientCommand;
import org.geooo.util.G2GUtil;
import org.geooo.util.Logger;
import org.geooo.util.ServerCommand;
import org.geooo.util.ServerResponse;

public abstract class ClientHelper {
    public static void handleServerInteraction(Client client, String[] args) {
        try {
            ServerCommand command = ServerCommand.valueOf(args[0]);
            if (!command.hasCorrectArgsAmount(args.length)) {
                Logger.error("Wrong number of arguments!");
                return;
            }

            if (command.equals(ServerCommand.AUTH)) {
                File[] ressourceFiles = new File(Client.RESSOURCE_DIRECTORY).listFiles((dir, name) -> name.equals(args[1] + ".g2g"));
                if (ressourceFiles == null || ressourceFiles.length == 0) {
                    Logger.error("Ressource to auth not present in filesystem!");
                    return;
                }
            }

            sendCommand(client, args);

            String resPayload = client.inputStream.readUTF();
            String[] resArgs = resPayload.split(" ");
            Logger.info(String.format("->: %s", resPayload));
            ServerResponse response = ServerResponse.valueOf(resArgs[0]);

            for (var entry : client.registeredServerResponses.entrySet()) {
                if (response.equals(entry.getKey())) {
                    entry.getValue().accept(client, resArgs);
                }
            }
        } catch (IllegalArgumentException e) {
            Logger.error(String.format("Unknown server-command %s! Try again...", args[0]));
        } catch (IOException e) {
            Logger.error("Error while receiving server response!");
            Logger.exception(e);
        }
    }

    public static void handleClientInteraction(Client client, String[] args) {
        try {
            ClientCommand command = ClientCommand.valueOf(args[0]);
            if (!command.hasCorrectArgsAmount(args.length)) {
                Logger.error("Wrong number of arguments!");
                return;
            }

            for (var entry : client.registeredClientCommands.entrySet()) {
                if (command.equals(entry.getKey())) {
                    entry.getValue().accept(client, args);
                }
            }
        } catch (IllegalArgumentException e) {
            Logger.error(String.format("Unknown client-command %s! Try again...", args[0]));
        }
    }

    // REDIRECT <destinationAddress>
    // Response to any command thats not supported by a normal server
    // The connection will automatically switch to the CCServer and send the original command
    public static void handleServerResponseREDIRECT(Client client, String[] args) {
        client.currentHost = args[1];
        Logger.info(String.format("Being redirected to: %s", args[1]));
        disconnect(client);
        handleClientCommandCONNECT(client, new String[]{"CONNECT", client.currentHost, String.valueOf(client.hostPort)});

        // redirect command to ccServer
        handleServerInteraction(client, client.currentClientInput);
    }

    // INFO <RESSOURCE | NETWORK> <? ressourceUUID>
    // Response to the INFO command
    // Gives information about the network or a specific ressource
    // If INFO was called on a non-CCServer it will redirect to the CCServer
    public static void handleServerResponseINFO(Client client, String[] args) {
        switch (args[1]) {
            case "NETWORK" -> {
                G2GUtil.receiveFileRemote(String.format("%s%s.g2gnet", Client.RESSOURCE_DIRECTORY, args[2]), client.inputStream);
                Logger.info("Received networkfile!");
            }
            case "RESSOURCE" -> {
                G2GUtil.receiveFileRemote(String.format("%s%s.g2g", Client.RESSOURCE_DIRECTORY, args[2]), client.inputStream);
                // update clientfile
                Logger.info("Received ressourcefile!");
            }
        }
    }

    // DOWNLOAD <ressourceUUID> <blockUUID>
    // Response to the GETBLOCK command
    // Is followed by the block file
    public static void handleServerResponseDOWNLOAD(Client client, String[] args) {
        try {
            // create directory
            String ressourceDirectoryPath = String.format("%s%s/", Client.RESSOURCE_DIRECTORY, args[1]);
            File ressourceDirectory = new File(ressourceDirectoryPath);

            if (!ressourceDirectory.exists()) {
                Logger.info("Creating ressource directory! Is this the first block download?");
                Files.createDirectory(Path.of(ressourceDirectoryPath));
            }

            G2GUtil.receiveFileRemote(String.format("%s%s.g2gblock", ressourceDirectoryPath, args[2]), client.inputStream);
            Logger.success(String.format("Download for block %s successful!", args[2]));
            client.clientFile.writeToFile(client);
        } catch (IOException e) {
            Logger.error("Error while creating client ressource directory!");
            Logger.exception(e);
        }
    }

    // AUTH <SUCCESS | FAIL>
    // Response to the AUTH (authorizing a ressource) command
    // Signals, whether the client can upload to the network or not
    // Is followed by an exchange of ressourcefiles
    public static void handleServerResponseAUTH(Client client, String[] args) {
        if (args[1].equals("SUCCESS")) {
            Logger.success("Upload authorization granted! Sending ressourcefile...");
            String ressourceUUID = args[2];
            String ressourceFilePath = String.format("%s%s.g2g", Client.RESSOURCE_DIRECTORY, ressourceUUID);

            G2GUtil.sendFileRemote(ressourceFilePath, client.outputStream);
            G2GUtil.receiveFileRemote("tmp.g2g", client.inputStream);
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
    public static void handleServerResponseSUCCESS(Client client, String[] args) {
        File blockFile = new File(String.format("%s%s/%s.g2gblock", Client.RESSOURCE_DIRECTORY, args[1], args[2]));
        if (!blockFile.exists()) {
            Logger.error(String.format("Blockfile [%s] doesn't exist!", blockFile.getPath()));
            return;
        }

        try {
            Logger.success("Block upload authorized! Uploading...");
            G2GUtil.sendFileRemote(blockFile, client.outputStream);

            String response = client.inputStream.readUTF();
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
    public static void handleClientCommandCONNECT(Client client, String[] args) {
        client.currentHost = args[1];
        client.hostPort = 7000;
        if (args.length >= 3) {
            client.hostPort = Integer.parseInt(args[2]);
        }

        try {
            client.socket = new Socket(client.currentHost, client.hostPort);
            client.socket.setSoTimeout(30000); // 30 sec
            client.outputStream = new DataOutputStream(client.socket.getOutputStream());
            client.inputStream = new DataInputStream(client.socket.getInputStream());
            Logger.success(String.format("Successfully connected to Server [%s:%d]!", client.currentHost, client.hostPort));

            client.isConnected = true;
        } catch (IOException e) {
            Logger.error("Error while connecting to server!");
            Logger.exception(e);
        }
    }

    // INFO <NETWORK | RESSOURCE> <networkUUID | ressourceUUID | ALL>
    // Gives information about all or a specific network / ressource
    public static void handleClientCommandINFO(Client client, String[] args) {
        switch (args[1]) {
            case "NETWORK" -> {
                if (args[2].equals("ALL")) {
                    Logger.info("Known networks (uuid):");
                    File[] networkFiles = new File(Client.RESSOURCE_DIRECTORY).listFiles((dir, name) -> name.endsWith(".g2gnet"));

                    for (File file : networkFiles) {
                        Logger.info(file.getName());
                    }
                } else {
                    NetworkFile networkFile = new NetworkFile(Client.RESSOURCE_DIRECTORY + args[2] + ".g2gnet");

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
                    File[] ressourceFiles = new File(Client.RESSOURCE_DIRECTORY).listFiles((dir, name) -> name.endsWith(".g2g"));

                    for (File file : ressourceFiles) {
                        Logger.info(file.getName());
                    }
                } else {
                    RessourceFile ressourceFile = new RessourceFile(Client.RESSOURCE_DIRECTORY + args[2] + ".g2g");

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
    public static void handleClientCommandAUTOGET(Client client, String[] args) {
        RessourceFile ressourceFile = new RessourceFile(Client.RESSOURCE_DIRECTORY + args[1] + ".g2g");

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
            handleClientCommandCONNECT(client, new String[]{"CONNECT", serverAddress});
            handleServerInteraction(client, entry.getKey()); // send GETBLOCK
            handleServerInteraction(client, new String[]{"DISCONNECT"});
        }

        Logger.success("All blocks downloaded!");
    }

    // AUTOUPLOAD <ressourceUUID>
    // Assembles AUTH commands from the ressourcefile and
    // sends the command to the according server where its
    // hosted - one after another
    public static void handleClientCommandAUTOUPLOAD(Client client, String[] args) {
        RessourceFile ressourceFile = new RessourceFile(Client.RESSOURCE_DIRECTORY + args[1] + ".g2g");

        if (!ressourceFile.getFile().exists()) {
            Logger.error("Ressourcefile doesn't exist!");
            return;
        }

        HashMap<String[], String> commands = ressourceFile.getAUTHCommands(client.getPrivateKey());

        if (commands.size() != Integer.parseInt(ressourceFile.getConfigContent().get("AmountOfBlocks"))) {
            Logger.error("Block amount mismatch between ressourcefile entry and amount of AUTH commands!");
        }

        Logger.info(String.format("Uploading %d blocks...", commands.size()));

        for (var entry : commands.entrySet()) {
            String serverAddress = entry.getValue();
            Logger.info(String.format("Authorizing block to %s... ", entry.getValue()));
            handleClientCommandCONNECT(client, new String[]{"CONNECT", serverAddress});
            handleServerInteraction(client, entry.getKey()); // send AUTH
            handleServerInteraction(client, new String[]{"DISCONNECT"});
        }

        Logger.success(String.format("[%d/%d] blocks uploaded!", commands.size(), commands.size()));
        Logger.success("Ressource uploaded successfully!");
    }

    // EXIT
    // Simply shuts the client down relatively gracefully
    public static void handleClientCommandEXIT(Client client, String[] args) {
        Logger.info("Shutting down...");
        System.exit(0);
    }

    public static void disconnect(Client client) {
        try {
            Logger.info("Disconnecting...");

            client.socket.close();
            client.outputStream.close();
            client.inputStream.close();

            client.isConnected = false;
        } catch (IOException e) {
            Logger.error("Error while closing connection!");
            Logger.exception(e);
        }
    }

    public static void sendCommand(Client client, String[] args) {
        String payload = String.join(" ", args);

        try {
            client.outputStream.writeUTF(payload);
            client.outputStream.flush();
        } catch (IOException e) {
            Logger.error("Error while sending command to server!");
            Logger.exception(e);
        }
    }
}
