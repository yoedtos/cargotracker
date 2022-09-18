package org.eclipse.cargotracker.interfaces.booking.web;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author hantsy
 */
@WebServlet(urlPatterns = {"/callback"})
public class CallbackServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String referer = (String) request.getSession().getAttribute("Referer");
        String redirectTo = referer != null ? referer : request.getContextPath() + "/admin";

        response.sendRedirect(redirectTo);
    }
}
