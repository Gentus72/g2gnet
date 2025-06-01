package org.geooo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

import org.geooo.dto.ClientDTO;
import org.geooo.util.FilesRemote;
import org.geooo.util.Logger;
import org.geooo.util.ServerCommand;

public class ClientHandler extends Thread {
    Socket serverSocket;
    ClientDTO client;
    Server server;

    public ClientHandler(Socket serverSocket, Server server) {
        this.serverSocket = serverSocket;
        this.server = server;

        this.client = new ClientDTO(UUID.randomUUID().toString().replace("-", ""));
        Logger.info(String.format("New client connected with UUID: %s", client.getUUID()));
        this.server.clients.add(this.client);
    }

    @Override
    public void run() {
        try (DataInputStream inputStream = new DataInputStream(serverSocket.getInputStream());
                DataOutputStream outputStream = new DataOutputStream(serverSocket.getOutputStream());) {
            String clientInput = inputStream.readUTF();
            ServerCommand command = getCommandFromClientInput(clientInput);
            String[] clientArgs = clientInput.split(" ");

            // GETBLOCK <ressourceUUID> <blockUUID>
            if (command.equals(ServerCommand.GETBLOCK)) {
                        if (clientArgs.length < 3) {
                            outputStream.writeUTF("ERROR insufficient arguments! Format is: GETBLOCK <ressourceUUID> <blockUUID>");
                            outputStream.flush();

                            inputStream.close();
                            outputStream.close();
                            return;
                        }

                        // check if ressource directory exists
                        String ressourceUUID = clientArgs[1];
                        File ressourceDirectory = new File(Server.RESSOURCE_DIRECTORY + ressourceUUID);

                        String blockUUID = clientArgs[2];
                        File blockFile = new File(String.format("%s%s/%s.g2gblock", Server.RESSOURCE_DIRECTORY, ressourceUUID, blockUUID));

                        if (!ressourceDirectory.exists() || !blockFile.exists()) {
                            outputStream.writeUTF("ERROR ressource directory or block file doesn't exist!");
                            outputStream.flush();

                            inputStream.close();
                            outputStream.close();
                            return;
                        }

                        // handle download
                        outputStream.writeUTF(String.format("DOWNLOAD %s %s", ressourceUUID, blockUUID));
                        outputStream.flush();

                        FilesRemote.sendFile(blockFile, outputStream);
            } else {
                Logger.info(String.format("Redirecting client %s to ccServer at: %s", this.client.getUUID(),
                        this.server.ccServer.getAddress()));
                outputStream.writeUTF(String.format("REDIRECT %s", server.ccServer.getAddress()));
            }
        } catch (IOException e) {
            Logger.error("Error while handling client request!");
            Logger.exception(e);
        }
    }

    public ServerCommand getCommandFromClientInput(String clientInput) {
        return ServerCommand.valueOf(clientInput.split(" ")[0]);
    }
}
