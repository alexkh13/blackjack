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
public enum Rank {

    TWO {
        @Override
        public String toString() { return "2"; }
    },
    THREE {
        @Override
        public String toString() { return "3"; }
    },
    FOUR {
        @Override
        public String toString() { return "4"; }
    },
    FIVE {
        @Override
        public String toString() { return "5"; }
    },
    SIX {
        @Override
        public String toString() { return "6"; }
    },
    SEVEN {
        @Override
        public String toString() { return "7"; }
    },
    EIGHT {
        @Override
        public String toString() { return "8"; }
    },
    NINE {
        @Override
        public String toString() { return "9"; }
    },
    TEN {
        @Override
        public String toString() { return "10"; }
    },
    JACK {
        @Override
        public String toString() { return "J"; }
    },
    QUEEN {
        @Override
        public String toString() { return "Q"; }
    },
    KING {
        @Override
        public String toString() { return "K"; }
    },
    ACE {
        @Override
        public String toString() { return "A"; }
    };

}