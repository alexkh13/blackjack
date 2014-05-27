/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package blackjack.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import blackjack.engine.enums.Rank;
import blackjack.engine.enums.Suit;

/**
 *
 * @author Alex
 */
public class Deck {
    
    private final Stack<Card> cards = new Stack<>();
    
    public Deck() {
        rebuild();
    }
    
    private void rebuild() {
        cards.clear();
        for(Rank rank : Rank.values()) {
            for(Suit suit: Suit.values()) {
                cards.push(new Card(rank,suit));
            }
        }
        Collections.shuffle(cards);
    }
    
    public Card pop() {
        return cards.pop();
    }

    void reset() {
        rebuild();
    }
}
