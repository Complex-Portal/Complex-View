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

import com.google.common.collect.Lists;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.*;
import org.springframework.security.providers.AuthenticationProvider;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.context.UserContext;
import uk.ac.ebi.intact.core.users.model.Role;
import uk.ac.ebi.intact.core.users.model.User;
import uk.ac.ebi.intact.core.users.persistence.dao.UsersDaoFactory;
import uk.ac.ebi.intact.editor.controller.AppController;
import uk.ac.ebi.intact.editor.controller.UserSessionController;

import java.util.Collection;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class EditorAuthenticationProvider implements AuthenticationProvider {

    private static final Log log = LogFactory.getLog( EditorAuthenticationProvider.class );

    @Autowired
    private UsersDaoFactory usersDaoFactory;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private UserContext userContext;

    @Autowired
    private AppController appController;

    @Transactional( value="users", readOnly = true )
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (log.isDebugEnabled()) {
            log.debug("Authenticating user: "+authentication.getPrincipal());
            log.debug("Credentials: '"+authentication.getCredentials() + "'");
        }

        final User user = usersDaoFactory.getUserDao().getByLogin( authentication.getPrincipal().toString() );

        if( user == null || ! user.getPassword().equals( authentication.getCredentials() ) ) {
            if (log.isDebugEnabled()) log.debug("Bad credentials for user: "+authentication.getPrincipal());
            throw new BadCredentialsException( "Unknown user or incorrect password." );
        }

        if( user.isDisabled() ) {
            throw new DisabledException( "User " + user.getLogin() + " has been disabled, please contact the IntAct team." );
        }

        UserSessionController userSessionController = (UserSessionController) applicationContext.getBean("userSessionController");

        if (log.isInfoEnabled()) log.info("Authentication successful for user: "+authentication.getPrincipal());

        // Roles are not loaded by default, even so the @ManyToMany annotation is set to fetch = FetchType.EAGER
        // TODO set fetch more eager in intact-users
        user.getRoles();
        
        userSessionController.setCurrentUser(user);
        userContext.setUserId(user.getLogin().toUpperCase());

        appController.getLoggedInUsers().add( user );

        Collection<GrantedAuthority> authorities = Lists.newArrayList();
        log.info( user.getLogin() + " roles: " + user.getRoles() );
        for ( Role role : user.getRoles() ) {
            final String authorityName = "ROLE_" + role.getName();
            log.info( "Adding GrantedAuthority: '"+ authorityName +"'" );
            authorities.add( new GrantedAuthorityImpl( authorityName ) );
        }

        return new UsernamePasswordAuthenticationToken(authentication.getPrincipal(),
                                                       authentication.getCredentials(),
                                                       authorities.toArray( new GrantedAuthority[]{} ) );
    }

    public boolean supports(Class authentication) {
        return true;
    }
}