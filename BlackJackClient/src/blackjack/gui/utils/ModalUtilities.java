/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package blackjack.gui.utils;

import java.awt.Paint;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author idmlogic
 */
public class ModalUtilities extends VBox {
    
    public static String getUserAnswer(String message, String input, String buttonText) {
        final Stage stage = new Stage();
        final SimpleBooleanProperty valid = new SimpleBooleanProperty(false);
        TextField text;
        Button confirmButton, cancelButton;
        Label description;
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.getChildren().addAll(
                confirmButton = new Button(buttonText),
                cancelButton = new Button("Cancel")
        );
        hbox.setSpacing(5);
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(3,10,3,10));
        Scene scene = new Scene(vbox);
        scene.setFill(Color.BLUE);
        vbox.setPadding(new Insets(20));
        vbox.getChildren().addAll(
                description = new Label(message),
                text = new TextField(input),
                hbox
        );
        description.setStyle("-fx-text-fill: white");
        text.setOnAction(new EventHandler() {

            @Override
            public void handle(Event t) {
                valid.set(true);
                stage.close();
            }
            
        });
        confirmButton.setOnAction(new EventHandler() {

            @Override
            public void handle(Event t) {
                valid.set(true);
                stage.close();
            }
            
        });
        cancelButton.setOnAction(new EventHandler() {

            @Override
            public void handle(Event t) {
                stage.close();
            }
            
        });
        stage.setTitle("BlackJack");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.showAndWait();
        return valid.get() ? text.getText() : null;
    }
}
