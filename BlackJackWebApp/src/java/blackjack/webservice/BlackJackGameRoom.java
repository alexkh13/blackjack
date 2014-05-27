/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package blackjack.webservice;

import blackjack.engine.Bet;
import blackjack.engine.Game;
import blackjack.engine.Player;
import blackjack.engine.exceptions.IllegalActionException;
import blackjack.engine.exceptions.NotEnoughMoneyException;
import blackjack.engine.exceptions.NotEnoughPlayersException;
import blackjack.engine.types.ActionType;
import blackjack.engine.utils.Event;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ws.blackjack.Action;
import ws.blackjack.EventType;
import ws.blackjack.GameDetails;
import ws.blackjack.GameStatus;
import ws.blackjack.InvalidParameters;
import ws.blackjack.InvalidParameters_Exception;
import ws.blackjack.PlayerDetails;
import ws.blackjack.PlayerStatus;
import ws.blackjack.PlayerType;

/**
 *
 * @author idmlogic
 */
public class BlackJackGameRoom {
    private final int SECONDS_TO_TIMEOUT = 60;
    private final int GAME_START_MONEY = 10000;
    
    private final ArrayList<ws.blackjack.Event> events = new ArrayList<>();
    private final HashMap<Integer,PlayerDetails> players = new HashMap<>();
    private final GameDetails details = new GameDetails();
    
    private static int playerCount = 0;
    private int lastEventId = 0;
    private final Game bjGame;
    private Thread timer;
    private int engineEventId;
    private int waitingForPlayerId;
    
    public BlackJackGameRoom(String name, int humans, int computers) {
        details.setHumanPlayers(humans);
        details.setComputerizedPlayers(computers);
        details.setName(name);
        details.setMoney(GAME_START_MONEY);
        details.setStatus(GameStatus.WAITING);
        details.setLoadedFromXML(false);
        details.setJoinedHumanPlayers(0);
        initializeComputerPlayers(bjGame = new Game());
    }
    
    private void initializeComputerPlayers(Game game) {
        for(int i=1;i<=details.getComputerizedPlayers();i++) {
            String playerName = "computer" + i;
            Player player = game.addPlayer(playerName, false);
            PlayerDetails playerDetails = getPlayerDetails(player);
            players.put(++playerCount, playerDetails);
        }
    }

    public int addPlayer(String playerName, float money) {
        Player player = bjGame.addPlayer(playerName, true, money);
        int playerId = ++playerCount;
        players.put(playerId, getPlayerDetails(player));
        details.setJoinedHumanPlayers(details.getJoinedHumanPlayers()+1);
        if(gameReady()) {
            startGame();
        }
        return playerId;
    }
    
    private ws.blackjack.Event getNewRoundEvent() {
        ws.blackjack.Event event = new ws.blackjack.Event();
        event.setType(ws.blackjack.EventType.NEW_ROUND);
        event.setId(lastEventId++);
        return event;
    }
    
    private void startGame() {
        engineEventId = 0;
        bjGame.Initialize();
        bjGame.runGame();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                    events.add(getNewRoundEvent());
                    parseEvents();
                } 
                catch (InterruptedException ex) {
                    Logger.getLogger(BlackJackGameRoom.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }
    
    private void applyPlayerAction(Event event, ws.blackjack.Event serverEvent) {
        if(event.getActionType() != null) {
            switch(event.getActionType()) {
                case BET:
                    serverEvent.setPlayerAction(Action.PLACE_BET);
                    break;
                case DOUBLE:
                    serverEvent.setPlayerAction(Action.DOUBLE);
                    break;
                case FOLD:
                    serverEvent.setPlayerAction(Action.STAND);
                    break;
                case HIT:
                    serverEvent.setPlayerAction(Action.HIT);
                    break;
                case SPLIT:
                    serverEvent.setPlayerAction(Action.SPLIT);
                    break;
                case STAND:
                    serverEvent.setPlayerAction(Action.STAND);
            }
        }
    }
    
    private ws.blackjack.Event parseEvent(Event event) {
        ws.blackjack.Event serverEvent = new ws.blackjack.Event();
        serverEvent.setPlayerName(event.getPlayerName());
        serverEvent.setMoney(event.getNumber());
        serverEvent.getCards().addAll(BlackJackUtilities.cardsListConverter(event.getCards()));
        switch(event.getEventType()) {
            case CARDS_DEALT:
                serverEvent.setType(EventType.CARDS_DEALT);
                break;
            case GAME_OVER:
                serverEvent.setType(EventType.GAME_OVER);
                break;
            case GAME_START:
                serverEvent.setType(EventType.GAME_START);
                break;
            case GAME_WINNER:
                serverEvent.setType(EventType.GAME_WINNER);
                break;
            case PLAYER_RESIGNED:
                serverEvent.setType(EventType.PLAYER_RESIGNED);
                break;
            case PLAYER_TURN:
                serverEvent.setType(event.getActionType() == null ? EventType.PLAYER_TURN : EventType.USER_ACTION);
                applyPlayerAction(event,serverEvent);
                break;
            case WAITING_ACTION:
                serverEvent.setType(EventType.PROMPT_PLAYER_TO_TAKE_ACTION);
                serverEvent.setTimeout(60);
        }
        return serverEvent;
    }
    
    private void parseEvents() {
        boolean endRound = false;
        for(Event event : bjGame.getEvents(engineEventId)) {
            ws.blackjack.Event serverEvent = parseEvent(event);
            serverEvent.setId(lastEventId++);
            switch(serverEvent.getType()) {
                case CARDS_DEALT:
                    if(serverEvent.getPlayerName() == null) {
                        if(serverEvent.getCards().size() > 1) {
                            endRound = true;
                        }
                    }
                break;
                case PROMPT_PLAYER_TO_TAKE_ACTION:
                    waitingForPlayerId = getIdByPlayerName(event.getPlayerName());
                    startTimeout(waitingForPlayerId, SECONDS_TO_TIMEOUT);
            }
            events.add(serverEvent);
            engineEventId = event.getId();
        }
        if(endRound) {
            startGame();
        }
    }
    
    private int getIdByPlayerName(String playerName) {
        for(int playerId : players.keySet()) {
            if(players.get(playerId).getName().equals(playerName)) {
                return playerId;
            }
        }
        return -1;
    }
    
    private PlayerDetails getPlayerDetails(Player player) {
        PlayerDetails playerDetails = new PlayerDetails();
        Bet bet1 = player.getBets().size() > 0 ? player.getBets().get(0) : null;
        Bet bet2 = player.getBets().size() > 1 ? player.getBets().get(1) : null;
        playerDetails.setFirstBetWage(bet1 != null ? bet1.getSum() : 0);
        playerDetails.setSecondBetWage(bet2 != null ? bet2.getSum() : 0);
        playerDetails.setMoney(player.getMoney());
        playerDetails.setName(player.getName());
        playerDetails.setStatus(PlayerStatus.RETIRED);
        playerDetails.setType(player.isHuman() ? PlayerType.HUMAN : PlayerType.COMPUTER);
        return playerDetails;
    }

    private boolean gameReady() {
        return bjGame.getPlayers().size() == details.getHumanPlayers() + details.getComputerizedPlayers();
    }
    
    public List<ws.blackjack.Event> getEvents(int eventId) {
        return events.subList(eventId + 1, events.size());
    }
    
    public GameDetails getGameDetails() {
        return details;
    }

    public List<PlayerDetails> getPlayerDetails() {
        ArrayList<PlayerDetails> result = new ArrayList<>();
        result.addAll(players.values());
        return result;
    }
    
    public PlayerDetails getPlayerDetails(int playerId) {
        return players.get(playerId);
    }

    public void resignPlayer(int playerId) {
        PlayerDetails playerDetails = players.get(playerId);
        players.remove(playerId);
        details.setJoinedHumanPlayers(details.getJoinedHumanPlayers() - 1);
        events.add(getPlayerResignedEvent(playerDetails.getName()));
        if(bjGame.removePlayer(playerDetails.getName())) {
            try {
                bjGame.runGame();
                parseEvents();
            }
            catch(NotEnoughPlayersException ex) {
                // last player exited
                // but it's ok! it will be removed...
            }
        }
    }
    
    private ws.blackjack.Event getPlayerResignedEvent(String playerName) {
        ws.blackjack.Event event = new ws.blackjack.Event();
        event.setId(lastEventId++);
        event.setPlayerName(playerName);
        event.setType(EventType.PLAYER_RESIGNED);
        return event;
    }

    public void playerAction(int playerId, int eventId, Action action, float money, int bet) throws InvalidParameters_Exception {
        if(waitingForPlayerId == playerId) {
            PlayerDetails player = players.get(playerId);
            try {
                switch(action) {
                    case DOUBLE:
                        bjGame.userAction(player.getName(), ActionType.DOUBLE);
                        break;
                    case HIT:
                        bjGame.userAction(player.getName(), ActionType.HIT);
                        break;
                    case SPLIT:
                        bjGame.userAction(player.getName(), ActionType.SPLIT);
                        break;
                    case PLACE_BET:
                        bjGame.userAction(null, ActionType.BET, money);
                        break;
                    case STAND:
                        bjGame.userAction(null, ActionType.STAND);
                        break;
                }
                stopTimeout();
            }
            catch(IllegalActionException | NotEnoughMoneyException ex) {
                throw new InvalidParameters_Exception(ex.getMessage(), new InvalidParameters());
            }
            parseEvents();
        }
    }
    
    private void startTimeout(int playerId, int timeout) {
        timer = new Thread(new PlayerActionTime(playerId, timeout));
        timer.start();
    }
    
    private void stopTimeout() {
        timer.interrupt();
    }

    public boolean isEmpty() {
        return details.getJoinedHumanPlayers() == 0;
    }

    public boolean isReady() {
        return details.getJoinedHumanPlayers() == details.getHumanPlayers();
    }
    
    private class PlayerActionTime implements Runnable {
        private final int timeout;
        private final int playerId;
        public PlayerActionTime(int playerId, int timeout) {
            this.timeout = timeout;
            this.playerId = playerId;
        }
        @Override
        public void run() {
            try {
                Thread.sleep(timeout * 1000);
                resignPlayer(playerId);
            } 
            catch (InterruptedException ex) {
                // canceled
            }
        }
    }
}
