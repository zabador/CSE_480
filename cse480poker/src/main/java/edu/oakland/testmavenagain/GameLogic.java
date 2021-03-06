package edu.oakland.testmavenagain;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

import java.util.logging.Logger;
import java.util.ArrayList;

public class GameLogic {

    private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    private static final Logger log = Logger.getLogger(MyEndpoint.class.getName());

    private final String PLACEBET = "3";
    private final String HANDOVER = "7";
    private final String GAMEOVER = "8";

    // game state variable
    private int currentPlayer;
    private int numberOfPlayers;
    private int highestBet;
    private int pot;
    private boolean firstBets;
    private boolean flopBets;
    private boolean turnBets;
    private boolean riverBets;
    private String strTurn;
    private String strFlop;
    private String strRiver;

    private Entity game = null;
    private Entity player = null;

    private Hand flop;
    private Hand turn;
    private Hand river;

    // player variables
    private ArrayList<String> playersList;
    private String regid;
    private boolean fold;
    private int tokens;

    private String handCards;

    public String test() {
        return "hope this works";
    }

    /**
     * Method will start the game and create a new table to store
     * game state it
     */
    public void startGame(boolean firstRound) {

        populateCards(firstRound);
        setUpNewGame();

        saveGameState();

        MyEndpoint endpoint = new MyEndpoint();
        endpoint.sendNotification(new MyRequest(getCurrentPlayer(1), PLACEBET));

    }

    private void setUpNewGame() {

        game = new Entity("GameState", "currentGame");
        currentPlayer = 1;
        numberOfPlayers = playersList.size();
        highestBet = 0;
        pot = 0;
        firstBets = true;
        flopBets = false;
        turnBets = false;
        riverBets = false;
        strTurn = turn.toString();
        strFlop = flop.toString();
        strRiver = river.toString();
    }

    private void populateCards(boolean newGame) {

        
        Deck deck = new Deck();
        deck.shuffle();

        playersList = getAllPlayers();
        Hand[] playerHand = new Hand[playersList.size()];

        // initialize the flop to store in the datastore
        flop = new Hand();
        for (int i = 0; i < 3; i++) {
			flop.addCard(deck.deal());
		}

		// initialize the turn to store in the datastore
		turn = new Hand();
		turn.addCard(deck.deal());

		// initialize the river to store in the datastore
		river = new Hand();
		river.addCard(deck.deal());

		for (int i = 0; i < playersList.size(); i++) {
			playerHand[i] = new Hand();
			playerHand[i].addCard(deck.deal());
			playerHand[i].addCard(deck.deal());
			updateHandOfCards(playersList.get(i), playerHand[i].toString(),
					newGame);
		}

	}

	/**
     *  Method will save the newly created
     *  hand cards for the user
     *
     */
    public void updateHandOfCards(String user, String handCards, boolean newGame) {

        pullPlayerState(user);


            // put fold back to false on new game
            fold = false;

            if(newGame) {
                tokens = 1000;
            }

            if(tokens <= 0) {
                this.handCards = "ic_launcher ic_laucher";
            }
            else {
                this.handCards = handCards;
            }

            savePlayerState();
    }

    public void placeBet() {
        // pull out currentgame state to resave after updateing
        pullGameState();
        boolean keepBetting = true;

        currentPlayer++;

        //increment currentPlayer if currenPlayer has folded
        while (checkIfCurrentPlayerFolded(currentPlayer)) {
            currentPlayer++;
        }

        while (!checkIfCurrentPlayerHasEnoughTokens(currentPlayer,
                    highestBet)) {
            currentPlayer++;
        }

        // go to next round of betting
        if (currentPlayer > numberOfPlayers) {
            currentPlayer = 1; // set back to one for next round of betting

  //          while (!checkIfCurrentPlayerHasEnoughTokens(currentPlayer,
  //                      highestBet)) {
  //              currentPlayer++;
  //          }

            while (checkIfCurrentPlayerFolded(currentPlayer)) {
                currentPlayer++;
            }

            if (!betIsLessThanHigh(currentPlayer, highestBet)) {
                keepBetting = goToNextRound();
            }

        }

        saveGameState();

        MyEndpoint endpoint = new MyEndpoint();

        if (keepBetting) {
            endpoint.sendNotification(new MyRequest(
                        getCurrentPlayer(currentPlayer), PLACEBET)); // save the update data
        }
    }



    private boolean goToNextRound() {
        if (firstBets) {
            firstBets = false; // go to flopBets
            flopBets = true; // start flop bets
        } else if (flopBets) {
            flopBets = false; // go to turnBets
            turnBets = true; // start turn bets
        } else if (turnBets) {
            turnBets = false; // go to turnBets
            riverBets = true; // start turn bets
        } else if (riverBets) {
            riverBets = false; // go to turnBets
            firstBets = true;
            endGame(pot);
            return false;
        }
        return true;

    }

    /**
     * Method will increment player postions in the game so 
     * a new person will start the betting on the new hand
     */
    private void updatePlayerPositions() {
        Query gaeQuery = new Query("Players");
        PreparedQuery pq = datastore.prepare(gaeQuery);
        for (Entity result : pq.asIterable()) {
            boolean tmpFold = (Boolean)result.getProperty("fold");
            String tmpHandCards = (String)result.getProperty("handCards");
            String tmpRegid = (String)result.getProperty("regid");
            int tmpCurrentPosition = ((Long)result.getProperty("currentPosition")).intValue();
            int tmpTokens = ((Long)result.getProperty("tokens")).intValue();

            if (tmpCurrentPosition == pq.countEntities(FetchOptions.Builder.withLimit(100))) {
                tmpCurrentPosition = 1;
            }
            else {
                tmpCurrentPosition++;
            }

            result.setProperty("currentBet", 0);
            result.setProperty("fold", tmpFold);
            result.setProperty("handCards", tmpHandCards);
            result.setProperty("regid", tmpRegid);
            result.setProperty("tokens", tmpTokens);
            result.setProperty("currentPosition", tmpCurrentPosition);
            datastore.put(result);
        }
    }

    private String getCurrentPlayer(int tmpCurrentPlayer) {
        String strPlayer = null;
        Query gaeQuery = new Query("Players");
        PreparedQuery pq = datastore.prepare(gaeQuery);
        for (Entity result : pq.asIterable()){
            String id = (String) result.getKey().getName();
            if (tmpCurrentPlayer == ((Long)result.getProperty("currentPosition")).intValue()) {
                strPlayer = id;
            }
        }
        return strPlayer;
    }

    private boolean checkIfCurrentPlayerFolded(int tmpCurrentPlayer) {
        Query gaeQuery = new Query("Players");
        PreparedQuery pq = datastore.prepare(gaeQuery);
        for (Entity result : pq.asIterable()){
            if (tmpCurrentPlayer == ((Long)result.getProperty("currentPosition")).intValue()) {
                log.severe("checking if player folded " + Integer.toString(tmpCurrentPlayer));
                log.severe("player to be checked if folded " +Integer.toString(tmpCurrentPlayer));
                log.severe("retrun value of fold " +(String.valueOf((Boolean)result.getProperty("fold"))));
                return (Boolean)result.getProperty("fold");
            }
        }
        return false;
    }

    private boolean checkIfCurrentPlayerHasEnoughTokens(int currentPlayer, int highestBet) {
        Query gaeQuery = new Query("Players");
        PreparedQuery pq = datastore.prepare(gaeQuery);
        for (Entity result : pq.asIterable()){
            if (currentPlayer == ((Long)result.getProperty("currentPosition")).intValue()) {
                int currentBet = ((Long)result.getProperty("currentBet")).intValue();
                int tokens = ((Long)result.getProperty("tokens")).intValue();
                int toCall = highestBet - currentBet;
                if (toCall > tokens) {
                    return false;
                }
            }
        }
        return true;
    }


    // Reads all previously stored device tokens from the database
    private ArrayList<String> getAllPlayers(){
        ArrayList<String> players = new ArrayList<String>();
        Query gaeQuery = new Query("Players");
        PreparedQuery pq = datastore.prepare(gaeQuery);
        for (Entity result : pq.asIterable()){
            String id = (String) result.getKey().getName();
            players.add(id);
        }

        return players;
    }

    public void endGame(int pot) {

        int n = numberOfPlayers();
        Hand[] playerHand = new Hand[n];

        String[] flopCards = strFlop.trim().split(" ");

        Query gaeQuery = new Query("Players");
        PreparedQuery pq = datastore.prepare(gaeQuery);
        for (Entity result : pq.asIterable()){
            int i = ((Long)result.getProperty("currentPosition")).intValue()-1;
            String strHandCards = (String)result.getProperty("handCards");

            String[] handCards = strHandCards.trim().split(" ");
            log.severe("first card is " +handCards[0]);
            log.severe("second card is " +handCards[1]);
            log.severe("third card is " +flopCards[0]);
            log.severe("forth card is " +flopCards[1]);
            log.severe("fifth card is " +flopCards[2]);
            log.severe("six card is " +strTurn);
            log.severe("seven card is " +strRiver);
            playerHand[i] = new Hand();
            playerHand[i].addCard(new Card(handCards[0]));
            playerHand[i].addCard(new Card(handCards[1]));
            playerHand[i].addCard(new Card(flopCards[0]));
            playerHand[i].addCard(new Card(flopCards[1]));
            playerHand[i].addCard(new Card(flopCards[2]));
            playerHand[i].addCard(new Card(strTurn));
            playerHand[i].addCard(new Card(strRiver));
        }



        Hand winning_hand = new Hand();
        winning_hand = playerHand[0];
        int winner = 0;
        int tiedWinner = 0;
        for (int i=0; i<n-1; i++){
            int compare = HandEvaluator.compareHands(winning_hand, playerHand[i+1]);
            if (compare == 1){
                log.severe("1 won hand");
                if (checkIfCurrentPlayerFolded(i+1)) {
                    log.severe("1 folded");
                    winner = i+1;
                    winning_hand = playerHand[i+1];
                }
            }

            if (compare == 2){
                log.severe("2 won hand");
                if(!checkIfCurrentPlayerFolded(i+2)) {
                    log.severe("2 not folded");
                    winner = i+1;
                    winning_hand = playerHand[i+1];
                }
                else {
                    winner = i;
                    winning_hand = playerHand[i];
                }
            }

            if (compare == 0){
            }
        }
        log.severe("winning hand = "+winning_hand.toString());

        String strWinner = getCurrentPlayer(winner + 1);

        givePotToWinner(getCurrentPlayer(winner + 1));
        updatePlayerPositions();

        MyEndpoint endpoint = new MyEndpoint();

        if(checkIfGameIsOver()) {
            firstBets = true;
            endpoint.sendMessage(new MyRequest(GAMEOVER + strWinner + " with "+winning_hand));

        }
        else {
            populateCards(false);
            setUpNewGame();
            firstBets = true;
            endpoint.sendMessage(new MyRequest(HANDOVER + strWinner + " with "+winning_hand));
        }

    }

    private void givePotToWinner(String winner) {

        pullGameState();
        pullPlayerState(winner);

        tokens += this.pot;
        this.pot = 0;
        this.highestBet = 0;

        savePlayerState();
        saveGameState();
    }

    public boolean betIsLessThanHigh(int currentPlayer, int highestBet) {
        Query gaeQuery = new Query("Players");
        PreparedQuery pq = datastore.prepare(gaeQuery);
        for (Entity result : pq.asIterable()){
            if (currentPlayer == ((Long)result.getProperty("currentPosition")).intValue()) {
                int currentBet = ((Long)result.getProperty("currentBet")).intValue();
                return currentBet < highestBet ? true : false;
            }
        }
        return false;

    }

    private int numberOfPlayers() {
        Query gaeQuery = new Query("Players");
        PreparedQuery pq = datastore.prepare(gaeQuery);
        int numOfPlayers = pq.countEntities(FetchOptions.Builder.withLimit(100));
        return numOfPlayers; 
    }

    private boolean checkIfGameIsOver() {
        ArrayList<String> playersStillIn = new ArrayList<String>();
        Query gaeQuery = new Query("Players");
        PreparedQuery pq = datastore.prepare(gaeQuery);
        for (Entity result : pq.asIterable()){
            String id = (String) result.getKey().getName();
            if (((Long)result.getProperty("tokens")).intValue() > 0) {
                playersStillIn.add(id);
            }
            if (playersStillIn.size() > 1) {
                return false;
            }
        }
        return true;
    }

    private void saveGameState() {

        game.setProperty("currentplayer", currentPlayer);
        game.setProperty("numberOfPlayers", numberOfPlayers);
        game.setProperty("highestbet", highestBet);
        game.setProperty("pot", pot);
        game.setProperty("firstBets", firstBets);
        game.setProperty("flopBets", flopBets);
        game.setProperty("turnBets", turnBets);
        game.setProperty("riverBets", riverBets);
        game.setProperty("turn", strTurn);
        game.setProperty("flop", strFlop);
        game.setProperty("river", strRiver);

        datastore.put(game);
    }

    private void pullGameState() {
        try {
            game = null;
            Key key = KeyFactory.createKey("GameState", "currentGame");
            game = datastore.get(key);
            currentPlayer = ((Long) game.getProperty("currentplayer"))
                .intValue();
            numberOfPlayers = ((Long) game.getProperty("numberOfPlayers"))
                .intValue();
            highestBet = ((Long) game.getProperty("highestbet")).intValue();
            pot = ((Long) game.getProperty("pot")).intValue();
            firstBets = (Boolean) game.getProperty("firstBets");
            flopBets = (Boolean) game.getProperty("flopBets");
            turnBets = (Boolean) game.getProperty("turnBets");
            riverBets = (Boolean) game.getProperty("riverBets");
            strTurn = (String) game.getProperty("turn");
            strFlop = (String) game.getProperty("flop");
            strRiver = (String) game.getProperty("river");
        }catch(EntityNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void pullPlayerState(String user) {
        try {
            player = null;
            Key key = KeyFactory.createKey("Players", user);
            player = datastore.get(key);
            regid = (String)player.getProperty("regid");
            fold = (Boolean)player.getProperty("fold");
            tokens = ((Long)player.getProperty("tokens")).intValue();
        }catch(EntityNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void savePlayerState() {
            // reset everything
            player.setProperty("regid", regid);
            player.setProperty("currentBet", 0); // reset current bet for new hand
            player.setProperty("fold", fold);
            player.setProperty("handCards", handCards);
            player.setProperty("tokens", tokens);

            datastore.put(player);
    }
}
