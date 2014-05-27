/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package blackjack.engine;

import blackjack.engine.enums.Rank;
import blackjack.engine.utils.XMLEntity;
import blackjack.engine.exceptions.IllegalActionException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Alex
 */
public class Bet implements XMLEntity {
    private final blackjack.engine.xml.Bet bet;
    private final List<Card> cards;
    private boolean declaredStand = false;

    public Bet(float sum) {
        this.bet = new blackjack.engine.xml.Bet();
        this.bet.setSum(sum);
        this.bet.setCards(new blackjack.engine.xml.Cards());
        this.cards = new ArrayList<>();
    }
    
    public Bet(blackjack.engine.xml.Bet bet) {
        this.bet = bet;
        this.cards = new ArrayList<>();
        // handle empty bets
        if(bet.getCards() == null) {
            bet.setCards(Game.factory.createCards());
        }
        // popolute cards
        for(blackjack.engine.xml.Cards.Card card : bet.getCards().getCard()) {
            cards.add(new Card(card));
        }
    }
    
    public float getSum() {
        return bet.getSum();
    }
    
    public List<Card> getCards() {
        return cards;
    }
    
    private static int rankToInt(Rank rank) {
        switch(rank) {
            case TWO: return 2;
            case THREE: return 3;
            case FOUR: return 4;
            case FIVE: return 5;
            case SIX: return 6;
            case SEVEN: return 7;
            case EIGHT: return 8;
            case NINE: return 9;
            case ACE: return 11;
            default: return 10;
        }
    }
    
    public int getRank() {
        int totalRank = 0;
        int numberOfAces = 0;
        for(Card card : cards) {
            if(card.getRank() == Rank.ACE) {
                numberOfAces++;
            }
            totalRank += rankToInt(card.getRank());
        }
        while(totalRank > 21 && numberOfAces > 0) {
            totalRank -= 10;
            numberOfAces--;
        }
        return totalRank;
    }
    
    public int addCard(Card card) {
        cards.add(card);
        bet.getCards().getCard().add((blackjack.engine.xml.Cards.Card)card.GetXMLEntity());
        return getRank();
    }
    
    public void declareStand() {
        declaredStand = true;
    }

    public boolean isActive() {
        return !declaredStand && getRank() < 21;
    }
    
    @Override
    public Object GetXMLEntity() {
        return bet;
    }

    public void addCards(List<Card> cards) {
        for(Card card : cards) {
            addCard(card);
        }
    }

    public void clear() {
        cards.clear();
    }
    
    public Bet split() {
        if(!cards.get(0).equals(cards.get(1))) {
            Bet secondBet = new Bet(getSum());
            secondBet.addCard(cards.remove(1));
            return secondBet;
        }
        else throw new IllegalActionException("Cards not equal");
    }

    public void Double() {
        bet.setSum(bet.getSum()*2);
    }
}
