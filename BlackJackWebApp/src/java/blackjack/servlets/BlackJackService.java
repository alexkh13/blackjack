/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package blackjack.servlets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import ws.blackjack.BlackJackWebService;
import ws.blackjack.DuplicateGameName_Exception;
import ws.blackjack.GameDetails;
import ws.blackjack.GameDoesNotExists_Exception;
import ws.blackjack.InvalidParameters_Exception;

/**
 *
 * @author idmlogic
 */
public class BlackJackService {
    private final float PLAYER_START_MONEY = 4000;
    private final BlackJackWebService webService;
    private String sessionId;
    private HashMap<String,HashMap<String,Integer>> map;
    BlackJackService(BlackJackWebService webService, String sessionId) {
        this.webService = webService;
        this.sessionId = sessionId;
        this.map = new HashMap<>();
    }
    
    private BlackJackResponse newResponse(GameDetails details) {
        BlackJackResponse res = new BlackJackResponse(details);
        res.setPlayerId(getPlayerId(details.getName()));
        return res;
    }
    
    private BlackJackResponse newResponse(String gameName) {
        GameDetails details = new GameDetails();
        details.setName(gameName);
        return newResponse(details);
    }
    
    public BlackJackResponse createGame(String gameName, String playerName, int humans, int computers) throws DuplicateGameName_Exception, InvalidParameters_Exception {
        try {
            webService.createGame(gameName, humans, computers);
            int playerId = webService.joinGame(gameName, playerName, PLAYER_START_MONEY);
            addPlayerId(gameName, playerId);
            BlackJackResponse res = newResponse(webService.getGameDetails(gameName));
            return res;
        } 
        catch (GameDoesNotExists_Exception ex) {
            Logger.getLogger(BlackJackService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public BlackJackResponse joinGame(String gameName, String playerName) throws GameDoesNotExists_Exception, InvalidParameters_Exception {
        int playerId = webService.joinGame(gameName, playerName, PLAYER_START_MONEY);
        addPlayerId(gameName, playerId);
        return newResponse(gameName);
    }

    ArrayList<BlackJackResponse> getAvailableGames() {
        ArrayList<String> gameNames = new ArrayList<>();
        ArrayList<BlackJackResponse> games = new ArrayList<>();
        gameNames.addAll(webService.getWaitingGames());
        gameNames.addAll(webService.getActiveGames());
        for(String gameName : gameNames) {
            try {
                games.add(newResponse(webService.getGameDetails(gameName)));
            } catch (GameDoesNotExists_Exception ex) {}
        }
        return games;
    }

    BlackJackResponse getGame(String requestGameName) throws GameDoesNotExists_Exception, InvalidParameters_Exception {
        BlackJackResponse res = new BlackJackResponse(webService.getGameDetails(requestGameName));
        res.setEvents(webService.getEvents(getPlayerId(requestGameName), 0));
        return res;
    }

    void resign(String gameName) throws InvalidParameters_Exception {
        int playerId = getPlayerId(gameName);
        webService.resign(playerId);
    }

    private int getPlayerId(String gameName) {
        return map.get(sessionId).get(gameName);
    }

    private void addPlayerId(String gameName, int playerId) {
        HashMap<String,Integer> games = map.get(sessionId);
        if(games == null) {
            games = new HashMap<>();
            map.put(sessionId, games);
        }
        games.put(gameName, playerId);
    }
}
