package org.geooo.dto;

/*
 * Helfer-Klasse für die Ressource-Klasse.
 * Enthält hauptsächlich Getter- und Setter-Methoden.
 */
public class RessourceDTO {

    public String title;
    public String uuid;
    public String totalHashSum; // Prüfsumme der gesamten Originaldatei
    public int blockAmount;

    public RessourceDTO() {
        
    }

    /**
     * @param uuid
     * @param title
     * @param blockAmount
     */
    public RessourceDTO(String uuid, String title, int blockAmount) {
        this.title = title;
        this.uuid = uuid;
        this.blockAmount = blockAmount;
    }

    /**
     * @return Gibt die Prüfsumme aller Daten der Originaldatei aus
     */
    public String getTotalHashSum() {
        return this.totalHashSum;
    }

    /**
     * @return Gibt den Titel der Ressource aus
     */
    public String getTitle() {
        return title;
    }

    /**
     * Setzt den Titel der Ressource
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return Gibt die UUID der Ressource aus
     */
    public String getUUID() {
        return uuid;
    }

    /**
     * @return Gibt die Anzahl an Blöcken der Ressource aus
     */
    public int getBlockAmount() {
        return this.blockAmount;
    }

    /**
     * Setzt die Anzahl an Blöcken der Ressource
     * @param blockAmount
     */
    public void setBlockAmount(int blockAmount) {
        this.blockAmount = blockAmount;
    }
}
