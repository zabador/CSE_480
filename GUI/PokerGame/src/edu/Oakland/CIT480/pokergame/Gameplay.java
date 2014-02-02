package edu.Oakland.CIT480.pokergame;

import java.util.Random;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class Gameplay extends Activity {
	public ImageView card1;
	public ImageView card2;
	public ImageView tblC1, tblC2, tblC3, tblC4, tblC5;
	public TextView showBet;
	public TextView nmP1, nmP2, nmP3, nmP4;
	public TextView B1, B2, B3, B4;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gameplay);
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
		showBet(bet);
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
		
		switch(CurrentTurn){
			case 1:
				nmP1.setText(strplayer1 + " turn");
				break;
			case 2:
				nmP2.setText(strplayer2 + " turn");
				break;
			case 3:
				nmP3.setText(strplayer3 + " turn");
				break;
			case 4:
				nmP4.setText(strPlayerMe + "turn");
				break;
		}
		B1 = (TextView) findViewById(R.id.lblPlayer1Blind);
		B2 = (TextView) findViewById(R.id.lblPlayer2Blind);
		B3 = (TextView) findViewById(R.id.lblPlayer3Blind);
		B4 = (TextView) findViewById(R.id.lblYourBlind);
		switch(SMblind){
		case 1:
			B1.setText("Small Blind");
			break;
		case 2:
			B2.setText("Small Blind");
			break;
		case 3:
			B3.setText("Small Blind");
			break;
		case 4:
			B4.setText("Small Blind");
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
		paintPlayers(1, 2, 3, "Geoff", "Tara", "Mike", "Jon");
			
		myHand(i1, i2);
		tableCards(i1,i2,i3,i4,i5);
		//Substitute values of 0 into the calls. It defaults to the ic_launcher image
	}
	
	public void clickedBet(View view){
		//For now, do nothing. Eventually a prompt box
	}
	public void clickedFold(View view){
		//Skye you'll need to enter your fold info in here
	}
	public void showBet(int intBet){
		showBet = (TextView) findViewById(R.id.lblCurrentBet);
		showBet.setText("Current bet: " + intBet);
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
