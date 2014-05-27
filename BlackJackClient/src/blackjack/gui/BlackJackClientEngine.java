/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package blackjack.gui;

import blackjack.wsclient.Action;
import blackjack.wsclient.BlackJackWebService;
import blackjack.wsclient.Event;
import blackjack.wsclient.EventType;
import blackjack.wsclient.GameDoesNotExists_Exception;
import blackjack.wsclient.InvalidParameters_Exception;
import blackjack.wsclient.PlayerDetails;
import blackjack.wsclient.PlayerType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author idmlogic
 */
public class BlackJackClientEngine {
    private final BlackJackWebService webService;
    private final LinkedList<Event> eventQueue;
    private final HashMap<String,ClientPlayer> players = new HashMap<>();
    private int playerId;
    private int lastEventId;
    private String gameName;
    private boolean gotBet;
    private boolean gotSplit;

    public BlackJackClientEngine(BlackJackWebService webService) {
        this.webService = webService;
        this.eventQueue = new LinkedList<>();
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
        this.lastEventId = -1;
        initializePlayerList();
    }
    
    private void initializePlayerList() {
        try {
            players.clear();
            for(PlayerDetails details : webService.getPlayersDetails(gameName)) {
                players.put(details.getName(), new ClientPlayer(details.getName(),details.getMoney(),details.getType() == PlayerType.HUMAN));
            }
        } 
        catch (GameDoesNotExists_Exception ex) {
            Logger.getLogger(BlackJackClientEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void resign() {
        try {
            webService.resign(playerId);
        } 
        catch (InvalidParameters_Exception ex) {
            Logger.getLogger(BlackJackClientEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    boolean isAlreadySplit() {
        return this.gotSplit;
    }
    
    public enum PlayerAction {
        SPLIT,
        STAND,
        HIT,
        DOUBLE
    }
    
    public ClientEvent getNextEvent() throws InvalidParameters_Exception {
        if(eventQueue.isEmpty()) {
            try {
                eventQueue.addAll(webService.getEvents(playerId, lastEventId));
                lastEventId = eventQueue.peekLast().getId();
            }
            catch(NullPointerException ex) {
                // no more events
            }
        }
        if(eventQueue.isEmpty()) {
            return null;
        }
        else {
            return parseEvent(eventQueue.pop());
        }
    }
    
    private ClientEvent parseEvent(Event event) {
        ClientEvent clientEvent = new ClientEvent(event);
        if(event.getType() == EventType.NEW_ROUND) {
            gotBet = false;
        }
        return clientEvent;
    }
    
    public boolean isPlayerAlreadyBet(String playerName) {
        return gotBet;
    }
    
    public void playerAction(PlayerAction action) throws InvalidParameters_Exception {
        try {
            webService.playerAction(playerId, lastEventId, getAction(action), 0, 0);
            if(action == PlayerAction.SPLIT) {
                gotSplit = true;
            }
        }
        catch(InvalidParameters_Exception ex) {
            throw ex;
        }
    }
    
    public boolean placeBet(float money) {
        try {
            webService.playerAction(playerId, lastEventId, Action.PLACE_BET, money, 0);
            return gotBet = true;
        } 
        catch (InvalidParameters_Exception ex) {
            return false;
        }
    }
    
    private Action getAction(PlayerAction action) {
        switch(action){
            case STAND: return Action.STAND;
            case SPLIT: return Action.SPLIT;
            case HIT: return Action.HIT;
            case DOUBLE: return Action.DOUBLE;
        }
        return null;
    }
    
    public List<ClientPlayer> getPlayers() {
        ArrayList<ClientPlayer> list = new ArrayList<>();
        list.addAll(players.values());
        return list;
    }
}
