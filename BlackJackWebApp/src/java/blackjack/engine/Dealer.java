/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package blackjack.engine;

import blackjack.engine.utils.XMLEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Alex
 */
public final class Dealer implements XMLEntity {
    
    private final int NUMBER_OF_DECKS = 6;
    private final ArrayList<Deck> decks = new ArrayList<>();
    
    private Bet bet;
    
    public Dealer(Bet bet) {
        this.bet = bet;
        for(int i=0;i<NUMBER_OF_DECKS;i++) {
            decks.add(new Deck());
        }
    }
    
    public Card hitCard() {
        int randomDeckIndex = new Random().nextInt(NUMBER_OF_DECKS);
        return decks.get(randomDeckIndex).pop();
    }
    
    public List<Card> hitCard(int count) {
        List<Card> cards = new ArrayList<>();
        for(int i=0; i<count; i++) {
            cards.add(hitCard());
        }
        return cards;
    }

    @Override
    public Object GetXMLEntity() {
        return bet.GetXMLEntity();
    }

    public int getRank() {
        return bet.getRank();
    }
    
    public void reset() {
        bet = new Bet(0);
        for(int i=0;i<NUMBER_OF_DECKS;i++) {
            decks.get(i).reset();
        }
    }
    
    public void DealCards() {
        do {
            bet.addCard(hitCard());
        }
        while(getRank() < 17);
    }
    
    public Card peek() {
        try {
            return bet.getCards().get(0);
        }
        catch(IndexOutOfBoundsException ex) {
            return null;
        }
    }

    public List<Card> getCards() {
        return bet.getCards();
    }
}
