/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package blackjack.gui.controllers;

import blackjack.gui.GameScreen;
import blackjack.gui.utils.ControlledScene;
import blackjack.wsclient.BlackJackWebService;
import blackjack.wsclient.GameDoesNotExists_Exception;
import blackjack.wsclient.InvalidParameters_Exception;
import java.awt.Paint;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author Alex
 */
public class JoinGameController extends ControlledScene<GameScreen>  {

    private final int PLAYER_START_MONEY=2000;
    
    private ObservableList gamesList = FXCollections.observableArrayList();
    
    @FXML private TextField playerName;
    @FXML private ListView list;
    @FXML private Label status;
    @FXML private Label emptyListMessage;
    
    private int myPlayerId = 0;
    private BlackJackWebService service;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }    
    
    public void handleBack(ActionEvent event) {
        list.getSelectionModel().clearSelection();
        this.getSceneLoader().setScene(GameScreen.MAIN_SCENE);
    }

    public void handleJoin(ActionEvent event) {
        try {
            String gameName = (String)list.getSelectionModel().getSelectedItem();
            myPlayerId = service.joinGame(gameName, getPlayerName(), PLAYER_START_MONEY);
            this.getSceneLoader().setScene(GameScreen.GAME_SETUP);
        } 
        catch (GameDoesNotExists_Exception ex) {
            raiseStatusMessage("ERROR: Game doesn't exist anymore.");
        } 
        catch (InvalidParameters_Exception ex) {
            raiseStatusMessage("ERROR: Your name is already taken in that game.");
        }
    }
    
    public String getSelectedGame() {
        String temp = (String)list.getSelectionModel().getSelectedItem();
        list.getSelectionModel().clearSelection();
        return temp;
    }
    
    public void handleRefresh(ActionEvent event) {
        refreshGamesList();
    }
    
    private void refreshGamesList() {
        try {
            emptyListMessage.setVisible(false);
            raiseStatusMessage("Please wait, loading...");
            list.setDisable(true);
            gamesList.clear();
            gamesList.addAll(service.getWaitingGames());
            list.setItems(gamesList);
            
            raiseStatusMessage("");
            if(gamesList.isEmpty()) {
                emptyListMessage.setVisible(true);
            }
            else {
                list.setDisable(false);
            }
        }
        catch(Exception ex) {
            raiseStatusMessage("ERROR: Unable connect to server.");
        }
    }

    private void raiseStatusMessage(String message) {
        status.setText(message);
    }
    
    @Override
    public void show() {
        service = this.getSceneLoader().getWebService();
        raiseStatusMessage("");
        myPlayerId = 0;
        refreshGamesList();
    }
    
    public int getJoinedPlayerId() {
        return myPlayerId;
    }
    
    public String getPlayerName() {
        return playerName.getText();
    }

    @Override
    public void hide() {
    }
    
}
