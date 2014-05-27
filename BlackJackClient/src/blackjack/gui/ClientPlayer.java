/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package blackjack.gui;

import blackjack.wsclient.Card;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author idmlogic
 */
public class ClientPlayer {
    private final String playerName;
    private final float money;
    private final boolean human;
    private final ArrayList<Card> firstBet,secondBet;
    
    public ClientPlayer(String playerName, float money, boolean isHuman) {
        this.playerName = playerName;
        this.money = money;
        this.human = isHuman;
        firstBet = new ArrayList<>();
        secondBet = new ArrayList<>();
    }

    public String getPlayerName() {
        return playerName;
    }

    public float getMoney() {
        return money;
    }

    public boolean isHuman() {
        return human;
    }

    public List<Card> getFirstBet() {
        return firstBet;
    }
    
    
}
