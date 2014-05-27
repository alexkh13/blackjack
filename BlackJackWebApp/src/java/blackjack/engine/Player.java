/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package blackjack.engine;

import blackjack.engine.exceptions.IllegalActionException;
import blackjack.engine.utils.XMLEntity;
import blackjack.engine.exceptions.NotEnoughMoneyException;
import blackjack.engine.types.ActionType;
import blackjack.engine.xml.PlayerType;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Alex
 */
public class Player implements XMLEntity, Cloneable {
    
    private static float START_MONEY = 2000;
    private boolean folded = false;
    private String name;
    
    private blackjack.engine.xml.Player player;
    
    private Bet currentBet;
    private final List<Bet> bets = new ArrayList<>();
    private final LinkedList<Bet> betsQueue = new LinkedList<>();

    public Player(String playerName) {
        this(playerName, false, START_MONEY);
    }
    
    public Player(String playerName, boolean isHuman, float money) {
        player = Game.factory.createPlayer();
        name = playerName;
        player.setName(playerName);
        player.setBets(Game.factory.createBets());
        player.setType(isHuman ? PlayerType.HUMAN : PlayerType.COMPUTER);
        player.setMoney(money > 0 ? money : START_MONEY);
    }
    
    public Player(blackjack.engine.xml.Player player) {
        this.player = player;
        for(blackjack.engine.xml.Bet bet : player.getBets().getBet()) {
            Bet b = new Bet(bet);
            bets.add(b);
            betsQueue.add(b);
        }
    }
    
    public String getName() {
        return name == null ? player.getName() : name;
    }
    
    public boolean isHuman() {
        return player.getType() == PlayerType.HUMAN;
    }
    
    public List<Bet> getBets() {
        return bets;
    }
    
    public Bet placeBet(float sum) throws NotEnoughMoneyException {
        float newMoney = player.getMoney() - sum;
        if(newMoney < 0) {
            throw new NotEnoughMoneyException();
        }
        else {
            Bet bet = new Bet(sum);
            bets.clear();
            betsQueue.clear();
            bets.add(bet);
            betsQueue.add(bet);
            player.getBets().getBet().add((blackjack.engine.xml.Bet)bet.GetXMLEntity());
            player.setMoney(newMoney);
            return bet;
        }
    }
    
    public boolean isActive() {
        if(folded) {
            return false;
        }
        if(bets.isEmpty() && !folded) {
            return true;
        }
        for(Bet bet : bets) {
            if(bet.isActive()) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public Object GetXMLEntity() {
        return player;
    }

    void reset() {
        folded = false;
        bets.clear();
        player.setBets(Game.factory.createBets());
    }
    
    public float getMoney() {
        return player.getMoney();
    }
    
    public void setMoney(float money) {
        player.setMoney(money);
    }
    
    public void giveMoney(float money) {
        player.setMoney(player.getMoney() + money);
    }
    
    public void takeMoney(float money) throws NotEnoughMoneyException {
        float newMoney = player.getMoney() - money;
        if(newMoney >= 0) {
            player.setMoney(newMoney);
        }
        else {
            throw new NotEnoughMoneyException();
        }
    }

    public boolean isFold() {
        return folded;
    }

    public void fold() {
        folded = true;
    }
    
    public int hit(Card card) {
        currentBet = getNextBet();
        if(currentBet != null) {
            int rank = currentBet.addCard(card);
            if(rank > 20) {
                currentBet = null;
            }
            return rank;
        }
        else {
            return -1;
        }
    }
    
    private Bet getNextBet() {
        return (currentBet != null) ? (currentBet) : (betsQueue.poll());
    }

    public Bet getCurrentBet() {
        return currentBet;
    }
    
    public void stand() {
        currentBet = getNextBet();
        currentBet.declareStand();
        currentBet = null;
    }
    
    public void doubleBet() {
        if(currentBet != null) {
            if(currentBet.getCards().size() == 2) {
                takeMoney(currentBet.getSum() * 2);
                currentBet.Double();
            }
            else {
                throw new IllegalActionException("Bet already got hit");
            }
        }
    }

    public void split(Card card1, Card card2) {
        if((currentBet = getNextBet()) != null) {
            takeMoney(currentBet.getSum());
            Bet secondBet = currentBet.split();
            bets.add(secondBet);
            betsQueue.add(secondBet);
            currentBet.addCard(card1);
            secondBet.addCard(card2);
        }
    }
    
    public ActionType suggestAction() {
        currentBet = getNextBet();
        if(currentBet != null) {
            if(currentBet.getRank() < 17) {
                return ActionType.HIT;
            }
            else if(currentBet.getRank() < 22) {
                return ActionType.STAND;
            }
            else return null;
        }
        else {
            return null;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Player other = (Player) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

}
