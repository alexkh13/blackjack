package blackjack.servlets;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import blackjack.servlets.BlackJackRequest.PlayerAction;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.ws.WebServiceRef;
import ws.blackjack.BlackJackWebService;
import ws.blackjack.BlackJackWebService_Service;
import ws.blackjack.DuplicateGameName_Exception;
import ws.blackjack.Event;
import ws.blackjack.EventType;
import ws.blackjack.GameDoesNotExists_Exception;
import ws.blackjack.InvalidParameters_Exception;

/**
 *
 * @author idmlogic
 */
@WebServlet(urlPatterns = {"/api/*"})
public class BlackJackServlet extends HttpServlet {

    private BlackJackWebService_Service blackJackWebService;
    private BlackJackWebService webService;
    
    private BlackJackService service;
            
    private final Gson gson = new Gson();
    private String requestGameName;
    private PrintWriter out;

    private boolean validateSession(HttpSession session) {
        webService = (BlackJackWebService)session.getAttribute("webService");
        return webService != null;
    }

    private boolean playerResigned(BlackJackResponse res, String playerName) {
        for(Event event : res.events) {
            if(event.getType() == EventType.PLAYER_RESIGNED && event.getPlayerName().equals(playerName)) {
                return true;
            }
        }
        return false;
    }
    
    enum BlackJackError {
        NOT_LOGGED_IN,
        GAME_DOES_NOT_EXIST,
        GAME_EXIST,
        NOT_IN_GAME
    }
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected boolean processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        out = response.getWriter();
        requestGameName = null;
        
        if(!validateSession(request.getSession(true))) {
            error(response, BlackJackError.NOT_LOGGED_IN);
            return false;
        }
        else {
            String pathInfo = request.getPathInfo();
            if(pathInfo != null) {
                String[] pathParts = request.getPathInfo().split("/");
                if(pathParts.length > 0) {
                    requestGameName = pathParts[1];
                }
            }

            service = new BlackJackService(webService);
        }
        
        return true;
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if(processRequest(request, response)) {
            if(requestGameName != null) {
                try {
                    int playerId = (int)request.getSession().getAttribute(requestGameName);
                    BlackJackResponse res = service.getGame(playerId, requestGameName);
                    if(playerResigned(res, (String)request.getSession().getAttribute("playerName"))) {
                        request.getSession().setAttribute(requestGameName, null);
                    }
                    send(res);
                } 
                catch (GameDoesNotExists_Exception ex) {
                    error(response, BlackJackError.GAME_DOES_NOT_EXIST);
                } 
                catch (InvalidParameters_Exception ex) {
                    error(response, ex.getMessage());
                }
                catch (NullPointerException ex) {
                    error(response, BlackJackError.NOT_IN_GAME);
                }
            }
            else {
                // get waiting games
                ArrayList<BlackJackResponse> ress = service.getAvailableGames();
                for(BlackJackResponse res : ress) {
                    try {
                        res.setPlayerId((int)request.getSession().getAttribute(res.getName()));
                    }
                    catch(NullPointerException ex) {}
                }
                send(ress);
            }
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if(processRequest(request, response)) {
            try {
                BlackJackRequest req = getBlackJackRequest(request);
                BlackJackResponse res;
                switch(req.getType()) {
                    case CREATE:
                        res = service.createGame(req.getGameName(), req.getPlayerName(), req.getHumans(), req.getComputers());
                        request.getSession().setAttribute(res.getName(), res.getPlayerId());
                        send(res);
                        break;
                    case JOIN:
                        res = service.joinGame(req.getGameName(), req.getPlayerName());
                        request.getSession().setAttribute(res.getName(), res.getPlayerId());
                        send(res);
                        break;
                    case RESIGN:
                        int playerId = (int)request.getSession().getAttribute(req.getGameName());
                        request.getSession().setAttribute(req.getGameName(), null);
                        service.resign(playerId);
                        break;
                    case ACTION:
                        if(req.getAction() == PlayerAction.PLACE_BET) {
                            service.placeBet((int)request.getSession().getAttribute(req.getGameName()), req.getMoney());
                        }
                        else {
                            service.userAction((int)request.getSession().getAttribute(req.getGameName()), req.getAction());
                        }
                        // we have to put something in reponse in order for it to be valid
                        send("OK");
                        break;
                }
            } 
            catch (GameDoesNotExists_Exception ex) {
                error(response, BlackJackError.GAME_DOES_NOT_EXIST);
            } 
            catch (DuplicateGameName_Exception ex) {
                error(response, BlackJackError.GAME_EXIST);
            } 
            catch (InvalidParameters_Exception ex) {
                error(response, ex.getMessage());
            }
            catch(NullPointerException ex) {
                error(response, "Bad request");
            }
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
    
    private void send(Object obj) {
        out.print(gson.toJson(obj));
    }
    
    private BlackJackRequest getBlackJackRequest(HttpServletRequest request) {
        BlackJackRequest blackJackRequest = null;
        try {
            StringBuilder sb = new StringBuilder();
            String s;
            while ((s = request.getReader().readLine()) != null) {
                sb.append(s);
            }
            System.out.println(sb.toString());
            blackJackRequest = (BlackJackRequest) gson.fromJson(sb.toString(), BlackJackRequest.class);
        } catch (IOException ex) {
            Logger.getLogger(BlackJackServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
 
        return blackJackRequest;
    }
    
    private void error(HttpServletResponse response, BlackJackError error) throws IOException {
        switch(error) {
            case GAME_DOES_NOT_EXIST:
                response.sendError(404, "Request game does not exist");
                break;
            case GAME_EXIST:
                error(response, "Game already exists");
                break;
            case NOT_LOGGED_IN:
                response.sendError(403, "Not logged in");
                break;
            case NOT_IN_GAME:
                error(response, "You didn't join this game");
        }
    }
    
    private void error(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        send(new ErrorMessage(message));
    }
    
    private class ErrorMessage {
        String message;
        ErrorMessage(String message) {
            this.message = message;
        }
    }
    
}
