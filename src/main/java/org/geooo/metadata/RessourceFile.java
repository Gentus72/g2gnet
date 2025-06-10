package org.geooo.metadata;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.geooo.Ressource;
import org.geooo.dto.RessourceBlockDTO;
import org.geooo.util.Logger;

// TODO make non-static, since there can be multiple ressourcefiles
public class RessourceFile extends ConfigFile {

    private final String blockHeader = "Blocks (uuid, location, hash, sequenceID):";

    public RessourceFile(String filePath) {
        super(filePath);
    }

    public void writeToFile(Ressource ressource, String clientPublicKey) {
        ensureConfigFile(false);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(String.format("UUID: %s\n", ressource.getUUID()));
            writer.write(String.format("Title: %s\n", ressource.getTitle()));
            writer.write(String.format("HashSum: %s\n", ressource.getTotalHashSum()));
            writer.write(String.format("Uploader: %s\n", clientPublicKey));
            writer.write(String.format("AmountOfBlocks: %d\n", ressource.getBlockAmount()));
            writer.write(String.format("SourceFileName: %s\n", ressource.getSourceFile().getName()));

            writer.write(blockHeader + "\n");
            for (var entry : ressource.getBlockLocations().entrySet()) {
                writer.write(String.format("%s,%s,%s,%d\n", entry.getKey().getUUID(), entry.getValue(), entry.getKey().getHashSum(), entry.getKey().getSequenceID()));
            }
        } catch (IOException e) {
            Logger.error("Error while writing to ressourcefile!");
            Logger.exception(e);
        }
    }

    public ArrayList<RessourceBlockDTO> getBlocks() {
        ensureConfigFile(true);
        ArrayList<RessourceBlockDTO> blocks = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(this.file))) {
            String line = reader.readLine();

            while (!line.equals(blockHeader) && !line.equals("Blocks (uuid, hash, sequenceID):")) {
                line = reader.readLine();
            }
            line = reader.readLine(); // skip header
            while (line != null) {
                String[] components = line.split(",");

                if (components.length != 4) {
                    Logger.error("Malformed ressourcefile! Component entry doesn't have 4 components!");
                    return null;
                }

                RessourceBlockDTO newBlock = new RessourceBlockDTO();
                newBlock.setUUID(components[0]);
                newBlock.setLocation(components[1]);
                newBlock.setHashSum(components[2]);
                newBlock.setSequenceID(Integer.parseInt(components[3]));

                blocks.add(newBlock);
                line = reader.readLine();
            }

            return blocks;
        } catch (IOException e) {
            Logger.error("Error while reading configfile section!");
            Logger.exception(e);
        }

        return null;
    }

    public HashMap<String[], String> getGETBLOCKCommands() {
        HashMap<String[], String> commands = new HashMap<>();
        ArrayList<RessourceBlockDTO> ressourceBlocks = getBlocks();

        for (RessourceBlockDTO block : ressourceBlocks) {
            String address = block.getLocation();
            String[] command = new String[]{"GETBLOCK", this.getConfigContent().get("UUID"), block.getUUID()};

            commands.put(command, address);
        }

        return commands;
    }
}
