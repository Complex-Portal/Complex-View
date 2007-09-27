/*
Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.view.feature;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.apache.struts.tiles.ComponentContext;
import uk.ac.ebi.intact.application.editor.business.EditUserI;
import uk.ac.ebi.intact.application.editor.struts.framework.EditorFormI;
import uk.ac.ebi.intact.application.editor.struts.framework.util.AbstractEditViewBean;
import uk.ac.ebi.intact.application.editor.struts.framework.util.EditorConstants;
import uk.ac.ebi.intact.application.editor.struts.framework.util.EditorMenuFactory;
import uk.ac.ebi.intact.application.editor.util.DaoProvider;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.persistence.dao.*;

import java.util.*;

/**
 * Feature view bean.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class FeatureViewBean extends AbstractEditViewBean<Feature> {
    // Class Data
    private static final Log log = LogFactory.getLog(FeatureViewBean.class);

    /**
     * The menu items for the boolean list.
     */
    private static final List ourBoolenMenus = Arrays.asList(
            new String[]{Boolean.TRUE.toString(), Boolean.FALSE.toString()});

    /**
     * The default laytout name.
     */
    private static final String ourDefaultLayoutName = "edit.feature.layout";
    
    /**
     * The component this feature belongs to.
     */
    private Component myComponent;

    /**
     * The CvFeature type; null for a feature not yet persisted.
     */
    private String myCvFeatureType;

    /**
     * The CvFeatureIdentification type; null for a feature not yet persisted.
     */
    private String myCvFeatureIdent;

    /**
     * List of ranges for the feature. This collection is cleared once the user
     * commits the transaction.This represents the current view of ranges for
     * the feature.
     */
    private List<RangeBean> myRanges = new ArrayList<RangeBean>();

    /**
     * List of new ranges to add to the feature. This collection is cleared
     * once the user commits the transaction.
     */
    private Set<RangeBean> myRangesToAdd = new HashSet<RangeBean>();

    /**
     * List of ranges to delete from the feature. This collection is cleared
     * once the user commits the transaction.
     */
    private Set<RangeBean> myRangesToDel = new HashSet<RangeBean>();

    /**
     * List of ranges to update for the current feature. This collection is
     * cleared once the user commits the transaction.
     */
    private Set<RangeBean> myRangesToUpdate = new HashSet<RangeBean>();

    /**
     * True if this is a new feature (as a result of selecting Add Feature). Once
     * this is set it will remain till the current Feature is ended.
     */
    private boolean myNewFeature;

    /**
     * The name of the layout. Set it to the default layout.
     */
    private String myCurrentLayoutName = ourDefaultLayoutName;

    /**
     * True if the mutation mode is requested.
     */
    private boolean myMutationMode;

    /**
     * The map of menus for this view.
     */
    private transient Map<String,List<String>> myMenus = new HashMap<String,List<String>>();

    // Override to provide the Feature layout.
    @Override
    public void setLayout(ComponentContext context) {
        context.putAttribute("content", myCurrentLayoutName);
    }

    // Override to provide Experiment help tag.
    @Override
    public String getHelpTag() {
        return "editor.int.features";
    }

    // Override to provide set feature from the form.
    @Override
    public void copyPropertiesFrom(EditorFormI form) {
        // Set the common values by calling super first.
        super.copyPropertiesFrom(form);

        // Cast to the feature form to get feature data.
        FeatureActionForm featureForm = (FeatureActionForm) form;
        setCvFeatureType(featureForm.getFeatureType());
        setCvFeatureIdentification(featureForm.getFeatureIdent());
    }

    // Override to copy Feature data.
    @Override
    public void copyPropertiesTo(EditorFormI form) {
        super.copyPropertiesTo(form);

        // Properties related to the parent protein.
        FeatureActionForm featureForm = (FeatureActionForm) form;
        featureForm.setParentAc(getParentAc());
        featureForm.setParentShortLabel(getParentShortLabel());
        featureForm.setParentFullName(getParentFullName());

        // Properties related to feature.
        featureForm.setShortLabel(getShortLabel());
        featureForm.setAc(getAc());
        featureForm.setFeatureType(getCvFeatureType());
        featureForm.setFeatureIdent(getCvFeatureIdentification());
        featureForm.setMutationState(myMutationMode);

        // Properties related to the ranges.
        featureForm.setRanges(myRanges);
    }

    // Override to not to display the Delete button.
    @Override
    public boolean getDeleteState() {
        return false;
    }

    // Override to not to display for a mutation entry.
    @Override
    public boolean getSaveState() {
        return !myMutationMode;
    }

    /**
     * Sets the component for this feature.
     * @param component the component this feature belongs to.
     */
    public void setComponent(Component component) {
        myComponent = component;
    }

    public Component getComponent() {
        if( myComponent != null && myComponent.getAc() != null){
            ComponentDao  componentDao = DaoProvider.getDaoFactory().getComponentDao();
            myComponent = componentDao.getByAc(myComponent.getAc());
        }
        return myComponent;
    }

    /**
     * Returns a defined feature instance. Needed by JSPs.
     * @return the defined feature instance.
     */
    public DefinedFeatureBean getDefinedFeature() {
        return DefinedFeatureBean.getInstance();
    }

    public String getCvFeatureType() {
        return myCvFeatureType;
    }

    public void setCvFeatureType(String featureType) {
        myCvFeatureType = EditorMenuFactory.normalizeMenuItem(featureType);
    }

    public String getCvFeatureIdentification() {
        return myCvFeatureIdent;
    }

    public void setCvFeatureIdentification(String featureIdent) {
        myCvFeatureIdent = EditorMenuFactory.normalizeMenuItem(featureIdent);
    }

    public boolean isInMutationMode() {
        return myMutationMode;
    }

    public boolean isInNonMutationMode() {
        return !myMutationMode;
    }

    // For JSPs
    public boolean getInNonMutationMode() {
        return isInNonMutationMode();
    }

    public void turnOffMutationMode() {
        myMutationMode = false;
        setDefaultLayout();
    }

    /**
     * Toggles between normal/mutation mode.
     */
    public void toggleEditMode() {
        if (isInMutationMode()) {
            // Back to the normal feature editor.
            turnOffMutationMode();
        }
        else {
            // Switch back to the mutation feature editor.
            turnOnMutationMode();
        }
    }

    // Override super to add extra.
    @Override
    public void clearTransactions() {
        super.clearTransactions();

        // Clear transaction ranges.
        myRangesToAdd.clear();
        myRangesToDel.clear();
        myRangesToUpdate.clear();
    }

    /**
     * Override to provide the menus for this view.
     * @return a map of menus for this view. It consists of common menus for
     * annotation/xref, feature type (add or edit), feature identification (add).
     */
    @Override
    public Map<String,List<String>> getMenus() throws IntactException {
        return myMenus;
    }

    /**
     * Returns the boolean menu. Warning this method exposes the internal list
     * to outside (this avoids the overhead of creating an array for each method
     * call).
     * @return the boolean menu.
     *
     * <pre>
     * post: return->size() == 2
     * post: return->forall(obj : Object | obj.oclIsTypeOf(String))
     * </pre>
     */
    public List getBooleanMenu() {
        return ourBoolenMenus;
    }

    /**
     * Adds a range.
     * @param rb the range bean to add.
     *
     * <pre>
     * post: myRangesToAdd = myRangesToAdd@pre + 1
     * post: myRanges = myRanges@pre + 1
     * </pre>
     */
    public void addRange(RangeBean rb) {
        // Add to the container to add a range.
        myRangesToAdd.add(rb);
        // Add to the view as well.
        myRanges.add(rb);
    }

    /**
     * Removes a range for given bean.
     * @param rb the bean to remove the range for. The key in the bean identifies
     * the range to delete.
     */
    public void delRange(RangeBean rb) {
        // Add to the container to delete the range.
        myRangesToDel.add(rb);

        // Remove from the view as well.
        myRanges.remove(rb);
    }

    /**
     * Replaces an existing range bean with a new bean.
     * @param rb the new bean to replace existing bean.
     */
    public void saveRange(RangeBean rb) {
        // The updated bean can only exists in one collection.
        if (!myRangesToAdd.contains(rb)) {
             myRangesToUpdate.add(rb);
        }
    }

    /**
     * True if given range exists in the current display.
     * @param bean the Range bean to compare.
     * @return true if <code>bean</code> exists in the current display.
     *
     * @see RangeBean#isEquivalent(RangeBean)
     */
    public boolean rangeExists(RangeBean bean) {
        for (RangeBean rb : myRanges)
        {
            // Avoid comparing to itself.
            if (rb.getKey() == bean.getKey())
            {
                continue;
            }
            if (rb.isEquivalent(bean))
            {
                return true;
            }
        }
        // Not equivalent; false is returned.
        return false;
    }

    // Override the super to persist others.
    @Override
    public void persistOthers(EditUserI user) throws IntactException {
        try {
            // persist the view.
            persistCurrentView();
            user.rollback(); //to end editing
        }
        catch (IntactException ie1) {
            log.error("", ie1);
            ie1.printStackTrace();
            user.rollback();
            // Rethrow the exception to be logged.
            throw ie1;
        }
    }

    /**
     * Return the status (new or old) of the current feature.
     * @return true if this is a new feature (Add Feature); for all other
     * instances false is returned.
     */
    public boolean isNewFeature() {
        return myNewFeature;
    }

    // FOR JSPs
    public boolean getNewFeature() {
        return isNewFeature();
    }

    // Override the super method to initialize this class specific resetting.
    @Override
    public void reset() {
        super.reset();

        setCvFeatureType(null);
        setCvFeatureIdentification(null);

        // Clear ranges
        myRanges.clear();

        setComponent(null);
        myMutationMode = false;
        myCurrentLayoutName = ourDefaultLayoutName;
    }

    // Override the super method to initialize this class specific resetting.
    @Override
    public void reset(Class<Feature> clazz) {
        super.reset(clazz);
        // Mark it as a new feature.
        myNewFeature = true;
    }

    // Reset the fields to null if we don't have values to set. Failure
    // to do so will display the previous edit object's values as current.
    @Override
    public void reset(Feature feature) {
        super.reset(feature);

        // Reset the view with the given feature.
        setComponent(feature.getComponent());

        // CVFeatureType is compulsory.
        setCvFeatureType(feature.getCvFeatureType().getShortLabel());

        // CvFeatureIdent is not compulsory.
        CvFeatureIdentification ident = feature.getCvFeatureIdentification();
        setCvFeatureIdentification(ident != null
                ? feature.getCvFeatureIdentification().getShortLabel() : null);

        // Set the ranges for the form to update.
        setRanges(feature.getRanges());

        // Not a new feature.
        myNewFeature = false;
    }

    // --------------------- Protected Methods ---------------------------------

    // Implements abstract methods
    @Override
    protected void updateAnnotatedObject() throws IntactException {
        // The feature type for the current feature.
        CvObjectDao<CvFeatureType> cvObjectDao = DaoProvider.getDaoFactory().getCvObjectDao(CvFeatureType.class);
        CvFeatureType featureType = cvObjectDao.getByShortLabel(getCvFeatureType());

        // The current feature.
        Feature feature = getAnnotatedObject();

        // null if creating a new Feature.
        if (feature == null) {
            // Not persisted; create a new feature object.
            feature = new Feature(IntactContext.getCurrentInstance().getConfig().getInstitution(), getShortLabel(),
                    getComponent(), featureType);
            setAnnotatedObject(feature);
        }
        else {
            // Update the existing feature.
            feature.setCvFeatureType(featureType);
        }
        feature.setCvFeatureIdentification(getCvFeatureIndent());
    }

    @Override
    public void loadMenus() throws IntactException {
        // Handler to the menu factory.
        EditorMenuFactory menuFactory = EditorMenuFactory.getInstance();

        myMenus.clear();
        
//        myMenus.putAll(super.getMenus());
        myMenus.putAll(super.getMenus(Feature.class));

        // The feature type menu
        String name = EditorMenuFactory.FEATURE_TYPE;
        int mode = (getCvFeatureType() == null) ? 1 : 0;
        myMenus.put(name, menuFactory.getMenu(name, mode));

        // The feature identification menu.
        name = EditorMenuFactory.FEATURE_IDENTIFICATION;
        myMenus.put(name, menuFactory.getMenu(name, 1));
    }

    // Helper methods.

    private String getParentAc() {
        return getComponent().getInteractor().getAc();
    }

    private String getParentShortLabel() {
        return getComponent().getInteractor().getShortLabel();
    }

    private String getParentFullName() {
        return getComponent().getInteractor().getFullName();
    }

    private void setRanges(Collection<Range> ranges) {
        // Clear any previous ranges.
        myRanges.clear();
        for (Range range : ranges)
        {
            myRanges.add(new RangeBean(range));
        }
    }

    private CvFeatureIdentification getCvFeatureIndent()
            throws IntactException {
        if (myCvFeatureIdent == null) {
            return null;
        }
        CvObjectDao<CvFeatureIdentification> cvObjectDao = DaoProvider.getDaoFactory().getCvObjectDao(CvFeatureIdentification.class);
        CvFeatureIdentification identification = cvObjectDao.getByShortLabel(myCvFeatureIdent);
        return identification;
    }

    /**
     * Returns a collection of ranges to add.
     * @return the collection of ranges to add to the current Feature.
     * Could be empty if there are no ranges to add.
     *
     * <pre>
     * post: return->forall(obj: Object | obj.oclIsTypeOf(RangeBean)
     * </pre>
     */
    private Collection<RangeBean> getRangesToAdd() {
        // Ranges common to both add and delete.
        Collection<RangeBean> common = CollectionUtils.intersection(myRangesToAdd, myRangesToDel);
        // All the ranges only found in 'ranges to add' collection.
        return CollectionUtils.subtract(myRangesToAdd, common);
    }

    /**
     * Returns a collection of ranges to delete.
     * @return the collection of ranges to delete from the current Feature.
     * Could be empty if there are no ranges to delete..
     *
     * <pre>
     * post: return->forall(obj: Object | obj.oclIsTypeOf(RangeBean)
     * </pre>
     */
    private Collection<RangeBean> getRangesToDel() {
        // Ranges common to both add and delete.
//        Collection<RangeBean> common = CollectionUtils.intersection(myRangesToAdd, myRangesToDel);
//        // All the ranges only found in 'ranges to delete' collection.
//        return CollectionUtils.subtract(myRangesToDel, common);
        return myRangesToDel;
    }
    
    private void persistCurrentView() throws IntactException {
        // The helper to access persistence API.
        FeatureDao featureDao = (FeatureDao) DaoProvider.getDaoFactory(Feature.class);
        RangeDao rangeDao = DaoProvider.getDaoFactory().getRangeDao();
        // The current feature.
        Feature feature =  getAnnotatedObject();

        // The sequence to set in Ranges.
        Polymer polymer = (Polymer) getComponent().getInteractor();
        if(polymer != null && polymer.getAc()!= null && (!"".equals(polymer.getAc()))){
            InteractorDao interactorDao = (InteractorDao) DaoProvider.getDaoFactory(polymer.getClass());
            polymer = (Polymer) interactorDao.getByAc(polymer.getAc());
        }
        String sequence = polymer.getSequence();

        // Add new ranges.
        log.debug("getRangesToAdd.size()" + getRangesToAdd().size());
        for (RangeBean rangeBean : getRangesToAdd())
        {
            // Create the updated range.
            Range range = rangeBean.getUpdatedRange();
            //From the sequence of the protein, taking into account the caracteristics of the protein (cvFuzzyType,
            //from interval start... the prepareSequence prepare and return the sequence that should be the range sequen-
            //ce
            sequence = range.prepareSequence(sequence);
            // Set the sequence for the range.
            range.setSequence(sequence);

            if(getCorrespondingRange(feature, range) != null){
                continue;
            }
            range.setFeature(feature);
            rangeDao.persist(range);
            feature.addRange(range);
            featureDao.saveOrUpdate(feature);
        }
        setAnnotatedObject(feature);
         
        // Delete ranges.
        for (RangeBean rangeBean : getRangesToDel())
        {
            Range range = rangeBean.getRange();
            Range correspondingRange = getCorrespondingRange(getAnnotatedObject(),range);
            if(correspondingRange != null){
               feature.removeRange(correspondingRange);
               range.setFeature(null);
               rangeDao.delete(correspondingRange);
               featureDao.saveOrUpdate(feature);
            }
        }

        // Update existing ranges.
        for (RangeBean myRangeToUpdate : myRangesToUpdate)
        {
            // Update the 'updated' range.
            Range range = myRangeToUpdate.getUpdatedRange();
            Range correspondingRange = getCorrespondingRange(getAnnotatedObject(),range);
            if(correspondingRange == null){
                range.setSequence(sequence);
                rangeDao.update(range);
            }
        }
        setAnnotatedObject(feature);
        // No need to test whether this 'feature' persistent or not because we
        // know it has been already persisted by persist() call.
        // Looks like we can do without this method call.
//        user.update(feature);
    }

    /**
     * Return the feature range corresponding to the searchedRange
     *      ** if the searchedRange has an ac, it will return the feature range having the same ac after having set it's
     *         different property with the property of the searchedRange.
     *      ** if the searchedRange has no ac, it will return the feature range having the same property then the
     *         searchedRange (same fromCvFuzzyType, same toCvFuzzyTYpe, same fromIntervalStart, same fromIntervalEnd,
     *         same toIntervalStart, same toIntervalEnd, same sequence, same isUndetermined, same isLinked.
     * If no corresponding range is found or if feature or range is null, it returns null.
     * @param feature
     * @param searchedRange
     * @return
     */
    public Range getCorrespondingRange(Feature feature, Range searchedRange){
        if(feature == null || searchedRange == null){
            return null;
        }
        Range correspondingRange = null;
        for(Range range : feature.getRanges()){
            if(range.getAc().equals(searchedRange.getAc())){
                range.setFromCvFuzzyType(searchedRange.getFromCvFuzzyType());
                range.setToCvFuzzyType(searchedRange.getToCvFuzzyType());

                range.setFromIntervalEnd(searchedRange.getFromIntervalEnd());
                range.setFromIntervalStart(searchedRange.getFromIntervalStart());
                range.setToIntervalEnd(searchedRange.getToIntervalEnd());
                range.setToIntervalStart(searchedRange.getToIntervalStart());

                range.setLinked(searchedRange.isLinked());
                range.setUndetermined(searchedRange.isUndetermined());
                range.setSequence(range.getSequence());
                return range;

            } else {
                if(range == null){
                    continue;
                }
                if( !((range.getFromCvFuzzyType() == null && range.getFromCvFuzzyType() == null) ||
                    (range.getFromCvFuzzyType()!= null  && searchedRange.getFromCvFuzzyType() != null && range.getFromCvFuzzyType().getAc().equals(searchedRange.getFromCvFuzzyType().getAc()))) ){
                    continue;
                }else if(!((range.getToCvFuzzyType() == null && searchedRange.getToCvFuzzyType() == null) ||
                    (range.getToCvFuzzyType()!= null && searchedRange.getToCvFuzzyType() != null && range.getToCvFuzzyType().getAc().equals(searchedRange.getToCvFuzzyType().getAc())))){
                    continue;
                }else if( range.getFromIntervalEnd() != searchedRange.getFromIntervalEnd()){
                    continue;
                }else if( range.getFromIntervalStart() != searchedRange.getFromIntervalEnd()){
                    continue;
                }else if( range.getToIntervalEnd() != searchedRange.getFromIntervalEnd()){
                    continue;
                }else if( range.getToIntervalStart() != searchedRange.getFromIntervalEnd()){
                    continue;
                }else if( range.isLinked() != searchedRange.isLinked()){
                    continue;
                }else if( range.isUndetermined() != searchedRange.isUndetermined()){
                    continue;
                }else{
                    correspondingRange = range;
                    break;
                }

            }
        }
        return correspondingRange;
    }

    private void turnOnMutationMode() {
        myMutationMode = true;
        setMutationLayout();
    }

    private void setDefaultLayout() {
        myCurrentLayoutName = ourDefaultLayoutName;
    }

    private void setMutationLayout() {
        myCurrentLayoutName = "edit.feature.mutation.layout";
    }
}
