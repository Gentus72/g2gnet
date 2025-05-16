package org.geooo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import org.geooo.util.HashSum;
import org.geooo.util.Logger;

public class Ressource extends EmptyRessource {

    public static final int BLOCK_SIZE = 16 * 1024 * 1024; // 16 MiB
    public static final String PARENT_DIRECTORY = "res/"; // directory for all ressources

    private ArrayList<RessourceBlock> ressourceBlocks; // block amount equals ressourceBlocks.length
    private File sourceFile;

    /*
     * Mit diesem Konstruktor wird ein Ordner der Ressource mit Ressource-Blöcken und der Ressource-Datei erstellt.
     * Die Blöcke enthalten Teile der Datei.
     * Die Ressource-Datei enthält wichtige Metadaten, die auf Anfrage an den Client übergeben werden.
     */
    public Ressource(File sourceFile, String title) {
        super(title);

        Logger.info("Generating Ressource with title: " + title);

        this.sourceFile = sourceFile;

        // UUID generieren
        this.uuid = UUID.randomUUID().toString().replace("-", ""); // dash-less uuid
        Logger.info("UUID of Ressource is: " + this.uuid);

        // Ressource-Ordner erstellen
        new File(PARENT_DIRECTORY + this.uuid).mkdir();

        try {
            // TODO maybe not load everything into memory
            byte[] allDataBytes = Files.readAllBytes(this.sourceFile.toPath());
            Logger.info("Creating ressource from source with " + allDataBytes.length + " bytes!");
            int ressourceBlockAmount = (int) Math.ceil((double) allDataBytes.length / BLOCK_SIZE);

            this.totalHashSum = HashSum.fromFile(sourceFile);
            this.blockAmount = ressourceBlockAmount;

            this.ressourceBlocks = new ArrayList<>();

            if (blockAmount == 0) {
                Logger.error("Block amount is zero while creating ressource! This cannot be intended!");
            }

            // Blöcke erstellen
            Logger.info("Generating " + ressourceBlockAmount + " blocks!");
            for (int i = 0; i < ressourceBlockAmount; i++) {
                String blockUUID = UUID.randomUUID().toString().replace("-", "");

                RessourceBlock newBlock = new RessourceBlock(blockUUID, PARENT_DIRECTORY + this.uuid);

                newBlock.setData(Arrays.copyOfRange(allDataBytes, i * BLOCK_SIZE, Math.min((i + 1) * BLOCK_SIZE, allDataBytes.length)));
                newBlock.setHashSum(HashSum.fromBytes(newBlock.getData()));
                saveBlockHelper(newBlock);

                ressourceBlocks.add(newBlock);
            }

            Logger.info(ressourceBlockAmount + " blocks generated!");

            new RessourceFile(this);
            ServerFile.reloadRessources();
        } catch (IOException e) {
            Logger.error("Error while getting Data from sourceFile: " + this.sourceFile.toPath().toString());
            Logger.exception(e);
        }

        // move sourceFile to ressourceDirectory
        this.sourceFile.renameTo(new File(PARENT_DIRECTORY + this.uuid, this.sourceFile.getName()));
    }

    private void saveBlockHelper(RessourceBlock block) {
        block.writeToFile();
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
            this.ressourceBlocks = new ArrayList<>();

            while (true) {
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

                ressourceBlocks.add(newBlock);
            }

            if (this.blockAmount != ressourceBlocks.size()) {
                Logger.error("Ressource block amount and actual size of block list dont match!");
                Logger.error("blockAmount = " + this.blockAmount + ", ressourceBlocks.size = " + this.ressourceBlocks.size());
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

    /**
     * @return File return the sourceFile
     */
    public File getSourceFile() {
        return sourceFile;
    }

    public ArrayList<RessourceBlock> getRessourceBlocks() {
        return this.ressourceBlocks;
    }

    @Override
    public int getBlockAmount() {
        return this.blockAmount;
    }
}
