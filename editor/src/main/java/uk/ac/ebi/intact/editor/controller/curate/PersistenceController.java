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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import psidev.psi.mi.jami.model.CvTerm;
import uk.ac.ebi.intact.core.persister.CoreDeleter;
import uk.ac.ebi.intact.core.persister.IntactObjectDeleteException;
import uk.ac.ebi.intact.core.util.DebugUtil;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.editor.controller.curate.interaction.ImportCandidate;
import uk.ac.ebi.intact.editor.controller.curate.interaction.ImportJamiCandidate;
import uk.ac.ebi.intact.editor.controller.curate.interaction.ModelledParticipantImportController;
import uk.ac.ebi.intact.editor.controller.curate.interaction.ParticipantImportController;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.model.IntactPrimaryObject;
import uk.ac.ebi.intact.jami.model.audit.Auditable;
import uk.ac.ebi.intact.jami.model.extension.IntactInteractor;
import uk.ac.ebi.intact.jami.synchronizer.IntactDbSynchronizer;
import uk.ac.ebi.intact.model.*;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

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

    @Transactional(value = "jamiTransactionManager", propagation = Propagation.REQUIRED)
    public boolean doSave( IntactPrimaryObject object, IntactDbSynchronizer dbSynchronizer, AnnotatedObjectController controller,
                           IntactDao dao) {
        if ( object == null ) {
            addErrorMessage( "No annotated object to save", "How did I get here?" );
            return false;
        }

        try {
            getIntactTransactionSynchronization().registerDaoForSynchronization(dao);

            controller.setJamiObject((IntactPrimaryObject)dbSynchronizer.synchronize(object, true));

            return true;

        } catch ( Throwable e ) {
            // clear cache after exception
            dao.getSynchronizerContext().clearCache();

            addErrorMessage("Problem persisting object: "+e.getMessage(), object.toString());
            handleException(e);

            return false;
        }
    }

    @Transactional(value = "jamiTransactionManager", propagation = Propagation.REQUIRED)
    public uk.ac.ebi.intact.jami.model.audit.Auditable doSynchronize( Object object, IntactDbSynchronizer dbSynchronizer) {
        if ( object == null ) {
            addErrorMessage( "No annotated object to save", "How did I get here?" );
            return null;
        }

        try {
            getIntactTransactionSynchronization().registerDaoForSynchronization(getIntactDao());

            return dbSynchronizer.synchronize(object, true);

        } catch ( Throwable e ) {
            // clear cache after exception
            getIntactDao().getSynchronizerContext().clearCache();
            addErrorMessage("Problem persisting object: "+e.getMessage(), object.toString());
            handleException(e);

            return null;
        }
    }

    @Transactional(value = "transactionManager", propagation = Propagation.SUPPORTS)
    public IntactObject doRevert(IntactObject intactObject) {
        if (intactObject.getAc() != null) {
            if (log.isDebugEnabled()) log.debug("Reverting: " + DebugUtil.intactObjectToString(intactObject, false));

            if (getDaoFactory().getEntityManager().contains(intactObject)) {
                getDaoFactory().getEntityManager().detach(intactObject);
            }

            intactObject = getDaoFactory().getEntityManager().find(intactObject.getClass(), intactObject.getAc());
        }

        return intactObject;
    }

    @Transactional(value = "jamiTransactionManager", propagation = Propagation.REQUIRED)
    public void doRevert(IntactPrimaryObject intactObject, AnnotatedObjectController controller) {
        if (intactObject.getAc() != null) {
            if (log.isDebugEnabled()) log.debug("Reverting: " + intactObject.getAc());

            if (getJamiEntityManager().contains(intactObject)) {
                getJamiEntityManager().detach(intactObject);
            }

            intactObject = getJamiEntityManager().find(intactObject.getClass(), intactObject.getAc());
        }

        controller.setJamiObject(intactObject);
    }

    @Transactional(value = "transactionManager", propagation = Propagation.SUPPORTS)
    public boolean doDelete(IntactObject intactObject) {
        if (intactObject.getAc() != null) {
            if (log.isDebugEnabled()) log.debug("Deleting: " + DebugUtil.intactObjectToString(intactObject, false));

//            final TransactionStatus transactionStatus = IntactContext.getCurrentInstance().getDataContext().beginTransaction();

            try{
                coreDeleter.delete(intactObject);

                changesController.removeFromDeleted(intactObject, null);

                addInfoMessage("Deleted object", DebugUtil.intactObjectToString(intactObject, false));

                return true;
            }
            catch (IntactObjectDeleteException e){
                addErrorMessage("Deletion not allowed", e.getMessage());
                FacesContext.getCurrentInstance().renderResponse();
            }
//            IntactContext.getCurrentInstance().getDataContext().commitTransaction(transactionStatus);
        }

        addErrorMessage("Object not deleted", "Object is not saved so it cannot be deleted");
        return false;
    }

    @Transactional(value = "jamiTransactionManager", propagation = Propagation.REQUIRED)
    public boolean doDelete(IntactPrimaryObject intactObject, IntactDbSynchronizer dbSynchronizer,
                            IntactDao dao) {
        if (intactObject.getAc() != null) {
            if (log.isDebugEnabled()) log.debug("Deleting: " + intactObject.getAc());

            try{
                getIntactTransactionSynchronization().registerDaoForSynchronization(dao);

                dbSynchronizer.delete(intactObject);

                changesController.removeFromDeleted(intactObject, null, dbSynchronizer, dao);

                addInfoMessage("Deleted object", intactObject.getAc());

                return true;
            }
            catch (IntactObjectDeleteException e){
                // clear cache
                dao.getSynchronizerContext().clearCache();
                addErrorMessage("Deletion not allowed: "+e.getMessage(), e.getMessage());
                FacesContext.getCurrentInstance().renderResponse();
            }
        }

        addErrorMessage("Object not deleted", "Object is not saved so it cannot be deleted");
        return false;
    }

    public void saveAll(ActionEvent actionEvent) {
        CurateController curateController = (CurateController) getSpringContext().getBean("curateController");

        Collection<UnsavedChange> changes = new ArrayList(changesController.getUnsavedChangesForCurrentUser());

        for (UnsavedChange unsaved : changes){
            IntactObject object = unsaved.getUnsavedObject();

            // checks that the current unsaved change is not obsolete because of a previous change (when saving/deleting, some unsaved change became obsolete and have been removed from the unsaved changes)
            if (changesController.getUnsavedChangesForCurrentUser().contains(unsaved)){
                curateController.save(object, false);

            }
        }

        Collection<UnsavedJamiChange> jamiChanges = new ArrayList(changesController.getUnsavedJamiChangesForCurrentUser());

        for (UnsavedJamiChange unsaved : jamiChanges){
            IntactPrimaryObject object = unsaved.getUnsavedObject();

            // checks that the current unsaved change is not obsolete because of a previous change (when saving/deleting, some unsaved change became obsolete and have been removed from the unsaved changes)
            if (changesController.getUnsavedJamiChangesForCurrentUser().contains(unsaved)){
                curateController.saveJami(object, false);
            }
        }

        // refresh current view now
        final AnnotatedObjectController currentAoController = curateController.getCurrentAnnotatedObjectController();
        currentAoController.forceRefreshCurrentViewObject();
    }

    /**
     * Save a master protein and update the cross reference of a protein transcript which will be created later
     * @param intactObject
     */
    public void doSaveMasterProteins(IntactObject intactObject) {
        if (intactObject instanceof Protein){
            Protein proteinTranscript = (Protein) intactObject;

            for (Xref xref : proteinTranscript.getXrefs()) {
                CvXrefQualifier qualifier = xref.getCvXrefQualifier();

                if (qualifier != null){
                    if (qualifier.getIdentifier().equals(CvXrefQualifier.CHAIN_PARENT_MI_REF) ||
                            qualifier.getIdentifier().equals(CvXrefQualifier.ISOFORM_PARENT_MI_REF)) {
                        if (xref.getPrimaryId().startsWith("?")) {
                            String primaryId = xref.getPrimaryId().replaceAll("\\?", "");

                            ParticipantImportController participantImportController = (ParticipantImportController) getSpringContext().getBean("participantImportController");
                            Set<ImportCandidate> importCandidates = participantImportController.importParticipant(primaryId);

                            if (!importCandidates.isEmpty()) {
                                ImportCandidate candidate = importCandidates.iterator().next();
                                Interactor interactor = candidate.getInteractor();

                                if (interactor.getAc() == null){
                                    doSave(interactor);
                                }

                                xref.setPrimaryId(interactor.getAc());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Save a master protein and update the cross reference of a protein transcript which will be created later
     * @param intactObject
     */
    public void doSaveJamiMasterProteins(IntactPrimaryObject intactObject) {
        if (intactObject instanceof psidev.psi.mi.jami.model.Protein){
            psidev.psi.mi.jami.model.Protein proteinTranscript = (psidev.psi.mi.jami.model.Protein) intactObject;

            Collection<psidev.psi.mi.jami.model.Xref> xrefsToDelete = new ArrayList<psidev.psi.mi.jami.model.Xref>(proteinTranscript.getXrefs().size());
            Collection<psidev.psi.mi.jami.model.Xref> xrefsToAdd = new ArrayList<psidev.psi.mi.jami.model.Xref>(proteinTranscript.getXrefs().size());

            for (psidev.psi.mi.jami.model.Xref xref : proteinTranscript.getXrefs()) {
                CvTerm qualifier = xref.getQualifier();

                if (qualifier != null){
                    if (qualifier.getMIIdentifier().equals(CvXrefQualifier.CHAIN_PARENT_MI_REF) ||
                            qualifier.getMIIdentifier().equals(CvXrefQualifier.ISOFORM_PARENT_MI_REF)) {
                        String primaryId = xref.getId();

                        if (!(intactObject instanceof IntactInteractor)){
                            ModelledParticipantImportController participantImportController = (ModelledParticipantImportController) getSpringContext().getBean("modelledParticipantImportController");
                            Set<ImportJamiCandidate> importCandidates = participantImportController.importParticipant(primaryId);

                            if (!importCandidates.isEmpty()) {
                                ImportJamiCandidate candidate = importCandidates.iterator().next();
                                psidev.psi.mi.jami.model.Interactor interactor = candidate.getInteractor();

                                if (!(interactor instanceof IntactInteractor)) {
                                    IntactDao intactDao = getIntactDao();
                                    Auditable persistedInteractor = doSynchronize(interactor, intactDao.getSynchronizerContext().getInteractorSynchronizer());

                                    if (persistedInteractor != null){
                                        xrefsToDelete.add(xref);
                                        xrefsToAdd.add(new uk.ac.ebi.intact.jami.model.extension.InteractorXref(xref.getDatabase(),
                                                ((IntactPrimaryObject)persistedInteractor).getAc(),
                                                xref.getQualifier()));
                                    }
                                }
                            }
                        }
                    }
                }
            }

            proteinTranscript.getXrefs().removeAll(xrefsToDelete);
            proteinTranscript.getXrefs().addAll(xrefsToAdd);
        }
    }

    public void revertAll(ActionEvent actionEvent) {

        CurateController curateController = (CurateController) getSpringContext().getBean("curateController");
        Collection<UnsavedChange> changes = new ArrayList(changesController.getUnsavedChangesForCurrentUser());

        revertIntactCoreChanges(curateController, changes);

        Collection<UnsavedJamiChange> jamiChanges = new ArrayList(changesController.getUnsavedJamiChangesForCurrentUser());

        revertJamiChanges(curateController, jamiChanges);
    }

    @Transactional(value = "jamiTransactionManager", propagation = Propagation.REQUIRED)
    public void revertJamiChanges(CurateController curateController, Collection<UnsavedJamiChange> jamiChanges) {
        for (UnsavedJamiChange unsaved : jamiChanges){
            IntactPrimaryObject object = unsaved.getUnsavedObject();

            // checks that the current unsaved change is not obsolete because of a previous change (when saving/deleting, some unsaved change became obsolete and have been removed from the unsaved changes)
            if (changesController.getUnsavedJamiChangesForCurrentUser().contains(unsaved)){
                curateController.discardJami(object);
            }
        }
    }

    @Transactional(value = "transactionManager", propagation = Propagation.NEVER)
    public void revertIntactCoreChanges(CurateController curateController, Collection<UnsavedChange> changes) {
        for (UnsavedChange unsaved : changes){
            IntactObject object = unsaved.getUnsavedObject();

            // checks that the current unsaved change is not obsolete because of a previous change (when saving/deleting, some unsaved change became obsolete and have been removed from the unsaved changes)
            if (changesController.getUnsavedChangesForCurrentUser().contains(unsaved)){
                curateController.discard(object);
            }
        }
    }
}
