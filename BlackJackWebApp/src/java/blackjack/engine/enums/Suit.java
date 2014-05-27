/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package blackjack.engine.enums;

/**
 *
 * @author Alex
 */
public enum Suit {

    CLUBS {
        @Override
        public String toString() { return "♣"; }
    },
    DIAMONDS {
        @Override
        public String toString() { return "♦"; }
    },
    HEARTS {
        @Override
        public String toString() { return "♥"; }
    },
    SPADES {
        @Override
        public String toString() { return "♠"; }
    };
    
}
