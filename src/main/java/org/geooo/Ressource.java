package org.geooo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;

import org.geooo.dto.RessourceDTO;
import org.geooo.util.HashSum;
import org.geooo.util.Logger;

public class Ressource extends RessourceDTO {

    public static final int BLOCK_SIZE = 16 * 1024 * 1024; // 16 MiB
    public static final String PARENT_DIRECTORY = "res/"; // directory for all ressources

    private File sourceFile;
    private RessourceDistributionStrategy distributionStrategy; // TODO implement
    private RessourceBlock[] ressourceBlocks; // block amount equals ressourceBlocks.length

    /*
     * Mit diesem Konstruktor wird ein Ordner der Ressource mit Ressource-Blöcken und der Ressource-Datei erstellt.
     * Die Blöcke enthalten Teile der Datei.
     * Die Ressource-Datei enthält wichtige Metadaten, die auf Anfrage an den Client übergeben werden.
     */
    public Ressource(File sourceFile, String title, RessourceDistributionStrategy distributionStrategy) {
        super(title);

        Logger.info("Generating Ressource with title: " + title);

        this.sourceFile = sourceFile;
        this.distributionStrategy = distributionStrategy;

        // UUID generieren
        this.uuid = UUID.randomUUID().toString().replace("-", ""); // dash-less uuid
        Logger.info("UUID of Ressource is: " + this.uuid);

        // Ressource-Ordner erstellen
        new File(PARENT_DIRECTORY + this.uuid).mkdir();

        try {
            long sourceFileBytesAmount = Files.size(Path.of(sourceFile.getPath()));
            Logger.info("Creating ressource from source with " + sourceFileBytesAmount + " bytes!");

            this.totalHashSum = HashSum.fromFile(sourceFile);
            this.blockAmount = (int) Math.ceil((double) sourceFileBytesAmount / BLOCK_SIZE);

            this.ressourceBlocks = new RessourceBlock[this.blockAmount];

            if (blockAmount == 0) {
                Logger.error("Block amount is zero while creating ressource! This cannot be intended!");
            }

            // Blöcke erstellen
            Logger.info("Generating " + this.blockAmount + " blocks!");

            for (int i = 0; i < this.blockAmount; i++) {
                String blockUUID = UUID.randomUUID().toString().replace("-", "");

                RessourceBlock newBlock = new RessourceBlock(blockUUID, PARENT_DIRECTORY + this.uuid);


                
                newBlock.setHashSum(HashSum.fromBytes(newBlock.getData()));
                newBlock.writeToFile();

                ressourceBlocks[i] = newBlock;
            }

            Logger.info(this.blockAmount + " blocks generated!");

            new RessourceFile(this);
            ServerFile.reloadRessources();
        } catch (IOException e) {
            Logger.error("Error while getting Data from sourceFile: " + this.sourceFile.toPath().toString());
            Logger.exception(e);
        }

        // move sourceFile to ressourceDirectory
        this.sourceFile.renameTo(new File(PARENT_DIRECTORY + this.uuid, this.sourceFile.getName()));
    }

    /*
     * Dieser Konstuktor dient dem Erstellen von virtuellen Ressourcen, die bereits im Dateisystem vorhanden sind.
     */
    public Ressource(File G2GFile) {
        super(G2GFile);

        try (BufferedReader reader = new BufferedReader(new FileReader(G2GFile))) {
            HashMap<String, String> values = new HashMap<>();

            // Ressource-Informationen lesen
            while (true) {
                String line = reader.readLine();

                if (line == null || line.equals("blocks (uuid, hash):")) {
                    break;
                }

                String[] keyValuePair = line.split(":");
                values.put(keyValuePair[0], keyValuePair[1].strip());
            }

            this.sourceFile = new File(values.get("sourceFile"));
            this.blockAmount = Integer.parseInt(values.get("total_blocks"));
            this.ressourceBlocks = new RessourceBlock[this.blockAmount];

            for (int i = 0; i < this.blockAmount; i++) {
                String line = reader.readLine();

                if (line == null) {
                    break;
                }

                String[] uuidHashsumPair = line.split(",");

                RessourceBlock newBlock = new RessourceBlock(uuidHashsumPair[0], PARENT_DIRECTORY + this.uuid);

                // TODO maybe not load everything into memory...
                newBlock.setData(Files.readAllBytes(new File(PARENT_DIRECTORY + this.uuid, newBlock.getUUID() + ".g2gblock").toPath()));

                newBlock.setHashSum(HashSum.fromBytes(newBlock.getData()));

                if (!newBlock.getHashSum().equals(uuidHashsumPair[1])) {
                    Logger.error("Ressource block hashsum doesn't match hashsum from ressource file. BlockUUID: " + newBlock.getUUID());
                }

                ressourceBlocks[i] = newBlock;
            }
        } catch (IOException e) {
            Logger.error("Error while parsing existing G2GFile to Ressource object!");
            Logger.exception(e);
        }

        ServerFile.reloadRessources();
    }

    /*
     * Diese Funktion dient dem Wiederherstellen der Originaldatei anhand der Blöcke
     * TODO maybe change to from file
     */
    public void assembleSourceFile(File destinationFile) {
        try (FileOutputStream outputStream = new FileOutputStream(destinationFile)) {
            for (RessourceBlock block : this.ressourceBlocks) {
                outputStream.write(block.getData());
            }
        } catch (IOException e) {
            Logger.error("Error while assembling blocks to source file!");
            Logger.exception(e);
        }
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public RessourceBlock[] getRessourceBlocks() {
        return this.ressourceBlocks;
    }

    @Override
    public int getBlockAmount() {
        return this.blockAmount;
    }
}
