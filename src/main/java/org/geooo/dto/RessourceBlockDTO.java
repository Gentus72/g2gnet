package org.geooo.dto;

/*
 * Helfer-Klasse für die RessourceBlock-Klasse.
 * Enthält hauptsächlich Getter- und Setter-Methoden.
 */
public class RessourceBlockDTO {
    public String uuid;
    public String location;
    public String hashSum;
    public int sequenceID;

    /**
     * @return Gibt die UUID des Blocks aus
     */
    public String getUUID() {
        return this.uuid;
    }

    /**
     * Setzt die UUID des Blocks
     * @param uuid
     */
    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    /**
     * @return Gibt die IPv4-Adresse des Server aus, auf dem der Block gespeichert ist / werden soll
     */
    public String getLocation() {
        return this.location;
    }

    /**
     * Setzt die IPv4-Adresse des Servers, auf dem der Block gespeichert ist / werden soll
     * @param location
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * @return Gibt die Prüfsumme der Daten des Blocks aus
     */
    public String getHashSum() {
        return this.hashSum;
    }

    /**
     * Setzt die Prüfsumme der Daten des Blocks
     * @param hashSum
     */
    public void setHashSum(String hashSum) {
        this.hashSum = hashSum;
    }

    /**
     * @return Gibt die SequenceID, also die Stelle in der Reihenfolge der Blöcke aus
     */
    public int getSequenceID() {
        return this.sequenceID;
    }

    /**
     * Setzt die SequenceID, also die Stelle in der Reihenfolge der Blöcke
     * @param sequenceID
     */
    public void setSequenceID(int sequenceID) {
        this.sequenceID = sequenceID;
    }

}
