/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rigits reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.view.interaction;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.apache.struts.tiles.ComponentContext;
import uk.ac.ebi.intact.application.editor.business.EditUserI;
import uk.ac.ebi.intact.application.editor.exception.validation.InteractionException;
import uk.ac.ebi.intact.application.editor.exception.validation.ValidationException;
import uk.ac.ebi.intact.application.editor.struts.framework.EditorFormI;
import uk.ac.ebi.intact.application.editor.struts.framework.util.AbstractEditViewBean;
import uk.ac.ebi.intact.application.editor.struts.framework.util.EditorConstants;
import uk.ac.ebi.intact.application.editor.struts.framework.util.EditorMenuFactory;
import uk.ac.ebi.intact.application.editor.struts.view.AbstractEditBean;
import uk.ac.ebi.intact.application.editor.struts.view.experiment.InteractionRowData;
import uk.ac.ebi.intact.application.editor.struts.view.feature.FeatureBean;
import uk.ac.ebi.intact.application.editor.util.DaoProvider;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.persistence.dao.*;

import java.util.*;

/**
 * Interaction edit view bean.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class InteractionViewBean extends AbstractEditViewBean<Interaction> {
    protected static Log log = LogFactory.getLog(InteractionViewBean.class);
    /**
     * The KD.
     */
    private Float myKD;

    /**
     * The host organism.
     */
    private String myOrganism;

    /**
     * The interaction with the experiment.
     */
    private String myInteractionType;

    /**
     * The collection of Experiments. Transient as it is only valid for the
     * current display.
     */
    private transient List<ExperimentRowData> myExperiments = new ArrayList<ExperimentRowData>();

    /**
     * Holds Experiments to add. This collection is cleared once the user
     * commits the transaction.
     */
    private transient List<ExperimentRowData> myExperimentsToAdd = new ArrayList<ExperimentRowData>();

    /**
     * Holds Experiments to del. This collection is cleared once the user
     * commits the transaction.
     */
    private transient List<ExperimentRowData> myExperimentsToDel = new ArrayList<ExperimentRowData>();

    /**
     * Holds Experiments to not yet added. Only valid for the current session.
     */
    private transient List<ExperimentRowData> myExperimentsToHold = new ArrayList<ExperimentRowData>();

    /**
     * The collection of Components. Transient as it is only valid for the
     * current display.
     */
    private transient List<ComponentBean> myComponents = new ArrayList<ComponentBean>();

    /**
     * Holds Components to del. This collection is cleared once the user
     * commits the transaction.
     */
    private transient List<ComponentBean> myComponentsToDel = new ArrayList<ComponentBean>();

    /**
     * Holds Components to update. This collection is cleared once the user
     * commits the transaction.
     */
    private transient Set<ComponentBean> myComponentsToUpdate = new HashSet<ComponentBean>();

    /**
     * Keeps a track of features to link
     */
    private List<FeatureBean> myLinkFeatures = new ArrayList<FeatureBean>();

    /**
     * Keeps a track of features to unlink
     */
    private List<FeatureBean> myUnlinkFeatures = new ArrayList<FeatureBean>();

    /**
     * Handler to sort link features.
     */
    private transient ItemLinkSorter myFeatureSorter;

    /**
     * The map of menus for this view.
     */
    private transient Map<String,List<String>> myMenus = new HashMap<String,List<String>>();

    // Override super method to clear experiments and componets.
    @Override
    public void reset() {
        super.reset();

        // Set fields to null.
        setKD(Float.valueOf("0"));
        setOrganism(null);
        setInteractionType(null);
        myFeatureSorter = null;

        // Reset components and experiments
        myExperiments.clear();
        myComponents.clear();
    }

    @Override
    public void reset(Interaction intact) {
        super.reset(intact);

        // Reset the current interaction with the argument interaction.
        resetInteraction(intact);

        // Prepare for Proteins and Experiments for display.
        makeExperimentRows(intact.getExperiments());
        Collection<Component> components = intact.getComponents();
        if(components != null){
            log.debug("reset method, component size : " + components.size());
        }
        makeProteinBeans(intact.getComponents());
    }

    // Reset the fields to null if we don't have values to set. Failure
    // to do so will display the previous edit object's values as current.
    @Override
    public void resetClonedObject(Interaction copy, EditUserI user) {
        super.resetClonedObject(copy, user);

        // Clear existing exps and comps.
        myExperiments.clear();
        myComponents.clear();

        // Reset the interaction view.
        resetInteraction(copy);

        // Add cloned proteins as new proteins.
        for (Component component : copy.getComponents())
        {
            ComponentBean cb = new ComponentBean();
            cb.setFromClonedObject(component);
            // Add to the view.
            myComponents.add(cb);
            // The componen needs to be updated as well.
            myComponentsToUpdate.add(cb);
        }
    }


    public Set<ComponentBean> getMyComponentsToUpdate() {
        return myComponentsToUpdate;
    }


    // Override the super to persist others.
    @Override
    public void persistOthers(EditUserI user) throws IntactException {
        // First transaction for
        try {
            // persist the view.
            persistCurrentView();
            user.rollback(); //to end editing
        }
        catch (IntactException ie1) {
            log.error("", ie1);
            user.rollback();
            // Rethrow the exception to be logged.
            throw ie1;
        }
        // Need another transaction to delete features.
        try {

            // persist the view in a second transaction
            persistCurrentView2();

            user.rollback(); //to end editing

        }
        catch (IntactException ie1) {
            log.error("", ie1);
            user.rollback();
            throw ie1;
        }
    }

    // Override the super method as the current interaction is added to the
    // recent interaction list.
    @Override
    public void addToRecentList(EditUserI user) {
        InteractionRowData row = new InteractionRowData(getAnnotatedObject());
        user.addToCurrentInteraction(row);
    }

    // Override to remove the current interaction from the recent list.
    @Override
    public void removeFromRecentList(EditUserI user) {
        InteractionRowData row = new InteractionRowData(getAc());
        user.removeFromCurrentInteraction(row);
    }

    // Override to provide Experiment layout.
    @Override
    public void setLayout(ComponentContext context) {
        context.putAttribute("content", "edit.int.layout");
    }

    // Override to provide Interaction help tag.
    @Override
    public String getHelpTag() {
        return "editor.interaction";
    }

    // Override to copy data from the form.
    @Override
    public void copyPropertiesFrom(EditorFormI editorForm) {
        // Set the common values by calling super first.
        super.copyPropertiesFrom(editorForm);

        // Cast to the interaction to get interaction specific data.
        InteractionActionForm intform = (InteractionActionForm) editorForm;
        setInteractionType(intform.getInteractionType());
        setOrganism(intform.getOrganism());
        setKD(intform.getKd());
    }

    // Override to copy Interaction data.
    @Override
    public void copyPropertiesTo(EditorFormI form) {
        super.copyPropertiesTo(form);

        // Cast to the interaction form to copy interaction data.
        InteractionActionForm intform = (InteractionActionForm) form;

        intform.setInteractionType(getInteractionType());
        intform.setOrganism(getOrganism());
        intform.setKd(getKD());

        intform.setExperiments(getExperiments());
        intform.setExpsOnHold(getHoldExperiments());
        intform.setComponents(getComponents());
    }

    @Override
    public void sanityCheck() throws ValidationException, IntactException {
        // Look for any unsaved or error proteins.
        for (ComponentBean pb : myComponents)
        {
            if (!pb.getEditState().equals(AbstractEditBean.VIEW))
            {
                throw new InteractionException("int.unsaved.prot",
                                               "error.int.sanity.unsaved.prot");
            }
        }

        // Any missing experiments (check 7).
        if (myExperiments.isEmpty()) {
            throw new InteractionException("int.sanity.exp",
                    "error.int.sanity.exp");
        }
    }

    /**
     * Override to provide the menus for this view.
     * @return a map of menus for this view. It consists of common menus for
     * annotation/xref, organism (add), interaction type and role (add & edit).
     */
    @Override
    public Map<String,List<String>> getMenus() throws IntactException {
        return myMenus;
    }

    public void setKD(Float kd) {
        myKD = kd;
    }

    public Float getKD() {
        return myKD;
    }

    public void setOrganism(String organism) {
        myOrganism = EditorMenuFactory.normalizeMenuItem(organism);
    }

    public String getOrganism() {
        return myOrganism;
    }

    public void setInteractionType(String interaction) {
        myInteractionType = EditorMenuFactory.normalizeMenuItem(interaction);
    }

    public String getInteractionType() {
        return myInteractionType;
    }

    /**
     * Adds an Experiment.
     *
     * @param expRow the Experiment row to add.
     * <p/>
     * <pre>
     * post: myExperimentsToAdd = myExperimentsToAdd@pre + 1
     * post: myExperiments = myExperiments@pre + 1
     * </pre>
     */
    public void addExperiment(ExperimentRowData expRow) {
        // Experiment to add.
        myExperimentsToAdd.add(expRow);
        // Add to the view as well.
        myExperiments.add(expRow);
    }

    /**
     * True if given experiment exists in this object's experiment collection.
     *
     * @param expRow the experiment row to compare.
     * @return true <code>expRow</code> exists in this object's experiment
     * collection. The comparision uses the equals method of
     * <code>ExperimentRowData</code> class.
     * <p/>
     * <pre>
     * post: return->true implies myExperimentsToAdd.exists(exbean)
     * </pre>
     */
    public boolean experimentExists(ExperimentRowData expRow) {
        return myExperiments.contains(expRow);
    }

    /**
     * Removes an Experiment
     *
     * @param expRow the Experiment row to remove.
     * <p/>
     * <pre>
     * post: myExperimentsToDel = myExperimentsToDel@pre - 1
     * post: myExperiments = myExperiments@pre - 1
     * </pre>
     */
    public void delExperiment(ExperimentRowData expRow) {
        // Add to the container to delete experiments.
        myExperimentsToDel.add(expRow);
        // Remove from the view as well.
        myExperiments.remove(expRow);
    }

    /**
     * Adds an Experiment bean to hold if the new experiment doesn't
     * already exists in the experiment hold collection and in the
     * current experiment collection for this interaction.
     *
     * @param exps a collection of <code>Experiment</code> to add.
     * <p/>
     * <pre>
     * pre:  forall(obj : Object | obj.oclIsTypeOf(Experiment))
     * </pre>
     */
    public void addExperimentToHold(Collection<ExperimentRowData> exps) {
        for (ExperimentRowData expRow : exps)
        {
            // Avoid duplicates.
            if (!myExperimentsToHold.contains(expRow) && !myExperiments.contains(expRow))
            {
                myExperimentsToHold.add(expRow);
            }
        }
    }

    /**
     * Hides an Experiment bean from hold.
     *
     * @param expRow experiment row to hide.
     * <pre>
     * pre: myExperimentsToHold->includes(expRow)
     * post: myExperimentsToHold = myExperimentsToHold@pre - 1
     * </pre>
     */
    public void hideExperimentToHold(ExperimentRowData expRow) {
        myExperimentsToHold.remove(expRow);
    }

    /**
     * Clears all the experiments on hold.
     */
    public void clearExperimentToHold() {
        myExperimentsToHold.clear();
    }

    /**
     * Returns a collection of <code>ExperimentRowData</code> objects.
     * <p/>
     * <pre>
     * post: return != null
     * post: return->forall(obj : Object | obj.oclIsTypeOf(ExperimentRowData))
     * </pre>
     */
    public List<ExperimentRowData> getExperiments() {
        return myExperiments;
    }

    /**
     * Returns a collection of <code>ExperimentRowData</code> objects on hold.
     * <p/>
     * <pre>
     * post: return != null
     * post: return->forall(obj : Object | obj.oclIsTypeOf(ExperimentRowData))
     * </pre>
     */
    public List<ExperimentRowData> getHoldExperiments() {
        return myExperimentsToHold;
    }

    /**
     * Returns an <code>ExperimentRowData</code> at given location.
     *
     * @param index the position to return <code>ExperimentRowData</code>.
     * @return <code>ExperimentRowData</code> at <code>index</code>.
     * <p/>
     * <pre>
     * pre: index >=0 and index < myExperiments->size
     * post: return != null
     * post: return = myExperiments->at(index)
     * </pre>
     */
    public ExperimentRowData getExperiment(int index) {
        return myExperiments.get(index);
    }

    /**
     * Returns an <code>ExperimentRowData</code> from a collection of
     * 'hold' experiments at given location.
     *
     * @param index the position to return <code>ExperimentRowData</code>.
     * @return <code>ExperimentRowData</code> at <code>index</code> from 'hold'
     * (or experiment not yet added) collection.
     * <p/>
     * <pre>
     * pre: index >=0 and index < myExperimentsToHold->size
     * post: return != null
     * post: return = myExperimentsToHold->at(index)
     * </pre>
     */
    public ExperimentRowData getHoldExperiment(int index) {
        return myExperimentsToHold.get(index);
    }

    /**
     * Adds an Protein.
     *
     * @param interactor the Interactor to add.
     * <p/>
     * <pre>
     * post: myComponents = myComponents@pre + 1
     * </pre>
     */
    public void addInteractor(Interactor interactor) {
        //Add to the view.
        if(interactor instanceof Protein ){
            myComponents.add(new ComponentBean((Protein)interactor));
        }else if (interactor instanceof NucleicAcid){
            myComponents.add(new ComponentBean((NucleicAcid)interactor));
        }else if (interactor instanceof SmallMolecule ){
            myComponents.add(new ComponentBean((SmallMolecule)interactor));
        }
    }

    /**
     * Removes a Protein from given position.
     *
     * @param pos the position in the current Protein collection.
     * <p/>
     * <pre>
     * post: myComponentsToDel = myComponentsToDel@pre + 1
     * post: myComponents = myComponents@pre - 1
     * </pre>
     */

   public void delPolymer(int pos) {
        // The component bean at position 'pos'.
        ComponentBean cb =  myComponents.get(pos);

        // Avoid creating an empty list if the comp has no features.
        if (!cb.getFeatures().isEmpty()) {
            // Collects features to delete (to get around concurrent modification prob)
            List<FeatureBean> featuresToDel = new ArrayList<FeatureBean>(cb.getFeatures());
            // Delete all the Features belonging to this component.
            for (FeatureBean featureToDel : featuresToDel)
            {
                delFeature(featureToDel);
            }
        }
        // Remove it from the view.
        myComponents.remove(pos);
        // Add to the container to delete it.
        myComponentsToDel.add(cb);
        // Remove from the update list if it has already been added.
        myComponentsToUpdate.remove(cb);
    }

    /**
     * Adds a Component bean to update.
     *
     * @param cb a <code>ComponentBean</code> object to update.
     * <p/>
     * <pre>
     * post: myComponentsToUpdate = myComponentsToUpdate@pre + 1
     * post: myComponents = myComponents@pre
     * </pre>
     */
    public void addPolymerToUpdate(ComponentBean cb) {
        myComponentsToUpdate.add(cb);
    }

    /**
     * Removes all the unsaved proteins for the current protein collection. A
     * protein bean whose state equivalent to ComponentBean.SAVE_NEW is
     * considered as unsaved.
     */
    public void removeUnsavedProteins() {
        CollectionUtils.filter(myComponents, ProteinBeanPredicate.ourInstance);
    }

    /**
     * Returns a collection of <code>ComponentBean</code> objects.
     * <p/>
     * <pre>
     * post: return != null
     * post: return->forall(obj : Object | obj.oclIsTypeOf(ComponentBean))
     * </pre>
     */
    public List getComponents() {
        return myComponents;
    }

//    public void removeAllComponentsToUpdate(){
//        myComponentsToUpdate.clear();
//    }

    // Override super to add extra.
    @Override
    public void clearTransactions() {
        super.clearTransactions();

        // Clear experiments.
        myExperimentsToAdd.clear();
        myExperimentsToDel.clear();
        myExperimentsToHold.clear();

        // Clear components.
        myComponentsToDel.clear();
        myComponentsToUpdate.clear();

        // Clear links
        myLinkFeatures.clear();
        myUnlinkFeatures.clear();

        // Clear any transactions associated with component beans.
        for (ComponentBean myComponent : myComponents)
        {
            myComponent.clearTransactions();
        }
    }

    /**
     * Returns the state for this editor to clone.
     *
     * @return true for all the persistent interactions (i.e., false for a
     *         new interaction not yet persisted).
     */
    @Override
    public boolean getCloneState() {
        return getAc() != null;
    }

    /**
     * Updates the Feature bean within a Component. If this is a new Feature,
     * it will add as a bean to the appropriate component. Any existing matching
     * bean is removed before adding the updated feature (at the same location).
     *
     * @param feature the feature to update.
     */
    public void saveFeature(Feature feature) {
        // The component bean the feature belongs to.
        ComponentBean compBean = null;

        // The ac to match to retrieve the component.
        String compAc = feature.getComponent().getAc();

        // Find the component bean this feature bean belongs to.
        log.debug("myComponents.size()" + myComponents.size());
        for (ComponentBean cb : myComponents)
        {
            if(cb == null){
                continue;
            }
            if (cb.getAc().equals(compAc))
            {
                log.debug("Cb found");
                compBean = cb;
                break;
            }
        }
        // We should have this component.
        assert compBean != null;

        // The feature to AC to compare with.
        String featureAc = feature.getAc();

        // Get corresponding feature bean among this component bean.
        FeatureBean featureBean = null;
        for (FeatureBean fb : compBean.getFeatures())
        {
            if (fb.getAc().equals(featureAc))
            {
                featureBean = fb;
                break;
            }
        }
        // Assume that short label has been changed.
        boolean labelChanged = true;

        // Feature bean can be null for a new feature.
        if (featureBean == null) {
            log.debug("No featureBean found attach to this component.");
            featureBean = new FeatureBean(feature);
            // Update this component for it to persist correctly.
            addPolymerToUpdate(compBean);
        }
        else {
            log.debug("FeatureBean found attach to this component.");
            // The flag is based on the short label of the bean and updated Feature
            labelChanged = !featureBean.getShortLabel().equals(feature.getShortLabel());
            // New bean based on the same key as the existing one.
//            log.debug("The featureBean key is :" + featureBean.getKey());
            featureBean = new FeatureBean(feature, featureBean.getKey());
        }
        // Should have this feature.
        assert featureBean != null;

        // Save the 'updated' feature.
        compBean.saveFeature(featureBean);

        // This feature may be linked to another feature. If the short label
        // has been changed, we need to update the linked Feature as well.
        if (labelChanged && featureBean.hasBoundDomain()) {
            FeatureBean destFb = getFeatureBean(featureBean.getBoundDomainAc());
            destFb.setBoundDomain(featureBean.getShortLabel());
        }
    }

    /**
     * Deletes the given feature from the current view.
     * @param fb the Feature bean to delete. This feature must exist
     * in the current view.
     */
    public void delFeature(FeatureBean fb) {
        // Extract the corresponding feature bean.
        FeatureBean bean = getFeatureBean(fb);

        // Has this feature linked to another feature?
        if (bean.hasBoundDomain()) {
            addFeatureToUnlink(bean);
            // The linked feature bean.
        }
        // The component bean the feature belongs to.
        ComponentBean comp = getComponentBean(bean);

        // We should have this component.
        assert comp != null;

        // Remove it from the component beans.
        comp.delFeature(bean);

        // Update this component for it to persist correctly.
        addPolymerToUpdate(comp);
    }

    /**
     * Adds to the Set that maintains which Features to be linked.
     *
     * @param fb1 the Feature bean to add the link to. This bean
     * replaces any previous similar feature (i.e, no duplicates).
     * @param fb2 other Feature bean to link.
     */
    public void addFeatureLink(FeatureBean fb1, FeatureBean fb2) {
        // Update the screen beans.
        fb1.setBoundDomain(fb2.getShortLabel());
        fb2.setBoundDomain(fb1.getShortLabel());

        // This important as we can't reply on unique features anymore!
        fb1.setBoundDomainAc(fb2.getAc());
        fb2.setBoundDomainAc(fb1.getAc());

        // Make sure to set the linked beans to false (or else they will
        // display as checked).
        fb1.setChecked(false);
        fb2.setChecked(false);

        // Add to the list of Features to link.
        myLinkFeatures.add(fb1);
        myLinkFeatures.add(fb2);
    }

    /**
     * Adds to the Set that maintains which Features to be unlinked.
     *
     * @param fb the Feature bean to remove the link. This bean
     * replaces any previous similar feature (i.e, no duplicates).
     */
    public void addFeatureToUnlink(FeatureBean fb) {
        // The destination feature as a bean.
        FeatureBean toFb = getFeatureBean(fb.getBoundDomainAc());
        // This bean must exist.
        assert toFb != null;

        // Update the screen beans.
        fb.setBoundDomain("");
        toFb.setBoundDomain("");

        // Make sure to set the linked beans to false (or else they will
        // display as checked).
        fb.setChecked(false);
        toFb.setChecked(false);

        // Add to the set to update the database later.
        myUnlinkFeatures.add(fb);
        myUnlinkFeatures.add(toFb);
    }

    /**
     * @return true only if there are no new features added to the current view.
     */
    public boolean hasFeaturesAdded() {
        // Search among the updated and deleted components.
        return !(myComponentsToUpdate.isEmpty() && myComponentsToDel.isEmpty());
    }

    /**
     * Deletes features that have been added. This is required when an Interaction
     * is cancelled after adding features (these features are submitted via the
     * Feature editor).
     *
     * @throws IntactException for errors in deleting features.
     */
    public void delFeaturesAdded() throws IntactException {
        // Search among the updated and deleted components.
        deleteFeaturesAdded(myComponentsToUpdate);
        // Could be that newly added Feature was deleted.
        deleteFeaturesAdded(myComponentsToDel);
    }

    /**
     * Returns a list of selected (checkboxes) Feature beans.
     * @return a list of selected features beans. The list is empty if no items
     * were selected.
     */
    public List<FeatureBean> getFeaturesToDelete() {
        // The array to collect features to delete.
        List<FeatureBean> fbs = new ArrayList<FeatureBean>();

        // Loop through components collecting checked features.
        for (ComponentBean compBean : myComponents)
        {
            for (FeatureBean featureBean : compBean.getFeatures())
            {
                if (featureBean.isChecked())
                {
                    fbs.add(featureBean);
                }
            }
        }
        return fbs;
    }

    /**
     * Returns an array that contains two selected (checkboxes) Feature beans.
     * This method assumes that the form has been validated.
     * @return an array containing two selected features beans. The array contains
     * null items if none were selected.
     */
    public FeatureBean[] getFeaturesForLink() {
        // The two feature beans to return.
        FeatureBean[] fbs = new FeatureBean[2];

        // For array indexing.
        int idx = 0;

        // Loop through components until we found two items.
        for (Iterator<ComponentBean> iter0 = myComponents.iterator(); iter0.hasNext()
                && fbs[1] == null;) {
            ComponentBean compBean = iter0.next();
            for (Iterator<FeatureBean> iter1 = compBean.getFeatures().iterator(); iter1
                    .hasNext()
                    && fbs[1] == null;) {
                FeatureBean featureBean = iter1.next();
                if (featureBean.isChecked()) {
                    fbs[idx] = featureBean;
                    ++idx;
                }
            }
        }
        return fbs;
    }

    /**
     * Returns the selected feature for selecting a checkbox. This method
     * @return the selected feature or null if none was selected.
     */
    public FeatureBean getFeatureForUnlink() {
        // Loop till we found the selected feature.
        for (ComponentBean compBean : myComponents)
        {
            for (FeatureBean fb : compBean.getFeatures())
            {
                if (fb.isChecked())
                {
                    return fb;
                }
            }
        }
        // None found so far, return null.
        return null;
    }

    /**
     * Returns the selected feature bean by way of selecting edit/delete feature
     * buttons. A selected bean returns true for
     * {@link uk.ac.ebi.intact.application.editor.struts.view.feature.FeatureBean#isSelected()}
     * method. If a bean was found, it will be 'unselected' before returning it.
     * @return the selected feature bean. <code>null</code> is returned
     * if a Feature wasn't selected.
     *
     * <pre>
     * post: return isSelected() == FALSE iff return != null
     * </pre>
     */
    public FeatureBean getSelectedFeature() {
        for (ComponentBean cb : myComponents)
        {
            for (FeatureBean fb : cb.getFeatures())
            {
                if (fb.isSelected())
                {
                    fb.unselect();
                    return fb;
                }
            }
        }
        return null;
    }

    /**
     * Returns the Feature bean for given AC.
     *
     * @param ac the AC to get the feature for.
     * @return the matching Feature bean for <code>ac</code> or null
     *         if none found.
     */
    public FeatureBean getFeatureBean(String ac) {
        // Look in the componets.
        for (ComponentBean myComponent : myComponents)
        {
            List<FeatureBean> features = myComponent.getFeatures();
            for (FeatureBean fb : features)
            {
                if (fb.getAc().equals(ac))
                {
                    return fb;
                }
            }
        }
        // Not found the bean, return null.
        return null;
    }

    // --------------------- Protected Methods ---------------------------------

    // Implements abstract methods
    @Override
    protected void updateAnnotatedObject() throws IntactException {
        // The cv interaction type for the interaction.
        CvObjectDao<CvObject> cvObjectDao = DaoProvider.getDaoFactory().getCvObjectDao(CvObject.class);
        CvInteractionType type =(CvInteractionType) cvObjectDao.getByShortLabel(myInteractionType);

        // The current Interaction.
        Interaction intact = getAnnotatedObject();

        // Have we set the annotated object for the view?
        if (intact == null) {
            // Collect experiments from beans.
            List<Experiment> exps = new ArrayList<Experiment>();
            for (ExperimentRowData row : getExperimentsToAdd())
            {
                Experiment exp = row.getExperiment();
                if (exp == null)
                {
                    ExperimentDao experimentDao = DaoProvider.getDaoFactory().getExperimentDao();
                    exp = experimentDao.getByAc(row.getAc());
                }
                exps.add(exp);
            }
            // The interactor type.
            CvInteractorType intType = (CvInteractorType) cvObjectDao.getByXref(CvInteractorType.getInteractionMI());

            // Not persisted. Create a new Interaction.
            intact = new InteractionImpl(exps, new ArrayList(),
                    type, intType, getShortLabel(), IntactContext.getCurrentInstance().getConfig().getInstitution());
            // Set this interaction as the annotated object.
            setAnnotatedObject(intact);
        }
        else {
            // Update the existing interaction.
            intact.setCvInteractionType(type);
        }
        // Get the objects using their short label.
        if (myOrganism != null) {
            BioSourceDao bioSourceDao = DaoProvider.getDaoFactory().getBioSourceDao();
            BioSource biosource = bioSourceDao.getByShortLabel(myOrganism);
            intact.setBioSource(biosource);
        }
        intact.setKD(myKD);

        // Delete experiments.
        for (ExperimentRowData row : getExperimentsToDel())
        {
            Experiment exp = row.getExperiment();
            if (exp == null)
            {
                ExperimentDao experimentDao = DaoProvider.getDaoFactory().getExperimentDao();
                exp = experimentDao.getByAc(row.getAc());
            }
            if(exp.getAc() != null){
                exp = (Experiment) DaoProvider.getDaoFactory(Experiment.class).getByAc(exp.getAc());
            }
            intact.removeExperiment(exp);
        }
    }

    /**
     * Override to load the menus for this view.
     */
    @Override
    public void loadMenus() throws IntactException {
        // Holds the menu name.
        String name;

        // Temp variable to hold a menu.
        List<String> menu;

        // Handler to the menu factory.
        EditorMenuFactory menuFactory = EditorMenuFactory.getInstance();

        // Clear any existing menus first.
        myMenus.clear();
        myMenus.putAll(super.getMenus(Interaction.class));

//        myMenus.putAll(super.getMenus());
//        myMenus.putAll(super.getMenus(Interaction.class.getName()));

        // The organism menu
        name = EditorMenuFactory.ORGANISM;
        myMenus.put(name, menuFactory.getMenu(name, 1));

        // The interactiontype menu.
        name = EditorMenuFactory.INTERACTION_TYPE;
        int mode = (myInteractionType == null) ? 1 : 0;
        myMenus.put(name, menuFactory.getMenu(name, mode));

        // Protein role edit menu
        name = EditorMenuFactory.EXPROLE;
        menu = menuFactory.getMenu(name, 0, CvExperimentalRole.NEUTRAL);
        myMenus.put(name, menu);

        // Protein role edit menu
        name = EditorMenuFactory.BIOROLE;
        menu = menuFactory.getMenu(name, 0, CvBiologicalRole.UNSPECIFIED);
        myMenus.put(name, menu);

        // Add the Role add menu.
        name = EditorMenuFactory.EXPROLE;
        menu = myMenus.get(name);
        myMenus.put(name + "_", menuFactory.convertToAddMenu(menu, CvExperimentalRole.NEUTRAL));

          // Add the Role add menu.
        name = EditorMenuFactory.BIOROLE;
        menu = myMenus.get(name);
        myMenus.put(name + "_", menuFactory.convertToAddMenu(menu, CvBiologicalRole.UNSPECIFIED));
    }

    private void makeProteinBeans(Collection<Component> components) {
        myComponents.clear();
        for (Component comp : components)
        {
            myComponents.add(new ComponentBean(comp));
        }
    }

    private void makeExperimentRows(Collection<Experiment> exps) {
        myExperiments.clear();
        for (Experiment exp : exps)
        {
            myExperiments.add(new ExperimentRowData(exp));
        }
    }

    // Helper methods

    /**
     * Returns a collection of experiments to add.
     *
     * @return the collection of experiments to add to the current Interaction.
     * Empty if there are no experiments to add.
     * <p/>
     * <pre>
     * post: return->forall(obj: Object | obj.oclIsTypeOf(ExperimentRowData)
     * </pre>
     */
    private Collection<ExperimentRowData> getExperimentsToAdd() {
        // Experiments common to both add and delete.
        Collection<ExperimentRowData> common = CollectionUtils.intersection(myExperimentsToAdd,
                myExperimentsToDel);
        // All the experiments only found in experiments to add collection.
        return CollectionUtils.subtract(myExperimentsToAdd, common);
    }

    /**
     * Returns a collection of experiments to remove.
     *
     * @return the collection of experiments to remove from the current Interaction.
     * Could be empty if there are no experiments to delete.
     * <p/>
     * <pre>
     * post: return->forall(obj: Object | obj.oclIsTypeOf(ExperimentRowData)
     * </pre>
     */
    private Collection<ExperimentRowData> getExperimentsToDel() {
        // Experiments common to both add and delete.
        Collection<ExperimentRowData> common = CollectionUtils.intersection(myExperimentsToAdd,
                myExperimentsToDel);
        // All the experiments only found in experiments to delete collection.
        return CollectionUtils.subtract(myExperimentsToDel, common);
    }

    private void persistCurrentView() throws IntactException {
        // The current Interaction.
        Interaction intact = getAnnotatedObject();

        // Add experiments here. Make sure this is done after persisting the
        // Interaction first. - IMPORTANT. don't change the order.
        for (Iterator iter = getExperimentsToAdd().iterator(); iter.hasNext();) {
            ExperimentRowData row = (ExperimentRowData) iter.next();
            Experiment exp = null;
            if (row.getAc() != null){
                log.debug("row ac is " + row.getAc());
                ExperimentDao experimentDao = DaoProvider.getDaoFactory().getExperimentDao();
                exp = experimentDao.getByAc(row.getAc());
            }
            intact.addExperiment(exp);
        }

        // Delete components and remove it from the interaction.
        deleteComponents(intact);

        // Update components.
        updateComponents(intact);

        // No need to test whether this 'intact' persistent or not because we
        // know it has been already persisted by persist() call.
        InteractionDao interactionDao = DaoProvider.getDaoFactory().getInteractionDao();

        log.debug("we have updated the components, now we save the interaction, it has " + intact.getComponents().size() + " components");
        interactionDao.update((InteractionImpl) intact);
    }

    private void persistCurrentView2() throws IntactException {
        FeatureDao featureDao = DaoProvider.getDaoFactory().getFeatureDao();
        // Keeps a track of Features to update. This avoids multiple updates to the
        // same feature.
        Set<Feature> featuresToUpdate = new HashSet<Feature>();

        // Handler to the link sorter and sort links.
        ItemLinkSorter sorter = getFeatureSorter();
        sorter.doIt(myLinkFeatures, myUnlinkFeatures);

        // Links features.
        linkFeatures(featuresToUpdate, sorter.getItemsToLink().iterator());

        // Unlinks features.
        unlinkFeatures(featuresToUpdate, sorter.getItemsToUnLink().iterator());

        // Update the feature in the Set.
        for (Feature featureToUpdate : featuresToUpdate)
        {
            featureDao.update(featureToUpdate);
        }

        for (ComponentBean cb : myComponentsToUpdate)
        {
            ComponentDao componentDao = DaoProvider.getDaoFactory().getComponentDao();
            Component comp = cb.getComponent();

            // Process features deleted from the current component.
            for (FeatureBean fb : (Iterable<FeatureBean>) cb.getFeaturesToDelete())
            {
                Feature featureToDel = fb.getUpdatedFeature();
                if(featureToDel != null && featureToDel.getAc() != null){
                    featureToDel = featureDao.getByAc(featureToDel.getAc());
                }

                // Remove from the component and delete the feature
                log.debug("Remove feature from component");
                comp.removeBindingDomain(featureToDel);
                log.debug("Save or update comp");
                componentDao.saveOrUpdate(comp);
                log.debug("Delete feature");
                featureDao.delete(featureToDel);

                // No further action if this feature is not linked.
                if (!fb.hasBoundDomain())
                {
                    continue;
                }
                // The linked feature bean.
                FeatureBean fb2 = getFeatureBean(fb.getBoundDomainAc());

                // Linked feature may have already been deleted.
                if (fb2 == null)
                {
                    continue;
                }
                // The linked feature.
                Feature linkedFeature = fb2.getUpdatedFeature();
                linkedFeature.setBoundDomain(null);
                featureDao.update(linkedFeature);
            }
            componentDao.update(comp);
        }
    }

    /**
     * Resets this interaction with give interaction object.
     *
     * @param interaction the Interaction to set the current interaction.
     */
    private void resetInteraction(Interaction interaction) {
        setKD(interaction.getKD());

        // Only set the short labels if the interaction has non null values.
        BioSource biosrc = interaction.getBioSource();
        setOrganism(biosrc != null ? biosrc.getShortLabel() : null);

        CvInteractionType inter = interaction.getCvInteractionType();
        setInteractionType(inter != null
                ? interaction.getCvInteractionType().getShortLabel() : null);
    }

    /**
     * Returns the Component bean for given Feature bean.
     * @param fb the Feature bean to search for.
     * @return the Component bean for <code>fb</code>; null is returned if there
     * is no matching component exists for <code>fb</code>.
     */
    private ComponentBean getComponentBean(FeatureBean fb) {
        for (ComponentBean cb : myComponents)
        {
            if (cb.getFeatures().contains(fb))
            {
                return cb;
            }
        }
        // None found.
        return null;
    }

    private FeatureBean getFeatureBean(FeatureBean fb) {
        // Look in the componets.
        for (ComponentBean myComponent : myComponents)
        {
            List<FeatureBean> features = myComponent.getFeatures();
            if (features.contains(fb))
            {
                return features.get(features.indexOf(fb));
            }
        }
        // Not found the bean, return null.
        return null;
    }

    /**
     * Deletes added featues from given collection.
     * @param components the components to search for Features
     * @throws IntactException for errors in deleting a Feature.
     */
    private void deleteFeaturesAdded(Collection<ComponentBean> components)
            throws IntactException {
        for (ComponentBean cb : components)
        {
            for (FeatureBean featureBean : cb.getFeaturesAdded())
            {
                FeatureDao featureDao = DaoProvider.getDaoFactory().getFeatureDao();
                featureDao.delete(featureBean.getFeature());
            }
        }
    }

    /**
     * Deletes all the components in the components to delete collection.
     * @param intact the current Interaction
     * @throws IntactException for errors in creating or retrieving a Component.
     */
    private void deleteComponents(Interaction intact) throws IntactException {
        for (ComponentBean cb : myComponentsToDel)
        {
            Component comp = cb.getComponent(true);
            // No need to delete from persistent storage if the link to this
            // Protein is not persisted.
            if ((comp == null) || (comp.getAc() == null)) {
                Collection<Component> components = getCorrespondingComponent(intact,cb);
                for(Component componentToDelete : components){
                    intact.removeComponent(componentToDelete);
                    componentToDelete.setInteraction(null);
                }
                continue;
            }
            // Disconnect any links between features in the component.
            disconnectLinkedFeatures(cb);
            ComponentDao componentDao = DaoProvider.getDaoFactory().getComponentDao();
            intact.removeComponent(comp);

            comp.setInteraction(null);
            componentDao.delete(comp);

            InteractionDao intDao = DaoProvider.getDaoFactory().getInteractionDao();
            intDao.saveOrUpdate((InteractionImpl) intact);
        }
    }


    /**
     * Updates Components.
     * @param intact the current Interaction
     * @throws IntactException for errors in creating/retrieving a Feature or a Range
     */
    private void updateComponents(Interaction intact) throws IntactException {

        FeatureDao featureDao = DaoProvider.getDaoFactory().getFeatureDao();
        RangeDao rangeDao = DaoProvider.getDaoFactory().getRangeDao();
        ComponentDao componentDao = DaoProvider.getDaoFactory().getComponentDao();
        // Update components.
        for(ComponentBean cb : myComponentsToUpdate){
            cb.setInteraction(getAnnotatedObject());

            // Disconnect any links between features in the component which are
            disconnectLinkedFeatures(cb);

            Component comp = cb.getComponent(true);
            componentDao.saveOrUpdate(comp);

            // Add features
            for (FeatureBean featureBean : cb.getFeaturesToAdd())
            {
                Feature feature = featureBean.getUpdatedFeature();
                feature.setComponent(comp);

                // Feature AC is null for a cloned interaction.
                if (feature.getAc() == null) {
                    // Create a new Feature.
                    featureDao.persist(feature);
                    // Create ranges for the feature.
                    for (Range range : feature.getRanges()) {
                        range.setFeature(feature);
                    }
                }
                // Add to the component.
                for (Range range : feature.getRanges()) {
                    range.setFeature(feature);
                }
                comp.addBindingDomain(feature);
                comp.setInteraction(intact);
            }

            Iterator<FeatureBean> fbIterator = cb.getFeaturesToDelete().iterator();
            while(fbIterator.hasNext()){
                FeatureBean featureBean = fbIterator.next();
                if(featureBean.getAc() != null){
                    Iterator<Feature> iterator = comp.getBindingDomains().iterator();
                    while(iterator.hasNext()){
                        Feature feature = iterator.next();
                        if(featureBean.getAc().equals(feature.getAc())){
                            featureDao.delete(feature);
                            iterator.remove();
                        }
                    }
                   fbIterator.remove(); 
                }
            }


            if(comp.getAc()!= null){
                log.debug("comp.getAc() =" + comp.getAc() + ".");
                Iterator<Component> compIterator = intact.getComponents().iterator();
                log.debug("intact.getComponents().size() =" + intact.getComponents().size());
                while(compIterator.hasNext()){
                    Component component = compIterator.next();
                    System.out.println("component.getAc()=" + component.getAc() + ".");
                    if(comp.getAc().equals(component.getAc())){
                        log.debug("Removing the component =" + comp.getAc());
                        compIterator.remove();
                    }
                }
            }

            intact.addComponent(comp);

            componentDao.saveOrUpdate(comp);

//            iterator.remove();
        }
    }

    public Component interactionContainsComp(Interaction interaction, Component comp){
        if(comp.getAc() != null){
            Collection<Component> components = interaction.getComponents();
            for(Component component : components){
                if(comp.getAc().equals(component.getAc())){
                    return component;
                }
            }
        }
        return null;
    }


    /**
     * Links features.
     * @param set collects Features to update.
     * @param iter an iterator to iterate throgh feature beans.
     * @throws IntactException error in accessing a Feature.
     */
    private void linkFeatures(Set<Feature> set, Iterator iter)
            throws IntactException {
        while (iter.hasNext()) {
            // The feature bean to link.
            FeatureBean fb = (FeatureBean) iter.next();

            // The bound domain can be empty if this link was later unlinked
            // before it was persisted.
            if (!fb.hasBoundDomain()) {
                // Already unliked, carry on with the next.
                continue;
            }
            // The Feature objets to link together. Use 'user' to get changes
            // to the bound domain.
            Feature srcFeature = fb.getUpdatedFeature();
            Feature toFeature = getFeatureBean(fb.getBoundDomainAc()).getUpdatedFeature();

            // Sets the links.
            srcFeature.setBoundDomain(toFeature);
            toFeature.setBoundDomain(srcFeature);

            // Update features.
            set.add(srcFeature);
            set.add(toFeature);
        }
    }

    /**
     * Unlinks features.
     * @param set collects Features to update.
     */
    private void unlinkFeatures(Set<Feature> set, Iterator iter) {
        while (iter.hasNext()) {
            // The Feature to unlink.
            Feature feature = ((FeatureBean) iter.next()).getFeature();

            // Set the bound domain to null.
            if(feature != null){
                feature.setBoundDomain(null);
                // Update features.
                set.add(feature);
            }

        }
    }

    /**
     * Disconnects the link between two Features.
     * @param cb the bean to search among the Features to delete
     * @throws IntactException for update errors.
     */
    private void disconnectLinkedFeatures(ComponentBean cb)
            throws IntactException {
        // Delete any links among features to delete. This should be done
        // first before deleting a feature. Actual deleting a feature is
        // done in a separate transaction.
        for (FeatureBean featureBean : cb.getFeaturesToDelete())
        {
            Feature feature = featureBean.getUpdatedFeature();
            // Remove any links if this feature is linked to another feature.
            if (feature.getBoundDomain() != null)
            {
                Feature toFeature = feature.getBoundDomain();
                if (toFeature.getBoundDomain() == null)
                {
                    continue;
                }
                // Disconnect the links between two features.
                toFeature.setBoundDomain(null);
                feature.setBoundDomain(null);
                FeatureDao featureDao = DaoProvider.getDaoFactory().getFeatureDao();
                featureDao.update(feature);
                featureDao.update(toFeature);
            }
        }
    }

    private ItemLinkSorter getFeatureSorter() {
        if (myFeatureSorter == null) {
            myFeatureSorter = new ItemLinkSorter();
        }
        return myFeatureSorter;
    }

    // Static Inner Class -----------------------------------------------------

    private static class ProteinBeanPredicate implements Predicate {

        private static ProteinBeanPredicate ourInstance = new ProteinBeanPredicate();

        public boolean evaluate(Object object) {
            return !((ComponentBean) object).getEditState().equals(ComponentBean.SAVE_NEW);
        }
    }

    // End of Inner class -----------------------------------------------------

    // Sanity checking routines
}