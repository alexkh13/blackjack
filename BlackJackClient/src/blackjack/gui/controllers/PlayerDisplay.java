/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package blackjack.gui.controllers;

import blackjack.gui.utils.SceneIcons;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author Alex
 */

public class PlayerDisplay {
    private final SimpleStringProperty name = new SimpleStringProperty("");
    private final SimpleStringProperty type = new SimpleStringProperty("");

    public PlayerDisplay() {
        this("",true);
    }
    
    public PlayerDisplay(String name, boolean isHuman) {
        this.name.set(name);
        this.type.set(isHuman ? SceneIcons.ICON_HUMAN : SceneIcons.ICON_COMPUTER);
    }

    public String getName() {
        return name.get();
    }

    public String getType() {
        return type.get();
    }
    
    public void setName(String name) {
        this.name.set(name);
    }
    
    public void setType(String type) {
        this.type.set(type);
    }
}

