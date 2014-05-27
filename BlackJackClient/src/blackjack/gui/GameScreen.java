/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package blackjack.gui;

/**
 *
 * @author Alex
 */
public enum GameScreen {
    MAIN_SCENE,
    GAME_SCENE,
    GAME_SETUP,
    JOIN_SCENE;
    
    private String getPath(String name) {
        return "fxml/" + name + ".fxml";
    }
    
    public String getFXML() {
        switch(this) {
            case MAIN_SCENE: return getPath("mainScreen");
            case GAME_SCENE: return getPath("gameScreen");
            case GAME_SETUP: return getPath("gameSetupScreen");
            case JOIN_SCENE: return getPath("joinGameScreen");
            default: return null;
        }
    }
    
    
}
