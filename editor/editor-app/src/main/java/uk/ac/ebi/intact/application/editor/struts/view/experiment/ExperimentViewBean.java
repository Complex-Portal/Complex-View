/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.view.experiment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.tiles.ComponentContext;
import uk.ac.ebi.intact.application.editor.business.EditUserI;
import uk.ac.ebi.intact.application.editor.exception.validation.ValidationException;
import uk.ac.ebi.intact.application.editor.struts.framework.EditorFormI;
import uk.ac.ebi.intact.application.editor.struts.framework.util.AbstractEditViewBean;
import uk.ac.ebi.intact.application.editor.struts.framework.util.EditorMenuFactory;
import uk.ac.ebi.intact.application.editor.struts.view.interaction.ExperimentRowData;
import uk.ac.ebi.intact.application.editor.struts.view.wrappers.ResultRowData;
import uk.ac.ebi.intact.application.editor.util.DaoProvider;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.persistence.dao.BioSourceDao;
import uk.ac.ebi.intact.persistence.dao.CvObjectDao;
import uk.ac.ebi.intact.persistence.dao.InteractionDao;

import java.util.*;

/**
 * Experiment edit view bean.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class ExperimentViewBean extends AbstractEditViewBean<Experiment> {

    private static final Log log = LogFactory.getLog(ExperimentViewBean.class);


    private String myPubmedId;

    /**
     * The host organism.
     */
    private String myOrganism;

    /**
     * The interaction with the experiment.
     */
    private String myInter;

    /**
     * The experiment identification.
     */
    private String myIdent;

    /**
     * True if the maximum interactions allowed for the experiment has exceeded.
     */
    private boolean myHasLargeInts;

    /**
     * Holds the number of interactions. This value is only set if
     * {@link #myHasLargeInts} is true.
     */
    private int myLargeInts;

    /**
     * The collection of Interactions. Transient as it is only valid for the
     * current display.
     */
    private transient List<InteractionRowData> myInteractions = new ArrayList<InteractionRowData>();

    /**
     * Holds ACs of Interactions to delete. This collection is cleared once the user
     * commits the transaction.
     */
    private transient List<String> myInteractionsToDel = new ArrayList<String>();

    /**
     * Holds Interaction to not yet added. Only valid for the current session.
     */
    private transient List<InteractionRowData> myInteractionsToHold = new ArrayList<InteractionRowData>();

    /**
     * The map of menus for this view.
     */
    private transient Map<String, List<String>> myMenus = new HashMap<String, List<String>>();

    // Override the super method to clear this object.
    @Override
    public void reset() {
        super.reset();
        // Set fields to null.
        setOrganism(null);
        setInter(null);
        setIdent(null);
        setPubmedId(null);

        myHasLargeInts = false;
        myLargeInts = 0;

        // Clear previous interactions.
        myInteractions.clear();
    }

    // Reset the fields to null if we don't have values to set. Failure
    // to do so will display the previous edit object's values as current.
    @Override
    public void reset(Experiment exp) {
        super.reset(exp);

        // Reset the experiment view.
        resetExperiment(exp);

        // Check the limit for interactions.
        int maxLimit = getService().getInteractionLimit();

        // The number of interactions for the current experiment.
        int intsSize = exp.getInteractions().size();

        if (intsSize > maxLimit) {
            // Reached the maximum limit.
            myHasLargeInts = true;
            // Save it as we need to returns this value for
            // getNumberOfInteractions method.
            myLargeInts = intsSize;
        }
        else {
            // Prepare for Interactions for display.
            makeInteractionRows(exp.getInteractions());
        }
    }

    // Reset the fields to null if we don't have values to set. Failure
    // to do so will display the previous edit object's values as current.
    @Override
    public void resetClonedObject(Experiment copy, EditUserI user) {
        super.resetClonedObject(copy, user);
        // Reset the experiment view with the copy.
        resetExperiment(copy);
    }

    // Override the super method as the current experiment is added to the
    // recent experiment list.
    @Override
    public void addToRecentList(EditUserI user) {
        ExperimentRowData row = new ExperimentRowData(getAnnotatedObject());
        user.addToCurrentExperiment(row);
    }

    // Override to remove the current experiment from the recent list.
    @Override
    public void removeFromRecentList(EditUserI user) {
        ExperimentRowData row = new ExperimentRowData(getAc());
        user.removeFromCurrentExperiment(row);
    }

    // Override to provide Experiment layout.
    @Override
    public void setLayout(ComponentContext context) {
        context.putAttribute("content", "edit.exp.layout");
    }

    // Override to provide Experiment help tag.
    @Override
    public String getHelpTag() {
        return "editor.experiment";
    }

    // Override to provide set experiment from the form.
    @Override
    public void copyPropertiesFrom(EditorFormI editorForm) {
        // Set the common values by calling super first.
        super.copyPropertiesFrom(editorForm);

        // Cast to the experiment form to get experiment data.
        ExperimentActionForm expform = (ExperimentActionForm) editorForm;

        setOrganism(expform.getOrganism());
        setInter(expform.getInter());
        setIdent(expform.getIdent());
        setPubmedId(expform.getPubmedId());
    }

    // Override to copy Experiment data.
    @Override
    public void copyPropertiesTo(EditorFormI form) {
        super.copyPropertiesTo(form);

        // Cast to the experiment form to copy experiment data.
        ExperimentActionForm expform = (ExperimentActionForm) form;

        expform.setOrganism(getOrganism());
        expform.setInter(getInter());
        expform.setIdent(getIdent());
        expform.setPubmedId(getPubmedId());
    }

    // Override to check for a large experiment.
    @Override
    public Boolean getReadOnly() {
        return myHasLargeInts ? Boolean.TRUE : Boolean.FALSE;
    }

    @Override
    public void sanityCheck() throws ValidationException, IntactException {
        // COMMENTED OUT these checks as they need to be warning!

        /// keeps track of pubmed xrefs.
//        int pmCount = 0;
//
//        // Keeps track of primary pubmed counts.
//        int pmPrimaryCount = 0;
//
//        for ( Iterator iterator = getXrefs().iterator(); iterator.hasNext(); ) {
//            Xref xref = ((XreferenceBean) iterator.next()).getXref(user);
//            if (xref.getCvDatabase().getShortLabel().equals("pubmed")) {
//                pmCount++;
//                if (xref.getCvXrefQualifier().getShortLabel().equals(
//                        "primary-reference")) {
//                    pmPrimaryCount++;
//                }
//            }
//        }
//        if (pmCount == 0) {
//            throw new ExperimentException("error.exp.sanity.pubmed");
//        }
//        if (pmPrimaryCount != 1) {
//            throw new ExperimentException("error.exp.sanity.primary.pubmed");
//        }
    }

    /**
     * Override to provide the menus for this view.
     * @return a map of menus for this view. It consists of common menus for
     * annotation/xref, organism (add or edit), CV interaction (add or edit) and
     * CV identification (add or edit).
     */
    @Override
    public Map<String,List<String>> getMenus() throws IntactException {
        return myMenus;
    }

    //Getter/Setter methods for pubmedId

    public String getPubmedId(){
        return myPubmedId;
    }

    public void setPubmedId(String pubmedId){
        myPubmedId=pubmedId;
    }

    // Getter/Setter methods for Organism.
    public String getOrganism() {
        return myOrganism;
    }

    public void setOrganism(String organism) {
        myOrganism = EditorMenuFactory.normalizeMenuItem(organism);
    }

    // Getter/Setter methods for Interaction.
    public String getInter() {
        return myInter;
    }

    public void setInter(String interaction) {
        myInter = EditorMenuFactory.normalizeMenuItem(interaction);
    }

    // Getter/Setter methods for Identification.
    public String getIdent() {
        return myIdent;
    }

    public void setIdent(String identification) {
        myIdent = EditorMenuFactory.normalizeMenuItem(identification);
    }


    /**
     * Adds an Interaction.
     * @param row an Interaction row to add.
     *
     * <pre>
     * post: myInteractionsToAdd = myInteractionsToAdd@pre + 1
     * post: myInteractions = myInteractions@pre + 1
     * </pre>
     */
    public void addInteraction(InteractionRowData row) {
        // Add to the view.
        myInteractions.add(row);
    }

    /**
     * Removes an Interaction
     * @param ac the AC of the Interaction to remove.
     *
     * <pre>
     * post: myInteractionsToDel = myInteractionsToDel@pre - 1
     * post: myInteractions = myInteractions@pre - 1
     * </pre>
     */
    public void delInteraction(String ac) {
        // The interaction to delete
        myInteractionsToDel.add(ac);
        // Remove from the view as well.
        myInteractions.remove(new InteractionRowData(ac));
    }

    /**
     * Returns a collection of <code>Interaction</code>s for the current
     * experiment.
     *
     * <pre>
     * post: return != null
     * post: return->forall(obj : Object | obj.oclIsTypeOf(InteractionRowData))
     * </pre>
     */
    public List getInteractions() {
        return myInteractions;
    }

    /**
     * Adds an Interaction to hold if the new interaction doesn't
     * already exists in the interaction hold collection and in the
     * current interaction collection for this experiment.
     * @param ints a collection of <code>Interaction</code> to add.
     *
     * <pre>
     * pre:  forall(obj : Object | obj.oclIsTypeOf(ResultRowData))
     * </pre>
     */
    public void addInteractionToHold(Collection<ResultRowData> ints) {
        for (ResultRowData rowData : ints)
        {
            InteractionRowData row = InteractionRowData.makeSearchRow(rowData);
            // Avoid duplicates.
            if (!myInteractionsToHold.contains(row)
                    && !interactionExists(row.getAc()))
            {
                myInteractionsToHold.add(row);
            }
        }
    }

    /**
     * Returns a collection of <code>Interaction</code> objects on hold.
     *
     * <pre>
     * post: return != null
     * post: return->forall(obj : Object | obj.oclIsTypeOf(InteractionSearchRowData))
     * </pre>
     */
    public List<InteractionRowData> getHoldInteractions() {
        return myInteractionsToHold;
    }

    /**
     * Returns the number of interactions in the hold interaction collection.
     * @return the number of interactions in the hold interaction collection.
     */
    public int getHoldInteractionCount() {
        return myInteractionsToHold.size();
    }

    /**
     * Returns the row for given AC
     * @param ac the AC to search for a row.
     * @return the row corresponding to given AC or null if none found.
     */
    public InteractionRowData getHoldInteraction(String ac) {
        // Dummy record to search.
        InteractionRowData dummy = new InteractionRowData(ac);
        int pos = myInteractionsToHold.indexOf(dummy);
        if (pos != -1) {
            return myInteractionsToHold.get(pos);
        }
        return null;
    }

    /**
     * Hides an Interaction bean from hold.
     * @param ac the accession number of the <code>Interaction</code> to hide.
     * <pre>
     * pre: myInteractionsToHold->includes(intbean)
     * post: myInteractionsToHold = myInteractionsToHold@pre - 1
     * </pre>
     */
    public void hideInteractionToHold(String ac) {
        myInteractionsToHold.remove(new InteractionRowData(ac));
    }

    /**
     * Clears all the interactions on hold.
     */
    public void clearInteractionToHold() {
        myInteractionsToHold.clear();
    }

    // Override super to add extra.
    @Override
    public void clearTransactions() {
        super.clearTransactions();

        // Clear interactions involved in transactions.
        myInteractionsToDel.clear();
        myInteractionsToHold.clear();
        clearInteractionToHold();
    }

    /**
     * Returns the number of interactions for this experiment.
     * @return the number of interactions for this experiment.
     */
    public int getNumberOfInteractions() {
        return myHasLargeInts ? myLargeInts : myInteractions.size();
    }

    /**
     * Returns the state for this editor to clone.
     * @return true for all the persistent experiments  (i.e., false for a
     * new experiment not yet persisted).
     */
    @Override
    public boolean getCloneState() {
        return getAc() != null;
    }

    /**
     * Updates the a row for given interaction. A new row is created if this
     * doesn't exist.
     * @param interaction the interaction to update or add.
     */
    public void updateInteractionRow(Interaction interaction) {
        // Create a dummy row data for comparision.
        InteractionRowData dummy = new InteractionRowData(interaction);
        
        // Compare with the existing rows.
        if (myInteractions.contains(dummy)) {
            int pos = myInteractions.indexOf(dummy);
            myInteractions.remove(pos);
        }
        // Extract the experiment ACs for gievn interaction
        List exps = extractExperimentACs(interaction.getExperiments());

        // Add the updated row if the current experiment is part of the list.
        if (exps.contains(getAc())) {
            myInteractions.add(dummy);
        }
    }
    
    /**
     * Deletes the row matching the given ac.
     * @param ac the AC to delete the interaction row.
     * @return true if the interaction was removed successfully (i.e, the
     * interaction for given ac belongs to this experiment).
     */
    public boolean deleteInteractionRow(String ac) {
        // Create a dummy row data for comparision.
        InteractionRowData dummy = new InteractionRowData(ac);
        
        // Compare with the existing rows.
        if (myInteractions.contains(dummy)) {
            int pos = myInteractions.indexOf(dummy);
            myInteractions.remove(pos);
            return true;
        }
        return false;
    }

    // Implements abstract methods
    @Override
    protected void updateAnnotatedObject() throws IntactException {
        // Get the objects using their short label.
        BioSourceDao bsDao = DaoProvider.getDaoFactory().getBioSourceDao();
        BioSource biosource = bsDao.getByShortLabel(myOrganism);

        CvObjectDao<CvInteraction> cvInteractionDao = DaoProvider.getDaoFactory().getCvObjectDao(CvInteraction.class);
        CvInteraction interaction = cvInteractionDao.getByShortLabel(myInter);

        CvObjectDao<CvIdentification> cvIdentificationDao = DaoProvider.getDaoFactory().getCvObjectDao(CvIdentification.class);
        CvIdentification ident = cvIdentificationDao.getByShortLabel(myIdent);
        if(ident != null){
            log.debug("CvInteraction type is : " + ident.getShortLabel());
        }else{
            log.debug("No CvInteraction found with shorlabel : " + myIdent);
        }
        // The current experiment.
        Experiment exp = getAnnotatedObject();

        // Have we set the annotated object for the view?
        if (exp == null) {
            // Can't read from the persistent system. Create a new Experiment.
            exp = new Experiment(IntactContext.getCurrentInstance().getConfig().getInstitution(), getShortLabel(), biosource);
            setAnnotatedObject(exp);
        }
        else {
            // No need to set the biosource for a new experiment as it is done
            // in the constructor.
            exp.setBioSource(biosource);
        }
        exp.setCvInteraction(interaction);
        exp.setCvIdentification(ident);

        // There is no need to touch interactions for a large interaction.
        if (!myHasLargeInts) {
            // Delete interactions from the experiment. Do this block of code before
            // clearing interactions or else 'this' experiment wouldn't be removed
            // from interactions.
            InteractionDao interactionDao = DaoProvider.getDaoFactory().getInteractionDao();
            for (Iterator iter = myInteractionsToDel.iterator(); iter.hasNext();) {
                String ac = (String) iter.next();
                Interaction intact = interactionDao.getByAc(ac);
                exp.removeInteraction(intact);
            }

            // --------------------------------------------------------------------
            // Need this fix to get around the proxies.
            // 1. Clear all the interaction proxies first.
            exp.getInteractions().clear();

            // 2. Now add the interaction as real objects.
            for (InteractionRowData row : myInteractions)
            {
                Interaction inter = row.getInteraction();
                // could be null for an interaction added from the 'hold' area.
                if (inter == null)
                {
                    inter = interactionDao.getByAc(row.getAc());
                }else{
                    inter = interactionDao.getByAc(inter.getAc());
                }
                inter.addExperiment(exp);
                exp.addInteraction(inter);
            }
            // --------------------------------------------------------------------
        }
    }



    /**
     * Override to load the menus for this view.
     */
    @Override
    public void loadMenus() throws IntactException {
        // Handler to the menu factory.
        EditorMenuFactory menuFactory = EditorMenuFactory.getInstance();

        myMenus.putAll(super.getMenus(Experiment.class));
//        myMenus.putAll(super.getMenus(Experiment.class.getName()));

        // The organism menu
        String name = EditorMenuFactory.ORGANISM;
        int mode = (myOrganism == null) ? 1 : 0;
        myMenus.put(name, menuFactory.getMenu(name, mode));

        // The CVInteraction menu.
        name = EditorMenuFactory.INTERACTION;
        mode = (myInter == null) ? 1 : 0;
        myMenus.put(name, menuFactory.getMenu(name, mode));

        // The CVIdentification menu.
        name = EditorMenuFactory.IDENTIFICATION;
        mode = (myIdent == null) ? 1 : 0;
        myMenus.put(name, menuFactory.getMenu(name, mode));
    }

    // Helper methods

    private void makeInteractionRows(Collection<Interaction> ints) {
        for (Interaction inter : ints)
        {
            InteractionRowData row = new InteractionRowData(inter);
            myInteractions.add(row);
        }
    }

    private void resetExperiment(Experiment exp) {
        // Only set the short labels if the experiment has the objects.
        BioSource biosrc = exp.getBioSource();
        setOrganism(biosrc != null ? biosrc.getShortLabel() : null);

        CvInteraction inter = exp.getCvInteraction();
        log.debug("The cvInteraction of the experiment[" + exp.getAc() + "," + exp.getShortLabel() + "] is : "  + inter + "." );
        setInter(inter != null ? inter.getShortLabel() : null);

        CvIdentification ident = exp.getCvIdentification();
        log.debug("The CvIdentification of the experiment[" + exp.getAc() + "," + exp.getShortLabel() + "] is : "  + ident + "." );
        setIdent(ident != null ? ident.getShortLabel() : null);

        // No interactions at this stage.
        myHasLargeInts = false;

        // Clear any previous interactions.
        myInteractions.clear();
    }
    
    private List<String> extractExperimentACs(Collection<Experiment> exps) {
        List<String> list = new ArrayList<String>();
        for (Experiment exp : exps)
        {
            list.add(exp.getAc());
        }
        return list;
    }

    private boolean interactionExists(String ac) {
        for (InteractionRowData myInteraction : myInteractions)
        {
            if (myInteraction.getAc().equals(ac))
            {
                return true;
            }
        }
        return false;
    }
}