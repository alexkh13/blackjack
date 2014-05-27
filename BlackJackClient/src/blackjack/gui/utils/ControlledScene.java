/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package blackjack.gui.utils;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.scene.Node;

/**
 *
 * @author Alex
 * @param <T>
 */
public abstract class ControlledScene<T> implements Initializable  {
    
    private boolean initialized = false;
    private Node root;
    private SceneLoader sceneLoader;
    private T key;
    
    public void setSceneLoader(SceneLoader sceneLoader) {
        this.sceneLoader = sceneLoader;
    }
    
    public SceneLoader getSceneLoader() {
        return sceneLoader;
    }
    
    protected void setRootNode(Node root) {
        this.root = root;
    }
    
    protected Node getRootNode() {
        return root;
    }

    protected void setInitialized() {
        initialized = true;
    }
    
    protected boolean isInitialized() {
        return initialized;
    }
    
    public void setKey(T key) {
        this.key = key;
    }
    
    public T getKey() {
        return key;
    }
    
    abstract public void show();
    
    abstract public void hide();
    
}
