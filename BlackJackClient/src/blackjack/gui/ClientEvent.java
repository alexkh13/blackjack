/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package blackjack.gui;

import blackjack.wsclient.Action;
import blackjack.wsclient.Card;
import blackjack.wsclient.Event;
import blackjack.wsclient.EventType;
import java.util.List;

/**
 *
 * @author idmlogic
 */
public class ClientEvent {
    
    private String playerName = null;
    private List<Card> cardsList = null;
    private Type eventType = null;
    private float money = 0;
    private int id = 0;
    private int timeout;
    private boolean fault = false;
    
    public enum Type {
        DO_PLAYER_HIT,
        DO_PLAYER_SPLIT,
        DO_PLAYER_DOUBLE,
        DO_PLAYER_BET,
        DO_PLAYER_STAND,
        ANNOUNCE_NEW_ROUND,
        ANNOUNCE_GAME_START,
        ANNOUNCE_GAME_OVER,
        ANNOUNCE_PLAYER_CARDS,
        ANNOUNCE_PLAYER_WINNER,
        ANNOUNCE_PLAYER_TURN,
        ANNOUNCE_PLAYER_RESIGN,
        ANNOUNCE_DEALER_CARDS,
        PROMPT_PLAYER_FOR_ACTION
    }
    
    public ClientEvent() {
        
    }
    
    public ClientEvent(Event event) {
        if(event != null) {
            id = event.getId();
            playerName = event.getPlayerName();
            cardsList = event.getCards();
            money = event.getMoney();
            timeout = event.getTimeout();
            eventType = getTypeByType(event);
        }
    }
    
    private Type getTypeByType(Event event) {
        switch(event.getType()) {
            case CARDS_DEALT:
                return (event.getPlayerName() != null) ? Type.ANNOUNCE_PLAYER_CARDS : Type.ANNOUNCE_DEALER_CARDS;
            case PLAYER_RESIGNED:
                return Type.ANNOUNCE_PLAYER_RESIGN;
            case PLAYER_TURN:
                return Type.ANNOUNCE_PLAYER_TURN;
            case GAME_WINNER:
                return Type.ANNOUNCE_PLAYER_WINNER;
            case USER_ACTION:
                return getTypeByAction(event);
            case PROMPT_PLAYER_TO_TAKE_ACTION:
                return Type.PROMPT_PLAYER_FOR_ACTION;
            case NEW_ROUND:
                return Type.ANNOUNCE_NEW_ROUND;
            case GAME_START:
                return Type.ANNOUNCE_GAME_START;
            case GAME_OVER:
                return Type.ANNOUNCE_GAME_OVER;
        }
        return null;
    }
    
    private Type getTypeByAction(Event event) {
        switch(event.getPlayerAction()) {
            case PLACE_BET:
                return Type.DO_PLAYER_BET;
            case DOUBLE:
                return Type.DO_PLAYER_DOUBLE;
            case HIT:
                return Type.DO_PLAYER_HIT;
            case SPLIT:
                return Type.DO_PLAYER_SPLIT;
            case STAND:
                return Type.DO_PLAYER_STAND;
        }
        return null;
    }
    
    public void setFaultEvent() {
        this.fault = true;
    }
    
    public boolean getFault() {
        return this.fault;
    }

    public boolean isEndGame() {
        return false;
    }

    public Type getType() {
        return eventType;
    }
    
    public String getPlayer() {
        return playerName;
    }
    
    public List<Card> getCards() {
        return cardsList;
    }
    
    public Card getCard() {
        return cardsList.get(0);
    }
    
    public float getMoney() {
        return money;
    }
    
    public int getId() {
        return id;
    }
    
    public int getTimeout() {
        return timeout;
    }
    
    public ClientEvent copy(ClientEvent event) {
        if(event != null) {
            this.id = event.getId();
            this.timeout = event.getTimeout();
            this.eventType = event.getType();
            this.money = event.getMoney();
            this.playerName = event.getPlayer();
            this.cardsList = event.getCards();
            return this;
        }
        else {
            return null;
        }
    }
}
