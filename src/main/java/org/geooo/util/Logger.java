package org.geooo.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;

public class Logger {

    /*
     * Statisch kodierte Farben für farbige Befehlszeilenausgabe
     */
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_WHITE = "\u001B[37m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_GREEN = "\033[0;32m";
    public static final String ANSI_RESET = "\u001B[0m";

    /*
     * Singleton-Objekt und Logdatei-Objekt
     */
    private static Logger logger;
    private File logFile;

    /*
     * Konstuktor des Logger-Objekts
     * Initialisiert auch die Logdatei.
     */
    public Logger() {
        try {
            this.logFile = new File("logFile.txt");
            this.logFile.createNewFile();
        } catch (IOException e) {
            Logger.error("Error while creating or detecting database file!");
            System.exit(1);
        }
    }

    /*
     * Die getInstance-Methode als Referenz für die Singleton-Instanz des Loggers
     */
    public static Logger getInstance() {
        if (logger == null) {
            logger = new Logger();

            logToFile("--------------------------------NEW APPLICATION START--------------------------------");
        }

        return logger;
    }

    /*
     * Einzelne Methoden für einfacherere / angenehmere Benutzung
     * 
     * Jede einzelne der folgenden vier Methoden ändert nur die Farbe der
     * ausgegebenen Zeile im Terminal.
     * Die exception-Methode und die error-Methode machen Errorhandling einfacher
     * und übersichtlicher.
     */
    public static void info(String message) {
        log(LoggingType.INFO, message);
    }

    public static void warn(String message) {
        log(LoggingType.WARNING, message);
    }

    public static void error(String message) {
        log(LoggingType.ERROR, message);
        log(LoggingType.ERROR, "originated from: " + Thread.currentThread().getStackTrace()[2].getClassName());
    }

    public static void exception(Exception e) {
        log(LoggingType.ERROR, "Message / Cause: " + e.getMessage());
        log(LoggingType.ERROR, "Exception: " + e.getClass().getName());
        log(LoggingType.ERROR, "----------------------Full stack trace----------------------");
        log(LoggingType.ERROR, Arrays.toString(e.getStackTrace()));
    }

    /*
     * Die wichtigste Methode dieses Objekts. Sie gibt Nachrichten auf der
     * Befehlszeile und in der Logdatei mit Zeitstempel und
     * Nachrichtenklassifizierung aus.
     */
    private static void log(LoggingType loggingType, String message) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        String currentDateTimeFormatted = currentDateTime.toString().replace("T", " ").split("\\.")[0];
        String output = "";

        // Die Nachricht wird entsprechend schön formattiert.
        switch (loggingType) {
            case LoggingType.INFO -> output = ANSI_WHITE + "[" + currentDateTimeFormatted + " INFO]: " + message;
            case LoggingType.WARNING -> output = ANSI_YELLOW + "[" + currentDateTimeFormatted + " WARNING]: " + message;
            case LoggingType.ERROR -> output = ANSI_RED + "[" + currentDateTimeFormatted + " ERROR]: " + message;
        }

        // Die Nachricht wird auf der Befehlszeile ausgegeben.
        System.out.println(output + ANSI_RESET);

        // Die Nachricht wird in die Logdatei geschrieben. Dabei wird der Farbcode
        // wieder herausgeschnitten.
        logToFile(output.substring(5));
    }

    /*
     * Eine Methode um nur in die Logdatei und nicht auf die Befehlszeile zu
     * schreiben.
     */
    public static void logToFile(String message) {
        try (BufferedWriter fileContent = new BufferedWriter(new FileWriter(getInstance().logFile.getName(), true))) {
            fileContent.append(message + "\n");
        } catch (IOException e) {
            Logger.error("Error while writing to log File!");
            Logger.exception(e);
            System.exit(1);
        }
    }
}
