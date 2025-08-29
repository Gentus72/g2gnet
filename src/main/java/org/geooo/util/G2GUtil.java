package org.geooo.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public abstract class G2GUtil {

    /**
     * @return Gibt eine einzigartige UUID ohne Bindestriche aus
     */
    public static String getRandomUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * @return Gibt die lokale IPv4-Adresse aus
     */
    public static String getLocalIPv4Address() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            Logger.error("Error while getting local ipv4address!");
            Logger.exception(e);
            System.exit(1);

            return null;
        }
    }

    /*
     * Inspiriert von
     * https://www.geeksforgeeks.org/transfer-the-file-client-socket-to-server-socket-in-java/
     * Letzter Zugriff: 28.03.2025, 12:21 Uhr
     */
    /**
     * Sendet eine Datei via Java Sockets
     * @param fileName
     * @param outputStream
     */
    public static void sendFileRemote(String fileName, DataOutputStream outputStream) {
        try (FileInputStream fileInputStream = new FileInputStream(fileName)) {
            int bytes;

            outputStream.writeLong(new File(fileName).length());
            byte[] buffer = new byte[4 * 1024];

            while ((bytes = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytes);
                outputStream.flush();
            }
        } catch (IOException e) {
            Logger.error("Error while sending file!");
            Logger.exception(e);
        }
    }

    /*
     * Inspiriert von
     * https://www.geeksforgeeks.org/transfer-the-file-client-socket-to-server-socket-in-java/
     * Letzter Zugriff: 28.03.2025, 12:21 Uhr
     */
    /**
     * Sendet eine Datei via Java Sockets
     * @param file
     * @param outputStream
     */
    public static void sendFileRemote(File file, DataOutputStream outputStream) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            int bytes;

            outputStream.writeLong(file.length());
            byte[] buffer = new byte[4 * 1024];

            while ((bytes = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytes);
                outputStream.flush();
            }
        } catch (IOException e) {
            Logger.error("Error while sending file!");
            Logger.exception(e);
        }
    }

    /*
     * Inspiriert von
     * https://www.geeksforgeeks.org/transfer-the-file-client-socket-to-server-socket-in-java/
     * Letzter Zugriff: 28.03.2025, 12:21 Uhr
     */
    /**
     * Empfängt eine Datei via Java Sockets
     * @param fileName
     * @param inputStream
     */
    public static void receiveFileRemote(String fileName, DataInputStream inputStream) {
        try {
            int bytes = 0;
            try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
                long size = inputStream.readLong(); // read file size
                byte[] buffer = new byte[4 * 1024];

                while (size > 0 && (bytes = inputStream.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                    fileOutputStream.write(buffer, 0, bytes);
                    size -= bytes; // read upto file size
                }

            }
        } catch (IOException e) {
            Logger.error("Error while receiving file!");
            Logger.exception(e);
        }
    }

    /**
     * Generiert die Prüfsumme aus eine Bytearray.
     * @param data
     * @return Die Prüfsumme der Daten
     */
    public static String getHashsumFromBytes(byte[] data) {
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

    /**
     * @param source
     * @return die Prüfsumme einer Datei (aller Daten)
     */
    public static String getHashsumFromFile(File source) {
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

    /**
     * Verschlüsselt Daten mithilfe eines privaten Schlüssels um authentizität zu gewährleisten
     * @param plainText
     * @param privateKey
     * @return Die verschlüsselten Daten
     */
    public static String encryptWithPrivateKey(String plainText, PrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);

            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            Logger.error("Error while encrypting message with privatekey!");
            Logger.exception(e);
        }

        return null;
    }

    /**
     * Entschlüsselt Daten mithilfe eines öffentlichen Schlüssels um authentizität zu gewährleisten
     * @param encryptedText
     * @param publicKey
     * @return die entschlüsselten Daten
     */
    public static String decryptWithPublicKey(String encryptedText, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, publicKey);

            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (InvalidKeyException e) {
            // do nothing, the decryption failed on purpose - the key didn't work!
            return null;
        } catch (NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            Logger.error("Error while decrypting message with public key!");
            Logger.exception(e);
        }

        return null;
    }
}
