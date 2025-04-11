package org.geooo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.geooo.util.Logger;

public class RessourceFile {

    /*
     * Erstellen der Ressource-Datei mit wichtigen Metadaten für den Client
     */
    public RessourceFile(Ressource ressource) {
        Logger.info("Initiating ressource file!");

        String[] content = new String[]{
            "sourceFile: " + ressource.getSourceFile().getName(),
            "title: " + ressource.getTitle(),
            "uuid: " + ressource.getUUID(),
            "scope: public", // change when signatures are introduced
            "totalHashSum: " + ressource.getTotalHashSum(),
            "total_blocks: " + ressource.getBlockAmount(),
            "blocks (uuid, hash):", // blocks
        };

        File ressourceFile = new File(Ressource.PARENT_DIRECTORY + ressource.getUUID(), "ressourceFile.g2g");

        try (BufferedWriter fileContent = new BufferedWriter(new FileWriter(ressourceFile, true))) {
            for (String line : content) {
                fileContent.append(line + "\n");
            }

            for (RessourceBlock ressourceBlock : ressource.getRessourceBlocks()) {
                fileContent.append(ressourceBlock.getUUID() + "," + ressourceBlock.getHashSum() + "\n");
            }
        } catch (IOException e) {
            Logger.error("Error while writing to ressource File: " + ressourceFile.toPath());
            Logger.exception(e);
            System.exit(1);
        }

        Logger.info("Ressource file initialized!");
    }
}
