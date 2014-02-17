package edu.oakland.cse480poker;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.appengine.api.users.User;

import javax.inject.Named;
import java.util.ArrayList;
/**
 * Defines v1 of a helloworld API, which provides simple "greeting" methods.
 */
@Api(
	name = "testing",
	version = "v1",
	scopes = {Constants.EMAIL_SCOPE},
	clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID, Constants.API_EXPLORER_CLIENT_ID},
	audiences = {Constants.ANDROID_AUDIENCE}
)
public class Greetings {
	public static ArrayList<HelloGreeting> greetings = new ArrayList<HelloGreeting>();

	static {
		greetings.add(new HelloGreeting("hello world!"));
		greetings.add(new HelloGreeting("goodbye world!"));
	}

	public HelloGreeting getGreeting(@Named("id") Integer id) {
		return greetings.get(id);
	}

	@ApiMethod(name = "greetings.authed", path = "greeting/authed")
		public HelloGreeting authedGreeting(User user) {
			HelloGreeting response = new HelloGreeting("hello " + user.getEmail());
			return response;
		}
}
