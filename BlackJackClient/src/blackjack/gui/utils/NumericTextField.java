/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package blackjack.gui.utils;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFieldBuilder;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;

/**
 *
 * @author idmlogic
 */
public class NumericTextField extends HBox {
    private TextField text;
    private Button up,down;
    private int number;
    private int jump = 1;
    private int max = 10;
    private int min = 0;
    
    public NumericTextField() {
        number = 0;
        text = new TextField();
        text.setEditable(false);
        VBox cont = VBoxBuilder.create().children(
            up = ButtonBuilder.create().styleClass("icon").text("\uf0d8").build(),
            down = ButtonBuilder.create().styleClass("icon").text("\uf0d7").build()
        ).build();
        this.getChildren().addAll(text,cont);
        
        up.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                increase();
            }
        });
        
        down.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                decrease();
            }
        });
    }
    
    private void increase() {
        int n = getNumber()+jump;
        if(n <= max) {
            setNumber(n);
        }
    }
    
    private void decrease() {
        int n = getNumber()-jump;
        if(n >= min) {
            setNumber(n);
        }
    }
    
    public void setNumber(int n) {
        text.setText(String.valueOf(number = n));
    }
    
    public int getNumber() {
        return number;
    }
    
    public void setMax(int max) {
        this.max = max;
    }
    
    public int getMax() {
        return max;
    }
    
    public void setMin(int min) {
        this.min = min;
    }
    
    public int getMin() {
        return min;
    }
    
    public void setJump(int jump) {
        this.jump = jump;
    }
    
    public int getJump() {
        return jump;
    }
}
