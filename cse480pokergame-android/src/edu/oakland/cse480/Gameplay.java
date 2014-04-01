package edu.oakland.cse480;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
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

		context = getApplicationContext();
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
		showBet.setText("Current bet: " + "100");

		List<String> players = result.getPlayers();
		String strPlayerMe = "zabador";
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
	public void paintCards (int bet, int hand1, int hand2, int card1, int card2, int card3, int card4, int card5 ){
		//Will pull down cards and integers, and send off values to each as integers. 
		//Integer conversions will take place in each method
		myHand(hand1, hand2);
		tableCards(card1, card2, card3, card4, card5);
//		showBet(bet);
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
				nmP1.setText(strplayer1 + "s turn");
				break;
			case 2:
				nmP2.setText(strplayer2 + "s turn");
				break;
			case 3:
				nmP3.setText(strplayer3 + "s turn");
				break;
			case 4:
				nmP4.setText(strPlayerMe + "s turn");
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
	public void generateCards(View view){
		//This button exists to test the card populators
		
		Random r = new Random();
		int i1=r.nextInt(52-1) + 1;
		int i2=r.nextInt(52-1) + 1;
		int i3=r.nextInt(52-1) + 1;
		int i4=r.nextInt(52-1) + 1;
		int i5=r.nextInt(52-1) + 1;
		int i6=r.nextInt(5-1) + 1;
		int i7=r.nextInt(5-1) + 1;
		int i8=r.nextInt(5-1) + 1;
		paintPlayers(i6, i7, i8, "Geoff", "Tara", "Mike", "Jon");
			
		myHand(i1, i2);
		tableCards(i1,i2,i3,i4,i5);
//		showBet(intBet);
		//Substitute values of 0 into the calls. It defaults to the ic_launcher image
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
	
	public void tableCards(int card1, int card2, int card3, int card4, int card5){
		String c1, c2, c3, c4, c5;
		c1 = decodeCards(card1);
		c2 = decodeCards(card2);
		c3 = decodeCards(card3);
		c4 = decodeCards(card4);
		c5 = decodeCards(card5);
		
		int tblCard1 = getResources().getIdentifier(c1, "drawable", getPackageName());
		tblC1 = (ImageView) findViewById(R.id.imgTable1);
		tblC1.setImageResource(tblCard1);
		
		int tblCard2 = getResources().getIdentifier(c2, "drawable", getPackageName());
		tblC2 = (ImageView) findViewById(R.id.imgTable2);
		tblC2.setImageResource(tblCard2);
		
		int tblCard3 = getResources().getIdentifier(c3, "drawable", getPackageName());
		tblC3 = (ImageView) findViewById(R.id.imgTable3);
		tblC3.setImageResource(tblCard3);
		
		int tblCard4 = getResources().getIdentifier(c4, "drawable", getPackageName());
		tblC4 = (ImageView) findViewById(R.id.imgTable4);
		tblC4.setImageResource(tblCard4);
		
		int tblCard5 = getResources().getIdentifier(c5, "drawable", getPackageName());
		tblC5 = (ImageView) findViewById(R.id.imgTable5);
		tblC5.setImageResource(tblCard5);
		
	}

	public String decodeCards(int cardNum){
		String cardValue;
		switch(cardNum) {
	    case 1:
	        cardValue = "c1";
	        break;
	    case 2:
	        cardValue = "c2";
	        break;
	    case 3:
	        cardValue = "c3";
	        break;
	    case 4:
	        cardValue = "c4";
	        break;
	    case 5:
	        cardValue = "c5";
	        break;
	    case 6:
	        cardValue = "c6";
	        break;
	    case 7:
	        cardValue = "c7";
	        break;
	    case 8:
	        cardValue = "c8";
	        break;
	    case 9:
	        cardValue = "c9";
	        break;
	    case 10:
	        cardValue = "c10";
	        break;
	    case 11:
	        cardValue = "cj";
	        break;
	    case 12:
	        cardValue = "cq";
	        break;
	    case 13:
	        cardValue = "ck";
	        break;
	    case 14:
	        cardValue = "d1";
	        break;
	    case 15:
	        cardValue = "d2";
	        break;
	    case 16:
	        cardValue = "d3";
	        break;
	    case 17:
	        cardValue = "d4";
	        break;
	    case 18:
	        cardValue = "d5";
	        break;
	    case 19:
	        cardValue = "d6";
	        break;
	    case 20:
	        cardValue = "d7";
	        break;
	    case 21:
	        cardValue = "d8";
	        break;
	    case 22:
	        cardValue = "d9";
	        break;
	    case 23:
	        cardValue = "d10";
	        break;
	    case 24:
	        cardValue = "dj";
	        break;
	    case 25:
	        cardValue = "dq";
	        break;
	    case 26:
	        cardValue = "dk";
	        break;
	    case 27:
	        cardValue = "h1";
	        break;
	    case 28:
	        cardValue = "h2";
	        break;
	    case 29:
	        cardValue = "h3";
	        break;
	    case 30:
	        cardValue = "h4";
	        break;
	    case 31:
	        cardValue = "h5";
	        break;
	    case 32:
	        cardValue = "h6";
	        break;
	    case 33:
	        cardValue = "h7";
	        break;
	    case 34:
	        cardValue = "h8";
	        break;
	    case 35:
	        cardValue = "h9";
	        break;
	    case 36:
	        cardValue = "h10";
	        break;
	    case 37:
	        cardValue = "hj";
	        break;
	    case 38:
	        cardValue = "hq";
	        break;
	    case 39:
	        cardValue = "hk";
	        break;
	    case 40:
	        cardValue = "s1";
	        break;
	    case 41:
	        cardValue = "s2";
	        break;
	    case 42:
	        cardValue = "s3";
	        break;
	    case 43:
	        cardValue = "s4";
	        break;
	    case 44:
	        cardValue = "s5";
	        break;
	    case 45:
	        cardValue = "s6";
	        break;
	    case 46:
	        cardValue = "s7";
	        break;
	    case 47:
	        cardValue = "s8";
	        break;
	    case 48:
	        cardValue = "s9";
	        break;
	    case 49:
	        cardValue = "s10";
	        break;
	    case 50:
	        cardValue = "sj";
	        break;
	    case 51:
	        cardValue = "sq";
	        break;
	    case 52:
	        cardValue = "sk";
	        break;
	    default:
	        cardValue = "ic_launcher";
	}
		return cardValue;
		}
	
	public void myHand(int card1int, int card2int){
		String card1string, card2string;
		card1string = decodeCards(card1int);
		card2string = decodeCards(card2int);
		int resCard1 = getResources().getIdentifier(card1string, "drawable", getPackageName());
		int resCard2 = getResources().getIdentifier(card2string, "drawable", getPackageName());
		card1 = (ImageView) findViewById(R.id.card1spot);
		card1.setImageResource(resCard1);
		card2 = (ImageView) findViewById(R.id.card2spot);
		card2.setImageResource(resCard2);
    }

}

