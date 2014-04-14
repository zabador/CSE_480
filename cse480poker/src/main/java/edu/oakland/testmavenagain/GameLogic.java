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

    private ArrayList<String> playersList;
    private Hand flop;
    private Hand turn;
    private Hand river;

    private String handcards;

    public String test() {
        return "hope this works";
    }

    /**
     * Method will start the game and create a new table to store
     * game state it
     */
    public void startGame(boolean firstRound) {

        populateCards(firstRound);

        Entity game = new Entity("GameState", "currentGame");
        game.setProperty("currentplayer", 1);
        game.setProperty("highestbet", 0);
        game.setProperty("pot", 0);
        game.setProperty("numberOfPlayers", playersList.size());
        game.setProperty("firstBets", true);
        game.setProperty("flopBets", false);
        game.setProperty("turnBets", false);
        game.setProperty("riverBets", false);
        game.setProperty("flop", flop.toString());
        game.setProperty("turn", turn.toString());
        game.setProperty("river", river.toString());
        datastore.put(game);


        MyEndpoint endpoint = new MyEndpoint();
        endpoint.sendNotification(new MyRequest(getCurrentPlayer(1), PLACEBET));

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
            updateHandOfCards(playersList.get(i), playerHand[i].toString(), newGame);
        }
    }

    /**
     *  Method will save the newly created
     *  hand cards for the user
     *
     */
    public void updateHandOfCards(String user, String handCards, boolean newGame) {
        //TODO find a better way to update properties
        try {
            Entity entity = null;
            Key key = KeyFactory.createKey("Players", user);
            entity = datastore.get(key);
            String regid = (String)entity.getProperty("regid");
            boolean fold = (Boolean)entity.getProperty("fold");
            int tokens = ((Long)entity.getProperty("tokens")).intValue();

            // reset everything
            entity.setProperty("regid", regid);
            entity.setProperty("currentBet", 0); // reset current bet for new hand
            entity.setProperty("fold", fold);
            entity.setProperty("handCards", handCards);
            if(newGame) {
                tokens = 100;
            }
            entity.setProperty("tokens", tokens);

            datastore.put(entity);
        }catch(EntityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void placeBet() {
        // pull out currentgame state to resave after updateing
        boolean keepBetting = true;
        try {
            Entity game = null;
            Key key = KeyFactory.createKey("GameState", "currentGame");
            game = datastore.get(key);
            currentPlayer = ((Long)game.getProperty("currentplayer")).intValue();
            numberOfPlayers = ((Long)game.getProperty("numberOfPlayers")).intValue();
            highestBet = ((Long)game.getProperty("highestbet")).intValue();
            pot = ((Long)game.getProperty("pot")).intValue();
            firstBets = (Boolean)game.getProperty("firstBets");
            flopBets = (Boolean)game.getProperty("flopBets");
            turnBets = (Boolean)game.getProperty("turnBets");
            riverBets = (Boolean)game.getProperty("riverBets");
            strTurn = (String)game.getProperty("turn");
            strFlop = (String)game.getProperty("flop");
            strRiver = (String)game.getProperty("river");

            currentPlayer++;

            //increment currentPlayer if currenPlayer has folded
            while(checkIfCurrentPlayerFolded(currentPlayer)) {
                currentPlayer++;
            }
            
            while(!checkIfCurrentPlayerHasEnoughTokens(currentPlayer, highestBet)) {
                currentPlayer++;
            }

            // go to next round of betting
            if (currentPlayer > numberOfPlayers) {
                currentPlayer = 1; // set back to one for next round of betting

                while(!checkIfCurrentPlayerHasEnoughTokens(currentPlayer, highestBet)) {
                    currentPlayer++;
                }

                if (!betIsLessThanHigh(currentPlayer, highestBet)) {
                    keepBetting = goToNextRound();
                }
            }

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

            MyEndpoint endpoint = new MyEndpoint();
            
            if(keepBetting) {
                endpoint.sendNotification(new MyRequest(
                            getCurrentPlayer(currentPlayer), PLACEBET)); // save the update data
            }
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
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
            boolean fold = (Boolean)result.getProperty("fold");
            handcards = (String)result.getProperty("handCards");
            String regid = (String)result.getProperty("regid");
            int currentPosition = ((Long)result.getProperty("currentPosition")).intValue();
            int tokens = ((Long)result.getProperty("tokens")).intValue();

            if (currentPosition == pq.countEntities(FetchOptions.Builder.withLimit(100))) {
                currentPosition = 1;
            }
            else {
                currentPosition++;
            }

            result.setProperty("currentBet", 0);
            result.setProperty("fold", fold);
            result.setProperty("handCards", handcards);
            result.setProperty("regid", regid);
            result.setProperty("tokens", tokens);
            result.setProperty("currentPosition", currentPosition);
            datastore.put(result);
        }
    }

    private String getCurrentPlayer(int currentPlayer) {
        String player = null;
        Query gaeQuery = new Query("Players");
        PreparedQuery pq = datastore.prepare(gaeQuery);
        for (Entity result : pq.asIterable()){
            String id = (String) result.getKey().getName();
            if (currentPlayer == ((Long)result.getProperty("currentPosition")).intValue()) {
                player = id;
            }
        }
        return player;
    }

    private boolean checkIfCurrentPlayerFolded(int currentPlayer) {
        Query gaeQuery = new Query("Players");
        PreparedQuery pq = datastore.prepare(gaeQuery);
        for (Entity result : pq.asIterable()){
            if (currentPlayer == ((Long)result.getProperty("currentPosition")).intValue()) {
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
            playerHand[i] = new Hand();
            playerHand[i].addCard(new Card(handCards[0]));
            playerHand[i].addCard(new Card(handCards[1]));
            playerHand[i].addCard(new Card(flopCards[0]));
            playerHand[i].addCard(new Card(flopCards[1]));
            playerHand[i].addCard(new Card(flopCards[2]));
            playerHand[i].addCard(new Card(strTurn));
            playerHand[i].addCard(new Card(strRiver));
        }

        HandEvaluator[] handEval = new HandEvaluator[n];
        for(int i=0; i<n; i++){
            handEval[i] = new HandEvaluator();
        }

        //Finding the best hand for each player
        Hand[] bestHand = new Hand[n];
        for (int i=0; i<n; i++){
            bestHand[i] = handEval[i].getBest5CardHand(playerHand[i]);
        }

        Hand winning_hand = new Hand();
        winning_hand = bestHand[0];
        int winner = 0;
        int tiedWinner = 0;
        for (int i=0; i<n-1; i++){
            int compare = HandEvaluator.compareHands(winning_hand, bestHand[i+1]);
            if (compare == 1){
                if (checkIfCurrentPlayerFolded(i+1)) {
                    winner = i+1;
                    winning_hand = bestHand[i+1];
                }
            }

            if (compare == 2){
                if(!checkIfCurrentPlayerFolded(i+1)) {
                    winner = i+1;
                    winning_hand = bestHand[i+1];
                }
            }

            if (compare == 0){
            }
        }

        log.severe("winning hand is "+ winning_hand.toString());
        log.severe("winner is "+Integer.toString(winner + 1 ));
        String strWinner = getCurrentPlayer(winner + 1);
        log.severe("winner is "+strWinner);

        givePotToWinner(getCurrentPlayer(winner + 1));
        updatePlayerPositions();

        MyEndpoint endpoint = new MyEndpoint();

        if(checkIfGameIsOver()) {
            firstBets = false;
            endpoint.sendMessage(new MyRequest(GAMEOVER + strWinner));
            
        }
        else {
            populateCards(false);
            firstBets = true;
            endpoint.sendMessage(new MyRequest(HANDOVER + strWinner));
        }

    }

    private void givePotToWinner(String winner) {
        Entity entity = null;
        Key key = KeyFactory.createKey("Players", winner);
        try {
            //TODO find a better way to update properties
            entity = datastore.get(key);
            String regid = (String) entity.getProperty("regid");
            boolean fold = (Boolean) entity.getProperty("fold");
            String handCards = (String) entity.getProperty("handCards");
            int currentBet = ((Long) entity.getProperty("currentBet")).intValue();
            int tokens = ((Long) entity.getProperty("tokens")).intValue();
            int currentPosition = ((Long) entity.getProperty("currentPosition"))
                    .intValue();

            tokens += this.pot;
            this.pot = 0;
            this.highestBet = 0;
            // reset everything
            entity.setProperty("regid", regid);
            entity.setProperty("currentBet", currentBet); // store the bet
            entity.setProperty("fold", fold);
            entity.setProperty("handCards", handCards);
            entity.setProperty("tokens", tokens);
            entity.setProperty("currentPosition", currentPosition);

            datastore.put(entity);

        } catch (EntityNotFoundException e) {
            e.printStackTrace();
        }


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
        String player = null;
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
}
