/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package blackjack.gui.utils;

import blackjack.wsclient.BlackJackWebService;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

/**
 *
 * @author Alex
 * @param <T>
 */
public class SceneLoader<T> extends StackPane {
    
    private BlackJackWebService webservice;
    private final HashMap<T, ControlledScene> scenes = new HashMap<>(); 
    private final List<SceneListener> listeners = new ArrayList<>();
    private final Stage stage;
    private ControlledScene currentScene;

    public SceneLoader(Stage stage) {
        this.stage = stage;
        this.stage.setOnHiding(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                currentScene.hide();
            }
        });
    }

    public void setWebService(BlackJackWebService ws) {
        webservice = ws;
    }
    
    public BlackJackWebService getWebService() {
        return webservice;
    }
    
    public boolean loadScene(T key, URL resource) {
        try { 
            FXMLLoader myLoader = new FXMLLoader(resource);
            Parent loadScene = (Parent) myLoader.load(); 
            ControlledScene mySceneController = 
                   ((ControlledScene) myLoader.getController());
            mySceneController.setKey(key);
            mySceneController.setRootNode(loadScene);
            mySceneController.setSceneLoader(this);
            addScene(key, mySceneController); 
            return true; 
        }
        catch(Exception e) { 
            System.out.println(e); 
            return false; 
        } 
    } 

    public void addScene(T name, ControlledScene scene) {
        scenes.put(name, scene);    
    }

    private void hideCurrentScene() {
        if(!getChildren().isEmpty()) {
            getChildren().remove(0); 
        }
    }

    private void showScene(ControlledScene scene) {
        getChildren().add(0, scene.getRootNode());
    }

    public ControlledScene setScene(T key) {

        currentScene = getScene(key);

        hideCurrentScene();
        showScene(currentScene);

        currentScene.show();

        for(SceneListener listener : listeners) {
            listener.changed(currentScene);
        }

        return currentScene;
    }

    public Stage getStage() {
        return stage;
    }
    
    public ControlledScene getScene(T key) {
        return scenes.get(key);
    }

    public void showAndWait(Parent root) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initStyle(StageStyle.TRANSPARENT);
        popup.setScene(new Scene(root));
        popup.showAndWait();
    }
}
