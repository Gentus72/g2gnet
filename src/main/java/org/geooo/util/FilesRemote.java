package org.geooo.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public abstract class FilesRemote {

    /*
     * Inspiriert von https://www.geeksforgeeks.org/transfer-the-file-client-socket-to-server-socket-in-java/
     * Letzter Zugriff: 28.03.2025, 12:21 Uhr
     */
    public static void sendFile(File file, DataOutputStream outputStream) {
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
     * Inspiriert von https://www.geeksforgeeks.org/transfer-the-file-client-socket-to-server-socket-in-java/
     * Letzter Zugriff: 28.03.2025, 12:21 Uhr
     */
    public static void receiveFile(String fileName, DataInputStream inputStream) {
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

    /*
     * Inspiriert von https://www.geeksforgeeks.org/transfer-the-file-client-socket-to-server-socket-in-java/
     * Letzter Zugriff: 28.03.2025, 12:21 Uhr
     */
    public static void receiveFile(File file, DataInputStream inputStream) {
        try {
            int bytes = 0;
            try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
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
}
