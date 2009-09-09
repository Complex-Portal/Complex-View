/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.application.editor.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.*;
import org.springframework.security.providers.AuthenticationProvider;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import uk.ac.ebi.intact.application.editor.event.EventListener;
import uk.ac.ebi.intact.application.editor.event.LoginEvent;
import uk.ac.ebi.intact.application.editor.exception.AuthenticateException;
import uk.ac.ebi.intact.application.editor.struts.security.UserAuthenticator;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Component
public class EditorAuthenticationProvider implements AuthenticationProvider {

    private static final Log log = LogFactory.getLog( EditorAuthenticationProvider.class );

    @Autowired
    private EventListener eventListener;

    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (log.isDebugEnabled()) {
            log.debug("Authenticating user: "+authentication.getPrincipal());
        }

        try {
            UserAuthenticator.authenticate((String)authentication.getPrincipal(), (String)authentication.getCredentials());
        } catch (AuthenticateException e) {
            if (log.isInfoEnabled()) log.info("Authentication failed for user: "+authentication.getPrincipal());

            throw new BadCredentialsException("Bad credentials for user: "+authentication.getPrincipal(), e);
        }

        if (log.isInfoEnabled()) log.info("Authentication successful for user: "+authentication.getPrincipal());

        eventListener.notifyObservers(new LoginEvent((String)authentication.getPrincipal()));

        GrantedAuthority grantedAuthority = new GrantedAuthorityImpl("ROLE_CURATOR");
        Authentication auth = new UsernamePasswordAuthenticationToken(authentication.getPrincipal(), authentication.getCredentials(),
                new GrantedAuthority[] {grantedAuthority});

        return auth;
    }

    public boolean supports(Class authentication) {
        return true;
    }
}
