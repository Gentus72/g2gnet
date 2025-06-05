package org.geooo.dto;

public class RessourceBlockDTO {
    public String uuid;
    public String location;
    public String hashSum;
    public int sequenceID;

    public String getUUID() {
        return this.uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getHashSum() {
        return this.hashSum;
    }

    public void setHashSum(String hashSum) {
        this.hashSum = hashSum;
    }

    public int getSequenceID() {
        return this.sequenceID;
    }

    public void setSequenceID(int sequenceID) {
        this.sequenceID = sequenceID;
    }

}
