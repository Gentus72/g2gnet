package org.geooo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import org.geooo.dto.RessourceBlockDTO;
import org.geooo.dto.RessourceDTO;
import org.geooo.metadata.RessourceFile;
import org.geooo.util.ChunkedFileReader;
import org.geooo.util.G2GUtil;
import org.geooo.util.HashSum;
import org.geooo.util.Logger;

public class Ressource extends RessourceDTO {

    public static final long BLOCK_SIZE = 16 * 1024 * 1024; // 16 MiB

    private File sourceFile;
    private File parentDirectory;
    RessourceFile ressourceFile;
    HashMap<RessourceBlockDTO, String> blockLocations;

    public Ressource(File sourceFile, String title) {
        super();

        this.sourceFile = sourceFile;
        this.title = title;
    }

    // TODO implement tmpfile
    // TODO implement parentdirectory
    public static Ressource disassemble(String parentDirectory, File sourceFile, String title, String clientPublicKey) {
        if (doesLocalRessourceExist(parentDirectory, sourceFile)) {
            Logger.error("Ressource hashsum has been found locally!");
            return null;
        }

        Ressource ressource = new Ressource(sourceFile, title);
        Logger.info("Generating Ressource with title: " + title);

        ressource.sourceFile = sourceFile;

        // UUID generieren
        ressource.uuid = UUID.randomUUID().toString().replace("-", ""); // dash-less uuid
        Logger.info("UUID of Ressource is: " + ressource.uuid);
        File ressourceDirectory = new File(parentDirectory + ressource.uuid);

        try {
            Files.createDirectory(Path.of(ressourceDirectory.getPath()));
        } catch (IOException e) {
            Logger.error("Error while creating ressource directory!");
            Logger.exception(e);
        }

        try {
            long sourceFileBytesAmount = Files.size(Path.of(sourceFile.getPath()));

            ressource.totalHashSum = HashSum.fromFile(sourceFile);
            ressource.blockAmount = (int) Math.ceil((double) sourceFileBytesAmount / BLOCK_SIZE);

            if (ressource.blockAmount == 0) {
                Logger.error("Block amount is zero while creating ressource! This cannot be intended!");
            }

            // Blöcke erstellen
            Logger.info("Generating " + ressource.blockAmount + " blocks!");
            ressource.blockLocations = new HashMap<>();

            // chunked reader to not load everything into memory
            try (ChunkedFileReader chunkedReader = new ChunkedFileReader(sourceFile.getPath(), (int) BLOCK_SIZE)) {
                for (int i = 0; i < ressource.blockAmount; i++) {
                    String blockUUID = G2GUtil.getRandomUUID();

                    RessourceBlock newBlock = new RessourceBlock(blockUUID);

                    // read next 16 MiB and write to Block
                    newBlock.setData(chunkedReader.readNextChunk());
                    newBlock.setParentDirectory(ressourceDirectory.getPath());
                    newBlock.setSequenceID(i);
                    newBlock.setHashSum(HashSum.fromBytes(newBlock.getData()));
                    newBlock.writeToFile();
                    ressource.blockLocations.put(newBlock, "<loc>");
                }
            }

            Logger.success(ressource.blockAmount + " blocks generated!");
        } catch (IOException e) {
            Logger.error("Error while getting Data from sourceFile: " + ressource.sourceFile.toPath().toString());
            Logger.exception(e);
        }

        RessourceFile ressourceFile = new RessourceFile(String.format("%s/%s.g2g", ressourceDirectory.getPath(), ressource.getUUID()));
        ressourceFile.writeToFile(ressource, clientPublicKey);

        return ressource;
    }

    public static Ressource reassemble(String parentDirectory, String uuid, File outputFile) {
        // if (!doesLocalRessourceExist(parentDirectory, outputFile)) {
        //     Logger.error("Ressource doesn't exist! Can't reassemble!");
        //     return null;
        // }

        RessourceFile ressourceFile = new RessourceFile(String.format("%s%s/%s.g2g", parentDirectory, uuid, uuid));
        File[] blockFiles = new File(parentDirectory + uuid).listFiles((dir, name) -> name.endsWith(".g2gblock"));

        if (ressourceFile.getFile() == null || blockFiles == null || blockFiles.length == 0) {
            Logger.error("Some files supplied for reassembly didn't exist!");
        }

        ArrayList<RessourceBlockDTO> blocks = ressourceFile.getBlocks();
        HashMap<String, String> configContent = ressourceFile.getConfigContent();

        if (Integer.parseInt(configContent.get("AmountOfBlocks")) != blocks.size() || blockFiles.length != blocks.size()) {
            Logger.error("Mismatch between length of blockFile-array and blockamount from ressourcefile! Continuing...");
        }

        try (FileOutputStream writer = new FileOutputStream(outputFile)) {
            outputFile.createNewFile();
            Logger.info(String.format("Reassembling %d blocks!", blocks.size()));

            for (int i = 0; i < blocks.size(); i++) {
                for (int j = 0; j < blocks.size(); j++) {
                    RessourceBlockDTO block = blocks.get(j);
                    if (block.getSequenceID() == i) {
                        writer.write(Files.readAllBytes(Path.of(parentDirectory + uuid + "/" + block.getUUID() + ".g2gblock")));
                        Logger.success(String.format("Added block %d to file!", block.getSequenceID()));
                    }
                }
            }
        } catch (IOException e) {
            Logger.error("Error while writing blocks to destinationfile");
            Logger.exception(e);
        }

        if (!configContent.get("HashSum").equals(HashSum.fromFile(outputFile))) {
            Logger.warn("HashSum mismatch between ressourcefile and reassembled file!");
        } else {
            Logger.success("Hashsums match!");
        }

        Logger.success("Successfully reassembled sourcefile!");
        Ressource ressource = new Ressource(outputFile, configContent.get("Title"));

        return ressource;
    }

    public static boolean doesLocalRessourceExist(String parentDirectory, File sourceFile) {
        String sourceHashSum = HashSum.fromFile(sourceFile);

        File[] ressourceDirectories = new File(parentDirectory).listFiles((dir, name) -> new File(dir, name).isDirectory());

        if (ressourceDirectories == null || ressourceDirectories.length == 0) {
            return false;
        }

        Logger.info(Arrays.toString(ressourceDirectories));

        for (File ressourceDirectory : ressourceDirectories) {
            RessourceFile ressourceFile = new RessourceFile(parentDirectory + ressourceDirectory.getName() + "/" + ressourceDirectory.getName() + ".g2g");
            String ressourceHashSum = ressourceFile.getConfigContent().get("TotalHashSum");

            if (sourceHashSum.equals(ressourceHashSum)) {
                Logger.info(String.format("source: %s", sourceHashSum));
                Logger.info(String.format("resfile: %s", ressourceHashSum));
                return true;
            }
        }

        return false;
    }

    public File getSourceFile() {
        return sourceFile;
    }

    @Override
    public int getBlockAmount() {
        return this.blockAmount;
    }

    public File getParentDirectory() {
        return this.parentDirectory;
    }

    public void setParentDirectory(File parentDirectory) {
        this.parentDirectory = parentDirectory;
    }

    public RessourceFile getRessourceFile() {
        return this.ressourceFile;
    }

    public void setRessourceFile(RessourceFile ressourceFile) {
        this.ressourceFile = ressourceFile;
    }

    public void setBlockLocations(HashMap<RessourceBlockDTO, String> blockLocations) {
        this.blockLocations = blockLocations;
    }

    public HashMap<RessourceBlockDTO, String> getBlockLocations() {
        return this.blockLocations;
    }
}
