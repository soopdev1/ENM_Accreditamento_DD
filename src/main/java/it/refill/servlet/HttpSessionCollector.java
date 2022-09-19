/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.refill.servlet;

import it.refill.action.ActionB;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 *
 * @author rcosco
 */
public class HttpSessionCollector implements HttpSessionListener {

    private static final Map<String, HttpSession> sessions = new HashMap<String, HttpSession>();

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        HttpSession session = event.getSession();        
        sessions.put(session.getId(), session);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        ActionB.deletesessionid(event.getSession().getId());
        sessions.remove(event.getSession().getId());
    }

    public static void destroy(String sessionId) {
        try {
            HttpSession se = sessions.get(sessionId);
            if (se != null) {
                se.setAttribute("bandorif", null);
                se.setAttribute("statouser", null);
                se.setAttribute("username", null);
                se.setAttribute("lang", null);
                se.setAttribute("numcell", null);
                se.setAttribute("indemail", null);
                se.setAttribute("tipo", null);
                //////////////////////////////////
                se.invalidate();
                se.setMaxInactiveInterval(1);
            }
        } catch (Exception e) {
        }
    }

}
