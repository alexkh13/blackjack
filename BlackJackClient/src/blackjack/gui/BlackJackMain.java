package blackjack.gui;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import blackjack.gui.controllers.GameController;
import blackjack.gui.controllers.GameSetupController;
import blackjack.gui.controllers.MainController;
import blackjack.gui.utils.ControlledScene;
import blackjack.gui.utils.ModalUtilities;
import blackjack.gui.utils.SceneLoader;
import blackjack.wsclient.BlackJackWebService;
import blackjack.wsclient.BlackJackWebService_Service;
import blackjack.wsclient.InvalidParameters_Exception;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 *
 * @author Alex
 */
public class BlackJackMain extends Application {
    
    private final String DEFAULT_SERVER_URL = "http://localhost:8081";
    
    private BlackJackWebService webService;
    private SceneLoader<GameScreen> sceneLoader;
    private GameController gameController;
    private BlackJackClientEngine clientEngine;
    private GameSetupController setupController;
    private Thread mainGameLoop;
    private MainController mainController;
    
    @Override
    public void init() throws Exception {
        super.init();
        Font.loadFont(getClass().getResource("resources/fontawesome-webfont.ttf").toExternalForm(), 12);
    }
    
    @Override
    public void start(Stage stage) {
        InitializeStage(stage);
        
        stage.show();
        setScreen(GameScreen.MAIN_SCENE);
        
        mainController.serviceStatus.addListener(new ChangeListener<MainController.Status>() {
            @Override
            public void changed(ObservableValue<? extends MainController.Status> ov, MainController.Status t, MainController.Status t1) {
                if(t1 == MainController.Status.CONNECTED) {
                    sceneLoader.setWebService(webService);
                    mainController.setStatus(MainController.Status.CONNECTED);
                    clientEngine = new BlackJackClientEngine(webService);
                }
            }
            
        });
        
        mainController.serviceStatus.set(MainController.Status.LOADING);
        
        mainController.setStatus(MainController.Status.LOADING);

        new Thread(new Runnable() {

            @Override
            public void run() {
                
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {

                        while(!isActive()) {

                            String inputURL = null;
                            try {
                                webService = (new BlackJackWebService_Service(new URL(Settings.getServerURL()))).getBlackJackWebServicePort();
                                mainController.setStatus(MainController.Status.CONNECTED);
                            } 
                            catch (Settings.UnableToLoadSettingsException ex) {
                                System.out.println("Unable to load settings");
                            } catch (MalformedURLException ex) {
                                System.out.println("Bad address");
                            }
                            catch (RuntimeException ex) {
                                System.out.println(ex.getMessage());
                            }
                            finally {
                                if(!isActive()) {
                                    inputURL = ModalUtilities.getUserAnswer(
                                            "Please enter the server's ip address", 
                                            inputURL == null ? DEFAULT_SERVER_URL : inputURL, 
                                            "Confirm");
                                    if(inputURL == null) {
                                        mainController.setStatus(MainController.Status.EXIT);
                                    }
                                    else {
                                        try {
                                            Settings.setServerURL(inputURL);
                                        } catch (Settings.UnableToSaveSettingsException ex) {
                                        }
                                    }
                                }
                            }
                        }
                    }

                });
                
            }

        }).start();
    }
    
    private boolean isActive() {
        return mainController.serviceStatus.get() != MainController.Status.LOADING;
    }

    private void InitializeStage(Stage stage) {
        stage.setTitle("BlackJack");
        Scene scene = new Scene(sceneLoader = new SceneLoader<>(stage),850,575); 
        for(GameScreen screen : GameScreen.values()) {
            loadScreen(screen);
            switch(screen) {
                case MAIN_SCENE:
                    mainController = ((MainController)sceneLoader.getScene(screen));
                    break;
                case GAME_SETUP:
                    setupController = ((GameSetupController)sceneLoader.getScene(screen));
                    setupController.gameReady.addListener(new gameReadyListener());
                    break;
                case GAME_SCENE:
                    gameController = (GameController)sceneLoader.getScene(screen);
                    gameController.playerChoice.addListener(new playerChoiceListener());
                    gameController.playerBet.addListener(new playerBetListener());
            }
        }
        stage.setScene(scene); 
        stage.setResizable(false);
    }
    
    private ControlledScene setScreen(GameScreen screen) {
        return sceneLoader.setScene(screen);
    }
    
    private void loadScreen(GameScreen screen) {
        sceneLoader.loadScene(screen, getClass().getResource(screen.getFXML()));
    }
    
    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    private class gameReadyListener implements ChangeListener<Boolean> {

        @Override
        public void changed(ObservableValue<? extends Boolean> ov, Boolean prev, Boolean gameReady) {
            if(gameReady) {
                clientEngine.setPlayerId(setupController.getPlayerId());
                clientEngine.setGameName(setupController.getGameName());
                sceneLoader.setScene(GameScreen.GAME_SCENE);
                gameController.setPlayers(clientEngine.getPlayers());
                mainGameLoop = new Thread(new MainGameLoop());
                mainGameLoop.start();
            }
        }
    }
    
    private class playerChoiceListener implements ChangeListener<GameController.PlayerChoice> {
        @Override
        public void changed(ObservableValue<? extends GameController.PlayerChoice> ov, GameController.PlayerChoice t, GameController.PlayerChoice playerChoice) {
            try {
                switch(playerChoice) {
                    case SPLIT:
                        clientEngine.playerAction(BlackJackClientEngine.PlayerAction.SPLIT);
                        break;
                    case STAND:
                        clientEngine.playerAction(BlackJackClientEngine.PlayerAction.STAND);
                        break;
                    case DOUBLE:
                        clientEngine.playerAction(BlackJackClientEngine.PlayerAction.DOUBLE);
                        break;
                    case HIT:
                        clientEngine.playerAction(BlackJackClientEngine.PlayerAction.HIT);
                        break;
                    case EXIT:
                        mainGameLoop.interrupt();
                        clientEngine.resign();
                        sceneLoader.setScene(GameScreen.MAIN_SCENE);
                    default:
                        return;
                }
                gameController.stopTimeout();
            } 
            catch (InvalidParameters_Exception ex) {
                gameController.requirePlayerChoice();
            }
        }
        
    }
    
    private class playerBetListener implements ChangeListener<Number> {
        @Override
        public void changed(ObservableValue<? extends Number> ov, Number t, Number playerBet) {
            float betAmount = playerBet.floatValue();
            if(betAmount > 0) {
                if(!clientEngine.placeBet(betAmount)) {
                    gameController.setMessage("Not enough money!");
                    gameController.requirePlayerBet();
                }
            }
            else if(betAmount == 0) {
                gameController.setMessage("Use exit button!");
                gameController.requirePlayerBet();
            }
        }
    }
    
    private boolean parseEvent(ClientEvent event) {
        switch(event.getType()) {
            case DO_PLAYER_HIT:
                gameController.playerHit(event.getPlayer(), event.getCard());
                if(event.getMoney() > 20) {
                    gameController.playerSwitchHand(event.getPlayer());
                }
            return true;
            case DO_PLAYER_SPLIT:
                return gameController.playerSplit(event.getPlayer(), event.getCards());
            case DO_PLAYER_DOUBLE:
                return gameController.playerDouble(event.getPlayer(), event.getCard(), event.getMoney());
            case DO_PLAYER_BET:
                return gameController.playerBet(event.getPlayer(), event.getMoney());
            case DO_PLAYER_STAND:
                gameController.playerStand(event.getPlayer());
                if(clientEngine.isAlreadySplit()){
                    gameController.playerSwitchHand(event.getPlayer());
                }
            return true;
            case ANNOUNCE_NEW_ROUND:
                return gameController.resetGame();
            case ANNOUNCE_PLAYER_TURN:
                return gameController.playerTurn(event.getPlayer());
            case ANNOUNCE_PLAYER_WINNER:
                return gameController.playerWinner(event.getPlayer(),event.getMoney());
            case ANNOUNCE_PLAYER_RESIGN:
                if(event.getPlayer().equals(setupController.getPlayerName())) {
                    mainGameLoop.interrupt();
                    sceneLoader.setScene(GameScreen.MAIN_SCENE);
                }
                return gameController.playerResign(event.getPlayer());
            case ANNOUNCE_DEALER_CARDS:
                return gameController.dealerCards(event.getCards());
            case ANNOUNCE_PLAYER_CARDS:
                return gameController.handPlayerCards(event.getPlayer(), event.getCards());
            case PROMPT_PLAYER_FOR_ACTION:
                gameController.startTimeout(event.getTimeout());
                if(setupController.getPlayerName().equals(event.getPlayer())) {
                    gameController.setMessage("It's your turn");
                    // We have to check with the engine whats the status of the player
                    // to decide if we need to require the player to bet or to make an action
                    if(clientEngine.isPlayerAlreadyBet(event.getPlayer())) {
                        return gameController.requirePlayerChoice();
                    }
                    else {
                        return gameController.requirePlayerBet();
                    }
                }
        }
        return false;
    }
    
    private class MainGameLoop implements Runnable {
        @Override
        public void run() {
            final ClientEvent currentEvent = new ClientEvent();
            while(!currentEvent.getFault()) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            while(currentEvent.copy(clientEngine.getNextEvent()) != null) {
                                try {
                                    parseEvent(currentEvent);
                                }
                                catch(RuntimeException ex) {
                                    System.out.println(ex);
                                }
                            }
                        } 
                        catch (InvalidParameters_Exception ex) {
                            currentEvent.setFaultEvent();
                        }
                    }
                });    
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException ex) {
                    currentEvent.setFaultEvent();
                }
            }
        }
    }
}
