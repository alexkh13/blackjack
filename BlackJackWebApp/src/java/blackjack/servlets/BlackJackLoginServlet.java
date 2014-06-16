/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package blackjack.servlets;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import ws.blackjack.BlackJackWebService;
import ws.blackjack.BlackJackWebService_Service;

/**
 *
 * @author idmlogic
 */
@WebServlet(name = "BlackJackLoginServlet", urlPatterns = {"/login"})
public class BlackJackLoginServlet extends HttpServlet {

    private final Gson gson = new Gson();
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
        BlackJackLoginRequest req = gson.fromJson(request.getReader(), BlackJackLoginRequest.class);
        BlackJackWebService webService = (BlackJackWebService)request.getSession(true).getAttribute("webService");
        if(webService == null && req != null) {
            BlackJackWebService_Service bjWebService = new BlackJackWebService_Service(new URL(req.serverUrl));
            BlackJackWebService bjWebServicePort = bjWebService.getBlackJackWebServicePort();
            request.getSession(true).setAttribute("webService", bjWebServicePort);
            request.getSession().setAttribute("serverUrl", req.serverUrl);
            request.getSession().setAttribute("playerName", req.playerName);
            output(response,req.playerName,req.serverUrl);
        }
        else if(webService == null){
            response.sendError(403, "Not logged in");
        }
        else {
            output(
                    response,
                    (String)request.getSession().getAttribute("playerName"),
                    (String)request.getSession().getAttribute("serverUrl")
            );
        }
    }
    
    private void output(HttpServletResponse response, String playerName, String serverUrl) throws IOException {
        BlackJackLoginRequest res = new BlackJackLoginRequest();
            res.playerName = playerName;
            res.serverUrl = serverUrl;
            response.getWriter().print(gson.toJson(res));
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
        processRequest(request, response);
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

    private class BlackJackLoginRequest {
        String serverUrl;
        String playerName;
    }
}
