package org.eclipse.cargotracker.application.security;

import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.security.enterprise.credential.Credential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.IdentityStore;
import static javax.security.enterprise.identitystore.IdentityStore.ValidationType.VALIDATE;

/**
 *
 * @author hantsy
 */
@ApplicationScoped
public class Auth0JwtIdentityStore implements IdentityStore {

    @Override
    public CredentialValidationResult validate(final Credential credential) {
        CredentialValidationResult result = CredentialValidationResult.NOT_VALIDATED_RESULT;
        if (credential instanceof Auth0JwtCredential) {
            Auth0JwtCredential auth0JwtCredential = (Auth0JwtCredential) credential;
            result = new CredentialValidationResult(auth0JwtCredential.getAuth0JwtPrincipal());
        }
        return result;
    }
    
    @Override
    public Set<ValidationType> validationTypes() {
        return Set.of(VALIDATE);
    }
}
