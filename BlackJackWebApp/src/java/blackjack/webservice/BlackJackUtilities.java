/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package blackjack.webservice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import ws.blackjack.Card;
import ws.blackjack.Rank;
import ws.blackjack.Suit;

/**
 *
 * @author idmlogic
 */
public class BlackJackUtilities {

    private static Rank getRank(blackjack.engine.enums.Rank rank) {
        switch(rank) {
            case TWO:
                return Rank.TWO;
            case THREE:
                return Rank.THREE;
            case FOUR:
                return Rank.FOUR;
            case FIVE:
                return Rank.FIVE;
            case SIX:
                return Rank.SIX;
            case SEVEN:
                return Rank.SEVEN;
            case EIGHT:
                return Rank.EIGHT;
            case NINE:
                return Rank.NINE;
            case TEN:
                return Rank.TEN;
            case JACK:
                return Rank.JACK;
            case QUEEN:
                return Rank.QUEEN;
            case KING:
                return Rank.KING;
            case ACE:
                return Rank.ACE;
            default:
                return null;
        }
    }
    
    private static Suit getSuit(blackjack.engine.enums.Suit suit) {
        switch(suit) {
            case CLUBS:
                return Suit.CLUBS;
            case DIAMONDS:
                return Suit.DIAMONDS;
            case HEARTS:
                return Suit.HEARTS;
            case SPADES:
                return Suit.SPADES;
            default:
                return null;
        }
    }
    
    private static Card convertCard(blackjack.engine.Card card) {
        Card result = new Card();
        result.setRank(getRank(card.getRank()));
        result.setSuit(getSuit(card.getSuit()));
        return result;
    }
    
    public static Collection<? extends Card> cardsListConverter(List<blackjack.engine.Card> cards) {
        List<Card> list = new ArrayList<>();
        if(cards != null) {
            for(blackjack.engine.Card card : cards) {
                list.add(convertCard(card));
            }
        }
        return list;
    }
}
