/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package blackjack.gui.utils;

import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;

/**
 *
 * @author Alex
 */
public class SceneIcons {
    public static final String ICON_COMPUTER = "\uf109";
    public static final String ICON_HUMAN = "\uf007";
    public static Label createIconLabel(String icon) {
        return LabelBuilder.create()
                .styleClass("icon")
                .text(icon)
                .build();
    }
}
