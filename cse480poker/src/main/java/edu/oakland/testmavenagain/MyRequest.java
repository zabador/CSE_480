package edu.oakland.testmavenagain;

import javax.persistence.Entity;

@Entity
public class MyRequest {
    private String regId;
    private String GCMmessage;
    private int bet;

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

    /**
     * @return the bet
     */
    public int getBet() {
        return bet;
    }

    /**
     * @param bet the bet to set
     */
    public void setBet(int bet) {
        this.bet = bet;
    }
}

