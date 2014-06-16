/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package blackjack.webservice;

import blackjack.engine.exceptions.IllegalPlayerName;
import blackjack.engine.exceptions.PlayerAlreadyExists;
import java.util.ArrayList;
import java.util.HashMap;
import javax.jws.WebService;
import ws.blackjack.DuplicateGameName;
import ws.blackjack.DuplicateGameName_Exception;
import ws.blackjack.GameDetails;
import ws.blackjack.GameDoesNotExists;
import ws.blackjack.GameDoesNotExists_Exception;
import ws.blackjack.InvalidParameters;
import ws.blackjack.InvalidParameters_Exception;
import ws.blackjack.InvalidXML_Exception;

/**
 *
 * @author idmlogic
 */
@WebService(serviceName = "BlackJackWebService", portName = "BlackJackWebServicePort", endpointInterface = "ws.blackjack.BlackJackWebService", targetNamespace = "http://blackjack.ws/", wsdlLocation = "WEB-INF/wsdl/BlackJackWebService/BlackJackWebService.wsdl")
public class BlackJackWebService {

    private final HashMap<String,BlackJackGameRoom> waitingGames = new HashMap<>();
    private final HashMap<String,BlackJackGameRoom> activeGames = new HashMap<>();
    private final HashMap<Integer,BlackJackGameRoom> playerGames = new HashMap<>();
    
    private boolean isGameExists(String name) {
        return waitingGames.containsKey(name) || activeGames.containsKey(name);
    }
    
    public java.util.List<ws.blackjack.Event> getEvents(int playerId, int eventId) throws InvalidParameters_Exception {
        try {
            return playerGames.get(playerId).getEvents(eventId);
        }
        catch(NullPointerException ex) {
            throw new InvalidParameters_Exception("Can't find player",new InvalidParameters());
        }
    }

    public java.lang.String createGameFromXML(java.lang.String xmlData) throws InvalidXML_Exception, DuplicateGameName_Exception, InvalidParameters_Exception {
        //TODO implement this method
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public ws.blackjack.GameDetails getGameDetails(java.lang.String gameName) throws GameDoesNotExists_Exception {
        try {
            BlackJackGameRoom room = waitingGames.containsKey(gameName) ? 
                waitingGames.get(gameName) : activeGames.get(gameName);
            return room.getGameDetails();
        }
        catch(NullPointerException ex) {
            throw new GameDoesNotExists_Exception("No game", new GameDoesNotExists());
        }
    }

    public java.util.List<java.lang.String> getWaitingGames() {
        cleanEmptyGames();
        ArrayList<String> games = new ArrayList<>();
        games.addAll(waitingGames.keySet());
        return games;
    }

    public java.util.List<java.lang.String> getActiveGames() {
        cleanEmptyGames();
        ArrayList<String> games = new ArrayList<>();
        games.addAll(activeGames.keySet());
        return games;
    }

    public java.util.List<ws.blackjack.PlayerDetails> getPlayersDetails(java.lang.String gameName) throws GameDoesNotExists_Exception {
        try {
            BlackJackGameRoom room = waitingGames.containsKey(gameName) ? 
                    waitingGames.get(gameName) : activeGames.get(gameName);
            return room.getPlayerDetails();
        }
        catch(NullPointerException ex) {
            throw new GameDoesNotExists_Exception("No game", new GameDoesNotExists());
        }
    }

    public ws.blackjack.PlayerDetails getPlayerDetails(int playerId) throws GameDoesNotExists_Exception, InvalidParameters_Exception {
        try {
            return playerGames.get(playerId).getPlayerDetails(playerId);
        }
        catch(NullPointerException ex) {
            throw new GameDoesNotExists_Exception("No game", new GameDoesNotExists());
        }
    }

    public void createGame(java.lang.String name, int humanPlayers, int computerizedPlayers) throws DuplicateGameName_Exception, InvalidParameters_Exception {
        if(isGameExists(name)) {
            throw new DuplicateGameName_Exception(name,new DuplicateGameName());
        }
        else if(humanPlayers < 1) {
            throw new InvalidParameters_Exception("At least 1 human needed.",new InvalidParameters());
        }
        else if(humanPlayers + computerizedPlayers > 6)
        {
            throw new InvalidParameters_Exception("The maximum number of players is 6.",new InvalidParameters());
        }
        else {
            waitingGames.put(name, new BlackJackGameRoom(name,humanPlayers,computerizedPlayers));
        }
    }

    public int joinGame(java.lang.String gameName, java.lang.String playerName, float money) throws InvalidParameters_Exception, GameDoesNotExists_Exception {
        if(!waitingGames.containsKey(gameName)) {
            throw new GameDoesNotExists_Exception("No game", new GameDoesNotExists());
        }
        else if(money <= 0){
            throw new InvalidParameters_Exception("Money problem",new InvalidParameters());
        }
        else {
            try {
                BlackJackGameRoom room = waitingGames.get(gameName);
                int playerId = room.addPlayer(playerName, money);
                if(room.isReady()) {
                    activeGames.put(gameName, room);
                    waitingGames.remove(gameName);
                }
                playerGames.put(playerId, room);
                return playerId;
            }
            catch(IllegalPlayerName ex) {
                throw new InvalidParameters_Exception("Illegal player name",new InvalidParameters());
            }
            catch(PlayerAlreadyExists ex) {
                throw new InvalidParameters_Exception("Player already exists",new InvalidParameters());
            }
        }
    }

    public void playerAction(int playerId, int eventId, ws.blackjack.Action action, float money, int bet) throws InvalidParameters_Exception {
        playerGames.get(playerId).playerAction(playerId, eventId, action, money, bet);
    }

    public void resign(int playerId) throws InvalidParameters_Exception {
        try {
            BlackJackGameRoom game = playerGames.get(playerId);
            game.resignPlayer(playerId);
            playerGames.remove(playerId);
        }
        catch(NullPointerException ex) {
            System.out.println("Player not in game");
        }
    }
    
    private void cleanEmptyGames() {
        for(String gameName : waitingGames.keySet()) {
            if(waitingGames.get(gameName).isEmpty()) {
                waitingGames.remove(gameName);
            }
        }
        for(String gameName : activeGames.keySet()) {
            if(activeGames.get(gameName).isEmpty()) {
                activeGames.remove(gameName);
            }
        }
    }
    
}
