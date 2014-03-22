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

import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

@Api(name = "myendpoint")
public class MyEndpoint {
    private static final Logger log = Logger.getLogger(MyEndpoint.class.getName());
    private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

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
            log.severe("CALLING");
            if (user == null) {
                return new MyResult("Login failed");
            } else {
                datastore.put(regId);

                return new MyResult("Login Success");
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
