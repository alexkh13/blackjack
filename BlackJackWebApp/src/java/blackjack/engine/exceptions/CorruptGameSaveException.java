/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package blackjack.engine.exceptions;

/**
 *
 * @author Alex
 */
public class CorruptGameSaveException extends RuntimeException {
    private final String message;
    public CorruptGameSaveException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
    
    @Override
    public String toString() {
        return "CorruptGameSaveException{" + "message=" + message + '}';
    }
}
