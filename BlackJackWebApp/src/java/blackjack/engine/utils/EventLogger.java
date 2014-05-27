/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package blackjack.engine.utils;

import blackjack.engine.types.ActionType;
import blackjack.engine.Card;
import blackjack.engine.types.EventType;
import blackjack.engine.Player;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Alex
 */
public class EventLogger {
    private int counter = 0;
    private final List<Event> events = new ArrayList<>();
    
    public void reset() {
        events.clear();
        counter = 0;
    }
    
    // Event(int, Player, Bet, EventType, ActionType, List<Card>, float)
    private Event createEvent(Player player, EventType eventType, ActionType actionType, List<Card> cards, float money) {
        return new Event(counter++, player == null ? null : player.getName(), eventType, actionType, cards, money);
    }
    
    public void push(Event event) {
        event.setId(counter++);
        events.add(event);
    }
    
    // To declare player's action
    public void push(Player player, ActionType actionType) {
        events.add(createEvent(player, EventType.PLAYER_TURN, actionType, null, 0));
    }
    
    // To declare who's turn it is
    public void push(Player player) {
        events.add(createEvent(player, EventType.PLAYER_TURN, null, null, 0));
    }
    
    // To declare the cards that were dealt for each player
    // For diller: player and bet would be null
    public void push(Player player, List<Card> cards) {
        events.add(createEvent(player,EventType.CARDS_DEALT,null,cards,0));
    }
    
    // To declare the cards that were dealt in a split
    public void push(Player player, ActionType actionType, List<Card> cards) {
        events.add(createEvent(player,EventType.PLAYER_TURN,actionType,cards,0));
    }
    
    public void push(Player player, ActionType actionType, final Card card) {
        // If no action, then we're dealing cards (CARDS_DEALT)
        // else, that means players is doing something (PLAYER_TURN)
        EventType eventType = actionType == null ? EventType.CARDS_DEALT : EventType.PLAYER_TURN;
        events.add(createEvent(player,eventType,actionType,new ArrayList<Card>() {{ add(card); }},0));
    }
    
    // To declare player's bets
    public void push(Player player, float money) {
        events.add(createEvent(player,EventType.PLAYER_TURN,ActionType.BET,null,money));
    }
    
    // To declare winning on bets
    public void push(Player player, EventType eventType, float money) {
        events.add(createEvent(player,eventType,null,null,money));
    }
    
    public void push(Player player, EventType eventType) {
        events.add(createEvent(player,eventType,null,null,0));
    }
    
    public List<Event> getEvents(int lastID) {
        return events.subList(lastID == 0 ? lastID : lastID + 1, events.size());
    }
}
