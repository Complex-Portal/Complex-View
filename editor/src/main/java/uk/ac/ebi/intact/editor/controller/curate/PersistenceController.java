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
package uk.ac.ebi.intact.editor.controller.curate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.hibernate.ejb.HibernateEntityManager;
import org.hibernate.engine.PersistenceContext;
import org.hibernate.impl.SessionImpl;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.util.DebugUtil;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.IntactObject;

import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import java.util.Collection;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */

@Controller
@Scope( "conversation.access" )
@ConversationName( "general" )
public class PersistenceController extends JpaAwareController {

    private static final Log log = LogFactory.getLog( PersistenceController.class );

    @Transactional("core")
    public boolean doSave( AnnotatedObject<?,?> annotatedObject ) {
        if ( annotatedObject == null ) {
            addErrorMessage( "No annotated object to save", "How did I get here?" );
            return false;
        }

        final String simpleName = annotatedObject.getClass().getSimpleName();

        if ( log.isDebugEnabled() ) log.debug( "Saving annotated object: " + DebugUtil.annotatedObjectToString(annotatedObject, false));

        try {
            getIntactContext().getCorePersister().saveOrUpdate( annotatedObject );

            addInfoMessage( simpleName +" saved", "AC: " + annotatedObject.getAc() );

            // clear the unsaved changes using the entity manager
            final HibernateEntityManager em = (HibernateEntityManager) getIntactContext().getDaoFactory().getEntityManager();
            final PersistenceContext persistenceContext = ((SessionImpl) em.getSession()).getPersistenceContext();

            UnsavedChangeManagerController unsavedChangeManagerController = (UnsavedChangeManagerController) getSpringContext().getBean("unsavedChangeManagerController");

            Collection<Object> entities = persistenceContext.getEntitiesByKey().values();
            for (Object entity : entities) {
                if (entity instanceof IntactObject) {
                    unsavedChangeManagerController.removeFromUnsaved((IntactObject)entity);
                }
            }

            return true;

        } catch (IllegalTransactionStateException itse) {
            if (log.isWarnEnabled()) log.warn("IllegalTransactionStateException happened when saving. It seems to be harmless " +
                    "but we should keep an eye on this: "+ itse.getMessage());
            return true;
        } catch ( Exception e ) {
            addErrorMessage( "Problem persisting object", "AC: " + annotatedObject.getAc() );
            FacesContext ctx = FacesContext.getCurrentInstance();
            ExceptionQueuedEventContext eventContext = new ExceptionQueuedEventContext( ctx, e );
            ctx.getApplication().publishEvent( ctx, ExceptionQueuedEvent.class, eventContext );

            return false;
        }
    }


}
