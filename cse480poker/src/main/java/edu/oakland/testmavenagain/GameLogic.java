package edu.oakland.cse480tester;

public class GameLogic {

	public static void main(String[] args){
		gameStart(7);
	}
	public static void gameStart(int n){
		Deck deck = new Deck();
		deck.shuffle();
		
		//Array of all players' hands
		Hand [] playerHand = new Hand[n];	
		for(int x=0; x<n; x++){
			playerHand[x] = new Hand();
		}
		
		//Dealing 2 cards for each player
		for (int i=0; i<2; i++){
			for (int j=0; j<n; j++){
				playerHand[j].addCard(deck.deal());
			}
		}
		
		for (int i=0; i<n; i++){
			String prtCard = Integer.toString(i+1);
			System.out.println("Player" + prtCard + "'s cards: " + playerHand[i]);
		}
		
		System.out.print("\nFlop Turn River: ");
		//Flop, Turn, and River cards
		Card [] ftr = new Card[5];
		for (int i=0; i<5; i++){
			ftr[i] = deck.deal();
			System.out.print( ftr[i] + " ");
		}
		System.out.println("\n");
		
		//Forming 7-card hand for each player
		for (int i=0; i<n; i++){
			for (int j=0; j<5; j++){
				playerHand[i].addCard(ftr[j]);
			}
		}
		
		for (int a=0; a<n; a++){
			String prtCard = Integer.toString(a+1);
			System.out.println("Player" + prtCard + "'s hand: " + playerHand[a]);
		}
		
		HandEvaluator [] handEval = new HandEvaluator[n];	
		for(int i=0; i<n; i++){
			handEval[i] = new HandEvaluator();
		}	
		
		//Finding the best hand for each player
		Hand [] bestHand = new Hand[n];
		for (int i=0; i<n; i++){
			bestHand[i] = handEval[i].getBest5CardHand(playerHand[i]);
		}
		
		for (int a=0; a<n; a++){
			String prtCard = Integer.toString(a+1);
			System.out.println("\nPlayer " + prtCard + " best hand: " + bestHand[a]);
			System.out.println(HandEvaluator.nameHand(bestHand[a]));
		}
		
		//Determining the winning hand
		Hand winning_hand = new Hand();
		winning_hand = playerHand[0];
		for (int i=0; i<n-1; i++){
			int compare = HandEvaluator.compareHands(winning_hand, playerHand[i+1]);
			if (compare == 1){
			//do nothing	
			}
			
			if (compare == 2){
				winning_hand = playerHand[i+1];
			}
			
			if (compare == 0){
			// do nothing
			}
		}
		
		System.out.println("\nWinning hand: " + winning_hand);
		System.out.println(HandEvaluator.nameHand(winning_hand));

		}
	}
