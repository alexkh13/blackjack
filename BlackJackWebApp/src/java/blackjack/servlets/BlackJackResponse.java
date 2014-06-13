/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package blackjack.servlets;

import java.util.ArrayList;
import java.util.List;
import ws.blackjack.Event;
import ws.blackjack.GameDetails;

/**
 *
 * @author idmlogic
 */
public class BlackJackResponse {
    String name;
    String status;
    int numberOfHumans;
    int numberOfComputers;
    int numberOfJoinedHumans;
    int playerId;
    List<Event> events;

    BlackJackResponse() {
        
    }
    BlackJackResponse(GameDetails gameDetails) {
        name = gameDetails.getName();
        status = gameDetails.getStatus().toString();
        numberOfHumans = gameDetails.getHumanPlayers();
        numberOfComputers = gameDetails.getComputerizedPlayers();
        numberOfJoinedHumans = gameDetails.getJoinedHumanPlayers();
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getNumberOfHumans() {
        return numberOfHumans;
    }

    public void setNumberOfHumans(int numberOfHumans) {
        this.numberOfHumans = numberOfHumans;
    }

    public int getNumberOfComputers() {
        return numberOfComputers;
    }

    public void setNumberOfComputers(int numberOfComputers) {
        this.numberOfComputers = numberOfComputers;
    }

    public int getNumberOfJoinedHumans() {
        return numberOfJoinedHumans;
    }

    public void setNumberOfJoinedHumans(int numberOfJoinedHumans) {
        this.numberOfJoinedHumans = numberOfJoinedHumans;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }
    
}
