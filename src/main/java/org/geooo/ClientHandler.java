package org.geooo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.geooo.util.Logger;

public class ClientHandler extends Thread {
    Socket serverSocket;
    Client client;
    Server server;

    public ClientHandler(Socket serverSocket, Server server) {
        this.serverSocket = serverSocket;
        this.server = server;

        this.client = new Client();
        this.server.clients.add(this.client);
    }

    @Override
    public void run() {
        try (DataInputStream inputStream = new DataInputStream(serverSocket.getInputStream()); DataOutputStream outputStream = new DataOutputStream(serverSocket.getOutputStream());) {
            String clientInput = inputStream.readUTF();
            ServerCommand command = getCommandFromClientInput(clientInput);

            if (command.equals(ServerCommand.GETBLOCK)) {
                // handle download
            } else {
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
