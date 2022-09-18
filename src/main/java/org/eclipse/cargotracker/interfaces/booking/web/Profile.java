/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.eclipse.cargotracker.interfaces.booking.web;

import com.auth0.jwt.interfaces.Claim;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.security.enterprise.SecurityContext;
import org.eclipse.cargotracker.application.security.Auth0JwtPrincipal;

/**
 * @author hantsy
 */
@RequestScoped
@Named("profile")
public class Profile {

    private static final Logger LOGGER = Logger.getLogger(Profile.class.getName());

    @Inject SecurityContext securityContext;

    private Map<String, Claim> claims;

    public void load() {
        LOGGER.log(Level.FINEST, "get profile info.");
        var principal = securityContext.getCallerPrincipal();
        if (principal instanceof Auth0JwtPrincipal) {
            var jwtPrincipal = (Auth0JwtPrincipal) principal;
            claims = jwtPrincipal.getIdToken().getClaims();
        }
    }

    public Map<String, Claim> getClaims() {
        return claims;
    }
}
