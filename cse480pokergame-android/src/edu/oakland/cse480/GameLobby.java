package edu.oakland.cse480;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.EditText;

import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.jackson.JacksonFactory;
import com.appspot.testmavenagain.myendpoint.Myendpoint;
import com.appspot.testmavenagain.myendpoint.model.MyRequest;
import com.appspot.testmavenagain.myendpoint.model.MyResult;

public class GameLobby extends Activity implements OnUpdateFinish {
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
    private OnUpdateFinish onUpdateFinish;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private Button start;
    private Button join;

    private String[] playersLoggedIn;

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
        listView = (ListView)findViewById(R.id.list);
		context = this;
        onUpdateFinish = this;
        playersLoggedIn = new String[] {"No Players logged in"};

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

        // handle the button click for starting game
        start = (Button) findViewById(R.id.btnStartGame);
        start.setVisibility(View.INVISIBLE);
        start.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
				new DoSomethingAsync(context, onUpdateFinish, endpoint, gcm, true).execute();
                Intent intent = new Intent(context, Gameplay.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        // handle the button click for joining game
        join = (Button) findViewById(R.id.btnJoinGame);
        join.setVisibility(View.INVISIBLE);
        join.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(context, Gameplay.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
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

        this.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getExtras().getBoolean("GAMESTARTED")) {
                    join.setVisibility(View.VISIBLE);
                }
                updateGameLobby();
            }
        }, new IntentFilter("UpdateGameLobby"));

        // define arrayadapter for the listview of players
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, playersLoggedIn);
        listView.setAdapter(adapter);
	}

	private void chooseAccount() {
		startActivityForResult(credential.newChooseAccountIntent(),
				REQUEST_ACCOUNT_PICKER);
	}

	public void onPlaceBetFinish() {

    }

	public void onGetGameStateFinish(MyResult result) {
        Log.i("update ", "adapter");
        try {
            List<String> players = result.getPlayers();
            playersLoggedIn = new String[players.size()];
            playersLoggedIn = players.toArray(playersLoggedIn);
            for(int i = 0; i < playersLoggedIn.length; i++) {
                playersLoggedIn[i] = playersLoggedIn[i].substring(playersLoggedIn[i].indexOf("\"")+1, playersLoggedIn[i].indexOf("@")); 
                Log.i("player = ",""+playersLoggedIn[i]);
            }
            Log.i("player for list = ", ""+ playersLoggedIn[0]);
            Log.i("me = ",""+credential.getSelectedAccountName());
            if (credential.getSelectedAccountName().contains(playersLoggedIn[0])) {
                start.setVisibility(View.VISIBLE);
            }
            else {
                start.setVisibility(View.INVISIBLE);
            }
        }catch(Exception e) {
            Log.e("Exeption ", ""+e.getMessage());
        }

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, playersLoggedIn);
        listView.setAdapter(adapter);

        adapter.notifyDataSetChanged();
    }

    public void updateGameLobby() {
			new UpdateAsync(onUpdateFinish, context, endpoint, gcm, credential.getSelectedAccountName()).execute();
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
                        new DoSomethingAsync(this, onUpdateFinish, endpoint, gcm, false).execute();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch(item.getItemId())
        {
            case R.id.update:
                updateGameLobby();
                break;
            case R.id.joinGame:
                intent = new Intent(context, Gameplay.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;
            case R.id.startGame:
				new DoSomethingAsync(context, onUpdateFinish, endpoint, gcm, true).execute();
                intent = new Intent(context, Gameplay.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;
        }
        return true;
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


	private class DoSomethingAsync extends AsyncTask<Void, Void, String> {
		private Myendpoint endpoint;
		private GoogleCloudMessaging gcm;
        private boolean startGame;
        private ProgressDialog dialog;
        private Context mContext;
        private OnUpdateFinish onUpdateFinish;

		public DoSomethingAsync(Context mContext, OnUpdateFinish onUpdateFinish, Myendpoint endpoint, GoogleCloudMessaging gcm, boolean startGame) {
			this.endpoint = endpoint;
			this.gcm = gcm;
            this.startGame = startGame;
            this.dialog = new ProgressDialog(mContext);
            this.mContext = mContext;
            this.onUpdateFinish = onUpdateFinish;
		}

        /**
         *
         * Displays the progress dialog box
         *
         */
        @Override
        protected void onPreExecute()
        {
            dialog.setMessage("Authenticating");
            dialog.show();
        } 

        @Override
        protected String doInBackground(Void... params) {
            try {
                if (gcm == null) {
                    gcm = GoogleCloudMessaging.getInstance(context);
                }
                return regid = gcm.register("699958132030");

            } catch (IOException ex) {
                return null;
            }
		}

		@Override
		protected void onPostExecute(String regId) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            Log.d("regid = ",""+regId);
            new GetCredentialAsync(mContext, onUpdateFinish, endpoint, gcm, regId, startGame).execute();;
		}
	}
}



