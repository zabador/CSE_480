package edu.oakland.cse480;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.users.User;


import static com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID;

import java.util.logging.Logger;


@Api(name = "myendpoint")
public class MyEndpoint {
	private static final Logger log = Logger.getLogger(MyEndpoint.class.getName());

	@ApiMethod(name = "compute",
			clientIds = {
				Ids.WEB_CLIENT_ID,
				Ids.ANDROID_CLIENT_ID,
				API_EXPLORER_CLIENT_ID 
			},
			audiences = {
				Ids.WEB_CLIENT_ID, Ids.ANDROID_CLIENT_ID
			},
			scopes = {
				"https://www.googleapis.com/auth/userinfo.email",
				"https://www.googleapis.com/auth/userinfo.profile"
			})
	public MyResult compute(MyRequest req, User user) {
		log.severe("API CALLED");
		if(user == null) {
			return new MyResult("HELLO " + req.getMessage());
		}
		else {
			return new MyResult("Hello " + user.getEmail());
		}
	}
}
