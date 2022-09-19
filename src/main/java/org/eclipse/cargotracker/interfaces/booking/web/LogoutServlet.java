package org.eclipse.cargotracker.interfaces.booking.web;

import java.io.IOException;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.cargotracker.application.security.Auth0Config;

/**
 * @author hantsy
 */
@WebServlet(urlPatterns = "/logout")
public class LogoutServlet extends HttpServlet {
    private final Auth0Config config;

    @Inject
    LogoutServlet(Auth0Config config) {
        this.config = config;
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        clearSession(request);
        response.sendRedirect(getLogoutUrl(request));
    }

    private void clearSession(HttpServletRequest request) {
        if (request.getSession() != null) {
            request.getSession().invalidate();
        }
    }

    private String getLogoutUrl(HttpServletRequest request) {
        String returnUrl = String.format("%s://%s", request.getScheme(), request.getServerName());
        int port = request.getServerPort();
        String scheme = request.getScheme();

        if (("http".equals(scheme) && port != 80) || ("https".equals(scheme) && port != 443)) {
            returnUrl += ":" + port;
        }

        returnUrl += request.getContextPath()+"/admin/";

        // Build logout URL like:
        // https://{YOUR-DOMAIN}/v2/logout?client_id={YOUR-CLIENT-ID}&returnTo=http://localhost:3000/
        String logoutUrl =
                String.format(
                        "https://%s/v2/logout?client_id=%s&returnTo=%s",
                        config.getDomain(), config.getClientId(), returnUrl);

        return logoutUrl;
    }
}
