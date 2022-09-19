package org.eclipse.cargotracker.interfaces.booking.web;

import com.auth0.AuthenticationController;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.DeclareRoles;
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
@WebServlet(urlPatterns = "/login")
@DeclareRoles({"ADMIN", "USER"})
public class LoginServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(LoginServlet.class.getName());

    private final Auth0Config config;
    private final AuthenticationController authenticationController;

    @Inject
    LoginServlet(Auth0Config config, AuthenticationController authenticationController) {
        this.config = config;
        this.authenticationController = authenticationController;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // URL where the application will receive the authorization code (e.g.,
        // http://localhost:3000/callback)
        String callbackUrl =
                String.format(
                        "%s://%s:%s%s/callback",
                        request.getScheme(),
                        request.getServerName(),
                        request.getServerPort(),
                        request.getContextPath());

        LOGGER.log(Level.INFO, "callback url:{0}", callbackUrl);

        // Create the authorization URL to redirect the user to, to begin the authentication flow.
        String authURL =
                authenticationController
                        .buildAuthorizeUrl(request, response, callbackUrl)
                        .withScope(config.getScope())
                        .build();

        response.sendRedirect(authURL);
    }
}
