package edu.oakland.testmavenagain;

import java.util.ArrayList;
import java.util.HashMap;

import javax.persistence.Entity;

@Entity
public class MyResult {
    private String value;
    private HashMap<String, String> gameState;
	private ArrayList<String> players;

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

	/**
	 * @return the players
	 */
	public ArrayList<String> getPlayers() {
		return players;
	}

	/**
	 * @param players the players to set
	 */
	public void setPlayers(ArrayList<String> players) {
		this.players = players;
	}
}
