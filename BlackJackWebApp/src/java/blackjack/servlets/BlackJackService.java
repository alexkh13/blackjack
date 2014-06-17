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
import ws.blackjack.Action;
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
    BlackJackService(BlackJackWebService webService) {
        this.webService = webService;
    }
    
    private BlackJackResponse newResponse(GameDetails details) {
        BlackJackResponse res = new BlackJackResponse(details);
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
            BlackJackResponse res = newResponse(webService.getGameDetails(gameName));
            res.setPlayerId(playerId);
            return res;
        } 
        catch (GameDoesNotExists_Exception ex) {
            Logger.getLogger(BlackJackService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public BlackJackResponse joinGame(String gameName, String playerName) throws GameDoesNotExists_Exception, InvalidParameters_Exception {
        int playerId = webService.joinGame(gameName, playerName, PLAYER_START_MONEY);
        return getGame(playerId, gameName);
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

    BlackJackResponse getGame(int playerId, String requestGameName) throws GameDoesNotExists_Exception, InvalidParameters_Exception {
        BlackJackResponse res = new BlackJackResponse(webService.getGameDetails(requestGameName));
        res.setPlayerId(playerId);
        res.setEvents(webService.getEvents(playerId, -1));
        res.setPlayers(webService.getPlayersDetails(requestGameName));
        return res;
    }

    void resign(int playerId) throws InvalidParameters_Exception {
        webService.resign(playerId);
    }
    
    void placeBet(int playerId, float money) throws InvalidParameters_Exception {
        webService.playerAction(playerId, 0, Action.PLACE_BET, money, 0);
    }

    private Action getServerAction(BlackJackRequest.PlayerAction action) {
        switch(action) {
            case HIT: return Action.HIT;
            case STAND: return Action.STAND;
            case SPLIT: return Action.SPLIT;
            case DOUBLE: return Action.DOUBLE;
        }
        return null;
    }
    
    void userAction(int playerId, BlackJackRequest.PlayerAction action) throws InvalidParameters_Exception {
        webService.playerAction(playerId, 0, getServerAction(action), 0, 0);
    }
}
