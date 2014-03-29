package edu.oakland.cse480;

import java.io.IOException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.text.InputType;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.jackson.JacksonFactory;
import com.appspot.testmavenagain.myendpoint.Myendpoint;
import com.appspot.testmavenagain.myendpoint.model.MyRequest;
import com.appspot.testmavenagain.myendpoint.model.MyResult;

public class GameLobby extends Activity {
	private String testmsg = "";
	public int intBet = 0;
	//GCMIntentService notify;
	public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
	private Myendpoint endpoint;
    private GoogleAccountCredential credential;
	private GoogleCloudMessaging gcm;
	private String regid;
	private Context context;
    static final int REQUEST_ACCOUNT_PICKER = 2;

	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	private static final String TAG = "poker";
    static final String WEB_CLIENT_ID = "699958132030-a9n9sl6hj6ogj139u15hdn0ci002if2n.apps.googleusercontent.com";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game_lobby);
		context = getApplicationContext();

		credential = GoogleAccountCredential.usingAudience(this,
                "server:client_id:" + WEB_CLIENT_ID);

        CredentialHack.credential = credential;

		gcm = GoogleCloudMessaging.getInstance(this);
		regid = getRegistrationId(context);

        Myendpoint.Builder endpointBuilder = new Myendpoint.Builder(
            AndroidHttp.newCompatibleTransport(),
            new JacksonFactory(),
            credential);
        endpoint = CloudEndpointUtils.updateBuilder(endpointBuilder).build();
        CredentialHack.endpoint = endpoint;

        // handle the button click for joining game
        Button join = (Button) findViewById(R.id.btnJoinGame);
        join.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
				new DoSomethingAsync(endpoint, gcm, true).execute();
                Intent intent = new Intent(context, Gameplay.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
            }
        });

        // handle the button click for Loging in
        Button login = (Button) findViewById(R.id.Login);
        login.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                chooseAccount();
				
            }
        });
	}

	private void chooseAccount() {
		startActivityForResult(credential.newChooseAccountIntent(),
				REQUEST_ACCOUNT_PICKER);
	}

	/**
	 ** Gets the current registration ID for application on GCM service.
	 ** 
	 ** If result is empty, the app needs to register.
	 **
	 ** @return registration ID, or empty string if there is no existing
	 **         registration ID.
	 **/

	private String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGCMPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			Log.i(TAG, "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Log.i(TAG, "App version changed.");
			return "";
		}
		return registrationId;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case REQUEST_ACCOUNT_PICKER:
				if (data != null && data.getExtras() != null) {
					String accountName = data.getExtras().getString(
							AccountManager.KEY_ACCOUNT_NAME);
					if (accountName != null) {
						credential.setSelectedAccountName(accountName);
                        Log.d("account name is ", credential.getSelectedAccountName());
                        new DoSomethingAsync(endpoint, gcm, false).execute();
						// User is authorized.
					}
				}
				break;
		}
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGCMPreferences(Context context) {
		return getSharedPreferences(Gameplay.class.getSimpleName(),
				Context.MODE_PRIVATE);
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
				.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.game_lobby, menu);
		return true;
	}
	
	public void joinGame (View view){
	}
	
	public void testNotification(View view){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Code,info");

		// Set up the input
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		builder.setView(input);

		// Set up the buttons
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		    	 testmsg = input.getText().toString();
		    	 //If you want to test the GCM based notifications, make this say
		    	 //notify.sendNotification(testmsg);
		    	 sendNotification(testmsg);
		    }
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        dialog.cancel();
		    }
		});

		builder.show();
		
	}
	public void sendNotification(String incomingMsg) {
    	String msg;
    	String[] separated = incomingMsg.split(",");
    	separated[0] = separated[0]; //code
    	separated[1] = separated[1] + ""; //Additional message with "" to negate a null
    	int msgCode = 0;
    	msgCode = Integer.parseInt(separated[0]);
    	switch (msgCode){
    	case 1:
    		msg = "New Player " + separated[1].toString() + " has joined";
    		break;
    	case 2:
    		msg = "The game has started";
    		//Send a request to draw two cards
    		break;
    	case 3:
    		msg = "It is your turn to bet";
    		//Stuff
    		break;
    	case 4:
    		msg = "Flop goes";
    		break;
    	case 5:
    		msg = "A card has been dealt";
    		//Stuff
    		break;
    	case 6:
    		msg = "The river card, " + separated[1] + " has been dealt";
    		//Stuff
    		break;
    	case 7:
    		msg = "Game over. Winner is " + separated[1];
    		//Stuff
    		break;
    	default:
    		msg = "Switch case isn't working";
    		//Some default stuff
    		break;
    	}
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, Gameplay.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentTitle("Poker Notification")
        .setStyle(new NotificationCompat.BigTextStyle()
        .bigText(msg))
        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }


	private class DoSomethingAsync extends AsyncTask<Void, Void, MyResult> {
		private Myendpoint endpoint;
		private GoogleCloudMessaging gcm;
        private boolean startGame;

		public DoSomethingAsync(Myendpoint endpoint, GoogleCloudMessaging gcm, boolean startGame) {
			this.endpoint = endpoint;
			this.gcm = gcm;
            this.startGame = startGame;
		}

		@Override
		protected MyResult doInBackground(Void... params) {
			try {
				if (gcm == null) {
					gcm = GoogleCloudMessaging.getInstance(context);
				}
				regid = gcm.register("699958132030");
				Log.d("regid from app = ", regid);

			} catch (IOException ex) {
			}

			try {
                if (!startGame) {
                    MyRequest r = new MyRequest();
                    r.setRegId(regid);
                    return endpoint.authenticate(r).execute();
                }
                else {
                    return endpoint.startGame().execute();

                }
			} catch (IOException e) {
				e.printStackTrace();
				MyResult r = new MyResult();
				Log.e("error = ", e.getMessage(), e);
				r.setValue("EXCEPTION");
				return r;
			}
		}

		@Override
		protected void onPostExecute(MyResult r) {
            Toast toast = Toast.makeText(context, r.getValue(), Toast.LENGTH_SHORT);
            toast.show();
		}
	}
}


