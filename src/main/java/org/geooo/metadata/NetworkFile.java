package org.geooo.metadata;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.geooo.CCServer;
import org.geooo.dto.RessourceDTO;
import org.geooo.dto.ServerDTO;
import org.geooo.util.G2GUUID;
import org.geooo.util.Logger;

public class NetworkFile extends ConfigFile {
    // public final String FILENAME = "networkFile.g2gnet";
    // public File file = new File(CCServer.RESSOURCE_DIRECTORY + FILENAME);

    public NetworkFile(String filePath) {
        super(filePath);
    }

    public void writeToFile(CCServer ccServer) {
        ensureConfigFile(false);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            if (ccServer.getNetworkUUID() == null) {
                Logger.warn("NetworkUUID was unset! Creating new one...");
                ccServer.setNetworkUUID(G2GUUID.getRandomUUID());
            }

            writer.write(String.format("UUID: %s\n", ccServer.getNetworkUUID()));

            writer.write("Servers (uuid, address):\n");
            for (ServerDTO server : ccServer.getServers()) {
                writer.write(String.format("%s, %s\n", server.getUUID(), server.getAddress()));
            }

            // addSection(writer, ccServer.getServers(), "Servers (uuid, address):",
            // ServerDTO::getUUID, ServerDTO::getAddress);

            writer.write("Ressources (uuid, title, size):\n");
            for (RessourceDTO ressource : ccServer.getRessources()) {
                writer.write(
                        String.format("%s,%s,%d", ressource.getUUID(), ressource.getTitle(), ressource.getSizeMiB()));
            }
        } catch (IOException e) {
            Logger.error("Error while writing to networkfile!");
            Logger.exception(e);
        }
    }

    public void readFromFile(CCServer ccServer) {
        ensureConfigFile(true);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String networkUUID = reader.readLine().split(" ")[1];
            ArrayList<ServerDTO> servers = new ArrayList<>();
            ArrayList<RessourceDTO> ressources = new ArrayList<>();

            if (ccServer.getNetworkUUID() != null && !ccServer.getNetworkUUID().equals(networkUUID)) {
                Logger.error("UUID mismatch between networkfile and ccServer's networkUUID!");
                return;
            }

            ccServer.setNetworkUUID(networkUUID);
            reader.readLine(); // line will be server headers
            String nextLine = reader.readLine(); // also header ? idk

            while (nextLine != null && !nextLine.contains("Ressources")) {
                String[] components = nextLine.split(",");

                servers.add(new ServerDTO(components[0], components[1])); // uuid & address

                nextLine = reader.readLine();
            }
            nextLine = reader.readLine(); // next line will be ressource header

            while (nextLine != null) {
                String[] components = nextLine.split(",");

                ressources.add(new RessourceDTO(components[0], components[1], Integer.parseInt(components[2]))); // uuid, title,
                // blockAmount

                nextLine = reader.readLine();
            }

            ccServer.setServers(servers);
            ccServer.setRessources(ressources);
        } catch (IOException e) {
            Logger.error("Error while reading networkfile!");
            Logger.exception(e);
        }
    }

    // get ressource metadata based on available ressourcefiles
    public void updateRessources(CCServer ccServer) {
        File ressourceDir = new File(CCServer.CCSERVER_DIRECTORY);
        ArrayList<RessourceDTO> ressources = new ArrayList<>();

        // check if resource dir exists

        File[] matchingFiles = ressourceDir.listFiles((dir, name) -> name.endsWith(".g2g"));

        if (matchingFiles == null || matchingFiles.length == 0) {
            Logger.error(
                    "Error while fetching local files or no ressource files found! Ressources in networkfile can't be updated!");
            return;
        }

        for (File ressourceFile : matchingFiles) {
            RessourceDTO ressource = new RessourceDTO(ressourceFile.getName().split("\\.")[0]); // uuid in the filename

            try (BufferedReader reader = new BufferedReader(new FileReader(ressourceFile))) {
                String uuid = reader.readLine().split(" ")[1];
                if (!uuid.equals(ressource.getUUID()))
                    Logger.error("UUID mismatch between ressourcefile name and first line!");

                ressource.setTitle(reader.readLine().split(" ")[1]);
                reader.readLine(); // next line is hashSum, which we don't need

                ressource.setBlockAmount(Integer.parseInt(reader.readLine().split(" ")[1]));
            } catch (IOException e) {
                Logger.error("Error while reading ressourcefiles!");
                Logger.exception(e);
            }

            ressources.add(ressource);
        }

        ccServer.setRessources(ressources);
    }
}
