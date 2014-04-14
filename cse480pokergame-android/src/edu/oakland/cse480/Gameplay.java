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
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;


public class Gameplay extends Activity implements OnUpdateFinish {
	public ImageView card1;
	public ImageView card2;
	public ImageView tblC1, tblC2, tblC3, tblC4, tblC5;
	public TextView currentBet, pot, lblToCall, tokens;
	public TextView nmP1, nmP2, nmP3, nmP4, nmP5, nmP6;
	public TextView B1, B2, B3, B4;
	private String betText = "";
	public int intBet = 0;
	private GoogleCloudMessaging gcm;
	private Context context;
	private OnUpdateFinish onUpdateFinish;
    private Button btnRaise, btnFold, btnCall;
    private int toCall;
    private final int FOLD = -1;
    private final int CALL = 0;
    private int intTokens;

	// defind constants
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";

	private Myendpoint endpoint;
	private GoogleAccountCredential credential;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gameplay);

		context = this;
		onUpdateFinish = this;
        final TextView winner = (TextView)findViewById(R.id.lblWinner);

		endpoint = CredentialHack.endpoint;
        credential = CredentialHack.credential;

        this.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                winner.setText(intent.getStringExtra("WINNNER"));
            
                updateActivity();
            }
        }, new IntentFilter("UpdateGamePlay"));

        this.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateActivity();
            
            }
        }, new IntentFilter("UpdateGameLobby"));

        btnRaise = (Button)findViewById(R.id.btnRaise);
        btnCall = (Button)findViewById(R.id.btnCall);
        btnFold = (Button)findViewById(R.id.btnFold);
        MyResult r = null;
		try {
			new UpdateAsync(onUpdateFinish, context, endpoint, gcm, credential.getSelectedAccountName()).execute();
    
		} catch (Exception ie) {
            Log.e("Erro", ""+ie.getMessage(), ie);
        }
	}

	public void onPlaceBetFinish() {
        
        btnRaise.setVisibility(View.INVISIBLE);
        btnCall.setVisibility(View.INVISIBLE);
        btnFold.setVisibility(View.INVISIBLE);

        new UpdateAsync(onUpdateFinish, context, endpoint, gcm, credential.getSelectedAccountName()).execute();

	}

    /** 
     * This will get called when the listener hears 
     * a broadcasted messge
     */
    public void updateActivity() {
        Log.i("it made it to the update","");
        new UpdateAsync(onUpdateFinish, context, endpoint, gcm, credential.getSelectedAccountName()).execute();
    }

	public void onGetGameStateFinish(MyResult result) {

        // pull information out of parameter 
		Map<String, Object> map = result.getGameState();
		int currentTurn = Integer.parseInt((String)map.get("currentPlayer"));
		List<String> players = result.getPlayers();

        // take care of player and card information
        handlePlayers(map, players, currentTurn);
        handleCards(map);

        // display current bet on screen
		String strHighestBet = (String)map.get("highestBet");
		currentBet = (TextView) findViewById(R.id.lblCurrentBet);
		currentBet.setText("Current bet: " + strHighestBet);

		String strPot = (String)map.get("pot");
        int intPot = Integer.parseInt(strPot);
		pot = (TextView) findViewById(R.id.lblPot);
		pot.setText("Pot: " + strPot);

		toCall = Integer.parseInt((String)map.get("highestBet")) - Integer.parseInt((String)map.get("currentBet"));
		lblToCall = (TextView) findViewById(R.id.lblToCall);
		lblToCall.setText("To Call: " + Integer.toString(toCall));

		String strTokens = (String)map.get("tokens");
        intTokens = Integer.parseInt(strTokens);
		tokens = (TextView) findViewById(R.id.lblTokens);
		tokens.setText("Tokens: " + strTokens);
        // only disply bet button when it is players turn
        int myTurn = Integer.parseInt((String)map.get("currentPosition"));
        if (myTurn == currentTurn) {
                btnRaise.setVisibility(View.VISIBLE);
                btnCall.setVisibility(View.VISIBLE);
                btnFold.setVisibility(View.VISIBLE);
        }
	}

    /** 
     * Method will handle players information returning from 
     * server so it can be painted to screen
     */
    public void handlePlayers(Map<String, Object> map, List<String> players, int currentTurn) {

        // strip away last part of email address so we can just have a user name
		String strPlayer1 = "";
		String strPlayer2 = "";
		String strPlayer3 = "";
		String strPlayer4 = "";
		String strPlayer5 = "";
		String strPlayer6 = "";
		try {
			strPlayer1 = players.get(0).substring(players.get(0).indexOf("\"")+1, players.get(0).indexOf("@")); 
        }catch(Exception e) {
			strPlayer1 = "No Player";
		}
		try {
			strPlayer2 = players.get(1).substring(players.get(1).indexOf("\"")+1, players.get(1).indexOf("@"));
		}catch(Exception e) {
			strPlayer2 = "No Player";
		}
		try {
			strPlayer3 = players.get(2).substring(players.get(2).indexOf("\"")+1, players.get(2).indexOf("@"));
		}catch(Exception e) {
			strPlayer3 = "No Player";
		}
		try {
			strPlayer4 = players.get(3).substring(players.get(3).indexOf("\"")+1, players.get(3).indexOf("@"));
		}catch(Exception e) {
			strPlayer4 = "No Player";
		}
		try {
			strPlayer5 = players.get(4).substring(players.get(4).indexOf("\"")+1, players.get(4).indexOf("@"));
		}catch(Exception e) {
			strPlayer5 = "No Player";
		}
		try {
			strPlayer6 = players.get(5).substring(players.get(5).indexOf("\"")+1, players.get(5).indexOf("@"));
		}catch(Exception e) {
			strPlayer6 = "No Player";
		}

		paintPlayers(currentTurn, 1, 2,strPlayer1, strPlayer2, strPlayer3, strPlayer4, strPlayer5,strPlayer6);
    }

    /** 
     * Method will handle card information returning from 
     * server so it can be painted to screen
     */
    public void handleCards(Map<String,Object> map) {

		// We need to swap the characters in the file name to match the files. The Poker api sends names like 8c, we need c8
		String tempHandCards = (String)map.get("handCards");
		String tempFlop = (String)map.get("flop");
		String tempTurn = (String)map.get("turn");
		String tempRiver = (String)map.get("river");

		Log.i("tempHandCards = ", ""+ tempHandCards);

		// swap hand cards to match file name
		String[] handCards = tempHandCards.trim().split(" ");
		String handCard1 = swap(handCards[0]); 
		String handCard2 = swap(handCards[1]);
	
		Log.i("flopCards = ", ""+ tempFlop);
		String[] flopCards = tempFlop.trim().split(" ");
		String flop1;
		String flop2;
		String flop3;
		String turn;
		String river;
		
		if(flopCards[0].equals("ic_launcher")) {
			flop1 = flopCards[0];  // keep it as ic_launcher
			flop2 = flopCards[0];
			flop3 = flopCards[0];
		}
		else {
			// swap flop cards to match file name
			flop1 = swap(flopCards[0]); 
			flop2 = swap(flopCards[1]);
			flop3 = swap(flopCards[2]);
		}

		String turnCard = tempTurn.trim();
		if(turnCard.equals("ic_launcher")) {
			turn = turnCard; 
		}
		else {
			// swap turn card to match file name
			turn = swap(turnCard); 
		}

		String riverCard = tempRiver.trim();
		if(turnCard.equals("ic_launcher")) {
			river = riverCard; 
		}
		else {
			// swap river card to match file name
			river = swap(riverCard); 
		}

		paintCards(handCard1, handCard2, flop1, flop2, flop3, turn, river);
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
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.update:
                new UpdateAsync(onUpdateFinish, context, endpoint, gcm, credential.getSelectedAccountName()).execute();
                break;
        }
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
	public void paintPlayers(int CurrentTurn, int SMblind, int BGblind, String strplayer1, String strplayer2, String strplayer3, String strPlayer4, String strPlayer5, String strPlayer6){
		nmP1 = (TextView) findViewById(R.id.txtPlayer1);
		nmP1.setText(strplayer1);
		
		nmP2 = (TextView) findViewById(R.id.txtPlayer2);
		nmP2.setText(strplayer2);
		
		nmP3 = (TextView) findViewById(R.id.txtPlayer3);
		nmP3.setText(strplayer3);
		
		nmP4 = (TextView) findViewById(R.id.txtPlayer4);
		nmP4.setText(strPlayer4);

		nmP5 = (TextView) findViewById(R.id.txtPlayer5);
		nmP5.setText(strPlayer5);

		nmP6 = (TextView) findViewById(R.id.txtPlayer6);
		nmP6.setText(strPlayer6);
		//need a loop
		//No loop. Function and call it a few times. 
		switch(CurrentTurn){
			case 1:
				nmP1.setText(strplayer1 + "'s turn");
                nmP1.setTextColor(Color.RED);
                nmP6.setTextColor(Color.BLACK);
                nmP5.setTextColor(Color.BLACK);
                nmP4.setTextColor(Color.BLACK);
                nmP3.setTextColor(Color.BLACK);
                nmP2.setTextColor(Color.BLACK);
				break;
			case 2:
				nmP2.setText(strplayer2 + "'s turn");
                nmP2.setTextColor(Color.RED);
                nmP6.setTextColor(Color.BLACK);
                nmP5.setTextColor(Color.BLACK);
                nmP4.setTextColor(Color.BLACK);
                nmP3.setTextColor(Color.BLACK);
                nmP1.setTextColor(Color.BLACK);
				break;
			case 3:
				nmP3.setText(strplayer3 + "'s turn");
                nmP3.setTextColor(Color.RED);
                nmP6.setTextColor(Color.BLACK);
                nmP5.setTextColor(Color.BLACK);
                nmP4.setTextColor(Color.BLACK);
                nmP2.setTextColor(Color.BLACK);
                nmP1.setTextColor(Color.BLACK);
                nmP2.setTextColor(Color.BLACK);
				break;
			case 4:
				nmP4.setText(strPlayer4 + "'s turn");
                nmP4.setTextColor(Color.RED);
                nmP6.setTextColor(Color.BLACK);
                nmP5.setTextColor(Color.BLACK);
                nmP2.setTextColor(Color.BLACK);
                nmP3.setTextColor(Color.BLACK);
                nmP1.setTextColor(Color.BLACK);
                nmP3.setTextColor(Color.BLACK);
				break;
			case 5:
				nmP4.setText(strPlayer5 + "'s turn");
                nmP5.setTextColor(Color.RED);
                nmP6.setTextColor(Color.BLACK);
                nmP2.setTextColor(Color.BLACK);
                nmP4.setTextColor(Color.BLACK);
                nmP3.setTextColor(Color.BLACK);
                nmP1.setTextColor(Color.BLACK);
                nmP4.setTextColor(Color.BLACK);
				break;
			case 6:
				nmP4.setText(strPlayer6 + "'s turn");
                nmP6.setTextColor(Color.RED);
                nmP2.setTextColor(Color.BLACK);
                nmP5.setTextColor(Color.BLACK);
                nmP4.setTextColor(Color.BLACK);
                nmP3.setTextColor(Color.BLACK);
                nmP1.setTextColor(Color.BLACK);
                nmP5.setTextColor(Color.BLACK);
				break;
		}
	}
	
	public void clickedRaise(View view){
        final String[] betAmounts = new String[intTokens/5];
        for (int i=0; i < intTokens/5; i++) {
            betAmounts[i] = Integer.toString((i+1)*5);
        }

        final ArrayAdapter<String> adp = new ArrayAdapter<String>(context,
                            R.layout.spinner, betAmounts);
        final Spinner spinner = new Spinner(context);
        spinner.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 30));
        spinner.setAdapter(adp);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(spinner);
        builder.setPositiveButton("BET", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                int betAmount = Integer.parseInt(spinner.getSelectedItem().toString());
                new PlaceBetAsync(onUpdateFinish, context, endpoint, gcm, betAmount).execute();
            }
        });
        builder.create().show();
	}

	public void clickedFold(View view){
        new PlaceBetAsync(onUpdateFinish, context, endpoint, gcm, FOLD).execute();
	}

	public void clickedCall(View view){
        new PlaceBetAsync(onUpdateFinish, context, endpoint, gcm, CALL).execute();
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

	private class DoSomethingAsync extends AsyncTask<Void, Void, MyResult> {
		private Myendpoint endpoint;
        private ProgressDialog dialog;
        private boolean newGame;

		public DoSomethingAsync(Context mContext, Myendpoint endpoint, boolean newGame) {
			this.endpoint = endpoint;
            this.newGame = newGame;
            this.dialog = new ProgressDialog(mContext);
		}

        /**
         *
         * Displays the progress dialog box
         *
         */
        @Override
        protected void onPreExecute()
        {
            dialog.setMessage("Starting Next Game");
            dialog.show();
        } 

        @Override
        protected MyResult doInBackground(Void... params) {

            try {
                MyRequest r = new MyRequest();
                r.setFirstRound(newGame);
                return endpoint.startGame(r).execute();
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
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
		}
	}
}

