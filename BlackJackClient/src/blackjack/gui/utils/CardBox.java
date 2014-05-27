/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package blackjack.gui.utils;

import blackjack.wsclient.Card;
import java.util.ArrayList;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

/**
 *
 * @author Alex
 */
public class CardBox extends StackPane {
    ArrayList<Card> cards;
    private final HBox box;
    private final Label text;
    private final int CARD_WIDTH = 75;
    private final int CARD_HEIGHT = 106;
    
    public CardBox() {
        super();
        this.getChildren().add(box = new HBox());
        this.getChildren().add(text = new Label());
        text.getStyleClass().add("card_box_text");
        box.setSpacing(-55);
        this.setMinHeight(CARD_HEIGHT);
        box.getStyleClass().add("card_box");
        cards = new ArrayList<>();
    }
    
    public void setText(String text) {
        this.text.setText(text);
    }
    
    public void addCard(Card card) {
        cards.add(card);
        render();
    }
    
    public Card removeLastCard() {
        return cards.remove(1);
    }
    
    private void render() {
        box.getChildren().clear();
        for(Card card : cards) {
            Label label = new Label();
            label.setPrefWidth(CARD_WIDTH);
            label.setPrefHeight(CARD_HEIGHT);
            label.getStyleClass().add("card");
            label.getStyleClass().add(cardToStyleClass(card));
            box.getChildren().add(label);
        }
    }
    
    private String cardToStyleClass(Card card) {
        String result = card.getSuit().name().toLowerCase().charAt(0) + card.getRank().toString();
        return result;
    }
    
    public void clear() {
        text.setText("");
        cards.clear();
        render();
    }

    boolean isEmpty() {
        return cards.isEmpty();
    }
}
