package edu.oakland.cse480;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.HttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.HttpResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity extends Activity
{
	TextView messageLabel;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		message = (TextView)findViewById(R.id.messageLabel);
	}
	/** 
	 * This method which is called from the button press 
	 * to send a GET request to the server and handle the response
	 * 
	 * @param view The button that is pressed that calls this method
	 *
	 */
	public void getMessage(View view) {

		/***************************************************************************************************
		 *
		 * This is not the final way we are going to do this. We will use the correct way of using endpoints.
		 * This was just a test to see if I could communicate with the server and get a response
		 *
		 ****************************************************************************************************
		 */
		HttpResponse response = null;
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet();

			// the 0 at the end can be changed to a 1 or a 2 to get a different message
			request.setURI(new URI("https://cse-480.appspot.com/_ah/api/endpoints/v1/hellogreeting/3"));

			response = client.execute(request);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch(ClientProtocolException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}

		// convert the response to a string and set the text on the textview to it
		try {
			HttpEntity entity = response.getEntity();
			InputStream is = entity.getContent();
			messageLabel.setText(convertStreamToString(is));
		} catch (IOException e) {
			messageLabel.setText("it failed");
			e.printStackTrace();
		}
	}
	/**
	 * This method that will take the input stream from the HttpResponse and 
	 * convert it into a string
	 *
	 * @param is The inputStream that will be converted to a string and returned
	 * @return String The string representation of the InputStream
	 */

	private static String convertStreamToString(InputStream is) {

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append((line + "\n"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
}
