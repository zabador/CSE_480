package edu.oakland.testmavenagain;

import javax.persistence.Entity;

@Entity
public class MyRequest {
    private String regId;
    private String GCMmessage;
    private int bet;
    private String user;
    private boolean firstRound;

    public MyRequest() {}

    public MyRequest(String GCMmessage) {
        this.GCMmessage = GCMmessage;
    }

    public MyRequest(String user, String GCMmessage) {
        this.GCMmessage = GCMmessage;
        this.user = user;
    }

    /**
     * @return the user
     */
    public String getUser() {;
        return user;
    }

    /**
     * @param regId the regId to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return the firstRound
     */
    public boolean isFirstRound() {
        return firstRound;
    }

    /**
     * @param firstRound the firstRound to set
     */
    public void setFirstRound(boolean firstRound) {
        this.firstRound = firstRound;
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

