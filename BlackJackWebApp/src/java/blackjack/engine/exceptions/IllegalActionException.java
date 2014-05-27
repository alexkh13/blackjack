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
public class IllegalActionException extends RuntimeException {
    private final String message;
    public IllegalActionException(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "IllegalActionException{" + "message=" + message + '}';
    }
    
}
