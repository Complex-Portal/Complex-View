package uk.ac.ebi.intact.editor.controller.curate.publication;

import uk.ac.ebi.intact.dataexchange.imex.idassigner.events.ImexErrorEvent;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.events.IntactUpdateEvent;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.events.NewAssignedImexEvent;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.listener.ImexUpdateListener;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.listener.ProcessorException;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

/**
 * Editor Listener of the IMEx assigner/updater
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>16/04/12</pre>
 */

public class EditorImexCentralListener implements ImexUpdateListener {
    @Override
    public void onImexError(ImexErrorEvent evt) throws ProcessorException {
        
        addErrorMessage(evt.getErrorMessage(),evt.getErrorType().toString());
    }

    @Override
    public void onIntactUpdate(IntactUpdateEvent evt) throws ProcessorException {
        String publication = evt.getPublicationId() != null ? evt.getPublicationId() : "-";
        int numberExperimentUpdated = evt.getUpdatedExp() != null ? evt.getUpdatedExp().size() : 0;
        int numberInteractionUpdated = evt.getUpdatedInteraction() != null ? evt.getUpdatedInteraction().size() : 0;
        
        addInfoMessage("Updated publication " + publication + ", updated " + numberExperimentUpdated + " experiment(s)" + ", updated " + numberInteractionUpdated + " interaction(s)","");
    }

    @Override
    public void onNewImexAssigned(NewAssignedImexEvent evt) throws ProcessorException {
        if (evt.getPublicationId() != null){
            addInfoMessage("assigned publication " + evt.getPublicationId() + ": " + evt.getImexId(),"");
        }
    }

    public void addInfoMessage( String message, String detail ) {
        FacesContext context = FacesContext.getCurrentInstance();
        FacesMessage facesMessage = new FacesMessage( FacesMessage.SEVERITY_INFO, message, detail );
        context.addMessage( null, facesMessage );
    }

    public void addWarningMessage( String message, String detail ) {
        FacesContext context = FacesContext.getCurrentInstance();
        FacesMessage facesMessage = new FacesMessage( FacesMessage.SEVERITY_WARN, message, detail );
        context.addMessage( null, facesMessage );
    }

    public void addErrorMessage( String message, String detail ) {
        FacesContext context = FacesContext.getCurrentInstance();
        FacesMessage facesMessage = new FacesMessage( FacesMessage.SEVERITY_ERROR, message, detail );
        context.addMessage( null, facesMessage );
    }
}
