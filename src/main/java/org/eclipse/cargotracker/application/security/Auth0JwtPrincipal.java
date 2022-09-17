package org.eclipse.cargotracker.application.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import javax.security.enterprise.CallerPrincipal;

/**
 *
 * @author hantsy
 */
public class Auth0JwtPrincipal extends CallerPrincipal {
    private final DecodedJWT idToken;

    Auth0JwtPrincipal(DecodedJWT idToken) {
        super(idToken.getClaim("name").asString());
        this.idToken = idToken;
    }

    public DecodedJWT getIdToken() {
        return this.idToken;
    }
}