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
import com.google.appengine.api.users.User;

import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

public class GameLogic {

    private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    private static final Logger log = Logger.getLogger(MyEndpoint.class.getName());
    private static Deck deck;

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

        Entity game = new Entity("GameState", "currentGame");
        game.setProperty("currentplayer", 1);
        game.setProperty("numberOfPlayers", playersList.size());
        game.setProperty("firstBets", true);
        game.setProperty("flopBets", false);
        game.setProperty("turnBets", false);
        game.setProperty("riverBets", false);
        game.setProperty("flop", "");
        game.setProperty("turn", "");
        game.setProperty("river", "");
        datastore.put(game);

        for (int i=0; i<playersList.size(); i++ ) {
            log.severe(""+playersList.get(i));
            playerHand[i] = new Hand();
            playerHand[i].addCard(deck.deal());
            playerHand[i].addCard(deck.deal());
            updateHandOfCards(playersList.get(i), playerHand[i].toString());
        }

        MyEndpoint endpoint = new MyEndpoint();
        endpoint.sendNotification(new MyRequest(playersList.get(0), "place bet"));

    }

    public void gameStart(int n){


        //Array of all players' hands
        Hand [] playerHand = new Hand[n];	
        for(int x=0; x<n; x++){
            playerHand[x] = new Hand();
        }
////////////////////////
//        //Dealing 2 cards for each player
//        for (int i=0; i<2; i++){
//            for (int j=0; j<n; j++){
//                playerHand[].addCard(deck.deal());
//            }
//        }

        for (int i=0; i<n; i++){
            String prtCard = Integer.toString(i+1);
            System.out.println("Player" + prtCard + "'s cards: " + playerHand[i]);
        }

        System.out.print("\nFlop Turn River: ");
        //Flop, Turn, and River cards
        Card [] ftr = new Card[5];
        for (int i=0; i<5; i++){
           // ftr[i] = deck.deal();
            System.out.print( ftr[i] + " ");
        }
        System.out.println("\n");

        //Forming 7-card hand for each player
        for (int i=0; i<n; i++){
            for (int j=0; j<5; j++){
                playerHand[i].addCard(ftr[j]);
            }
        }

        for (int a=0; a<n; a++){
            String prtCard = Integer.toString(a+1);
            System.out.println("Player" + prtCard + "'s hand: " + playerHand[a]);
        }

        HandEvaluator [] handEval = new HandEvaluator[n];	
        for(int i=0; i<n; i++){
            handEval[i] = new HandEvaluator();
        }	

        //Finding the best hand for each player
        Hand [] bestHand = new Hand[n];
        for (int i=0; i<n; i++){
            bestHand[i] = handEval[i].getBest5CardHand(playerHand[i]);
        }

        for (int a=0; a<n; a++){
            String prtCard = Integer.toString(a+1);
            System.out.println("\nPlayer " + prtCard + " best hand: " + bestHand[a]);
            System.out.println(HandEvaluator.nameHand(bestHand[a]));
        }

        //Determining the winning hand
        Hand winning_hand = new Hand();
        winning_hand = playerHand[0];
        for (int i=0; i<n-1; i++){
            int compare = HandEvaluator.compareHands(winning_hand, playerHand[i+1]);
            if (compare == 1){
                //do nothing	
            }

            if (compare == 2){
                winning_hand = playerHand[i+1];
            }

            if (compare == 0){
                // do nothing
            }
        }

        System.out.println("\nWinning hand: " + winning_hand);
        System.out.println(HandEvaluator.nameHand(winning_hand));

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

    public void placeBet(String user, int bet) {
        try {
            Entity game = null;
            Key key = KeyFactory.createKey("GameState", user);
            game = datastore.get(key);
            int currentPlayer = ((Long)game.getProperty("currentPlayer")).intValue();
            int numberOfPlayers = ((Long)game.getProperty("numberOfPlayers")).intValue();
            boolean firstBets = (Boolean)game.getProperty("firstBets");
            boolean flopBets = (Boolean)game.getProperty("flopBets");
            boolean turnBets = (Boolean)game.getProperty("turnBets");
            boolean riverBets = (Boolean)game.getProperty("riverBets");
            String turn = (String)game.getProperty("turn");
            String flop = (String)game.getProperty("flop");
            String river = (String)game.getProperty("river");

            //TODO figure out fold
            currentPlayer++;
            if(currentPlayer > numberOfPlayers) {
               currentPlayer = 1; // set back to one for next round of betting
               if(firstBets) {
                   firstBets = false; // go to flopBets
                   flopBets = true; // start flop bets
                   flop(); //TODO create a flop method to deal out flops and update datastore
               }
               else if(flopBets) {
                   flopBets = false; // go to turnBets
                   turnBets = true; // start turn bets
                   turn(); //TODO create a turn method to deal out flops and update datastore
               }
               else if(turnBets) {
                   turnBets = false; // go to turnBets
                   riverBets = true; // start turn bets
                   river(); //TODO create a flop method to deal out flops and update datastore
               }
               else if(riverBets) {
                   riverBets = false; // go to turnBets
                   endGame(); //TODO create a flop method to deal out flops and update datastore
               }
            }
            game.setProperty("currentPlayer", currentPlayer);
            game.setProperty("numberOfPlayers", numberOfPlayers);
            game.setProperty("firstBets", firstBets);
            game.setProperty("flopBets", flopBets);
            game.setProperty("turnBets", turnBets);
            game.setProperty("riverBets", riverBets);
            game.setProperty("turn", turn);
            game.setProperty("flop", flop);
            game.setProperty("river", river);

            datastore.put(game);
        }catch(EntityNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    //TEST!
    public void flop() {
        Card [] flop = new Card[3];
        for (int i=0; i<3; i++){
            flop[i] = deck.deal();
        }

        try {
            Entity game = null;
            Key key = KeyFactory.createKey("GameState", "currentGame");
            game = datastore.get(key);
            int currentPlayer = ((Long)game.getProperty("currentPlayer")).intValue();
            int numberOfPlayers = ((Long)game.getProperty("numberOfPlayers")).intValue();
            boolean firstBets = (Boolean)game.getProperty("firstBets");
            boolean flopBets = (Boolean)game.getProperty("flopBets");
            boolean turnBets = (Boolean)game.getProperty("turnBets");
            boolean riverBets = (Boolean)game.getProperty("riverBets");
            String turn = (String)game.getProperty("turn");
            String river = (String)game.getProperty("river");

            game.setProperty("currentPlayer", currentPlayer);
            game.setProperty("numberOfPlayers", numberOfPlayers);
            game.setProperty("firstBets", firstBets);
            game.setProperty("flopBets", flopBets);
            game.setProperty("turnBets", turnBets);
            game.setProperty("riverBets", riverBets);
            game.setProperty("turn", turn);
            game.setProperty("flop", flop);
            game.setProperty("river", river);

            datastore.put(game);
        }catch(EntityNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void turn() {

    }
    public void river() {

    }
    public void endGame() {

    }
}
