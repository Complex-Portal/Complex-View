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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persister.CoreDeleter;
import uk.ac.ebi.intact.core.util.DebugUtil;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.IntactObject;

import javax.faces.event.ActionEvent;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */

@Controller
@Scope( "conversation.access" )
@ConversationName( "general" )
public class PersistenceController extends JpaAwareController {

    private static final Log log = LogFactory.getLog( PersistenceController.class );

    @Autowired
    private ChangesController changesController;

    @Autowired
    private CoreDeleter coreDeleter;

    public boolean doSave( AnnotatedObject<?,?> annotatedObject ) {
        if ( annotatedObject == null ) {
            addErrorMessage( "No annotated object to save", "How did I get here?" );
            return false;
        }

        try {
            getIntactContext().getCorePersister().saveOrUpdate( annotatedObject );
            return true;

        } catch (IllegalTransactionStateException itse) {
            if (log.isWarnEnabled()) log.warn("IllegalTransactionStateException happened when saving. It seems to be harmless " +
                    "but we should keep an eye on this: "+ itse.getMessage());
            itse.printStackTrace();
            return true;
        } catch ( Throwable e ) {
            addErrorMessage( "Problem persisting object", "AC: " + annotatedObject.getAc() );
            handleException(e);

            return false;
        } 
    }

    @Transactional(propagation = Propagation.NEVER)
    public IntactObject doRevert(IntactObject intactObject) {
        if (intactObject.getAc() != null) {
            if (log.isDebugEnabled()) log.debug("Reverting: " + DebugUtil.intactObjectToString(intactObject, false));

            final TransactionStatus transactionStatus = IntactContext.getCurrentInstance().getDataContext().beginTransaction(getClass().getSimpleName());

            if (getDaoFactory().getEntityManager().contains(intactObject)) {
                getDaoFactory().getEntityManager().detach(intactObject);
            }

            intactObject = getDaoFactory().getEntityManager().find(intactObject.getClass(), intactObject.getAc());

            IntactContext.getCurrentInstance().getDataContext().commitTransaction(transactionStatus);
        }

        return intactObject;
    }

    @Transactional(propagation = Propagation.NEVER)
    public void doDelete(IntactObject intactObject) {
          if (intactObject.getAc() != null) {
            if (log.isDebugEnabled()) log.debug("Deleting: " + DebugUtil.intactObjectToString(intactObject, false));

//            final TransactionStatus transactionStatus = IntactContext.getCurrentInstance().getDataContext().beginTransaction();

            coreDeleter.delete(intactObject);

            addInfoMessage("Deleted object", DebugUtil.intactObjectToString(intactObject, false));

//            IntactContext.getCurrentInstance().getDataContext().commitTransaction(transactionStatus);
        }
    }

    @Transactional(value = "core", propagation = Propagation.NEVER)
    public void saveAll(ActionEvent actionEvent) {

        for (IntactObject intactObject : changesController.getAllUnsaved()) {

            if (intactObject instanceof AnnotatedObject) {
                doSave((AnnotatedObject) intactObject);
            }
        }

        for (IntactObject intactObject : changesController.getAllDeleted()) {
            doDelete(intactObject);
        }

        changesController.clearCurrentUserChanges();

    }

    @Transactional(value = "core", propagation = Propagation.NEVER)
    public void revertAll(ActionEvent actionEvent) {
        for (IntactObject intactObject : changesController.getAllUnsaved()) {
            doRevert(intactObject);
        }

         changesController.clearCurrentUserChanges();
    }
}
