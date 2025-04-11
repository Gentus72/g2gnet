package org.geooo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

import org.geooo.util.Logger;

public class ServerFile {

    public static final String SERVERFILE_NAME = "serverfile.g2gsrv";

    private static ServerFile serverFile;
    private final File file;

    private ArrayList<EmptyRessource> ressources;

    public ServerFile(String filePath) {
        this.file = new File(filePath);

        try {
            if (file.createNewFile()) {
                Logger.warn("No serverfile found, creating a new one! Is this the first start of this server?");
            }
        } catch (IOException e) {
            Logger.error("Error while instantiating serverfile!");
            Logger.exception(e);
        }
    }

    public static ServerFile getInstance() {
        if (serverFile == null) {
            serverFile = new ServerFile(SERVERFILE_NAME);

            reloadRessources();
        }

        return serverFile;
    }

    public static void initializeServerFile() {
        try {
            File thisServerFile = getInstance().file;

            if (!thisServerFile.createNewFile()) {
                Logger.warn("Serverfile detected! Initializer-function will re-initialize it to be sure!");

                Files.delete(thisServerFile.toPath());
                Files.createFile(thisServerFile.toPath());
            }
        } catch (IOException e) {
            Logger.error("Error while creating / detecting serverFile!");
            Logger.exception(e);
        }

        try (BufferedWriter fileContent = new BufferedWriter(new FileWriter(getInstance().file.getName(), true))) {
            File ressourcesDirectory = new File(Ressource.PARENT_DIRECTORY);
            File[] ressources = ressourcesDirectory.listFiles(File::isDirectory);

            fileContent.append("UUID, title, blocksAmount, ressourceFile, hashSum:\n");

            for (File ressourceDirectory : ressources) {
                try {
                    File ressourceFile = new File(ressourceDirectory.toPath().toString(), "ressourceFile.g2g");
                    EmptyRessource ressource = new Ressource(ressourceFile);

                    fileContent.append(ressource.getUUID() + "," + ressource.getTitle() + "," + ressource.getBlockAmount() + "," + ressourceFile.toPath().toString() + "," + ressource.getTotalHashSum() + "\n");
                } catch (IOException e) {
                    Logger.error("Error while parsing a ressource in the ressource directory!");
                    Logger.exception(e);
                }
            }
        } catch (IOException e) {
            Logger.error("Error while writing to log File!");
            Logger.exception(e);
            System.exit(1);
        }
    }

    public static void reloadRessources() {
        try {
            File ressourcesDirectory = new File(Ressource.PARENT_DIRECTORY);
            File[] ressourceDirectories = ressourcesDirectory.listFiles(File::isDirectory);

            if (getInstance().ressources != null) {
                getInstance().ressources = null;
            }

            getInstance().ressources = new ArrayList<>();

            for (File ressourceDirectory : ressourceDirectories) {
                File ressourceFile = new File(ressourceDirectory.toPath().toString(), "ressourceFile.g2g");

                getInstance().ressources.add(new EmptyRessource(ressourceFile));
            }
        } catch (Exception e) {
            Logger.error("Error while parsing ressources to serverfile instance!");
            Logger.exception(e);
        }
    }

    public static File getServerFile() {
        return getInstance().file;
    }

    public static boolean doesRessourceExist(String uuid) {
        for (EmptyRessource ressource : getInstance().ressources) {
            if (ressource.getUUID().equals(uuid)) {
                return true;
            }
        }

        return false;
    }

    public static EmptyRessource getEmptyRessource(String uuid) {
        // assumes doesRessourceExist has been called successfully

        for (EmptyRessource ressource : getInstance().ressources) {
            if (ressource.uuid.equals(uuid)) {
                return ressource;
            }
        }

        return null;
    }
}
