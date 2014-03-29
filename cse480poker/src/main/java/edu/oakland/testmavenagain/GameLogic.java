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
    private static Deck deck;

    private final String PLACEBET = "3";

    public String test() {
        return "hope this works";
    }

    /**
     * Method will start the game and create a new table to store
     * game state it
     */
    public void startGame() {
        deck = new Deck();
        deck.shuffle();

        ArrayList<String> playersList = getAllPlayers();
        Hand [] playerHand = new Hand[playersList.size()];  

        // initialize the flop to store in the datastore
        Card[] flop = new Card[3];
        for (int i=0; i<3; i++) {
            flop[i] = deck.deal();
        }

        // initialize the turn to store in the datastore
        Card[] turn = new Card[1];
        turn[0] = deck.deal();

        // initialize the river to store in the datastore
        Card[] river = new Card[1];
        river[0] = deck.deal();

        Entity game = new Entity("GameState", "currentGame");
        game.setProperty("currentplayer", 1);
        game.setProperty("numberOfPlayers", playersList.size());
        game.setProperty("firstBets", true);
        game.setProperty("flopBets", false);
        game.setProperty("turnBets", false);
        game.setProperty("riverBets", false);
        game.setProperty("flop", flop.toString());
        game.setProperty("turn", turn.toString());
        game.setProperty("river", river.toString());
        datastore.put(game);

        for (int i=0; i<playersList.size(); i++ ) {
            log.severe(""+playersList.get(i));
            playerHand[i] = new Hand();
            playerHand[i].addCard(deck.deal());
            playerHand[i].addCard(deck.deal());
            updateHandOfCards(playersList.get(i), playerHand[i].toString());
        }

        MyEndpoint endpoint = new MyEndpoint();
        endpoint.sendNotification(new MyRequest(playersList.get(0), PLACEBET));

    }

//    public void gameStart(int n){
//
//
//        //Array of all players' hands
//        Hand [] playerHand = new Hand[n];   
//        for(int x=0; x<n; x++){
//            playerHand[x] = new Hand();
//        }
//        //Dealing 2 cards for each player
//        for (int i=0; i<2; i++){
//            for (int j=0; j<n; j++){
//                playerHand[].addCard(deck.deal());
//            }
//        }
//
//        for (int i=0; i<n; i++){
//            String prtCard = Integer.toString(i+1);
//            System.out.println("Player" + prtCard + "'s cards: " + playerHand[i]);
//        }
//
//        System.out.print("\nFlop Turn River: ");
//        //Flop, Turn, and River cards
//        Card [] ftr = new Card[5];
//        for (int i=0; i<5; i++){
//           // ftr[i] = deck.deal();
//            System.out.print( ftr[i] + " ");
//        }
//        System.out.println("\n");
//
//        //Forming 7-card hand for each player
//        for (int i=0; i<n; i++){
//            for (int j=0; j<5; j++){
//                playerHand[i].addCard(ftr[j]);
//            }
//        }
//
//        for (int a=0; a<n; a++){
//            String prtCard = Integer.toString(a+1);
//            System.out.println("Player" + prtCard + "'s hand: " + playerHand[a]);
//        }
//
//        HandEvaluator [] handEval = new HandEvaluator[n];   
//        for(int i=0; i<n; i++){
//            handEval[i] = new HandEvaluator();
//        }   
//
//        //Finding the best hand for each player
//        Hand [] bestHand = new Hand[n];
//        for (int i=0; i<n; i++){
//            bestHand[i] = handEval[i].getBest5CardHand(playerHand[i]);
//        }
//
//        for (int a=0; a<n; a++){
//            String prtCard = Integer.toString(a+1);
//            System.out.println("\nPlayer " + prtCard + " best hand: " + bestHand[a]);
//            System.out.println(HandEvaluator.nameHand(bestHand[a]));
//        }
//
//        //Determining the winning hand
//        Hand winning_hand = new Hand();
//        winning_hand = playerHand[0];
//        for (int i=0; i<n-1; i++){
//            int compare = HandEvaluator.compareHands(winning_hand, playerHand[i+1]);
//            if (compare == 1){
//                //do nothing    
//            }
//
//            if (compare == 2){
//                winning_hand = playerHand[i+1];
//            }
//
//            if (compare == 0){
//                // do nothing
//            }
//        }
//
//        System.out.println("\nWinning hand: " + winning_hand);
//        System.out.println(HandEvaluator.nameHand(winning_hand));
//
//    }

    /**
     *  Method will save the newly created
     *  hand cards for the user
     *
     */
    public void updateHandOfCards(String user, String handCards) {
        //TODO find a better way to update properties
        try {
            Entity entity = null;
            Key key = KeyFactory.createKey("Players", user);
            entity = datastore.get(key);
            int bet = ((Long)entity.getProperty("currentBet")).intValue();
            String regid = (String)entity.getProperty("regid");
            boolean fold = (Boolean)entity.getProperty("fold");
            int tokens = ((Long)entity.getProperty("tokens")).intValue();

            // reset everything
            entity.setProperty("regid", regid);
            entity.setProperty("currentBet", bet);
            entity.setProperty("fold", fold);
            entity.setProperty("handCards", handCards);
            entity.setProperty("tokens", tokens);

            datastore.put(entity);
        }catch(EntityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void placeBet(int bet) {
        // pull out currentgame state to resave after updateing
        log.severe("in place bet game logic");
        MyEndpoint endpoint = new MyEndpoint();
        try {
            Entity game = null;
            Key key = KeyFactory.createKey("GameState", "currentGame");
            game = datastore.get(key);
            int currentPlayer = ((Long)game.getProperty("currentplayer")).intValue();
            int numberOfPlayers = ((Long)game.getProperty("numberOfPlayers")).intValue();
            boolean firstBets = (Boolean)game.getProperty("firstBets");
            boolean flopBets = (Boolean)game.getProperty("flopBets");
            boolean turnBets = (Boolean)game.getProperty("turnBets");
            boolean riverBets = (Boolean)game.getProperty("riverBets");
            String turn = (String)game.getProperty("turn");
            String flop = (String)game.getProperty("flop");
            String river = (String)game.getProperty("river");

            currentPlayer++;

            //increment currentPlayer if currenPlayer has folded
            while(checkIfCurrentPlayerFolded(currentPlayer)) {
                currentPlayer++;
            }

            // go to next round of betting
            if (currentPlayer > numberOfPlayers) {
                currentPlayer = 1; // set back to one for next round of betting
                if (firstBets) {
                    firstBets = false; // go to flopBets
                    flopBets = true; // start flop bets
                    endpoint.sendNotification(new MyRequest(
                            getCurrentPlayer(currentPlayer), PLACEBET));
                } else if (flopBets) {
                    flopBets = false; // go to turnBets
                    turnBets = true; // start turn bets
                    endpoint.sendNotification(new MyRequest(
                            getCurrentPlayer(currentPlayer), PLACEBET));
                } else if (turnBets) {
                    turnBets = false; // go to turnBets
                    riverBets = true; // start turn bets
                    endpoint.sendNotification(new MyRequest(
                            getCurrentPlayer(currentPlayer), PLACEBET));
                } else if (riverBets) {
                    riverBets = false; // go to turnBets
                    endGame(); //TODO
                }
            }
            else {
                endpoint.sendNotification(new MyRequest(
                        getCurrentPlayer(currentPlayer), PLACEBET)); // save the update data
            }

            game.setProperty("currentplayer", currentPlayer);
            game.setProperty("numberOfPlayers", numberOfPlayers);
            game.setProperty("firstBets", firstBets);
            game.setProperty("flopBets", flopBets);
            game.setProperty("turnBets", turnBets);
            game.setProperty("riverBets", riverBets);
            game.setProperty("turn", turn);
            game.setProperty("flop", flop);
            game.setProperty("river", river);

            datastore.put(game);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method will increment player postions in the game so 
     * a new person will start the betting on the new hand
     */
    private void updatePlayerPositions() {
        Query gaeQuery = new Query("Players");
        PreparedQuery pq = datastore.prepare(gaeQuery);
        for (Entity result : pq.asIterable()) {
            int currentBet = ((Long)result.getProperty("currentBet")).intValue();
            boolean fold = (Boolean)result.getProperty("fold");
            String handcards = (String)result.getProperty("handCards");
            String regid = (String)result.getProperty("regid");
            int currentPosition = ((Long)result.getProperty("currentPosition")).intValue();
            int tokens = ((Long)result.getProperty("tokens")).intValue();

            if (currentPosition == pq.countEntities(FetchOptions.Builder.withLimit(100))) {
                currentPosition = 1;
            }
            else {
                currentPosition++;
            }

            result.setProperty("currentBet", currentBet);
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

    public void turn() {

    }
    public void river() {

    }
    public void endGame() {
        log.severe("Made it to end game");
        updatePlayerPositions();
    }
}
