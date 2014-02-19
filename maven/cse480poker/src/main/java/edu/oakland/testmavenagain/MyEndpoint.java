package edu.oakland.testmavenagain;

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
			Entity regId = new Entity("GCMDeviceIds", req.getMessage());
			regId.setProperty("regid", req.getMessage());
			log.severe("CALLING");
			if (user == null) {
				return new MyResult("HELLO " + req.getMessage());
			} else {
				datastore.put(regId);
				log.info("user  " + user.getUserId());
				Entity entity = null;
				Key keyRegId = KeyFactory.createKey("GCMDeviceIds", req.getMessage());
				try {
					entity = datastore.get(keyRegId);
				}catch(EntityNotFoundException e) {
					e.printStackTrace();
				}

				return new MyResult(user.getEmail() + " sent " + entity.getKey().getName());
			}
		}
}
