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
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.*;
import org.springframework.security.providers.AuthenticationProvider;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.editor.controller.UserListener;
import uk.ac.ebi.intact.editor.controller.admin.UserManagerController;
import uk.ac.ebi.intact.model.user.Role;
import uk.ac.ebi.intact.model.user.User;

import java.util.Collection;
import java.util.Map;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class EditorAuthenticationProvider implements AuthenticationProvider {

    private static final Log log = LogFactory.getLog( EditorAuthenticationProvider.class );

    @Autowired
    private DaoFactory daoFactory;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private UserManagerController userManagerController;

    @Transactional(value = "transactionManager", readOnly = true )
    public Authentication authenticate( Authentication authentication ) throws AuthenticationException {

        log.debug( "======================= AUTHENTICATE ======================" );

        if ( log.isDebugEnabled() ) {
            log.debug( "Currently, there are " + userManagerController.getLoggedInUsers().size() + " users connected." );
            log.debug( "Authenticating user: " + authentication.getPrincipal() );
        }

        final User user = daoFactory.getUserDao().getByLogin( authentication.getPrincipal().toString() );

        // initialize the user collections because we will access it often
        if (user != null) {
            Hibernate.initialize(user.getPreferences());
            Hibernate.initialize(user.getRoles());
        }

        if ( user == null || !user.getPassword().equals( authentication.getCredentials() ) ) {
            if ( log.isDebugEnabled() ) log.debug( "Bad credentials for user: " + authentication.getPrincipal() );
            throw new BadCredentialsException( "Unknown user or incorrect password." );
        }

        if ( user.isDisabled() ) {
            throw new DisabledException( "User " + user.getLogin() + " has been disabled, please contact the IntAct team." );
        }



        if ( log.isInfoEnabled() ) log.info( "Authentication successful for user: " + authentication.getPrincipal() );

        // get all the "user listener" beans and notify the login
        final Map<String,UserListener> userListeners = applicationContext.getBeansOfType(UserListener.class);

        for (UserListener userListener : userListeners.values()) {
            userListener.userLoggedIn(user);
        }

        Collection<GrantedAuthority> authorities = Lists.newArrayList();
        log.info( user.getLogin() + " roles: " + user.getRoles() );
        for ( Role role : user.getRoles() ) {
            final String authorityName = "ROLE_" + role.getName();
            log.info( "Adding GrantedAuthority: '" + authorityName + "'" );
            authorities.add( new GrantedAuthorityImpl( authorityName ) );
        }

        return new UsernamePasswordAuthenticationToken( authentication.getPrincipal(),
                                                        authentication.getCredentials(),
                                                        authorities.toArray( new GrantedAuthority[authorities.size()] ) );
    }

    public boolean supports( Class authentication ) {
        return true;
    }
}