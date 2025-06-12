package org.geooo.dto;

/*
 * Helfer-Klasse für die Ressource-Klasse.
 * Enthält hauptsächlich Getter- und Setter-Methoden.
 */
public class RessourceDTO {

    public String title;
    public String parentDirectory;
    public String uuid;
    public String totalHashSum; // hash sum of the whole uploaded file
    public int blockAmount;

    public RessourceDTO() {
        // do nothing;
    }

    public RessourceDTO(String uuid, String title, int blockAmount) {
        this.title = title;
        this.uuid = uuid;
        this.blockAmount = blockAmount;
    }

    /**
     * @return String return the totalHashSum
     */
    public String getTotalHashSum() {
        return this.totalHashSum;
    }

    /**
     * @return String return the title
     */
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return String return the uuid
     */
    public String getUUID() {
        return uuid;
    }

    public int getBlockAmount() {
        return this.blockAmount;
    }

    public void setBlockAmount(int blockAmount) {
        this.blockAmount = blockAmount;
    }
}
