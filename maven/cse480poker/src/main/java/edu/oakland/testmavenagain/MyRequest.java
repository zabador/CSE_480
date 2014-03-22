package edu.oakland.testmavenagain;

import javax.persistence.Entity;

@Entity
public class MyRequest {
    private String regId;
    private String GCMmessage;

    public MyRequest() {}

    public MyRequest(String GCMmessage) {
        this.GCMmessage = GCMmessage;
    }
    /**
     * @return the regId
     */
    public String getRegId() {;
        return regId;
    }

    /**
     * @param regId the regId to set
     */
    public void setRegId(String regId) {
        this.regId = regId;
    }

    /**
     * @return the gCMmessage
     */
    public String getGCMmessage() {
        return GCMmessage;
    }

    /**
     * @param gCMmessage the gCMmessage to set
     */
    public void setGCMmessage(String gCMmessage) {
        GCMmessage = gCMmessage;
    }
}

