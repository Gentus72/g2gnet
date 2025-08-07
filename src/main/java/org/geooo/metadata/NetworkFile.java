package org.geooo.metadata;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.geooo.CCServer;
import org.geooo.dto.NetworkDTO;
import org.geooo.dto.RessourceDTO;
import org.geooo.dto.ServerDTO;
import org.geooo.util.G2GUtil;
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
                ccServer.setNetworkUUID(G2GUtil.getRandomUUID());
            }

            writer.write(String.format("UUID: %s\n", ccServer.getNetworkUUID()));
            writer.write(String.format("CCServer: %s\n", ccServer.getAddress()));
            String networkLabel = ccServer.getNetworkLabel() != null ? ccServer.getNetworkLabel() : "<noLabel>";
            writer.write(String.format("NetworkLabel: %s\n", networkLabel));

            writer.write("Servers (uuid, address):\n");
            Logger.info(String.format("Writing %d + 1 (this) servers to file!", ccServer.getServers().size()));
            writer.write(String.format("%s, %s\n", ccServer.getUUID(), ccServer.getAddress()));
            for (ServerDTO server : ccServer.getServers()) {
                writer.write(String.format("%s, %s\n", server.getUUID(), server.getAddress()));
            }

            // addSection(writer, ccServer.getServers(), "Servers (uuid, address):",
            // ServerDTO::getUUID, ServerDTO::getAddress);
            writer.write("Ressources (uuid, title, size):\n");
            Logger.info(String.format("Writing %d ressources to file", ccServer.getRessources().size()));
            for (RessourceDTO ressource : ccServer.getRessources()) {
                writer.write(String.format("%s,%s,%d\n", ressource.getUUID(), ressource.getTitle(),
                        ressource.getBlockAmount()));
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
            String ccServerAddress = reader.readLine().split(" ")[1];
            String networkLabel = reader.readLine().split(" ")[1];
            ArrayList<ServerDTO> servers = new ArrayList<>();
            ArrayList<RessourceDTO> ressources = new ArrayList<>();

            if (ccServer.getNetworkUUID() != null && !ccServer.getNetworkUUID().equals(networkUUID)) {
                Logger.error("UUID mismatch between networkfile and ccServer's networkUUID!");
                return;
            }

            if (ccServer.getAddress() != null && !ccServer.getAddress().equals(ccServerAddress)) {
                Logger.warn("IPv4 Address mismatch between CCServer and Networkfile!");
            }

            if (networkLabel.isEmpty()) {
                Logger.warn("No Label has been set in the networkFile!");
            }

            ccServer.setNetworkUUID(networkUUID);
            ccServer.setNetworkLabel(networkLabel);
            reader.readLine(); // line will be server headers
            String nextLine = reader.readLine(); // also header ? idk

            while (nextLine != null && !nextLine.contains("Ressources")) {
                String[] components = nextLine.split(",");

                if (!components[1].equals(ccServer.getAddress())) {
                    servers.add(new ServerDTO(components[0], components[1])); // uuid & address
                }

                nextLine = reader.readLine();
            }
            nextLine = reader.readLine(); // next line will be ressource header

            while (nextLine != null) {
                String[] components = nextLine.split(",");

                ressources.add(new RessourceDTO(components[0], components[1], Integer.parseInt(components[2].strip()))); // uuid,
                // title,
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
        File ressourceDir = new File(CCServer.getRessourceDirectory());
        ArrayList<RessourceDTO> ressources = new ArrayList<>();

        // check if resource dir exists
        File[] matchingFiles = ressourceDir.listFiles((dir, name) -> name.endsWith(".g2g"));

        if (matchingFiles == null || matchingFiles.length == 0) {
            Logger.warn(
                    "Error while fetching local files or no ressource files found! Ressources in networkfile can't be updated!");
            return;
        }

        for (File ressourceFile : matchingFiles) {
            RessourceDTO ressource = new RessourceDTO();

            try (BufferedReader reader = new BufferedReader(new FileReader(ressourceFile))) {
                String uuid = reader.readLine().split(" ")[1];
                if (!uuid.equals(ressource.getUUID())) {
                    Logger.error("UUID mismatch between ressourcefile name and first line!");
                }

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

    public ArrayList<ServerDTO> getServers() {
        ArrayList<ServerDTO> servers = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(this.file))) {
            String line = reader.readLine();

            do {
                line = reader.readLine();
            } while (!line.contains("Servers (uuid, address):"));
            line = reader.readLine();

            while (!line.contains("Ressources (uuid, title, size):")) {
                String[] components = line.split(",");
                ServerDTO newServer = new ServerDTO(components[0].strip(), components[1].strip());
                servers.add(newServer);

                line = reader.readLine();
            }

            return servers;
        } catch (IOException e) {
            Logger.error("Error while reading servers from networkfile!");
            return null;
        }
    }

    public ArrayList<RessourceDTO> getRessources() {
        ArrayList<RessourceDTO> ressources = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(this.file))) {
            String line = reader.readLine();

            do {
                line = reader.readLine();
            } while (!line.contains("Ressources (uuid, title, size):"));
            line = reader.readLine();

            while (line != null) {
                String[] components = line.split(",");
                RessourceDTO newRessource = new RessourceDTO(components[0].strip(), components[1].strip(),
                        Integer.parseInt(components[2].strip()));
                ressources.add(newRessource);

                line = reader.readLine();
            }

            return ressources;
        } catch (IOException e) {
            Logger.error("Error while reading ressources from networkfile!");
            return null;
        }
    }

    public NetworkDTO getNetwork() {
        HashMap<String, String> _configContent = getConfigContent();

        return new NetworkDTO(_configContent.get("UUID"), _configContent.get("NetworkLabel"),
                _configContent.get("CCServer"), this);
    }
}
