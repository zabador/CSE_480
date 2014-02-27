package edu.oakland.cse480tester;
import java.util.Arrays;
import java.util.Collections;


public class GameLogic {

	public static void main(String[] args){
		
		Deck deck = new Deck();
		deck.shuffle();
		
		//Dealing first card
		Card p1_card1 = deck.deal();
		Card p2_card1 = deck.deal();
		
		//Dealing 2nd card
		Card p1_card2 = deck.deal();
		Card p2_card2 = deck.deal();
		
		//Discard a card
		Card not_used_card1 = deck.deal();
		
		//Dealing the flop 
		Card flop1 = deck. deal();
		Card flop2 = deck. deal();
		Card flop3 = deck. deal();
		
		//Discard a card
		Card not_used_card2 = deck.deal();
		
		//Dealing the turn
		Card turn = deck.deal();
		
		//Discard a card
		Card not_used_card3 = deck.deal();
		
		//Dealing the river
		Card river = deck.deal();
		
		//Printing player 1 & 2 cards and community cards (for testing purpose)
		System.out.println("Player 1 cards");
		System.out.println(p1_card1.toString());
		System.out.println(p1_card2.toString());
		
		System.out.println("\nPlayer 2 cards");
		System.out.println(p2_card1.toString());
		System.out.println(p2_card2.toString());
		
		System.out.println("\nFlop");
		System.out.println(flop1.toString());
		System.out.println(flop2.toString());
		System.out.println(flop3.toString());
		
		System.out.println("\nTurn");
		System.out.println(turn.toString());
		
		System.out.println("\nRiver");
		System.out.println(river.toString());
		
		//Player 1 cards + community cards
		Hand p1_7card_hand = new Hand();
		p1_7card_hand.addCard(p1_card1);
		p1_7card_hand.addCard(p1_card2);
		p1_7card_hand.addCard(flop1);
		p1_7card_hand.addCard(flop2);
		p1_7card_hand.addCard(flop3);
		p1_7card_hand.addCard(turn);
		p1_7card_hand.addCard(river);
		
		System.out.println("\nPlayer 1's 7 cards");
		System.out.println(p1_7card_hand.toString());
		
		//Player 2 cards + community cards
		Hand p2_7card_hand = new Hand();
		p2_7card_hand.addCard(p2_card1);
		p2_7card_hand.addCard(p2_card2);
		p2_7card_hand.addCard(flop1);
		p2_7card_hand.addCard(flop2);
		p2_7card_hand.addCard(flop3);
		p2_7card_hand.addCard(turn);
		p2_7card_hand.addCard(river);
		
		System.out.println("\nPlayer 2's 7 cards");
		System.out.println(p2_7card_hand.toString());
		
		
		//Player1's best 5-card hand
		HandEvaluator p1_hand_eval = new HandEvaluator();
		Hand p1_best_hand = new Hand();
		p1_best_hand = p1_hand_eval.getBest5CardHand(p1_7card_hand);
		
		System.out.println("\nPlayer 1's best hand");
		//Name of the best hand
		System.out.println(HandEvaluator.nameHand(p1_best_hand));
		System.out.println(p1_best_hand);
		
		
		//Player2's best 5-card hand
		HandEvaluator p2_hand_eval = new HandEvaluator();
		Hand p2_best_hand = new Hand();
		p2_best_hand = p2_hand_eval.getBest5CardHand(p2_7card_hand);
		
		System.out.println("\nPlayer 2's best hand");
		//Name of the best hand
		System.out.println(HandEvaluator.nameHand(p2_best_hand));
		System.out.println(p2_best_hand);	
		
		
		//Comparing player 1's and player 2's best hand
		int compare = HandEvaluator.compareHands(p1_best_hand, p2_best_hand);
		Hand winning_hand = new Hand();
		if (compare == 1){
			winning_hand = p1_best_hand;
			System.out.println("\nPlayer 1 wins!");
			//System.out.println(winning_hand);
		}
		
		if (compare == 2){
			winning_hand = p2_best_hand;
			System.out.println("\nPlayer 2 wins!");
			//System.out.println(winning_hand);
		}
		
		if (compare == 0){
			winning_hand = p1_best_hand;
			System.out.println("\nTie!");
		}
	}
}
