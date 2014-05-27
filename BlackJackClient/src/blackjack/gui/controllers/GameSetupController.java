/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package blackjack.gui.controllers;

import blackjack.gui.GameScreen;
import blackjack.gui.utils.ControlledScene;
import blackjack.gui.utils.NumericTextField;
import blackjack.wsclient.BlackJackWebService;
import blackjack.wsclient.DuplicateGameName_Exception;
import blackjack.wsclient.GameDetails;
import blackjack.wsclient.GameDoesNotExists_Exception;
import blackjack.wsclient.InvalidParameters_Exception;
import blackjack.wsclient.PlayerDetails;
import blackjack.wsclient.PlayerType;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author Alex
 */
public class GameSetupController extends ControlledScene<GameScreen>  {

    private final int PLAYER_START_MONEY=2000;
    
    @FXML private TextField gameName;
    @FXML private TextField playerName;
    @FXML private NumericTextField numHumans;
    @FXML private NumericTextField numComputers;
    @FXML private TableView<PlayerDisplay> playersList;
    @FXML private Label message;
    @FXML private Button createButton;
    
    private int totalNumOfPlayers = 0;
    private boolean isJoined = false;
    private int myPlayerId = 0;
    private BlackJackWebService service;
    private Thread refresher;
    
    public SimpleBooleanProperty gameReady = new SimpleBooleanProperty();
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
       gameReady.set(false);
    }
    
    public void setRefresher(boolean start) {
        if(start) {
            refresher = new Thread(new PlayerListRefresher());
            refresher.start();
        }
        else {
            if(refresher != null) {
                refresher.interrupt();
            }
        }
    }
    
    public void handleCreateGame(ActionEvent event) {
        try {
            String name = gameName.getText().trim();
            gameName.setText(name);
            int nHumans = numHumans.getNumber();
            int nComputers = numComputers.getNumber();
            totalNumOfPlayers = nHumans + nComputers;
            service.createGame(name, nHumans, nComputers);
            myPlayerId = service.joinGame(name, getPlayerName(), PLAYER_START_MONEY);

            disableAllInputs();
            
            setMessage("Waiting for other players...", false);
            
            setRefresher(true);
        } 
        catch (DuplicateGameName_Exception ex) {
            setMessage("ERROR: Game with the same name already exists", true);
        } 
        catch (InvalidParameters_Exception ex) {
            setMessage("ERROR: " + ex.getMessage(), true);
        }
        catch (GameDoesNotExists_Exception ex) {
            setMessage("ERROR: Something went wrong... try again.", true);
        }
        catch(Exception ex) {
            setMessage("ERROR: Unable connect to server.", true);
        }
    }
    
    private void disableAllInputs() {
        gameName.setDisable(true);
        playerName.setDisable(true);
        createButton.setDisable(true);
        numHumans.setDisable(true);
        numComputers.setDisable(true);
        playersList.setDisable(false);
    }
    
    private void enableAllInputs() {
        gameName.setDisable(false);
        playerName.setDisable(false);
        createButton.setDisable(false);
        numHumans.setDisable(false);
        numComputers.setDisable(false);
        playersList.setDisable(true);
    }
    
    private void inputsResetDefaults() {
        isJoined = false;
        myPlayerId = 0;
        numHumans.setNumber(1);
        numComputers.setNumber(0);
        gameName.setText("");
        playersList.getItems().clear();
        setMessage("",true);
    }
    
    private void refreshPlayerList() {
        try {
            List<PlayerDetails> players = service.getPlayersDetails(gameName.getText());
            List list = playersList.getItems();
            list.clear();
            for(PlayerDetails player : players) {
                addPlayerToList(player);
            }
            if(players.size() == totalNumOfPlayers) {
                setRefresher(false);
                setMessage("The game will begin shortly...", false);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(3000);
                            gameReady.set(true);
                            gameReady.set(false);
                        } 
                        catch (InterruptedException ex) {}
                    }
                });
                
            }
        } 
        catch (GameDoesNotExists_Exception ex) {
            Logger.getLogger(GameSetupController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void setMessage(String text, boolean isError) {
        message.setText(text);
        message.getStyleClass().remove(isError ? "info_message" : "error_message");
        message.getStyleClass().add(isError ? "error_message" : "info_message");
    }
    
    public void handleRefresh(ActionEvent event) {
        this.refreshPlayerList();
    }
    
    public void handleCancel(ActionEvent event) {
        cancel();
        this.getSceneLoader().setScene(isJoined ? GameScreen.JOIN_SCENE : GameScreen.MAIN_SCENE);
        //game = new Game();
    }
    
    private void cancel() {
        setRefresher(false);
        try {
            if(myPlayerId != 0) {
                service.resign(myPlayerId);
            }
        } 
        catch (InvalidParameters_Exception ex) {
            System.out.println("Player id " + myPlayerId + " not part of game what-so-ever");
        }
    }
    
    private void addPlayerToList(PlayerDetails player) {
        playersList.getItems().add(new PlayerDisplay(player.getName(), player.getType() == PlayerType.HUMAN));
    }

    private void joinGame(String name) {
        try {
            isJoined = true;
            GameDetails details = service.getGameDetails(name);
            gameName.setText(name);
            numHumans.setNumber(details.getHumanPlayers());
            numComputers.setNumber(details.getComputerizedPlayers());
            totalNumOfPlayers = details.getHumanPlayers() + details.getComputerizedPlayers();
            disableAllInputs();
            setRefresher(true);
        } 
        catch (GameDoesNotExists_Exception ex) {
            Logger.getLogger(GameSetupController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void show() {
        inputsResetDefaults();
        enableAllInputs();
        
        service = this.getSceneLoader().getWebService();
        JoinGameController controller = (JoinGameController)this.getSceneLoader().getScene(GameScreen.JOIN_SCENE);
        String joinGameSelection = controller.getSelectedGame();
        myPlayerId = controller.getJoinedPlayerId();
        if(joinGameSelection != null) {
            joinGame(joinGameSelection);
            playerName.setText(controller.getPlayerName());
        }
    }
    
    public int getPlayerId() {
        return myPlayerId;
    }
    
    public String getGameName() {
        return gameName.getText();
    }
    
    public String getPlayerName() {
        return playerName.getText();
    }

    @Override
    public void hide() {
        cancel();
    }
    
    public class PlayerListRefresher implements Runnable {

        @Override
        public void run() {
            boolean valid = true;
            while(valid) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        refreshPlayerList();
                    }
                });
                try {
                    Thread.sleep(1000);
                } 
                catch (InterruptedException ex) {
                    valid = false;
                }
            }
        }
        
    }
    
}
