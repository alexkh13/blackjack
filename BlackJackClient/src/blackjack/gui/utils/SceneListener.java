/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package blackjack.gui.utils;

/**
 *
 * @author Alex
 * @param <T>
 */
public interface SceneListener<T> {
    public void changed(T newScene);
}
