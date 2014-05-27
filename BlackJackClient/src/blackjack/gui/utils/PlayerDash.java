/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package blackjack.gui.utils;

import blackjack.gui.ClientPlayer;
import blackjack.wsclient.Card;
import java.awt.Color;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 *
 * @author Alex
 */
public class PlayerDash extends VBox {
    
    private Pos cardsAlignment;
    private final HBox handContainer, statContainer;
    private final CardBox hand1,hand2;
    private CardBox activeHand;
    private final Label playerName;
    private final Label playerMoney;
    private final Label playerIcon;
    private ClientPlayer player;
    private float playerBalance;
    
    public PlayerDash() {
        getChildren().add(statContainer = new HBox());
        getChildren().add(handContainer = new HBox());
        
        statContainer.getChildren().add(playerIcon = new Label());
        statContainer.getChildren().add(playerName = new Label());
        statContainer.getChildren().add(playerMoney = new Label());
        
        playerIcon.getStyleClass().add("icon");
        
        handContainer.setSpacing(10);
        statContainer.setSpacing(10);
        
        handContainer.getChildren().add(hand1 = new CardBox());
        playerName.getStyleClass().add("player_name");
        playerMoney.getStyleClass().add("icon");
        hand2 = new CardBox();
        activeHand = hand1;
    }
    
    public void setCardsAlignment(Pos pos) {
        setAlignment(cardsAlignment = pos);
        handContainer.setAlignment(pos);
        statContainer.setAlignment(pos);
        hand1.setAlignment(pos);
        hand2.setAlignment(pos);
    }
    
    public Pos getCardsAlignment() {
        return cardsAlignment;
    }
    
    public void setPlayer(ClientPlayer player) {
        this.player = player;
        playerName.setText(player.getPlayerName());
        playerIcon.setText(player.isHuman() ? SceneIcons.ICON_HUMAN : SceneIcons.ICON_COMPUTER);
        playerBalance = player.getMoney();
        updateMoney(0,0);
        renderCards();
    }
    
    public void updateMoney(float betAmount, float winAmount) {
        if(player != null) {
            String text;
            if(betAmount > 0) {
                playerBalance -= betAmount;
                text = "(\uf063"+betAmount+"/\uf155"+ playerBalance +")";
            }
            else {
                text = "(\uf155"+ playerBalance +")";
            }
            if(winAmount > 0) {
                text = text + " \uf091" + winAmount;
                playerBalance += winAmount;
            }
            playerMoney.setText(text);
        }
    }
    
    public void unsetPlayer() {
        this.player = null;
        playerName.setText(null);
        playerMoney.setText(null);
        playerIcon.setText(null);
        hand1.clear();
        hand2.clear();
    }
    
    private void renderCards() {
        hand1.clear();
        for(Card card : player.getFirstBet()) {
            hand1.addCard(card);
        }
    }
    
    public void addCard(Card card) {
        activeHand.addCard(card);
    }
    
    public void addCard(List<Card> cards) {
        for(Card card : cards) {
            addCard(card);
        }
    }

    public void clear() {
        updateMoney(0,0);
        unsetActive();
        hand1.clear();
        hand2.clear();
        handContainer.getChildren().remove(hand2);
        activeHand = hand1;
        unsetActive();
    }

    public boolean isEmpty() {
        return hand1.isEmpty();
    }

    public void splitCards(List<Card> cards) {
        handContainer.getChildren().add(hand2);
        hand2.addCard(hand1.removeLastCard());
        hand1.addCard(cards.get(0));
        hand2.addCard(cards.get(1));
    }
    
    public void switchHand() {
        unsetActive();
        activeHand = hand2;
        setActive();
    }
    
    public void setActive() {
        activeHand.getStyleClass().remove("unactive");
        if(!activeHand.getStyleClass().contains("active"))
            activeHand.getStyleClass().add("active");
    }
     
    public void unsetActive() {
        activeHand.getStyleClass().remove("active");
        if(!activeHand.getStyleClass().contains("unactive"))
            activeHand.getStyleClass().add("unactive");
    }
    
    public enum BetStatus {
        BURNED,
        WINNER,
        STAND,
        PUSH
    }
    
    public void setBetStatus(BetStatus stat) {
        setBetStatus(activeHand.equals(hand1) ? 0 : 1,stat);
    }
    
    public void setBetStatus(int index, BetStatus stat) {
        CardBox cb = index == 0 ? hand1 : hand2;
        String icon = "";
        String color = "black";
        switch(stat) {
            case WINNER:
                icon = "\uf118";
                color = "yellow";
        }
        cb.setText(icon + " " + stat.toString());
        cb.setStyle("-fx-text-fill: " + color);
    }
    
}
