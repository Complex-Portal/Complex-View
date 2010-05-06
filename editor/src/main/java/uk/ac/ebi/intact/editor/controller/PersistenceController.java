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
package uk.ac.ebi.intact.editor.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.model.AnnotatedObject;

import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */

@Controller
@Scope( "conversation.access" )
@ConversationName( "general" )
public class PersistenceController extends JpaAwareController{

    private static final Log log = LogFactory.getLog( PersistenceController.class );
    
    @Transactional
    public boolean doSave( AnnotatedObject<?,?> annotatedObject ) {
        if ( annotatedObject == null ) {
            addErrorMessage( "No annotated object to save", "How did I get here?" );
            return false;
        }

        final String simpleName = annotatedObject.getClass().getSimpleName();

        if ( log.isDebugEnabled() ) log.debug( "Saving annotated object: " + annotatedObject );

        try {
            getIntactContext().getCorePersister().saveOrUpdate( annotatedObject );

            addInfoMessage( simpleName +" saved", "AC: " + annotatedObject.getAc() );

            return true;

        } catch ( Exception e ) {
            addErrorMessage( "Problem persisting "+annotatedObject, "AC: " + annotatedObject.getAc() );
            FacesContext ctx = FacesContext.getCurrentInstance();
            ExceptionQueuedEventContext eventContext = new ExceptionQueuedEventContext( ctx, e );
            ctx.getApplication().publishEvent( ctx, ExceptionQueuedEvent.class, eventContext );

            return false;
        }
    }


}
