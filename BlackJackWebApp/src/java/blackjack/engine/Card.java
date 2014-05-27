/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package blackjack.engine;

import blackjack.engine.enums.Rank;
import blackjack.engine.utils.XMLEntity;
import blackjack.engine.enums.Suit;

/**
 *
 * @author Alex
 */
public class Card implements XMLEntity{
    private final blackjack.engine.xml.Cards.Card card;
    private final Rank rank;
    private final Suit suit;
    
    public Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
        this.card = new blackjack.engine.xml.Cards.Card();
        card.setRank(blackjack.engine.xml.Rank.valueOf(rank.name()));
        card.setSuit(blackjack.engine.xml.Suit.valueOf(suit.name()));
    }
    
    public Card(blackjack.engine.xml.Cards.Card card) {
        this.card = card;
        this.rank = Rank.valueOf(card.getRank().value());
        this.suit = Suit.valueOf(card.getSuit().value());
    }
    
    public Rank getRank() {
        return rank;
    }
    
    @Override
    public String toString() {
        return this.rank + "" + this.suit;
    }
    
    @Override
    public Object GetXMLEntity() {
        return card;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Card other = (Card) obj;
        return this.rank == other.rank;
    }

    public Suit getSuit() {
        return suit;
    }
    
}
