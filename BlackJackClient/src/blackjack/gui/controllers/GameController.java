/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package blackjack.gui.controllers;

import blackjack.gui.ClientPlayer;
import blackjack.gui.GameScreen;
import blackjack.gui.utils.ControlledScene;
import blackjack.gui.utils.NumericTextField;
import blackjack.gui.utils.PlayerDash;
import blackjack.gui.utils.ProgressTimeout;
import blackjack.wsclient.Card;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;

/**
 * FXML Controller class
 *
 * @author Alex
 */
public class GameController extends ControlledScene<GameScreen>  {

    @Override
    public void hide() {
        progressTimeout.stop();
        playerChoice.set(PlayerChoice.EXIT);
    }

    public void stopTimeout() {
        progressTimeout.stop();
    }

    public void playerSwitchHand(String player) {
        getPlayerDash(player).switchHand();
    }

    public void setMessage(String message) {
        gameStatus.setText(message);
    }

    private enum ControlPane {
        NONE,
        START_ROUND,
        START_BETTING,
        BET_SETUP,
        PLAYER_CHOICE
    }
    
    public enum PlayerChoice {
        NONE,
        EXIT,
        SPLIT,
        STAND,
        HIT,
        DOUBLE
    }
    
    private ArrayList<PlayerDash> dashes;
    private HashMap<String,PlayerDash> playerDashes;
    private PlayerDash currentActiveDash;
    private ProgressTimeout progressTimeout;
    
    @FXML PlayerDash box1;
    @FXML PlayerDash box2;
    @FXML PlayerDash box3;
    @FXML PlayerDash box4;
    @FXML PlayerDash box5;
    @FXML PlayerDash box6;
    @FXML PlayerDash dealer;
    @FXML ProgressIndicator timeoutDisplay;
    
    @FXML NumericTextField betAmount;
    @FXML Label gameStatus;
    @FXML StackPane controlPane;
    @FXML Button saveButton;

    public SimpleObjectProperty<PlayerChoice> playerChoice = new SimpleObjectProperty<>();
    public SimpleFloatProperty playerBet = new SimpleFloatProperty();
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        playerDashes = new HashMap<>();
        dashes = new ArrayList<>();
        dashes.add(box1);
        dashes.add(box2);
        dashes.add(box3);
        dashes.add(box4);
        dashes.add(box5);
        dashes.add(box6);
        // connect the helper utility to our progress indicator
        progressTimeout = new ProgressTimeout(timeoutDisplay);
    }    

    private PlayerDash getPlayerDash(String playerName) {
        if(playerDashes.containsKey(playerName)) {
            return playerDashes.get(playerName);
        }
        else {
            throw new RuntimeException("No such player named " + playerName);
        }
    }
   
    private float getBetAmount() {
        return (float)betAmount.getNumber();
    }
    
    private void showControlPane(ControlPane pane) {
        for(Node node : controlPane.getChildren()) {
            ControlPane p = ControlPane.valueOf(node.getId());
            node.setVisible(p.equals(pane));
        }
        if(pane == ControlPane.NONE) {
            setMessage(null);
        }
    }
    
    public void handleExit(ActionEvent event) {
        playerChoice.set(PlayerChoice.EXIT);
    }

    public void handleHit(ActionEvent event) {
        showControlPane(ControlPane.NONE);
        playerChoice.set(PlayerChoice.HIT);
    }
    
    public void handleDouble(ActionEvent event) {
        showControlPane(ControlPane.NONE);
        playerChoice.set(PlayerChoice.DOUBLE);
    }
    
    public void handleSplit(ActionEvent event) {
        showControlPane(ControlPane.NONE);
        playerChoice.set(PlayerChoice.SPLIT);
    }
    
    public void handleStand(ActionEvent event) {
        showControlPane(ControlPane.NONE);
        playerChoice.set(PlayerChoice.STAND);
    }
    
    public void handleBet(ActionEvent event) {
        showControlPane(ControlPane.NONE);
        playerBet.set(getBetAmount());
    }
    
    @Override
    public void show() {
        showControlPane(ControlPane.NONE);
    }

    public boolean playerHit(String player, Card card) {
        getPlayerDash(player).addCard(card);
        return true;
    }

    public boolean playerSplit(String player, List<Card> cards) {
        getPlayerDash(player).splitCards(cards);
        return true;
    }

    public boolean playerDouble(String player, Card card, float money) {
        getPlayerDash(player).addCard(card);
        getPlayerDash(player).updateMoney(money, 0);
        return true;
    }

    public boolean playerBet(String player, float money) {
        setMessage(null);
        getPlayerDash(player).updateMoney(money, 0);
        return true;
    }

    public boolean playerStand(String player) {
        getPlayerDash(player).setBetStatus(PlayerDash.BetStatus.STAND);
        return true;
    }

    public boolean playerTurn(String player) {
        setMessage(player + "'s turn");
        if(currentActiveDash != null) {
            currentActiveDash.unsetActive();
        }
        getPlayerDash(player).setActive();
        currentActiveDash = getPlayerDash(player);
        return true;
    }

    public boolean playerWinner(String player, float amount) {
        PlayerDash playerDash = getPlayerDash(player);
        playerDash.setBetStatus(PlayerDash.BetStatus.WINNER); 
        playerDash.updateMoney(0, amount);
        return true;
    }

    public boolean playerResign(String player) {
        PlayerDash playerDash = getPlayerDash(player);
        playerDash.unsetPlayer();
        playerDash.unsetActive();
        return true;
    }

    public boolean dealerCards(List<Card> cards) {
        dealer.clear();
        dealer.addCard(cards);
        return true;
    }

    public boolean handPlayerCards(String player, List<Card> cards) {
        getPlayerDash(player).addCard(cards);
        return true;
    }

    public boolean requirePlayerChoice() {
        playerChoice.setValue(PlayerChoice.NONE);
        showControlPane(ControlPane.PLAYER_CHOICE);
        return true;
    }

    public boolean requirePlayerBet() {
        playerBet.setValue(-1);
        showControlPane(ControlPane.BET_SETUP);
        return true;
    }

    public void setPlayers(List<ClientPlayer> players) {
        resetDashes();
        playerDashes.clear();
        for(int index = 0; index < players.size(); index++) {
            PlayerDash dash = dashes.get(index);
            ClientPlayer player = players.get(index);
            dash.setPlayer(player);
            player.getMoney();
            playerDashes.put(player.getPlayerName(), dash);
        }
    }
    
    public void resetDashes() {
        for(PlayerDash dash : playerDashes.values()) {
            dash.clear();
            dash.unsetPlayer();
        }
    }
    
    public boolean resetGame() {
        dealer.clear();
        for(PlayerDash dash : playerDashes.values()) {
            dash.clear();
        }
        return true;
    }
    
    public void startTimeout(int timeout) {
        progressTimeout.start(timeout);
    }
    
}
