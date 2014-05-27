/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package blackjack.engine;

import blackjack.engine.types.ActionType;
import blackjack.engine.utils.Event;
import blackjack.engine.utils.EventLogger;
import blackjack.engine.types.EventType;
import blackjack.engine.exceptions.CorruptGameSaveException;
import blackjack.engine.exceptions.IllegalActionException;
import blackjack.engine.exceptions.IllegalPlayerName;
import blackjack.engine.exceptions.NotEnoughMoneyException;
import blackjack.engine.exceptions.NotEnoughPlayersException;
import blackjack.engine.exceptions.PlayerAlreadyExists;
import blackjack.engine.xml.Blackjack;
import blackjack.engine.xml.ObjectFactory;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author Alex
 */
public class Game {
    
    private Blackjack setup;
    
    protected static ObjectFactory factory = new ObjectFactory();
    
    private final EventLogger events = new EventLogger();
    private final HashMap<String,Player> players = new HashMap<>();
    private final LinkedList<Player> queue = new LinkedList<>();
    private final Dealer dealer;
    
    private Player currentPlayer;
    
    private int currentPlayerBetIndex = 0;
    
    private Game(Blackjack setup) throws CorruptGameSaveException {
        checkSetup(setup);
        this.setup = setup;
        // create a dealer that assigned to a bet
        this.dealer = new Dealer(new Bet(setup.getDealer()));
        // create players from xml
        for(blackjack.engine.xml.Player player : setup.getPlayers().getPlayer()) {
            players.put(player.getName(),new Player(player));
        }
        queue.addAll(players.values());
    }
    
    public Game(File file) throws CorruptGameSaveException {
        // Get setup from file
        this(getSetup(file));
    }
    
    public Game() {
        this(initSetup());
    }
    
    private static Blackjack initSetup() {
        Blackjack newSetup = factory.createBlackjack();
        newSetup.setPlayers(factory.createPlayers());
        newSetup.setDealer(factory.createBet());
        newSetup.setName(null); // TODO: Option to set a name
        return newSetup;
    }
    
    private static Blackjack getSetup(File file) throws CorruptGameSaveException {
        try {
            JAXBContext jc = JAXBContext.newInstance(Blackjack.class);
            Unmarshaller u = jc.createUnmarshaller();
            return (Blackjack)u.unmarshal(file);
        }
        catch(JAXBException ex) {
            throw new CorruptGameSaveException("Corrupt file");
        }
    }
    
    private boolean checkSetup(Blackjack setup) throws CorruptGameSaveException {
        if(setup.getDealer() == null) {
            throw new CorruptGameSaveException("Dealer not set");
        }
        if(setup.getPlayers() == null) {
            throw new CorruptGameSaveException("Players not set");
        }
        for(blackjack.engine.xml.Player player : setup.getPlayers().getPlayer()) {
            if(player.getBets() == null) {
                throw new CorruptGameSaveException("Bets element not set for " + player.getName());
            }
            if(player.getBets().getBet().size() != 1) {
                throw new CorruptGameSaveException("Too many bets for " + player.getName());
            }
            blackjack.engine.xml.Cards cards = player.getBets().getBet().get(0).getCards();
            if(cards == null) {
                throw new CorruptGameSaveException("No cards in bet for " + player.getName());
            }
            else {
                for(blackjack.engine.xml.Cards.Card card : player.getBets().getBet().get(0).getCards().getCard()) {
                    if(card == null) {
                        throw new CorruptGameSaveException("Problem with " + player.getName() +"'s cards");
                    }
                }
                if(cards.getCard().size() !=2) {
                    throw new CorruptGameSaveException("Expecting 2 cards for " + player.getName());
                }
            }
        }
        return true;
    }
    
    public Player addPlayer(String name, boolean isHuman) {
        return addPlayer(name, isHuman, 0);
    }
    
    public Player addPlayer(String name, boolean isHuman, float money) throws PlayerAlreadyExists {
        if(players.containsKey(name)) {
            throw new PlayerAlreadyExists();
        }
        else if(name.equals("")) {
            throw new IllegalPlayerName();
        }
        else {
            Player player = new Player(name, isHuman, money);
            players.put(name, player);
            queue.add(player);
            setup.getPlayers().getPlayer().add((blackjack.engine.xml.Player)player.GetXMLEntity());
            return player;
        }
    }
    
    public void Save(File file) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(Blackjack.class);
        Marshaller m = jc.createMarshaller();
        m.marshal(setup,file);
    }

    public void Initialize() throws NotEnoughPlayersException {
        if(players.isEmpty()) {
            throw new NotEnoughPlayersException();
        }
        // reset all hands
        for(String playerName : players.keySet()) {
            Player player = players.get(playerName);
            player.reset();
        }
        events.reset();
    }

    private Player nextPlayer() {
        if(currentPlayer != null && isStarted() && currentPlayer.isActive()) {
            return currentPlayer;
        }
        else {
            return queue.poll();
        }
    }
    
    public void runGame() {
        boolean waiting = false;
        do 
        {
            Player nextPlayer = nextPlayer();
            if(nextPlayer != null && !nextPlayer.equals(currentPlayer)) {
                events.push(nextPlayer, EventType.PLAYER_TURN);
            }
            
            currentPlayer = nextPlayer;
            
            if(currentPlayer == null) {
                if(!isStarted()) {
                    // if we reach this point, that means all players placed their bet
                    // and that means we need to deal them some cards
                    dealCards();
                    queue.addAll(players.values());
                }
                //waiting = true;
            }
            else {
                // check if started betting or player chose not to bet
                if(!currentPlayer.isFold()) {
                    if(currentPlayer.isHuman()) {
                        if(waiting = humanMove(currentPlayer)) {
                            events.push(currentPlayer, EventType.WAITING_ACTION);
                        }
                    }
                    else {
                        computerMove(currentPlayer);
                    }
                }
            }
        }
        while(!waiting && isActive());
        
        if(!isActive()) {
            analyzeWiners();
            dealer.reset();
            queue.addAll(players.values());
        }
    }

    public List<Event> getEvents(int lastEventID) {
        return events.getEvents(lastEventID);
    }
    
    // Return a boolean that idicates if we need to wait for an action or not
    private boolean humanMove(Player player) {
        if(player.getBets().isEmpty()) {
            return true;
        }
        else {
            do {
                if(currentPlayerBetIndex >= player.getBets().size()) {
                    // We went through all player bets
                    // so we can move forward to the next player
                    currentPlayerBetIndex = 0;
                    return false;
                }
                else {
                    Bet currentBet = player.getBets().get(currentPlayerBetIndex);
                    if(currentBet.getRank() > 20) {
                        // Player can't make a move on this bet
                        // he's already burned so we need to move to the next bet
                        currentPlayerBetIndex++;
                    }
                    else {
                        // The player can decide what to do next
                        // so we have to wait for his action
                        return true;
                    }
                }
            }
            // we're going to loop endlessly
            // until we run out of bets or we'll wait for an action
            while(true);
        }
    }
    
    private void computerMove(Player player) {
        
        if(player.getBets().isEmpty()) {
            if(player.getMoney() >= 100) {
                userAction(player.getName(), ActionType.BET , 100);
            }
            else {
                userAction(player.getName(), ActionType.BET , player.getMoney());
            }
        }
        else {
            ActionType action;
            while((action = player.suggestAction()) != null) {
                userAction(player.getName(), action);
            }
        }
        
    }
    
    public void userAction(String playerName, ActionType type, float money) throws NotEnoughMoneyException {
        
        switch(type) {
            case BET:
                if(money > 0) {
                    currentPlayer.placeBet(money);
                    events.push(currentPlayer, money);
                }
                else {
                    currentPlayer.fold();
                    events.push(currentPlayer, ActionType.FOLD);
                }
                break;
                
            case DOUBLE:
                currentPlayer.doubleBet();
            case HIT:
                Card card = dealer.hitCard();
                int rank = currentPlayer.hit(card);
                Event ev = new Event();
                ev.setPlayerName(currentPlayer.getName());
                ev.setActionType(ActionType.HIT);
                ev.setCard(card);
                ev.setNumber(rank);
                events.push(ev);
                break;
                
            case STAND: 
                events.push(currentPlayer, ActionType.STAND);
                currentPlayer.stand();
                break;
                
            case SPLIT:
                List<Card> cards = dealer.hitCard(2);
                currentPlayer.split(cards.get(0), cards.get(1));
                events.push(currentPlayer, ActionType.SPLIT, cards);
        }
        
        if(currentPlayer.isHuman()) {
            runGame();
        }
        
    }
    
    public void userAction(String playerName, ActionType actionType) throws IllegalActionException,NotEnoughMoneyException {
        userAction(playerName, actionType, 0);
    }
    
    private void dealCards() {
        dealer.DealCards();
        events.push(null, null, dealer.peek());
        // Two cards for each player
        for(Player player : players.values()) {
            if(!player.getBets().isEmpty()) {
                Bet bet = player.getBets().get(0);
                List<Card> cards = dealer.hitCard(2);
                bet.addCards(cards);
                events.push(player,cards);
            }
        }
    }
    
    private void analyzeWiners() {
        events.push(null, dealer.getCards());
        int dillerRank = dealer.getRank();
        for(String playerName : players.keySet()) {
            Player player = players.get(playerName);
            for(Bet bet : player.getBets()) {
                int playerRank = bet.getRank();
                if(compareRanks(dillerRank,playerRank)) {
                    // by default, return the money
                    float multiplier = 1;
                    // but if we got a blackjack(21)
                    if(playerRank == 21) {
                        // check if from start
                        if(bet.getCards().size() == 2) {
                            multiplier = (float) 2.5;
                        }
                        // better than the dealer afterall
                        else {
                            multiplier = 2;
                        }
                    }
                    float win = bet.getSum() * multiplier;
                    events.push(player, EventType.GAME_WINNER, win);
                    player.giveMoney(bet.getSum() + win);
                }
                else if(dillerRank == playerRank) {
                    player.giveMoney(bet.getSum());
                }
            }
        }
    }
    
    private boolean compareRanks(int dillerRank, int playerRank) {
        // diller and player are in rage
        if(dillerRank < 22 && playerRank < 22) {
            // the player has a bigger hand
            if(playerRank > dillerRank) {
                return true;
            }
        }
        else {
            // diller is burned
            if(dillerRank > 21 && playerRank < 22) {
                return true;
            }
        }
        return false;
    }
    
    public HashMap<String,Player> getPlayers() {
        return players;
    }

    private boolean isActive() {
        for(String playerName : players.keySet()) {
            Player player = players.get(playerName);
            if(player.isActive()) {
                return true;
            }
        }
        return false;
    }

    public boolean isStarted() {
        return !dealer.getCards().isEmpty();
    }

    public Card getDealerCard() {
        return dealer.peek();
    }

    public boolean removePlayer(String name) {
        Player playerToRemove = players.get(name);
        playerToRemove.fold();
        players.remove(name);
        queue.remove(playerToRemove);
        return currentPlayer.equals(playerToRemove);
    }
}
