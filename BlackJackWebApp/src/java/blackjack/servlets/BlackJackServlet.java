package blackjack.servlets;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

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
import javax.xml.ws.WebServiceRef;
import ws.blackjack.BlackJackWebService;
import ws.blackjack.BlackJackWebService_Service;
import ws.blackjack.DuplicateGameName_Exception;
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
    private String sessionId;
            
    private final Gson gson = new Gson();
    private String requestGameName;
    private PrintWriter out;
    
    enum BlackJackError {
        GAME_DOES_NOT_EXIST,
        GAME_EXIST
    }
    
    @Override
    public void init() {
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
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        out = response.getWriter();
        
        requestGameName = null;
        
        String pathInfo = request.getPathInfo();
        if(pathInfo != null) {
            String[] pathParts = request.getPathInfo().split("/");
            if(pathParts.length > 0) {
                requestGameName = pathParts[1];
            }
        }
        
        try {
            if(getServletContext().getAttribute("webService") == null) {
                blackJackWebService = new BlackJackWebService_Service(new URL(getServletContext().getInitParameter("webservice_url")));
                webService = blackJackWebService.getBlackJackWebServicePort();
                getServletContext().setAttribute("webService", webService);
            }
            else {
                webService = (BlackJackWebService)getServletContext().getAttribute("webService");
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(BlackJackServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        sessionId = request.getSession(true).getId();
        service = new BlackJackService(webService, sessionId);
        
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
        processRequest(request, response);
        if(requestGameName != null) {
            try {
                System.out.println("Request for " + requestGameName);
                send(service.getGame(requestGameName));
            } 
            catch (GameDoesNotExists_Exception ex) {
                error(response, BlackJackError.GAME_DOES_NOT_EXIST);
            } 
            catch (InvalidParameters_Exception ex) {
                error(response, ex.getMessage());
            }
        }
        else {
            System.out.println("Request for all games");
            // get waiting games
            send(service.getAvailableGames());
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
        try {
            processRequest(request, response);
            BlackJackRequest req = getBlackJackRequest(request);
            
            switch(req.getType()) {
                case CREATE:
                    send(service.createGame(req.getGameName(), req.getPlayerName(), req.getHumans(), req.getComputers()));
                    System.out.println("New player id: " + service.getPlayerId());
                    request.getSession().setAttribute("playerId", service.getPlayerId());
                    break;
                case JOIN:
                    send(service.joinGame(req.getGameName(), req.getPlayerName()));
                    request.getSession().setAttribute("playerId", service.getPlayerId());
                    break;
                case RESIGN:
                    service.resign(sessionId);
                    webService.resign(playerId);
                    break;
                case ACTION:
                    
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
                response.sendError(500, "Game already exist");
                break;
        }
    }
    
    private void error(HttpServletResponse response, String message) throws IOException {
        response.sendError(500, message);
    }
    
}