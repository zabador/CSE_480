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

	@ApiMethod(name = "compute",
			clientIds = {Ids.WEB_CLIENT_ID, Ids.ANDROID_CLIENT_ID, Ids.BRANDON_CLIENT_ID, API_EXPLORER_CLIENT_ID },
			audiences = {Ids.WEB_CLIENT_ID },
			scopes = {
				"https://www.googleapis.com/auth/userinfo.email",
				"https://www.googleapis.com/auth/userinfo.profile" })
		public MyResult compute(MyRequest req, User user) {
			Entity regId = new Entity("GCMDeviceIds", user.getEmail());
			regId.setProperty("regid", req.getMessage());
			log.severe("CALLING");
			if (user == null) {
				return new MyResult("HELLO " + req.getMessage());
			} else {
				datastore.put(regId);
				log.info("user  " + user.getUserId());
				Entity entity = null;
				Key keyRegId = KeyFactory.createKey("GCMDeviceIds", user.getEmail());
				try {
					entity = datastore.get(keyRegId);
				}catch(EntityNotFoundException e) {
					e.printStackTrace();
				}

				return new MyResult(user.getEmail() + " sent " + entity.getProperty("name"));
			}
		}

		@ApiMethod(name = "sendMessage")
		public void sendMessage(MyRequest req) {

			Sender sender = new Sender("699958132030");
			
			
			Message message = new Message.Builder().addData("message", "test").build();
			
			
			try {
				entity = datastore.get(keyRegId);
				List<String> devices = getAllRegIds();
				if(!devices.isEmpty()) {
					log.severe("not empty");
					MulticastResult result = sender.send(message, entity.getProperty("regid"), 5);
					log.severe("past sent");
				}

			}catch(IOException e) {
				log.severe("IOException");
			}

		}

		// Reads all previously stored device tokens from the database
		private ArrayList<String> getAllRegIds(){
			ArrayList<String> regIds = new ArrayList<String>();
			Query gaeQuery = new Query("GCMDeviceIds");
			PreparedQuery pq = datastore.prepare(gaeQuery);
			for (Entity result : pq.asIterable()){
				String id = (String) result.getProperty("regid");
				regIds.add(id);
			}

			return regIds;
		}
}
