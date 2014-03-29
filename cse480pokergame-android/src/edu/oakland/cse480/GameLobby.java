package edu.oakland.cse480;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.InputType;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class GameLobby extends Activity {
	private String testmsg = "";
	public int intBet = 0;
	//GCMIntentService notify;
	public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game_lobby);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.game_lobby, menu);
		return true;
	}
	
	public void joinGame (View view){
		Intent intent = new Intent(this, Gameplay.class);
		//intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		startActivity(intent);
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


}
