/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package blackjack.servlets;

/**
 *
 * @author idmlogic
 */
public class BlackJackRequest {
    
    enum RequestType {
        CREATE,
        JOIN,
        RESIGN,
        ACTION
    }
    
    String gameName;
    String playerName;
    int humans;
    int computers;
    RequestType type;
    
    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public RequestType getType() {
        return type;
    }

    public void setType(String type) {
        this.type = RequestType.valueOf(type);
    }

    public int getHumans() {
        return humans;
    }

    public void setHumans(int humans) {
        this.humans = humans;
    }

    public int getComputers() {
        return computers;
    }

    public void setComputers(int computers) {
        this.computers = computers;
    }
    
    
}
