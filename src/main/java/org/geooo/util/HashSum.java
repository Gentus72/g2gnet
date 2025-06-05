package org.geooo.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public abstract class HashSum {
    public static String fromBytes(byte[] data) {
        String hashSum = "";

        if (data == null) {
            Logger.error("Error while generating hashsum from bytes: No data supplied!");
            System.exit(1);
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hashSumBytes = digest.digest(data);

            hashSum = HexFormat.of().formatHex(hashSumBytes); // represent hashSum as String (in this case as hex)
        } catch (NoSuchAlgorithmException e) {
            Logger.error("Error while generating hashsum from bytes!");
            Logger.exception(e);
        }

        return hashSum;
    }

    public static String fromFile(File source) {
        String hashSum = "";

        try {
            byte[] allDataBytes = Files.readAllBytes(source.toPath());

            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hashSumBytes = digest.digest(allDataBytes);

            hashSum = HexFormat.of().formatHex(hashSumBytes);
        } catch (IOException | NoSuchAlgorithmException e) {
            Logger.error("Error while generating hashsum from file!");
            Logger.exception(e);
        }

        return hashSum;
    }
}
