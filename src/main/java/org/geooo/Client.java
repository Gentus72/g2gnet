package org.geooo;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Scanner;

import org.geooo.util.FilesRemote;
import org.geooo.util.Logger;

public class Client {

    public static final String RESSOURCE_DIRECTORY = "client/res/";
    public static final String HOST_ADDRESS = "localhost";
    public static final int HOST_PORT = 7000;

    public static void main(String[] args) {
        // Ressource res = new Ressource(new File("res/test.jpg"), HOST_ADDRESS, RessourceDistributionStrategy.EVEN_DISTRIBUTION);

        Ressource.reassembleSourceFile(new File("f922d9b0e27a41d7b708cf54dfd8e14c.g2g"), new File[] {
            new File("res/9efac98096e546c6956c462bf3c22f06.g2gblock"),
            new File("res/0136ba79e6af4fd59697d7b6d65ee99a.g2gblock")
        }, "res/");

        // new Client();
    }

    Socket socket;
    DataOutputStream outputStream;
    DataInputStream inputStream;
    Scanner userInputScanner;

    public Client() {
        startClient();
    }  

    public void startClient() {

        try {
            this.socket = new Socket(HOST_ADDRESS, HOST_PORT);
            this.outputStream = new DataOutputStream(this.socket.getOutputStream());
            this.inputStream = new DataInputStream(this.socket.getInputStream());
            this.userInputScanner = new Scanner(System.in);

            Logger.info("Client connected and waiting for commands!");

            // Ressource ressource = new Ressource(new File(RESSOURCE_DIRECTORY + "/2266302b65a742d584c37540b2d5e4a2/ressourceFile.g2g"));
            // ressource.assembleSourceFile(new File(RESSOURCE_DIRECTORY + "/source.png"));
            // System.exit(0);
            while (true) {
                String[] clientArguments = getUserCommandInputArgs();

                String response = this.inputStream.readUTF();
                Logger.info(response);
                ServerResponse responseCommand = ServerResponse.valueOf(clientArguments[0]);

                switch (responseCommand) {
                    case INFO -> {
                        Logger.info("Getting serverfile from server...");

                        FilesRemote.receiveFile("tempServerfile.g2gsrv", inputStream);

                        Logger.info("Received serverfile!");
                    }
                    case DOWNLOAD -> {
                        if (clientArguments.length < 2) {
                            Logger.error("No UUID supplied for GET command - should have been caught by server!");
                            continue;
                        }

                        receiveRessource(clientArguments);
                    }
                    case ERROR -> {
                        Logger.error("Error response from server: " + response.substring(6));
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

    private String[] getUserCommandInputArgs() {
        System.out.print("$> ");
        String clientInput = this.userInputScanner.nextLine();

        String[] arguments = clientInput.split(" ");

        try {
            this.outputStream.writeUTF(clientInput);
            this.outputStream.flush();
        } catch (IOException e) {
            Logger.error("Error while sending command to server!");
            Logger.exception(e);
        }

        return arguments;
    }

    private void readRessourceFile(File ressourceFile, HashMap<String, String> metadata, HashMap<String, String> blocksData) {
        try (BufferedReader reader = new BufferedReader(new FileReader(ressourceFile))) {
            // Ressource-Informationen lesen
            while (true) {
                String line = reader.readLine();

                if (line == null || line.equals("blocks (uuid, hash):")) {
                    break;
                }

                String[] keyValuePair = line.split(":");
                metadata.put(keyValuePair[0], keyValuePair[1].strip());
            }

            while (true) {
                String line = reader.readLine();

                if (line == null) {
                    break;
                }

                String[] uuidHashsumPair = line.split(",");

                blocksData.put(uuidHashsumPair[0], uuidHashsumPair[1]);
            }
        } catch (Exception e) {
            Logger.error("Error while reading client ressource file");
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
            HashMap<String, String> metadata = new HashMap<>();
            HashMap<String, String> blocksData = new HashMap<>();
            readRessourceFile(ressourceFile, metadata, blocksData);

            Logger.info("Receving block amount: " + metadata.get("total_blocks"));

            if (Integer.parseInt(metadata.get("total_blocks")) != blocksData.size()) {
                Logger.error("Block amount mismatch for ressource: " + clientArguments[1]);
            }

            // 3. receive blocks according to metadata
            for (String blockUUID : blocksData.keySet()) {
                File blockFile = new File(ressourceDirectory.toString() + "/" + blockUUID + ".g2gblock");

                outputStream.writeUTF("GET block " + blockUUID);

                FilesRemote.receiveFile(blockFile, inputStream);
            }
        } catch (IOException e) {
            Logger.error("Error while creating client ressource directory!");
            Logger.exception(e);
        }
    }
}
