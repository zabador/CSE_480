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
		messageLabel = (TextView)findViewById(R.id.messageLabel);
    }

	public void getMessage(View view) {

		HttpResponse response = null;
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet();
			request.setURI(new URI("https://cse-480.appspot.com/_ah/api/endpoints/v1/hellogreeting/0"));
			response = client.execute(request);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch(ClientProtocolException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}

		try {
			HttpEntity entity = response.getEntity();
			InputStream is = entity.getContent();
			messageLabel.setText(convertStreamToString(is));
		} catch (IOException e) {
			messageLabel.setText("it failed");
			e.printStackTrace();
		}
	}

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
