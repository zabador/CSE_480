package edu.oakland.testmavenagain;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Sender;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
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

import static com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID;
import edu.oakland.testmavenagain.GameLogic;

import java.security.KeyStore;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

@Api(name = "myendpoint")
public class MyEndpoint {
    private static final Logger log = Logger.getLogger(MyEndpoint.class.getName());
    private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    private GameLogic gameLogic = new GameLogic();
    Sender sender = new Sender("AIzaSyCS77k51Ezy6oyb0R5bhwh_bDs64fP7aFw");

    @ApiMethod(name = "authenticate",
            clientIds = {Ids.WEB_CLIENT_ID, 
                Ids.BEVERLY_CLIENT_ID, 
        Ids.MIRIAM_CLIENT_ID, 
        Ids.GEOFF_CLIENT_ID, 
        Ids.BRANDON_CLIENT_ID, 
        API_EXPLORER_CLIENT_ID },
        audiences = {Ids.WEB_CLIENT_ID },
        scopes = {
            "https://www.googleapis.com/auth/userinfo.email",
        "https://www.googleapis.com/auth/userinfo.profile" })
        public MyResult authenticate(MyRequest req, User user) {
            Entity regId = new Entity("Players", user.getEmail());
            regId.setProperty("regid", req.getRegId());
            regId.setProperty("currentBet", 0);
            regId.setProperty("fold", false);
            regId.setProperty("handCards", "");
            regId.setProperty("tokens", 100);
            regId.setProperty("currentPosition", numberOfPlayers() + 1);
            log.severe("CALLING");
            if (user == null) {
                return new MyResult("Login failed");
            } else {
                datastore.put(regId);

                return new MyResult("Login Successful");
            }
        }

    private int numberOfPlayers() {
        Query gaeQuery = new Query("Players");
        PreparedQuery pq = datastore.prepare(gaeQuery);
        int numOfPlayers = pq.countEntities(FetchOptions.Builder.withLimit(100));
        log.severe("numOfPlayers = " + numOfPlayers);
        return numOfPlayers; 
    }


    @ApiMethod(name = "startGame",
                clientIds = {Ids.WEB_CLIENT_ID, 
                    Ids.BEVERLY_CLIENT_ID, 
        Ids.MIRIAM_CLIENT_ID, 
        Ids.GEOFF_CLIENT_ID, 
        Ids.BRANDON_CLIENT_ID, 
        API_EXPLORER_CLIENT_ID },
        audiences = {Ids.WEB_CLIENT_ID },
        scopes = {
            "https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/userinfo.profile" })
        public MyResult startGame() {

            MyRequest req = new MyRequest("start");
            Message message = new Message.Builder().addData("message", req.getGCMmessage()).build();

            try { List<String> devices = getAllRegIds();
                if(!devices.isEmpty()) {
                    log.severe("not empty");

                    @SuppressWarnings("unused")
                    MulticastResult result = sender.send(message, devices, 1);
                }

            }catch(IOException e) {
                log.severe("IOException " + e.getCause());
            }

            gameLogic.startGame();
            sendMessage(new MyRequest("2"));
            return new MyResult("Game Started");
        }

        @ApiMethod(name = "placeBet",
                clientIds = {Ids.WEB_CLIENT_ID, 
                    Ids.BEVERLY_CLIENT_ID, 
            Ids.MIRIAM_CLIENT_ID, 
            Ids.GEOFF_CLIENT_ID, 
            Ids.BRANDON_CLIENT_ID, 
            API_EXPLORER_CLIENT_ID },
            audiences = {Ids.WEB_CLIENT_ID },
            scopes = {
                "https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/userinfo.profile" })
        public MyResult placeBet(MyRequest req, User user) {

            Entity entity = null;
            Key key = KeyFactory.createKey("Players", user.getEmail());
            try {
                //TODO find a better way to update properties
                entity = datastore.get(key);
                String regid = (String)entity.getProperty("regid");
                boolean fold = (Boolean)entity.getProperty("fold");
                String handCards = (String)entity.getProperty("handCards");
                int tokens = ((Long)entity.getProperty("tokens")).intValue();
                int currentPosition = ((Long)entity.getProperty("currentPosition")).intValue();

                // reset everything
                entity.setProperty("regid", regid);
                entity.setProperty("currentBet",req.getBet()); // store the bet
                entity.setProperty("fold", fold);
                entity.setProperty("handCards", handCards);
                entity.setProperty("tokens", tokens);
                entity.setProperty("currentPosition", currentPosition);

                datastore.put(entity);

            }catch(EntityNotFoundException e) {
                e.printStackTrace();
            }


            gameLogic.placeBet(req.getBet());
            log.severe("in placebet endpoint");
            return new MyResult("You placed you bet of "+ req.getBet());
        }

    @ApiMethod(name = "getGameState",
                clientIds = {Ids.WEB_CLIENT_ID, 
                    Ids.BEVERLY_CLIENT_ID, 
        Ids.MIRIAM_CLIENT_ID, 
        Ids.GEOFF_CLIENT_ID, 
        Ids.BRANDON_CLIENT_ID, 
        API_EXPLORER_CLIENT_ID },
        audiences = {Ids.WEB_CLIENT_ID },
        scopes = {
            "https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/userinfo.profile" })
        public MyResult getGameState(User user) {
            log.severe("it made it to getGameState");
            HashMap<String, String> map = new HashMap<String, String>(); 
            try {
                Entity entity = null;
                Key key = KeyFactory.createKey("Players", user.getEmail());
                entity = datastore.get(key);
                // fill map with player specific items
                map.put("tokens", Long.toString((Long)entity.getProperty("tokens")));
                map.put("fold",String.valueOf((Boolean)entity.getProperty("fold")));
                map.put("handCards",(String)entity.getProperty("handCards"));
                map.put("currentBet",Long.toString((Long)entity.getProperty("currentBet")));
                map.put("currentPosition",Long.toString((Long)entity.getProperty("currentPosition")));

                // mill map with gamestate items
                key = KeyFactory.createKey("GameState", "currentGame");
                entity = datastore.get(key);
                map.put("currentPlayer",(String)entity.getProperty("currentPlayer"));
                if ((Boolean)entity.getProperty("flopBets")) {
                    map.put("flop",(String)entity.getProperty("flop"));
                }
                else if ((Boolean)entity.getProperty("turnBets")) {
                    map.put("flop",(String)entity.getProperty("flop"));
                    map.put("turn",(String)entity.getProperty("turn"));
                }
                else if ((Boolean)entity.getProperty("riverBets")) {
                    map.put("flop",(String)entity.getProperty("flop"));
                    map.put("turn",(String)entity.getProperty("turn"));
                    map.put("river",(String)entity.getProperty("river"));
                }
                else {
                    map.put("flop","ic_launcher");
                    map.put("turn","ic_launcher");
                    map.put("river","ic_launcher");
                }
            }catch(EntityNotFoundException e) {
                e.printStackTrace();
            }

            MyResult r = new MyResult();
            r.setGameState(map);
            log.severe(" map is " + r.getGameState().toString());

			r.setPlayers(getAllPlayers());
            

            return r;
        }
    @ApiMethod(name = "sendMessage")
        public void sendMessage(MyRequest req) {



            Message message = new Message.Builder().addData("message", req.getGCMmessage()).build();

            try { List<String> devices = getAllRegIds();
                if(!devices.isEmpty()) {
                    log.severe("not empty");

                    @SuppressWarnings("unused")
                    MulticastResult result = sender.send(message, devices, 1);
                }

            }catch(IOException e) {
                log.severe("IOException " + e.getCause());
            }
        }

    /** this is called from the game logic to 
     *  send notifications to the users updating
     *  their game state
     */
    @ApiMethod(name = "sendNotification")
        public void sendNotification(MyRequest req) {

            Sender sender = new Sender("AIzaSyCS77k51Ezy6oyb0R5bhwh_bDs64fP7aFw");

            Message message = new Message.Builder().addData("message", req.getGCMmessage()).build();
            List<String> user = new ArrayList<String>(); 

            try {
                Entity entity = null;
                Key key = KeyFactory.createKey("Players", req.getUser());
                entity = datastore.get(key);
                String regid = (String)entity.getProperty("regid");
                user.add(regid);
            }catch(EntityNotFoundException e) {
                e.printStackTrace();
            }


            try { 
                @SuppressWarnings("unused")
                MulticastResult result = sender.send(message, user, 1);
                log.severe("past sent");
            }catch(IOException e) {
                log.severe("IOException " + e.getCause());
            }
        }

    // Reads all previously stored device tokens from the database
    private ArrayList<String> getAllRegIds(){
        ArrayList<String> regIds = new ArrayList<String>();
        Query gaeQuery = new Query("Players");
        PreparedQuery pq = datastore.prepare(gaeQuery);
        for (Entity result : pq.asIterable()){
            String id = (String) result.getProperty("regid");
            regIds.add(id);
        }

        return regIds;
    }
    // Reads all previously stored device tokens from the database
    private ArrayList<String> getAllPlayers(){
        ArrayList<String> players = new ArrayList<String>();
        Query gaeQuery = new Query("Players");
        PreparedQuery pq = datastore.prepare(gaeQuery);
        for (Entity result : pq.asIterable()){
            String id = result.getKey().toString();
			log.severe(id);
            players.add(id);
        }

        return players;
    }
}
