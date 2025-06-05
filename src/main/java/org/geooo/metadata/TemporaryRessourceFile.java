package org.geooo.metadata;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.geooo.Ressource;
import org.geooo.dto.ClientDTO;
import org.geooo.dto.RessourceBlockDTO;
import org.geooo.util.Logger;

// TODO make non-static
public class TemporaryRessourceFile extends ConfigFile {

    public TemporaryRessourceFile(String filePath) {
        super(filePath);
    }

    public void writeToFile(Ressource ressource, String ressourceDirectory, ClientDTO client) {
        ensureConfigFile(false);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("TEMPORARY RESSOURCE FILE");
            writer.write(String.format("UUID: %s\n", ressource.getUUID()));
            writer.write(String.format("Title: %s\n", ressource.getTitle()));
            writer.write(String.format("PublicKey: %s\n", client.getPublicKeyBase64()));
            writer.write(String.format("HashSum: %s\n", ressource.getTotalHashSum()));
            writer.write(String.format("AmountOfBlocks: %d\n", ressource.getBlockAmount()));
            writer.write(String.format("SourceFileName: %s\n", ressource.getSourceFile().getName()));

            writer.write("Blocks (uuid, hash, sequenceID):\n");
            for (var entry : ressource.getBlockLocations().entrySet()) {
                writer.write(String.format("%s,%s,%d\n", entry.getKey().getUUID(), entry.getKey().getHashSum(), entry.getKey().getSequenceID()));
            }
        } catch (IOException e) {
            Logger.error("Error while writing to ressourcefile!");
            Logger.exception(e);
        }
    }

    public File convertToRessourceFile(String destinationDirectory, String tmpFile, HashMap<Integer, String> blockLocations) {
        File ressourceFile = new File(destinationDirectory + "ressourceFile.g2g");

        if (blockLocations.size() != getBlocks().size()) {
            Logger.error("Length mismatch between blockLocations and blocks in tmpfile!");
            return null;
        }

        // populate

        return ressourceFile;
    }

    public ArrayList<RessourceBlockDTO> getBlocks() {
        return null;
    }
}
