/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package blackjack.engine.types;

/**
 *
 * @author Alex
 */
public enum EventType {
    GAME_START,
    GAME_OVER,
    GAME_WINNER,
    PLAYER_RESIGNED,
    PLAYER_TURN,
    CARDS_DEALT,
    WAITING_ACTION;
}
