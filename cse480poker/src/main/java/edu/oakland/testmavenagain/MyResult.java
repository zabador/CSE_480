package edu.oakland.testmavenagain;

import java.util.HashMap;

import javax.persistence.Entity;

@Entity
public class MyResult {
    private String value;
    private HashMap<String, String> gameState;

    public MyResult(String value) {
        this.value = value;
    }

    public MyResult() {
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the gameState
     */
    public HashMap<String, String> getGameState() {
        return gameState;
    }

    /**
     * @param gameState the gameState to set
     */
    public void setGameState(HashMap<String, String> gameState) {
        this.gameState = gameState;
    }
}
