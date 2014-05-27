/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package blackjack.engine.utils;

import blackjack.engine.types.ActionType;
import blackjack.engine.Card;
import blackjack.engine.types.EventType;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Alex
 */
public class Event {
    
    private int id;
    private String playerName;
    private EventType eventType;
    private ActionType actionType;
    private float number;
    private List<Card> cards;
    
    public Event() {
        
    }
    
    public Event(int eventId, String playerName, EventType eventType, ActionType actionType, List<Card> cards, float money) {
        this.id = eventId;
        this.playerName = playerName;
        this.eventType = eventType;
        this.actionType = actionType;
        this.cards = cards;
        this.number = money;
    }

    public int getId() {
        return id;
    }
    
    void setId(int i) {
        this.id = i;
    }

    public String getPlayerName() {
        return playerName;
    }
    
    public void setPlayerName(String name) {
        playerName = name;
    }

    public EventType getEventType() {
        return eventType;
    }
    
    public void setEventType(EventType type) {
        eventType = type;
    }

    public ActionType getActionType() {
        return actionType;
    }
    
    public void setActionType(ActionType action) {
        actionType = action;
        eventType = EventType.PLAYER_TURN;
    }
    
    public float getNumber() {
        return number;
    }
    
    public void setNumber(float n) {
        number = n;
    }
    
    public List<Card> getCards() {
        return cards;
    }
    
    public void setCards(List<Card> cards) {
        this.cards = cards;
    }
    
    public void setCard(final Card card) {
        cards = new ArrayList<Card>() {{ add(card); }};
    }

    
}
