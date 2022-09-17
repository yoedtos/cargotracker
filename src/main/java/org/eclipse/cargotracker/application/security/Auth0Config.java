package org.eclipse.cargotracker.application.security;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
@Named("auth0Config")
public class Auth0Config {

    private static final Logger LOGGER = Logger.getLogger(Auth0Config.class.getName());

    private static final String DEFAULT_SCOPE = "openid profile email";
    private String domain;
    private String clientId;
    private String clientSecret;
    private String issuerUri;
    private String scope;

    @PostConstruct
    void init() {
        LOGGER.config("Auth0Config.init()");
        try {
            var properties = new Properties();
            properties.load(getClass().getResourceAsStream("/auth0.properties"));
            domain = properties.getProperty("domain");
            clientId = properties.getProperty("clientId");
            clientSecret = properties.getProperty("clientSecret");
            issuerUri = properties.getProperty("issuerUri");
            scope = properties.getProperty("scope");
            
            if (scope == null) {
                scope = DEFAULT_SCOPE;
            }

            if (issuerUri == null && domain != null) {
                issuerUri = "https://" + this.domain + "/";
            }

            LOGGER.log(
                    Level.INFO,
                    "domain: {0}, clientId: {1}, clientSecret:{2}, issuerUri: {3}, scope: {4}",
                    new Object[]{
                        domain,
                        clientId,
                        clientSecret,
                        issuerUri,
                        scope
                    });
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load auth0.properties", e);
        }
    }

    public String getDomain() {
        return domain;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getIssuerUri() {
        return issuerUri;
    }

    public String getScope() {
        return scope;
    }

}
