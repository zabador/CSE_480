package edu.oakland.testmavenagain;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Sender;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
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

import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

@Api(name = "myendpoint")
public class MyEndpoint {
    private static final Logger log = Logger.getLogger(MyEndpoint.class.getName());
    private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    private GameLogic gameLogic = new GameLogic();

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
            log.severe("CALLING");
            if (user == null) {
                return new MyResult("Login failed");
            } else {
                datastore.put(regId);
                gameLogic.startGame();

                return new MyResult("hello moto");
            }
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


            // TODO implement game logic code
            sendMessage(new MyRequest("Game has started"));
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

                // reset everything
                entity.setProperty("regid", regid);
                entity.setProperty("currentBet",req.getBet());
                entity.setProperty("fold", fold);
                entity.setProperty("handCards", handCards);
                entity.setProperty("tokens", tokens);

                datastore.put(entity);

            }catch(EntityNotFoundException e) {
                e.printStackTrace();
            }

           // Entity bet = new Entity("Players", user.getEmail());
           // bet.setProperty("currentBet", req.getBet());
           // datastore.put(bet);

            // TODO implement game logic code
            return new MyResult("You placed you bet of "+ req.getBet());
        }

    @ApiMethod(name = "sendMessage")
        public void sendMessage(MyRequest req) {

            Sender sender = new Sender("AIzaSyCS77k51Ezy6oyb0R5bhwh_bDs64fP7aFw");


            Message message = new Message.Builder().addData("message", req.getGCMmessage()).build();

            try { List<String> devices = getAllRegIds();
                if(!devices.isEmpty()) {
                    log.severe("not empty");

                    @SuppressWarnings("unused")
                    MulticastResult result = sender.send(message, devices, 1);
                    log.severe("past sent");
                }

            }catch(IOException e) {
                log.severe("IOException " + e.getCause());
            }
        }

    /** this is called from the game logic to 
     *  send notifications to the users
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
}
