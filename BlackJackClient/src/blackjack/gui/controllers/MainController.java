/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package blackjack.gui.controllers;

import blackjack.gui.GameScreen;
import blackjack.gui.utils.ControlledScene;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * FXML Controller class
 *
 * @author Alex
 */
public class MainController extends ControlledScene<GameScreen>  {
    
    @FXML private Label status;
    @FXML private Button joinGameButton;
    @FXML private Button createGameButton;
    
    public final SimpleObjectProperty<Status> serviceStatus = new SimpleObjectProperty<>();
    
    public enum Status {
        LOADING,
        CONNECTED, 
        EXIT
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    public void handleCreateGame(ActionEvent event) {
        this.getSceneLoader().setScene(GameScreen.GAME_SETUP);
    }

    public void handleJoinGame(ActionEvent event) {
        this.getSceneLoader().setScene(GameScreen.JOIN_SCENE);
    }

    @Override
    public void show() {
    }

    @Override
    public void hide() {
    }
    
    public void setStatus(Status status) {
        serviceStatus.set(status);
        switch(status) {
            case LOADING:
                setMessage("Please wait, connecting...");
                joinGameButton.setDisable(true);
                createGameButton.setDisable(true);
                break;
            case CONNECTED:
                setMessage("");
                joinGameButton.setDisable(false);
                createGameButton.setDisable(false);
        }
    }
    
    private void setMessage(String message) {
        status.setText(message);
    }
    
}
