/*
 * Copyright (c) 2018, Xyneex Technologies. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * You are not meant to edit or modify this source code unless you are
 * authorized to do so.
 *
 * Please contact Xyneex Technologies, #1 Orok Orok Street, Calabar, Nigeria.
 * or visit www.xyneex.com if you need additional information or have any
 * questions.
 */
package com.demo.security;

import com.demo.access.AccessDAO;
import com.demo.access.CandidateAccess;
import com.demo.users.Candidate;
import com.demo.users.UserDAO;
import com.demo.utils.DateTimeUtil;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.persistence.exceptions.JSONException;
import org.json.JSONObject;

/**
 *
 * @author BLAZE
 */
public class CreateNewCandidateServlet extends HttpServlet
{

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        try
        {
            Candidate user = validateUser(request);
            System.out.println("Validated user.................");
            UserDAO.registerNewCandidate(user);
            System.out.println("Regitered user.................");
            JSONObject jsono = new JSONObject();
            jsono.put("message", "success");
            out.print(jsono);
        }
        catch(Exception xcp)
        {
            if(xcp instanceof IllegalArgumentException)
            {
                xcp.printStackTrace(System.err);
                try
                {
                    JSONObject jsono = new JSONObject();
                    jsono.put("message", xcp.getMessage());
                    out.print(jsono);
                }
                catch(JSONException jsone)
                {
                    throw new RuntimeException(jsone);
                }
            }
            else
                throw new RuntimeException(xcp);
        }
        finally
        {
            out.close();
        }
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
            throws ServletException, IOException
    {
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
            throws ServletException, IOException
    {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo()
    {
        return "Short description";
    }// </editor-fold>

    private Candidate validateUser(HttpServletRequest request) throws Exception
    {
        String name = request.getParameter("name").trim();
        String password = request.getParameter("password");
        String position = request.getParameter("position");
        String authPin = request.getParameter("authPin");
        if(UserDAO.getCandidateByName(name) != null)
            throw new IllegalArgumentException("An account with name: " + name + " already exists.");
        else if(password.isEmpty())
            throw new IllegalArgumentException("Please enter a password.");
        else if(password.length() < 6)
            throw new IllegalArgumentException("Password should be at least 6 characters.");
        else if(AccessDAO.getPinById(authPin) == null || AccessDAO.pinUsed(authPin) == true)
            throw new IllegalArgumentException("Oops invalid authorisation pin.");
        else
        {

            CandidateAccess pin = AccessDAO.getPinById(authPin);
            Digester digester = new Digester();
            String hashedPassword = digester.doDigest(password);
            Candidate user = new Candidate();
            user.setCanId(UserDAO.generateUniqueCandidateId());
            user.setEncrytedPasword(hashedPassword);
            user.setRole("candidate");
            user.setPosition(position);
            user.setName(name);

            AccessDAO.updatePinTable(pin, "used", DateTimeUtil.getTodayTimeZone(), name);
            return user;
        }
    }
}
