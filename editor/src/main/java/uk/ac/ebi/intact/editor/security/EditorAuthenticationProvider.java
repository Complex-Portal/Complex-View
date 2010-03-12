/**
 * Copyright 2010 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.editor.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.providers.AuthenticationProvider;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import uk.ac.ebi.intact.editor.controller.EditorUserContext;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class EditorAuthenticationProvider implements AuthenticationProvider {

    private static final Log log = LogFactory.getLog( EditorAuthenticationProvider.class );

    @Autowired
    private ApplicationContext applicationContext;

    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (log.isDebugEnabled()) {
            log.debug("Authenticating user: "+authentication.getPrincipal());
        }

        EditorUserContext editorUserContext = (EditorUserContext) applicationContext.getBean("editorUserContext");

        if (log.isInfoEnabled()) log.info("Authentication successful for user: "+authentication.getPrincipal());

        editorUserContext.setCurrentUser(authentication.getPrincipal().toString());

        GrantedAuthority curatorAuthority = new GrantedAuthorityImpl("ROLE_CURATOR");
        GrantedAuthority adminAuthority = new GrantedAuthorityImpl("ROLE_ADMIN");
        Authentication auth = new UsernamePasswordAuthenticationToken(authentication.getPrincipal(), authentication.getCredentials(),
                new GrantedAuthority[] {curatorAuthority, adminAuthority});

        return auth;
    }

    public boolean supports(Class authentication) {
        return true;
    }
}