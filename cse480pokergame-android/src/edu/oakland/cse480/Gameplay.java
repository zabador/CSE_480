package edu.oakland.cse480;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.jackson.JacksonFactory;
import com.appspot.testmavenagain.myendpoint.Myendpoint;
import com.appspot.testmavenagain.myendpoint.model.MyRequest;
import com.appspot.testmavenagain.myendpoint.model.MyResult;

import android.os.AsyncTask;
import android.os.Bundle;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.InputType;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;


public class Gameplay extends Activity implements OnUpdateFinish {
	public ImageView card1;
	public ImageView card2;
	public ImageView tblC1, tblC2, tblC3, tblC4, tblC5;
	public TextView showBet;
	public TextView nmP1, nmP2, nmP3, nmP4;
	public TextView B1, B2, B3, B4;
	private String betText = "";
	public int intBet = 0;
	private GoogleCloudMessaging gcm;
	private AtomicInteger msgId = new AtomicInteger();
	private SharedPreferences prefs;
	private Context context;
	private OnUpdateFinish onUpdateFinish;

	// defind constants
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	private static final String TAG = "poker";
	private final String SENDER_ID = "699958132030";

	private Myendpoint endpoint;
	private GoogleAccountCredential credential;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gameplay);

		context = this;
		onUpdateFinish = this;

		// handle the button click for joining game
		Button bet = (Button) findViewById(R.id.btnBet);
		bet.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				new PlaceBetAsync(onUpdateFinish, context, endpoint, gcm).execute();
			}
		});

		endpoint = CredentialHack.endpoint;
        credential = CredentialHack.credential;

		boolean getGameState = true;
        MyResult r = null;
		try {
			new UpdateAsync(this, this, endpoint, gcm, getGameState, credential.getSelectedAccountName()).execute();
    
		} catch (Exception ie) {
            Log.e("Erro", ""+ie.getMessage(), ie);
        }
	}

	public void onPlaceBetFinish() {
		Toast toast = Toast.makeText(this, "you placed a bet", Toast.LENGTH_SHORT);
		toast.show();

	}

	public void onGetGameStateFinish(MyResult result) {
		Map<String, Object> map = result.getGameState();
		String currentBet = (String)map.get("currentBet");
		int currentTurn = Integer.parseInt((String)map.get("currentPosition"));
		showBet = (TextView) findViewById(R.id.lblCurrentBet);
		showBet.setText("Current bet: " + currentBet);

		List<String> players = result.getPlayers();
        // strip away last part of email address so we can just have a user name
		String tmpStrPlayerMe = (String)map.get("me");
        String strPlayerMe = tmpStrPlayerMe.substring(0, tmpStrPlayerMe.indexOf("@"));
		String strPlayer1 = "";
		String strPlayer2 = "";
		String strPlayer3 = "";
		try {
			strPlayer1 = players.get(0).substring(players.get(0).indexOf("\""), players.get(0).indexOf("@")); 
        }catch(Exception e) {
			strPlayer1 = "No Player";
		}
		try {
			strPlayer2 = players.get(1).substring(players.get(1).indexOf("\""), players.get(1).indexOf("@"));
		}catch(Exception e) {
			strPlayer2 = "No Player";
		}
		try {
			strPlayer3 = players.get(2).substring(players.get(2).indexOf("\""), players.get(2).indexOf("@"));
		}catch(Exception e) {
			strPlayer3 = "No Player";
		}

		paintPlayers(currentTurn, 1, 2, strPlayerMe, strPlayer1, strPlayer2, strPlayer3);

		// We need to swap the characters in the file name to match the files. The Poker api sends names like 8c, we need c8
		String tempHandCards = (String)map.get("handCards");
		Log.i("tempHandCards = ", ""+ tempHandCards);

		String[] handCards = tempHandCards.split(" ");
		String handCard1 = swap(handCards[1]); // handCards[0] is empty
		String handCard2 = swap(handCards[2]);
	
		Log.i("first card = ", ""+ handCards[0]);

		paintCards(handCard1, handCard2, "ic_launcher","ic_launcher","ic_launcher","ic_launcher","ic_launcher");
		
		
	}

	public String swap(String s) {

		char[] tempCard = s.toCharArray();
		char temp = tempCard[0];
		tempCard[0] = tempCard[1];
		tempCard[1] = temp;

		return new String(tempCard).toLowerCase();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.gameplay, menu);
		return true;
	}
//(Current turn, Not blind, bet amount, player count, cards in hand, cards on table)
//Cards start with Ace, 2,3,4,5...
//c is clubs, d is diamonds, h is hearts, s is spades. s2 for ace
	public void paintCards (String hand1, String hand2, String card1, String card2, String card3, String card4, String card5 ){
		//Will pull down cards and integers, and send off values to each as integers. 
		//Integer conversions will take place in each method
		myHand(hand1, hand2);
		tableCards(card1, card2, card3, card4, card5);
	}
	public void paintPlayers(int CurrentTurn, int SMblind, int BGblind, String strPlayerMe, String strplayer1, String strplayer2, String strplayer3){
		nmP1 = (TextView) findViewById(R.id.txtPlayer1);
		nmP1.setText(strplayer1);
		
		nmP2 = (TextView) findViewById(R.id.txtPlayer2);
		nmP2.setText(strplayer2);
		
		nmP3 = (TextView) findViewById(R.id.txtPlayer3);
		nmP3.setText(strplayer3);
		
		nmP4 = (TextView) findViewById(R.id.txtPlayerMe);
		nmP4.setText(strPlayerMe);
		//need a loop
		//No loop. Function and call it a few times. 
		for (int i = 0; i <4; i++){
		switch(CurrentTurn){
			case 1:
				nmP1.setText(strplayer1 + "'s turn");
				break;
			case 2:
				nmP2.setText(strplayer2 + "'s turn");
				break;
			case 3:
				nmP3.setText(strplayer3 + "'s turn");
				break;
			case 4:
				nmP4.setText(strPlayerMe + "'s turn");
				break;
		}
		}
		B1 = (TextView) findViewById(R.id.lblPlayer1Blind);
		B2 = (TextView) findViewById(R.id.lblPlayer2Blind);
		B3 = (TextView) findViewById(R.id.lblPlayer3Blind);
		B4 = (TextView) findViewById(R.id.lblYourBlind);
		switch(SMblind){
		case 1:
			B1.setText("Small Blind");
			B2.setText("Not Blind");
			B3.setText("Not Blind");
			B4.setText("Not Blind");
			break;
		case 2:
			B2.setText("Small Blind");
			B3.setText("Not Blind");
			B4.setText("Not Blind");
			B1.setText("Not Blind");
			break;
		case 3:
			B3.setText("Small Blind");
			B2.setText("Not Blind");
			B1.setText("Not Blind");
			B4.setText("Not Blind");
			break;
		case 4:
			B4.setText("Small Blind");
			B2.setText("Not Blind");
			B3.setText("Not Blind");
			B1.setText("Not Blind");
			break;
		}
		switch(BGblind){
		case 1:
			B1.setText("Big Blind");
		
			break;
		case 2:
			B2.setText("Big Blind");
		
			break;
		case 3:
			B3.setText("Big Blind");
			
			break;
		case 4:
			B4.setText("Big Blind");
		
			break;
		}
		
	}
	
	public void clickedBet(View view){
		//For now, do nothing. Eventually a prompt box
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Enter your bid");

		// Set up the input
		final EditText input = new EditText(this);
		// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
		input.setInputType(InputType.TYPE_CLASS_NUMBER);
		builder.setView(input);

		// Set up the buttons
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        betText = input.getText().toString();
		        try {
		            intBet = Integer.parseInt(betText);
		        } catch(NumberFormatException nfe) {

		        } 
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
	public void clickedFold(View view){
		//Skye you'll need to enter your fold info in here
	}
	
	public void tableCards(String flopCard1, String flopCard2, String flopCard3, String turnCard, String riverCard){
		int tblCard1 = getResources().getIdentifier(flopCard1, "drawable", getPackageName());
		tblC1 = (ImageView) findViewById(R.id.imgTable1);
		tblC1.setImageResource(tblCard1);
		
		int tblCard2 = getResources().getIdentifier(flopCard2, "drawable", getPackageName());
		tblC2 = (ImageView) findViewById(R.id.imgTable2);
		tblC2.setImageResource(tblCard2);
		
		int tblCard3 = getResources().getIdentifier(flopCard3, "drawable", getPackageName());
		tblC3 = (ImageView) findViewById(R.id.imgTable3);
		tblC3.setImageResource(tblCard3);
		
		int tblCard4 = getResources().getIdentifier(turnCard, "drawable", getPackageName());
		tblC4 = (ImageView) findViewById(R.id.imgTable4);
		tblC4.setImageResource(tblCard4);
		
		int tblCard5 = getResources().getIdentifier(riverCard, "drawable", getPackageName());
		tblC5 = (ImageView) findViewById(R.id.imgTable5);
		tblC5.setImageResource(tblCard5);
		
	}
	
	public void myHand(String handCard1, String handCard2){
		int resCard1 = getResources().getIdentifier(handCard1, "drawable", getPackageName());
		int resCard2 = getResources().getIdentifier(handCard2, "drawable", getPackageName());
		card1 = (ImageView) findViewById(R.id.card1spot);
		card1.setImageResource(resCard1);
		card2 = (ImageView) findViewById(R.id.card2spot);
		card2.setImageResource(resCard2);
    }

}

