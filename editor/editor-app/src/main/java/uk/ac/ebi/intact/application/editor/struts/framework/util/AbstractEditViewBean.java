/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.framework.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.apache.struts.tiles.ComponentContext;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import uk.ac.ebi.intact.application.commons.util.AnnotationSection;
import uk.ac.ebi.intact.application.editor.business.EditUserI;
import uk.ac.ebi.intact.application.editor.business.EditorService;
import uk.ac.ebi.intact.application.editor.exception.validation.ValidationException;
import uk.ac.ebi.intact.application.editor.struts.framework.EditorFormI;
import uk.ac.ebi.intact.application.editor.struts.view.CommentBean;
import uk.ac.ebi.intact.application.editor.struts.view.XreferenceBean;
import uk.ac.ebi.intact.application.editor.struts.view.interaction.ComponentBean;
import uk.ac.ebi.intact.application.editor.util.DaoProvider;
import uk.ac.ebi.intact.business.IntactException;

import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.persistence.util.CgLibUtil;
import uk.ac.ebi.intact.persistence.dao.*;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.config.impl.AbstractHibernateDataConfig;

import java.io.Serializable;
import java.util.*;

/**
 * This super bean encapsulates behaviour for a common editing session. This
 * class must be extended to provide editor specific behaviour.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public abstract class  AbstractEditViewBean<T extends AnnotatedObject> implements Serializable {

    private static final Log log = LogFactory.getLog(AbstractEditViewBean.class);

    // Ac of the object from which the clone was created. (this should be null when myAnnotObject is persistent and
    // not a clone).
    private String originalAc;

    // AnnotatedObject from which the clone was created, this should be null if myAnnotObject is persistent and
    // not a clone).
    private AnnotatedObjectImpl myOriginal;
    /**
     * The Annotated object to wrap this bean around.
     */
    private T myAnnotObject;

    /**
     * The class we are editing.
     */
    private Class<T> myEditClass;

    /**
     * The name of the curator who created the current edit object.
     */
    private String myCreator;

    /**
     * The name of the curator who updated the current edit object.
     */
    private String myUpdator;

    /**
     * The time of creation of the current edit object.
     */
    private Date myCreated;

    /**
     * The time of last update of the current edit object.
     */
    private Date myUpdated;
    /**
     * The short label of the current edit object.
     */
    private String myShortLabel;

    /**
     * The full name of the current edit object.
     */
    private String myFullName;

    /**
     * The current anchor.
     */
    private String myAnchor;


//    protected static final Logger LOGGER = Logger.getLogger(EditorConstants.LOGGER);


    /**
     * The annotations to display.
     */
    private List<CommentBean> myAnnotations = new ArrayList<CommentBean>();

    /**
     * The Xreferences to display.
     */
    private List<XreferenceBean> myXrefs = new ArrayList<XreferenceBean>();

    /**
     * List of annotations to add. This collection is cleared once the user
     * commits the transaction.
     */
    private List<CommentBean> myAnnotsToAdd = new ArrayList<CommentBean>();

    /**
     * List of annotations to del. This collection is cleared once the user
     * commits the transaction.
     */
    private List<CommentBean> myAnnotsToDel = new ArrayList<CommentBean>();

    /**
     * Set of annotations to update. This collection is cleared once the user
     * commits the transaction.
     */
    private Set<CommentBean> myAnnotsToUpdate = new HashSet<CommentBean>();

    /**
     * List of xrefs to add. This collection is cleared once the user commits
     * the transaction.
     */
    private List<XreferenceBean> myXrefsToAdd = new ArrayList<XreferenceBean>();

    /**
     * List of xrefs to del. This collection is cleared once the user commits
     * the transaction.
     */
    private List<XreferenceBean> myXrefsToDel = new ArrayList<XreferenceBean>();

    /**
     * Set of xrefs to update. This collection is cleared once the user
     * commits the transaction.
     */
    private Set<XreferenceBean> myXrefsToUpdate = new HashSet<XreferenceBean>();

    // Override Objects's equal method.

    /**
     * Compares <code>obj</code> with this object according to
     * Java's equals() contract. Only used for testing a serialized
     * objects.
     * @param obj the object to compare.
     * @return true only if <code>obj</code> is an instance of this class
     * and all non transient fields are equal to given object's non tranient
     * fields. For all other instances, false is returned.
     */
    @Override
    public boolean equals(Object obj) {
        // Identical to this?
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AbstractEditViewBean)) {
            return false;
        }
        // Can safely cast it.
        AbstractEditViewBean other = (AbstractEditViewBean) obj;

        // Annotated object must match if it exists
        if (!equals(myAnnotObject, other.myAnnotObject)) {
            return false;
        }
        // Edit clas smust match; no need to check for null as edit class must
        // exist at all times.
        if (!myEditClass.equals(other.myEditClass)) {
            return false;
        }
        // Short labels must match.
        if (!equals(myShortLabel, other.myShortLabel)) {
            return false;
        }
        // Creators must match.
        if(!equals(myCreator, other.myCreator)){
            return false;
        }
        // Updator must match.
        if(!equals(myUpdator, other.myUpdator)){
            return false;
        }
        // Fullname must match.
        if (!equals(myFullName, other.myFullName)) {
            return false;
        }
        // Created must match
        if(!equals(myCreated, other.myCreated)){
            return false;
        }
        //Updated must match
        if(!equals(myUpdated,other.myUpdated)){
            return false;
        }
        // Annotations must equal.
        if (!myAnnotations.containsAll(other.myAnnotations)) {
            return false;
        }
        if (!myAnnotsToAdd.containsAll(other.myAnnotsToAdd)) {
            return false;
        }
        if (!myAnnotsToDel.containsAll(other.myAnnotsToDel)) {
            return false;
        }
        if (!myAnnotsToUpdate.containsAll(other.myAnnotsToUpdate)) {
            return false;
        }

        // Xrefs must equal.
        if (!myXrefs.containsAll(other.myXrefs)) {
            return false;
        }
        if (!myXrefsToAdd.containsAll(other.myXrefsToAdd)) {
            return false;
        }
        if (!myXrefsToDel.containsAll(other.myXrefsToDel)) {
            return false;
        }
        if (!myXrefsToUpdate.containsAll(other.myXrefsToUpdate)) {
            return false;
        }
        return true;
    }

    /**
     * Deletes all the links to sub objects of the current edit object.
     */
    public void reset() {
        // Clear Transaction containers.
        clearTransactions();

        // Clear annotations and xrefs.
        myAnnotations.clear();
        myXrefs.clear();

        // Set them to null as they may have previous values.
        setAnnotatedObject(null);
        setShortLabel(null);
        setFullName(null);
        setCreator(null);
        setUpdator(null);
        setCreated(null);
        setUpdated(null);
        setOriginalAc(null);

        // editclass is not set to null because passivateObject (EditViewBeanFactory)
        // method relies on this value (key in the object pool).
    }

    /**
     * Resets the bean with the current edit class. This method is called when
     * creating a new annotated object (only the class or the type is known at
     * that time).
     * @param clazz the Class of the new annotated object.
     */
    public void reset(Class<T> clazz) {
        // reset() methid is called before passivating the object and hence
        // no need to call it from here. See EditViewBeanFactory#passivateObject()
        myEditClass = clazz;
    }

    /**
     * Resets with the bean using an existing Annotated object.
     * @param annobj <code>AnnotatedObject</code> object to set this bean.
     */
    public void reset(T annobj) {
        // reset() methid is called before passivating the object and hence
        // no need to call it from here.
        setShortLabel(annobj.getShortLabel());
        setCreator(annobj.getCreator());
        setUpdator(annobj.getUpdator());
        setCreated(annobj.getCreated());
        setUpdated(annobj.getUpdated());
        resetAnnotatedObject(annobj);
        // Cache the annotations and xrefs here to save it from loading
        // multiple times with each invocation to getAnnotations()
        // or getXrefs() methods.
        makeCommentBeans(annobj.getAnnotations());
        makeXrefBeans(annobj.getXrefs());

        // Remove the current short label from the menu. Menus are already loaded
        // via activateObject method call of the EditViewBeanFactory class.
        //try {
        //    removeCurrentSLFromMenus();
        //}
//        catch (IntactException ie) {
//            // Log the error; the editor will display without menus!
//            log.error("loadMenus() error", ie);
//        }
    }

    /**
     * Resets the view with cloned object.
     * @param copy the cloned object to set as the new view. This method is
     * similar to {@link #reset(AnnotatedObject)} except for annotations and
     * xrefs are added as new objects (so, upon persisting this object, new
     * annotations and xrefs will be created).
     * @param user the handler to the user to set the available short label.
     */
    public void resetClonedObject(T copy, EditUserI user) {
        // Clear previous transactions.
        clearTransactions();

        // Clear existing annotations & xrefs (don't want to share them).
        myAnnotations.clear();
        myXrefs.clear();

        // Set it with most likely next short label from the database.
        String newSL = null;
        if(copy instanceof Component){
            newSL = Component.NON_APPLICABLE;
        }
        newSL = user.getNextAvailableShortLabel(copy.getClass(),
                                                       copy.getShortLabel());
        setShortLabel(newSL);


        // Add the annotations in the cloned as new annotations to add.
        Collection<Annotation> annotations = copy.getAnnotations();
        for (Annotation annotation : annotations){
            addAnnotation(new CommentBean(annotation));
        }
        // This is not persisted yet and there aren't any previous annots or
        // xrefs. Annotations need to be cleared or else deleting an annotation
        // after cloning causes persistence problems. see bug: 1011416
        copy.getAnnotations().clear();
        copy.setAnnotations(Collections.EMPTY_LIST);

        // Add the xrefs in the cloned as new xrefs to add.
        Collection<Xref> xrefs = copy.getXrefs();
        for (Xref xref : xrefs) {
            addXref(new XreferenceBean(xref));
        }
        copy.getXrefs().clear();

        // Reset the cloned object with values given by parameter.
        resetAnnotatedObject(copy);

    }

    /**
     * this method returns the hibernate Session  object that is attached to this request.
     * @return the Hibernate Session
     */
     private Session getSession(){
        AbstractHibernateDataConfig abstractHibernateDataConfig = (AbstractHibernateDataConfig) IntactContext.getCurrentInstance().getConfig().getDefaultDataConfig();
        SessionFactory factory = abstractHibernateDataConfig.getSessionFactory();
        Session session = factory.getCurrentSession();
        return session;
    }

    /**
     * The hibernate Session is open and closed after each request. In order to avoid Lazy loading Exception from
     * hibernate, myAnnotObject has to be loaded by the session that is attached to the request beeing handled.
     * Then there are particular cases if myAnnotObject is a clone. In this case myAnnotObject is not a persisted object
     * so it's not contained in the session but it contains some object that should be reloaded. For exemple, an Inter-
     * action clone will have a collection of Components that will be linked to a Interactor which are persited and
     * should be reloaded. In that case we reload the original annot object and reclone it into myAnnotObject, but we
     * make sure that we reload the clone only once within the request.
     *
     *
     * Returns the Annotated object. Could be null if the object is not persisted.
     * @return <code>AnnotatedObject</code> this instace is wrapped around.
     */
    public final T getAnnotatedObject() {
        // If myAnnotObject is contained in the session, we don't reload it but return it directly, otherwise continue.
        if(getSession().isOpen() && getSession().getTransaction().isActive() && getSession().contains(myAnnotObject)){
            if((myAnnotObject != null
                && myAnnotObject.getAc() != null
                && (! "".equals(myAnnotObject.getAc()) ))){
                log.debug("object is contained in the session already, it has an ac : " +  myAnnotObject.getAc());
                return myAnnotObject;
            }
         }

        // If this part of the code is riched it means that myAnnotObject was not contained in the Session. It if has an
        // an ac, we reload it. If it has no ac, it means that myAnnotObject is a clone we go to the next "if" statement
        log.debug("object not contained in the session already, reload it");
        if(myAnnotObject != null
                && myAnnotObject.getAc() != null
                && (! "".equals(myAnnotObject.getAc()) )){

            AnnotatedObjectDao<AnnotatedObjectImpl> annotObjectDao = DaoProvider.getDaoFactory(myAnnotObject.getClass());
            myAnnotObject = (T)annotObjectDao.getByAc(myAnnotObject.getAc());

             log.debug("myAnnotObject is instanceof " + myAnnotObject.getClass().getName());
            return myAnnotObject;
        }

        if (getOriginalAc() != null && myAnnotObject != null && myAnnotObject.getAc()==null){
            if(!(getSession().isOpen() && getSession().getTransaction().isActive() && getSession().contains(myOriginal))){
                log.debug("Recloning object");
                AnnotatedObjectDao<AnnotatedObjectImpl> annotatedObjectDao = DaoProvider.getDaoFactory(myAnnotObject.getClass());
                myOriginal = annotatedObjectDao.getByAc(getOriginalAc());
                try {
                    AnnotatedObjectImpl copy = (AnnotatedObjectImpl) myOriginal.clone();
                    copy.getXrefs().clear();
                    copy.getAnnotations().clear();
                    if(InteractionImpl.class.isAssignableFrom(copy.getClass())){
                        Interaction interaction = (Interaction) copy;
                        interaction.getComponents().clear();
                        log.debug("This was a cloned interaction we removed it's components");

                    }else{
                        log.debug("The annObj was not an interaction it was : " + copy.getClass().getName());
                    }
                    if(InteractionImpl.class.isAssignableFrom(copy.getClass())){
                        Interaction interaction = (Interaction) copy;
                        log.debug("The size of the components is : " + interaction.getComponents().size());
                    }
                    myAnnotObject = (T) copy;
                } catch (CloneNotSupportedException e) {
                    log.debug("Exception while cloning" + e.getMessage());  //To change body of catch statement use File | Settings | File Templates.
                }
            }else{
                log.debug("The annotated object attached is an already reloaded clone") ;
            }

        }

        return myAnnotObject;
    }

    /**
     * 
     * @param interaction
     * @param cb
     * @return
     */
    public Collection<Component> getCorrespondingComponent(Interaction interaction, ComponentBean cb){

        Collection<Component> potentialComps = new ArrayList<Component>();
        for(Component component : interaction.getComponents()){
            if(!component.getInteractor().getAc().equals(cb.getInteractorAc())){
                continue;
            }
            if(!component.getCvExperimentalRole().getShortLabel().equals(cb.getExpRole())){
                continue;
            }
            if(!component.getCvBiologicalRole().getShortLabel().equals(cb.getBioRole())){
                continue;
            }
            if(component.getBindingDomains().size() != cb.getFeatures().size()){
                continue;
            }
            potentialComps.add(component);
        }

        return potentialComps;
    }

    /**
     * Returns accession number.
     * @return the accession number as a <code>String</code> instance; null if
     * the current view is not persisted.
     */
    public final String getAc() {
        if (myAnnotObject != null) {
            return myAnnotObject.getAc();
        }
        return null;
    }

    /**
     * Returns a link to the search application.
     * @return ac as a link to the seatch application or an empty string if this
     * object is not yet persisted (i.r., ac is not yet set).
     */
    public final String getAcLink() {
        if (getAc() == null) {
            return "";
        }
        String topic = EditorService.getTopic(getEditClass());
        return "<a href=\"" + "javascript:show('" + topic + "', '" + getShortLabel() + "')\""
                + ">" + getAc() + "</a>";
    }

    /**
     * Returns the edit class.
     * @return the class name of the current edit object.
     */
    public final Class<T> getEditClass() {
        return myEditClass;
    }

    /**
     * Sets the short label.
     * @param shortLabel the short label to set.
     */
    public final void setShortLabel(String shortLabel) {
        myShortLabel = shortLabel;
    }

    /**
     * Returns the short label.
     * @return the short label as a <code>String</code> instance.
     */
    public final String getShortLabel() {
        return myShortLabel;
    }

    /**
     * Sets the creator.
     * @param creator the creator to set.
     */
    public final void setCreator(String creator) {
        myCreator = creator;
    }

    /**
     * Returns the creator.
     * @return the creator as a <code>String</code> instance.
     */
    public final String getCreator() {
        return myCreator;
    }

    /**
     * Sets the updator.
     * @param updator the updator to set.
     */
    public final void setUpdator(String updator) {
        myUpdator = updator;
    }

    /**
     * Returns the updator.
     * @return the updator as a <code>String</code> instance.
     */
    public final String getUpdator() {
        return myUpdator;
    }

    /**
     * Returns the created timestamp.
     * @return the created as a <code>Date<code> instance
     */
    public Date getCreated() {
        return myCreated;
    }
    /**
     * Sets the updator.
     * @param created the created to set.
     */
    public void setCreated(Date created) {
        this.myCreated = created;
    }
    /**
     * Returns the updated timestamp.
     * @return the updated as a <code>Timestamp<code> instance
     */
    public Date getUpdated() {
        return myUpdated;
    }
   /**
     * Sets the updator.
     * @param updated the updated to set.
     */
    public void setUpdated(Date updated) {
        this.myUpdated = updated;
    }

    /**
     * OriginalAc getter
     * @return the original ac
     */
    public String getOriginalAc() {
        return originalAc;
    }

    /**
     * Setter of the orginalAc
     * @param originalAc string
     */
    public void setOriginalAc(String originalAc) {
        this.originalAc = originalAc;
    }

    /**
     * Sets the full name.
     * @param fullName the full name to set for the current edit object.
     * An empty name (set by tag library when submitting the form) is set
     * to null to avoid equals method returning false for identical objects
     * apart from the full name.
     */
    public final void setFullName(String fullName) {
        // It is improtant for this check or else the full name will be set to
        // an empty string thus causing equals method to return false.
        if (fullName != null && fullName.equals("")) {
            fullName = null;
        }
        myFullName = fullName;
    }

    /**
     * Return the full name.
     * @return the full name as a <code>String</code> instance.
     */
    public String getFullName() {
        return myFullName;
    }

    public String getAnchor() {
        return myAnchor;
    }

    public void setAnchor(String anchor) {
        myAnchor = anchor;
    }

    public void resetAnchor() {
        setAnchor("");
    }

    /**
     * Returns a collection of <code>CommentBean</code> objects.
     *
     * <pre>
     * post: return != null
     * post: return->forall(obj : Object | obj.oclIsTypeOf(CommentBean))
     * </pre>
     */
    public List<CommentBean> getAnnotations() {
        return myAnnotations;
    }

    /**
     * Returns a collection of <code>CommentBean</code> objects.
     *
     * <pre>
     * post: return != null
     * post: return->forall(obj : Object | obj.oclIsTypeOf(CommentBean))
     * </pre>
     */
    public List<CommentBean> getRefreshedAnnotations() {
        return myAnnotations;
    }

    /**
     * Adds an annotation.
     * @param cb the bean to add.
     *
     * <pre>
     * post: myAnnotsToAdd = myAnnotsToAdd@pre + 1
     * post: myAnnotations = myAnnotations@pre + 1
     * </pre>
     */
    public void addAnnotation(CommentBean cb) {
        // Add to the container to add an Annotation.
        myAnnotsToAdd.add(cb);
        // Add to the view as well.
        myAnnotations.add(cb);
    }

    /**
     * Removes an annotation
     * @param cb the comment bean to remove.
     *
     * <pre>
     * post: myAnnotsToDel = myAnnotsToDel@pre - 1
     * post: myAnnotations = myAnnotations@pre - 1
     * </pre>
     */
    public void delAnnotation(CommentBean cb) {
        // Add to the container to delete annotations.
        myAnnotsToDel.add(cb);
        // Remove from the view as well.
        myAnnotations.remove(cb);
    }

    /**
     * True if given Annotation bean exists in the current display.
     * @param bean the Annotation bean to compare.
     * @return true if <code>bean</code> exists in the current display.
     *
     * @see CommentBean#isEquivalent(CommentBean)
     */
    public boolean annotationExists(CommentBean bean) {
        for (CommentBean cb : myAnnotations) {
            // Avoid comparing to itself.
            if (cb.getKey() == bean.getKey()) {
                continue;
            }
            if (cb.isEquivalent(bean)) {
                return true;
            }
        }
        // Not equivalent; false is returned.
        return false;
    }

    /**
     * Returns a collection <code>Xref</code> objects.
     *
     * <pre>
     * post: return->forall(obj: Object | obj.oclIsTypeOf(XreferenceBean))
     * </pre>
     */
    public List<XreferenceBean> getXrefs() {
        return myXrefs;
    }

    /**
     * Adds an xref.
     * @param xb the bean to add.
     *
     * <pre>
     * post: myXrefsToAdd = myXrefsToAdd@pre + 1
     * post: myXrefs = myXrefs@pre + 1
     * </pre>
     */
    public void addXref(XreferenceBean xb) {
        // Xref to add.

//        Xref xref = getCorrespondingXref(getAnnotatedObject(), xb.getXref(getAnnotatedObject()));
//        if(xref == null){
            myXrefsToAdd.add(xb);
//        }else{
//            myXrefsToUpdate.add(xb);
//        }
        // Add to the view as well.
        myXrefs.add(xb);
    }

    /**
     * Removes a xref.
     * @param xb the X'reference bean to remove.
     *
     * <pre>
     * post: myXrefsToDel = myXrefsToDel@pre + 1
     * post: myXrefs = myXrefs@pre - 1
     * </pre>
     */
    public void delXref(XreferenceBean xb) {
        // Add to the container to delete the xref.
        myXrefsToDel.add(xb);
        // Remove from the view as well.
        myXrefs.remove(xb);
    }

    /**
     * True if given xref bean exists in the current display.
     * @param bean the Xref bean to compare.
     * @return true if <code>bean</code> exists in the current display.
     *
     * @see XreferenceBean#isEquivalent(XreferenceBean)
     */
    public boolean xrefExists(XreferenceBean bean) {
        for (XreferenceBean xb : myXrefs)
        {
            // Avoid comparing to itself.
            if (xb.getKey() == bean.getKey())
            {
                continue;
            }
            if (xb.isEquivalent(bean))
            {
                return true;
            }
        }
        // Not equivalent; false is returned.
        return false;
    }

    /**
     * Saves the bean in the update list. Replaces any previous bean with same
     * key (can perform many edits to a single bean). The bean is not added if
     * it is a new bean.
     * @param cb the bean to update.
     */
    public void saveAnnotation(CommentBean cb) {
        if (!myAnnotsToAdd.contains(cb)) {
            myAnnotsToUpdate.add(cb);
        }
    }

    /**
     * Saves the bean in the update list. Replaces any previous bean with same
     * key (can perform many edits to a single bean). The bean is not added if
     * it is a new bean.
     * @param xb the bean to update.
     */
    public void saveXref(XreferenceBean xb) {
        if (!myXrefsToAdd.contains(xb)) {
            myXrefsToUpdate.add(xb);
        }
    }

    /**
     * Clears any pending xrefs and annotations stored in the transaction
     * containers.
     * <pre>
     * post: myAnnotsToAdd->isEmpty
     * post: myAnnotsToDel->isEmpty
     * post: myAnnotsToUpdate->isEmpty
     * post: myXrefsToAdd->isEmpty
     * post: myXrefsToDel->isEmpty
     * post: myXrefsToUpdate->isEmpty
     * </pre>
     */
    public void clearTransactions() {
        this.clearTransAnnotations();
        this.clearTransXrefs();
    }

    /**
     * Persists the current state to the persistent system. After this
     * method is called the persistent view is as same as the current view.
     * @param user handler to access the persistent method calls.
     * @exception IntactException for errors in updating the persistent system.
     */
    public void persist(EditUserI user) throws IntactException {
        try {
            persistCurrentView();
            user.rollback(); //to end editing
        }
        catch (IntactException ie1) {
            user.rollback();
            // Rethrow the exception to be logged.
            throw ie1;
        }
    }

    /**
     * Sets the layout in given context. This method is currently empty as
     * the layout defaults to cv layout. Override this method to provide editor
     * specific layout.
     * @param context the Tiles context to set the layout.
     */
    public void setLayout(ComponentContext context) {
        // Empty
    }

    /**
     * Returns the help tag link for the current view bean; subclasses must
     * override this method to return the help tag (if necessary) or else the
     * link to the CV editor is returned.
     * @return the tag as a String for the current view bean.
     */
    public String getHelpTag() {
        return "editor.cv.editors";
    }

    /**
     * Copies properties from given form to the bean.
     * @param form the form to update the internal data.
     */
    public void copyPropertiesFrom(EditorFormI form) {
        setShortLabel(form.getShortLabel());
        setFullName(form.getFullName());
        setCreator(form.getCreator());
        setUpdator(form.getUpdator());
    }

    /**
     * Copies properties from bean to given form.
     * @param form the form to copy properties to.
     */
    public void copyPropertiesTo(EditorFormI form) {
        form.setAc(getAcLink());
        form.setShortLabel(getShortLabel());

        form.setFullName(getFullName());
        form.setAnnotations(getAnnotations());
        form.setXrefs(getXrefs());
        form.setCreator(getCreator());
        form.setUpdator(getUpdator());
        form.setUpdated(getUpdated());
        form.setCreated(getCreated());

    }

    // Empty methods to be overriden by sub classes.

    /**
     * Persist any sub objects of the edited object. For example, proteins
     * need to be persisted in a separate transaction for an Interaction
     * @param user handler to the user to persist sub objects.
     * @throws IntactException for errors in persisting.
     */
    public void persistOthers(EditUserI user) throws IntactException {
    }

    /**
     * Adds the current edit object to the recent edited item list.
     * Interaction or Experiment beans must override this method.
     * @param user the user handle to add to the recent list.
     */
    public void addToRecentList(EditUserI user) {}

    /**
     * Removes the current edit object from the recent edited item list.
     * Interaction or Experiment beans must override this method.
     * @param user the user handle to remove from the recent list.
     */
    public void removeFromRecentList(EditUserI user) {}

    /**
     * Performs sanity check on a bean. Currently this method doesn't provide
     * checks. Subclass must override this method to provide checks relevant to
     * a view bean.
     * @throws ValidationException if sanity check fails.
     * @throws IntactException for errors in searching for objects in the
     * persistent system.
     */
    public void sanityCheck() throws ValidationException, IntactException {
    }

    /**
     * False as this object is editable. Sublcasses such as ExperimentViewBean
     * must override this method if it has a large number of interactions.
     * This method is used by actions.jsp to disable saving a large Intact object.
     * @return false as all the edit beans are editable by default.
     */
    public Boolean getReadOnly() {
        return Boolean.FALSE;
    }

    /**
     * By default editor objects are not cloneable. Views such as Experiment
     * and Interaction must override this method to true as these objects
     * are cloneable.
     * @return false as by default editor objects are not cloneable.
     */
    public boolean getCloneState() {
        return false;
    }

    /**
     * By default editor objects can be deleted by selecting Delete button from
     * edit screen. However, Features are deleted from the Interaction editor.
     * @return true as by default editor objects are deletable.
     */
    public boolean getDeleteState() {
        return true;
    }

    /**
     * By default editor objects can be saved by selecting Save & Continue button from
     * edit screen. However, Mutation Features cannot save and continue.
      * @return true as by default editor objects should be saved.
     */
    public boolean getSaveState() {
        return true;
    }

    // Protected Methods

    /**
     * Loads menus. Subclasses must implement to provide their own menus.
     * @throws IntactException for errors in accessing the persistent system.
     */
    public abstract void loadMenus() throws IntactException;

    /**
     * Sets the annotated object for the bean.
     * @param annot AnnotatedObject to set the bean.
     */
    protected void setAnnotatedObject(T annot) {
        myAnnotObject = annot;
    }

    /**
     * Helper method for subclasses to access the editor service.
     * @return the only editor service for the application.
     */
    protected EditorService getService() {
        return EditorService.getInstance();
    }

    /**
     * Returns the map of menus which are common to all the editors. It calls the removeFromMenu method to remove the
     * non-relevent term in the menu (see javadoc for this method).
     * @param editedClass, the class of the object beeing edited to know how to filter the menu. Must be on of those class :
     * Experiment, Interaction, Protein, SmallMolecule, NucleicAcid, CvObject or Feature.
     * @return map of menus. This consists of edit/add menus for Topic, Database
     * and Qualifiers.
     * @throws IntactException for errors in accessing the persistent system.
     */
    protected Map<String,List<String>> getMenus(Class editedClass) throws IntactException {
        // The map containing the menus.
        Map<String,List<String>> map = new HashMap<String,List<String>>();

        // Handler to the menu factory.
        EditorMenuFactory menuFactory = EditorMenuFactory.getInstance();

        // The topic edit/add menu

        String name = EditorMenuFactory.TOPIC;
        List<String> menu = menuFactory.getTopicMenu(name, 0, editedClass);
        map.put(name, menu);
        map.put(name + "_", menuFactory.convertToAddMenu(menu));

        // The database edit/add menu.
        name = EditorMenuFactory.DATABASE;
        menu = menuFactory.getMenu(name, 0);
        map.put(name, menu);
        map.put(name + "_", menuFactory.convertToAddMenu(menu));

        // The qualifier edit/add menu.
        name = EditorMenuFactory.QUALIFIER;
        menu = menuFactory.getMenu(name, 0);
        map.put(name, menu);
        map.put(name + "_", menuFactory.convertToAddMenu(menu));

        return map;
    }

    /**
     * Returns the map of menus which are common to all the editors. It calls the removeFromMenu method to remove the
     * non-relevent term in the menu (see javadoc for this method).
     * @return map of menus. This consists of edit/add menus for Topic, Database
     * and Qualifiers.
     * @throws IntactException for errors in accessing the persistent system.
     */
    protected Map<String,List<String>> getMenus() throws IntactException {
        // The map containing the menus.
        Map<String,List<String>> map = new HashMap<String,List<String>>();

        // Handler to the menu factory.
        EditorMenuFactory menuFactory = EditorMenuFactory.getInstance();

        // The topic edit/add menu
        String name = EditorMenuFactory.TOPIC;
        List<String> menu = menuFactory.getMenu(name, 0);
        map.put(name, menu);
        map.put(name + "_", menuFactory.convertToAddMenu(menu));

        // The database edit/add menu.
        name = EditorMenuFactory.DATABASE;
        menu = menuFactory.getMenu(name, 0);
        map.put(name, menu);
        map.put(name + "_", menuFactory.convertToAddMenu(menu));

        // The qualifier edit/add menu.
        name = EditorMenuFactory.QUALIFIER;
        menu = menuFactory.getMenu(name, 0);
        map.put(name, menu);
        map.put(name + "_", menuFactory.convertToAddMenu(menu));

        return map;
    }

    /**
     * Returns the map of menus which are common to all the editors, it call the .
     * @param editorPageName The editor page name. This parameter is taken from EditorMenuFactory :
     *          Editor - Interaction =====> EditorMenuFactory.INTERACTION
     *          Editor - Experiment  =====> EditorMenuFactory.EXPERIMENT
     *          Editor - Cv?         =====> EditorMenuFactory.TOPIC
     *          Editor - Protein     =====> EditorMenuFactory.PROTEIN
     * @return
     * @throws IntactException
     */
    protected Map<String,List<String>> getMenus(String editorPageName) throws IntactException {
        // The map containing the menus.
        Map<String,List<String>> map = new HashMap<String,List<String>>();

        // Handler to the menu factory.
        EditorMenuFactory menuFactory = EditorMenuFactory.getInstance();

        // The topic edit/add menu
        String name = EditorMenuFactory.TOPIC;
        List<String> menu = menuFactory.getMenu(name, 0);
        //Remove the non relevant terms from topic menu
        //menu = removeFromCvMenu(menu,editorPageName);
        map.put(name, menu);
        map.put(name + "_", menuFactory.convertToAddMenu(menu));

        // The database edit/add menu.
        name = EditorMenuFactory.DATABASE;
        menu = menuFactory.getMenu(name, 0);
        map.put(name, menu);
        map.put(name + "_", menuFactory.convertToAddMenu(menu));

        // The qualifier edit/add menu.
        name = EditorMenuFactory.QUALIFIER;
        menu = menuFactory.getMenu(name, 0);
        map.put(name, menu);
        map.put(name + "_", menuFactory.convertToAddMenu(menu));

        return map;
    }

   /**
     * This method is in charge to remove from the Topic menu list of annotation all the topics which are not relevant
     * for the considered  Editor page. For exemple, it wouldn't be correct to add to an experiment an annotation with
     * the topic equal to "isoform-annotation". This topic can only fit for a protein so it shouldn't appear in the
     * Experiment - Editor page.
     *
     * @param menu The general menu containing all the cvTopic
     * @param editorPageName The editor page name. This parameter is taken from EditorMenuFactory :
     *          Editor - Interaction =====> EditorMenuFactory.INTERACTION
     *          Editor - Experiment  =====> EditorMenuFactory.EXPERIMENT
     *          Editor - Cv?         =====> EditorMenuFactory.TOPIC
     *          Editor - Protein     =====> EditorMenuFactory.PROTEIN
     * @return  The new menu list
     */

    protected List removeFromCvMenu(List<String> menu, String editorPageName) throws IntactException {

        //  The annotationSection object contains 5 Maps associating each of the editor page to a List. Those lists
        //  contains the relevant cvTopics that can be used to annotate the considered edited object.

        AnnotationSection annotationSection = new AnnotationSection();
        if(annotationSection!=null){
            List<String> newMenulist = new ArrayList<String>();

            // This method return a list containing all the cvTopic that can be used to annotate the Edited object.
            // For expemple if you are in the BioSource Editor the returned list will contained : 'caution', 'remark-internal'
            // and 'url'
             List<String> cvTopicRessources = annotationSection.getUsableTopics(editorPageName);

            if(cvTopicRessources!=null){

                for (String menuElement : menu) {
                    menuElement= menuElement.trim().toLowerCase();
                    boolean removeElement=true;
                    for (String ressourceElement : cvTopicRessources) {
                         ressourceElement = ressourceElement.trim().toLowerCase();
                        if (ressourceElement.equals(menuElement))
                        {
                            removeElement = false;
                        }
                    }
                    if(!removeElement){
                        newMenulist.add(menuElement);
                    }

                }
                return newMenulist;
            }
            else return menu;

        }else return menu;

    }

    // Abstract method

    /**
     * Gathers values in the view bean and updates the existing AnnotatedObject
     * if it exists or create a new annotated object for the view and sets the
     * annotated object.
     * @throws IntactException for errors in searching the persistent system.
     *
     * <pre>
     * post: getAnnotatedObject() != null
     * </pre>
     */
    protected abstract void updateAnnotatedObject() throws
                                                                       IntactException, IntactException;

    // Helper Methods

    /**
     * Creates a collection of <code>CommentBean</code> created from given
     * collection of annotations.
     * @param annotations a collection of <code>Annotation</code> objects.
     */
    private void makeCommentBeans(Collection<Annotation> annotations) {
        // Clear previous annotations.
        myAnnotations.clear();
        for (Annotation annot : annotations) {
            myAnnotations.add(new CommentBean(annot));
        }
    }

    /**
     * Creates a collection of <code>XrefBean</code> created from given
     * collection of xreferences.
     * @param xrefs a collection of <code>Xref</code> objects.
     */
    private void makeXrefBeans(Collection<Xref> xrefs) {
        // Clear previous xrefs.
        myXrefs.clear();
        for (Xref xref : xrefs) {
            myXrefs.add(new XreferenceBean(xref));
        }
    }

    /**
     * Returns a collection of annotations to add.
     * @return the collection of annotations to add to the current CV object.
     * Could be empty if there are no annotations to add.
     *
     * <pre>
     * post: return->forall(obj: Object | obj.oclIsTypeOf(CommentBean)
     * </pre>
     */
    private Collection<CommentBean> getAnnotationsToAdd() {
        // Annotations common to both add and delete.
        Collection<CommentBean> common = CollectionUtils.intersection(myAnnotsToAdd, myAnnotsToDel);
        // All the annotations only found in annotations to add collection.
        return CollectionUtils.subtract(myAnnotsToAdd, common);
    }

    /**
     * Returns a collection of annotations to remove.
     * @return the collection of annotations to remove from the current CV object.
     * Could be empty if there are no annotations to delete.
     *
     * <pre>
     * post: return->forall(obj: Object | obj.oclIsTypeOf(CommentBean)
     * </pre>
     */
    private Collection<CommentBean> getAnnotationsToDel() {
        // Annotations common to both add and delete.
//        Collection<CommentBean> common = CollectionUtils.intersection(myAnnotsToAdd, myAnnotsToDel);
//        // All the annotations only found in annotations to delete collection.
//        return CollectionUtils.subtract(myAnnotsToDel, common);
        return myAnnotsToDel;
    }

    /**
     * Returns a collection of annotations to update.
     * @return the collection of annotations to update from the current CV object.
     * Could be empty if there are no annotations to update.
     *
     * <pre>
     * post: return->forall(obj: Object | obj.oclIsTypeOf(CommentBean)
     * </pre>
     */
    private Collection<CommentBean> getAnnotationsToUpdate() {
        return myAnnotsToUpdate;
    }

    /**
     * Returns a collection of xrefs to add.
     * @return the collection of xrefs to add to the current CV object.
     * Could be empty if there are no xrefs to add.
     *
     * <pre>
     * post: return->forall(obj: Object | obj.oclIsTypeOf(XreferenceBean))
     * </pre>
     */
    private Collection<XreferenceBean> getXrefsToAdd() {
        // Xrefs common to both add and delete.
        Collection<XreferenceBean> common = CollectionUtils.intersection(myXrefsToAdd, myXrefsToDel);
        // All the xrefs only found in xrefs to add collection.
        return CollectionUtils.subtract(myXrefsToAdd, common);
    }

    /**
     * Returns a collection of xrefs to remove.
     * @return the collection of xrefs to remove from the current CV object.
     * Could be empty if there are no xrefs to delete.
     *
     * <pre>
     * post: return->forall(obj: Object | obj.oclIsTypeOf(XreferenceBean))
     * </pre>
     */
    private Collection<XreferenceBean> getXrefsToDel() {
        return myXrefsToDel;
        // Xrefs common to both add and delete.
//        Collection<XreferenceBean> common = CollectionUtils.intersection(myXrefsToAdd, myXrefsToDel);
//        // All the xrefs only found in xrefs to delete collection.
//        return CollectionUtils.subtract(myXrefsToDel, common);
    }

    /**
     * Returns a collection of xrefs to update.
     * @return the collection of xrefs to update from the current CV object.
     * Could be empty if there are no xrefs to update.
     *
     * <pre>
     * post: return->forall(obj: Object | obj.oclIsTypeOf(XreferenceBean)
     * </pre>
     */
    private Collection<XreferenceBean> getXrefsToUpdate() {
        return myXrefsToUpdate;
    }

    /**
     * Clears annotations stored transaction containers.
     */
    private void clearTransAnnotations() {
        myAnnotsToAdd.clear();
        myAnnotsToDel.clear();
        myAnnotsToUpdate.clear();
    }

    /**
     * Clears xrefs stored in transaction containers.
     */
    private void clearTransXrefs() {
        myXrefsToAdd.clear();
        myXrefsToDel.clear();
        myXrefsToUpdate.clear();
    }

    /**
     * Removes the current short label from menus if the current edit type is a
     * menu type.
     * @throws IntactException for errors in accessing menus.
     */
    private void removeCurrentSLFromMenus() throws IntactException {
        EditorMenuFactory menuFactory = EditorMenuFactory.getInstance();
        if (!menuFactory.isMenuType(getEditClass())) {
            // Not a menu type.
            return;
        }
        // The map of menus to iterate.
        for (List<String> strings : getMenus().values())
        {
            // Remove my short label to avoid circular reference.
            strings.remove(getShortLabel());
        }
    }

    // Persist the current annotated object.

    private void persistCurrentView() throws IntactException {
        AnnotationDao annotationDao = DaoProvider.getDaoFactory().getAnnotationDao();
        // First create/update the annotated object by the view.
        updateAnnotatedObject();

        // Update the short label and full name as they are common to all.
        getAnnotatedObject().setShortLabel(getShortLabel());
        getAnnotatedObject().setFullName(getFullName());

        persistAnnotatedObject();

        log.debug("As I have persisted myAnnotObject ac is " + myAnnotObject.getAc());

        // Don't care whether annotated object exists or not because we don't
        // need an AC in the annotation table.

        // Update annotations; update the object with values from the bean.
        // The update of annotated object ensures the sub objects are updated as well.
        for (CommentBean commentBean : getAnnotationsToUpdate())
        {
            Annotation annot = commentBean.getAnnotation();
            Annotation correspondingAnnotation = getCorrespondingAnnotation(getAnnotatedObject(), annot);
            if(correspondingAnnotation == null){
                if(annotateOtherObject(annot)){
                    log.info("The annotation " + annot.getAc() + " is shared amongst several other object.");
                    //delAnnotation(commentBean);
                    getAnnotatedObject().removeAnnotation(annot);
                    Annotation newAnnot = createAnnotation(annot);
                    annotationDao.persist(newAnnot);
                    getAnnotatedObject().addAnnotation(newAnnot);
                    CommentBean newCb = new CommentBean(newAnnot);
                    addAnnotation(newCb);
                }else{
                    annotationDao.saveOrUpdate(annot);
                }
            }
        }

        // Delete annotations and remove them from CV object.
        for (CommentBean commentBean : getAnnotationsToDel())
        {
            Annotation annot = commentBean.getAnnotation();
            if(annotateOtherObject(annot)){
                log.info("We are going to unlink, the shared annotation "+ annot.getAc() + " from this annotated object.");
                getAnnotatedObject().removeAnnotation(annot);
            }else{
                log.error("Not shared annotation, we delete it.");
                annotationDao.delete(annot);
                getAnnotatedObject().removeAnnotation(annot);
            }

            Annotation correspondingAnnotation = getCorrespondingAnnotation(myAnnotObject, annot);
            if(correspondingAnnotation != null){
                log.debug("deleting annot");
                myAnnotObject.removeAnnotation(correspondingAnnotation);
            }
        }

        // Create annotations and add them to CV object.
        for (CommentBean commentBean : getAnnotationsToAdd())
        {
            Annotation annot = commentBean.getAnnotation();
            Annotation correspondingAnnotation = getCorrespondingAnnotation(myAnnotObject, annot);

            if(correspondingAnnotation == null){
                log.error("Add annot " +  annot.getAnnotationText());
                // Need this to generate the PK for the indirection table.
                annotationDao.persist(annot);
                getAnnotatedObject().addAnnotation(annot);
            }
        }
        // Xref has a parent_ac column which is not a foreign key. So, the parent needs
        // to be persistent before we can create the Xrefs.
        persistAnnotatedObject();
        // Create xrefs and add them to CV object.
        XrefDao xrefDao = DaoProvider.getDaoFactory().getXrefDao();
        Collection<XreferenceBean> xrefBeans = getXrefsToAdd();
        for (XreferenceBean xreferenceBean : getXrefsToAdd())
        {
            Xref xref = xreferenceBean.getXref(getAnnotatedObject());
            Xref correspondingXref = getCorrespondingXref(getAnnotatedObject(),xref);
            if(correspondingXref == null){
                if(xref == null){
                    log.debug ( "xref is null");
                }
                xrefDao.saveOrUpdate(xref);
                getAnnotatedObject().addXref(xref);
            }
        }
        // Delete xrefs and remove them from CV object.
        for (XreferenceBean xreferenceBean : getXrefsToDel())
        {
            Xref xref = xreferenceBean.getXref(getAnnotatedObject());

            Xref correspondingXref = getCorrespondingXref(getAnnotatedObject(),xref);
            if(correspondingXref != null){
                xref = null;
                correspondingXref.setParent(null);
                getAnnotatedObject().removeXref(correspondingXref);
                xrefDao.delete(correspondingXref);
            }
        }
        // Update xrefs; see the comments for annotation update above.
        for (XreferenceBean xreferenceBean : getXrefsToUpdate()) {
            Xref xref = xreferenceBean.getXref(getAnnotatedObject());
            Xref correspondingXref = getCorrespondingXref(getAnnotatedObject(),xref);
            if(correspondingXref == null){
                xrefDao.saveOrUpdate(xref);
            }
        }
        persistAnnotatedObject();
        // update the cvObject in the cvContext (application scope)
        if (getAnnotatedObject() instanceof CvObject) {
            IntactContext.getCurrentInstance().getCvContext().updateCvObject((CvObject)getAnnotatedObject());
            log.info("CvObject updated: "+myAnnotObject);
        }
    }



    
    /**
     *
     * @param annotatedObject
     * @param searchedAnnot
     * @return false if "annotatedObject" is null or if "searchedAnnot" is null or if  the annot is not already contained in the
     * collection of annotations of the "annotatedObject"
     *         true if the "annotatedObjec" has an annotation that as the same cv and the same description than "searchedAnnot"
     */
    public Annotation getCorrespondingAnnotation(AnnotatedObject annotatedObject, Annotation searchedAnnot){
        if(annotatedObject == null || searchedAnnot == null){
            return null;
        }
        Annotation correspondingAnnotation = null;
        Collection<Annotation> annotations = annotatedObject.getAnnotations();
        for(Annotation annotation : annotations){
            if(! annotation.getCvTopic().getAc().equals(searchedAnnot.getCvTopic().getAc())){
                continue;
            }else if (! ((annotation.getAnnotationText() == null && searchedAnnot.getAnnotationText() == null) ||
                    (annotation.getAnnotationText() != null && annotation.getAnnotationText().equals(searchedAnnot.getAnnotationText())))){
                continue;
            } else {
                correspondingAnnotation = annotation;
                break;
            }
        }
        return correspondingAnnotation;
    }

    /**
     * It returns the annotatedObject xref corresponding to the searchedXref :
     *      ** if the searched xref has an ac, it will search for the xref having the same ac, if found, the ac will
     *      xref will set with the value of the searchedXref
     *      ** if the searched xref has no ac, it will try to find if the annotatedObject has an xref with the same value
     *      as the searchedXref (same primaryId, same secondaryId, same cvDatabase and same cvXrefQualifier)
     * If nothing is found it returns null.
     * It will also return null if the annotatedObject of the searchedXref is null.
     * @param annotatedObject an AnnotatedObject
     * @param searchedXref an Xref
     * @return and xref of null
     */
    public Xref getCorrespondingXref(AnnotatedObject annotatedObject, Xref searchedXref){
        if(annotatedObject == null || searchedXref == null){
            return null;
        }

        Xref correspondingXref = null;
        Collection<Xref> xrefs =  annotatedObject.getXrefs();
        String searchedAc = searchedXref.getAc();
        for( Xref xref : xrefs){
            if(xref.getAc().equals(searchedAc)){
                log.debug("We found an xref with the same ac");
                xref.setPrimaryId(searchedXref.getPrimaryId());
                xref.setSecondaryId(searchedXref.getSecondaryId());
                xref.setCvDatabase(searchedXref.getCvDatabase());
                xref.setCvXrefQualifier(searchedXref.getCvXrefQualifier());
                return xref;
            }

        }

        for( Xref xref : xrefs){
            if( !((xref.getPrimaryId() == null && searchedXref.getPrimaryId() == null) ||
                    (xref.getPrimaryId()!= null && xref.getPrimaryId().equals(searchedXref.getPrimaryId()))) ) {
                continue;
            } else if( !((xref.getSecondaryId() == null && searchedXref.getSecondaryId() == null) ||
                    (xref.getSecondaryId()!= null && xref.getSecondaryId().equals(searchedXref.getSecondaryId()))) ) {
                continue;
            }else if( !((xref.getCvDatabase() == null && searchedXref.getCvDatabase() == null) ||
                    (xref.getCvDatabase()!= null && xref.getCvDatabase().getAc().equals(searchedXref.getCvDatabase().getAc()))) ) {
                continue;
            } else if( !((xref.getCvXrefQualifier() == null && searchedXref.getCvXrefQualifier() == null) ||
                    (xref.getCvXrefQualifier()!= null && xref.getCvXrefQualifier().getAc().equals(searchedXref.getCvXrefQualifier().getAc()))) ) {
                continue;
            }else{
                correspondingXref = xref;
                break;
            }
        }
        return correspondingXref;
    }

    /**
     * Persist myAnnotObject in the database if it does not have an ac, update it if it has an ac.
     */
    public void persistAnnotatedObject(){
        boolean isPersisted =  false;
        getAnnotatedObject();
        if( getAnnotatedObject() != null && getAnnotatedObject().getAc() != null && (!"".equals(getAnnotatedObject().getAc().trim()))){
            isPersisted = true;
        }
        AnnotatedObjectDao annotatedObjectDao = DaoProvider.getDaoFactory(getAnnotatedObject().getClass());
        if( isPersisted ){
            annotatedObjectDao.saveOrUpdate(getAnnotatedObject());
        }else{
            annotatedObjectDao.persist(getAnnotatedObject());
        }
    }

    private void resetAnnotatedObject(T annobj) {
        // Need to get the real object for a proxy type.
        if (Polymer.class.isAssignableFrom(annobj.getClass())) {
            setAnnotatedObject( /*helper.materializeIntactObject(*/annobj/*)*/);
            myEditClass = (Class<T>)getAnnotatedObject().getClass();
        }
        else {
            setAnnotatedObject(annobj);
            myEditClass = CgLibUtil.getRealClassName(annobj);
        }
        setFullName(annobj.getFullName());
    }

    private boolean equals(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) {
            return true;
        }
        if (obj1 != null) {
            return obj1.equals(obj2);
        }
        return false;
    }

    /**
     * It returns true if the annotation given as a paremeter annotated other object false otherwise.
     * @param annot
     * @return
     * @throws IntactException
     */
    public boolean annotateOtherObject(Annotation annot) throws IntactException {

        if(annot!=null && annot.getAc()!=null){
            String annotationAc = annot.getAc();
            int num = 0;

            ExperimentDao experimentDao = DaoProvider.getDaoFactory().getExperimentDao();
            num = num + experimentDao.getByAnnotationAc(annotationAc).size();

            BioSourceDao bioSourceDao = DaoProvider.getDaoFactory().getBioSourceDao();
            num = num + bioSourceDao.getByAnnotationAc(annotationAc).size();

            InteractorDao interactorDao = DaoProvider.getDaoFactory().getInteractorDao();
            num = num + interactorDao.getByAnnotationAc(annotationAc).size();

            FeatureDao featureDao = DaoProvider.getDaoFactory().getFeatureDao();
            num = num + featureDao.getByAnnotationAc(annotationAc).size();

            CvObjectDao<CvObject> cvObjectDao = DaoProvider.getDaoFactory().getCvObjectDao(CvObject.class);
            num = num + cvObjectDao.getByAnnotationAc(annotationAc).size();

            if(num > 1){
                log.error("Annotation[" + annot.getAc() + "," + annot.getCvTopic().getShortLabel() + "] is " +
                        "persistant and annotate and annotate an other object");
                return true;
            } else {
                log.error("Annotation[" + annot.getAc() + "," + annot.getCvTopic().getShortLabel() + "] is " +
                        "persistant and annotate and do not annotate an other object");
                return false;
            }

        }

        log.error("Annotation[" + annot.getAc() + "," + annot.getCvTopic().getShortLabel() + "] is not " +
                        "persistant");
        return false;
    }

    public Annotation createAnnotation(Annotation annotation){
        return new Annotation(annotation.getOwner(),annotation.getCvTopic(), annotation.getAnnotationText());
    }
}
