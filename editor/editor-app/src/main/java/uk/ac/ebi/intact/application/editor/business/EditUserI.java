/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.business;

import uk.ac.ebi.intact.application.editor.struts.framework.util.AbstractEditViewBean;
import uk.ac.ebi.intact.application.editor.struts.view.experiment.InteractionRowData;
import uk.ac.ebi.intact.application.editor.struts.view.interaction.ExperimentRowData;
import uk.ac.ebi.intact.application.editor.struts.view.wrappers.ResultRowData;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.searchengine.ResultWrapper;
import uk.ac.ebi.intact.searchengine.business.IntactUserI;
import uk.ac.ebi.intact.util.NewtServerProxy;
import uk.ac.ebi.intact.util.go.GoServerProxy;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Provides methods specific to a user editing an Annotated object.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public interface EditUserI extends IntactUserI, Serializable {

    // Getter/Setter for topic selected.
    public String getSelectedTopic();
    public void setSelectedTopic(String topic);

    /**
     * Returns the state of editing.
     * @return <code>true</code> if the user is in edit screen;
     * <code>false</code> is returned for all other instances.
     */
    public boolean isEditing();

    /**
     * Starts editing session. This is needed for Save & Continue operation as
     * editing is turned off automatically upon comitting.
     */
    public void startEditing();

    // Transaction Methods
    public void rollback() throws IntactException;

    // Persistent Methods

    /**
     * This method clears the view of the current edit object, remove it from
     * the search cache, deletes from the experiment list (if the current edit
     * is an instance of an Experiment class),  tand finally delete the current
     * edit object.
     * @exception IntactException for errors in deleting the current edit object.
     */
    public void delete() throws IntactException;

    public void cancelEdit();

    // The current view.

    /**
     * Returns the user's current edit view.
     * @return user's current edit view.
     */
    public AbstractEditViewBean getView();

    /**
     * Releases the current view back to the pool. Once this method is called
     * the view is unusable till a new view is set again. Only do this as the
     * last item in a series of actions to do.
     */
    public void releaseView();
    
    /**
     * Sets the current view to the Interaction editor. This is used situation
     * where the last edited state is restored without reinitialising the view.
     * For example, the Interaction editor state is restored when returning from the
     * Feature editor.
     * @param view the view to set as the current interaction view.
     */
    public void setView(AbstractEditViewBean view);

    /**
     * This method is equivalent to calling {@link #setView(Class, boolean)} method
     * with release parameter set to true (release the current view).
     */
    public void setView(Class clazz);

    /**
     * Sets the current view for given class. This method is only used in when
     * creating a new object. For editing an existing object, please use
     * {@link #setView(uk.ac.ebi.intact.model.AnnotatedObject)} method.
     * @param clazz The class type is used when creating a view for a new object.
     * @param release true to release the current view. The current view is
     * stored on the view stack if this parameter is false
     */
    public void setView(Class clazz, boolean release);

    /**
     * This method is equivalent to calling {@link #setView(AnnotatedObject, boolean)}
     * method with release parameter set to true (release the current view).
     */
    public void setView(AnnotatedObject annobj) throws IntactException;

    /**
     * Sets the current view with given Annotated object. For creating a new
     * object, please use {@link #setView(Class)} method.
     * @param annobj the annotated object to set as the current view.
     * @param release true to release the current view. The current view is
     * stored on the view stack if this parameter is false
     */
    public void setView(AnnotatedObject annobj, boolean release) throws IntactException;

    /**
     * Sets the view as a cloned object. This is different to
     * {@link #setView(AnnotatedObject)}. For example a cloned object needs to set all
     * the annotations as new annotations to add for the persistence to work
     * correctly (these annotations need to be created first).
     * @param obj the cloned object.
     */
    public void setClonedView(AnnotatedObject obj, String originalAc) throws IntactException;

    /**
     * Restores the previously stored on the stack. This is a convenient method
     * to restore previous view without any updates to it. For example, this method
     * is called upon cancelling the current view.
     * @return true if the previous view was restored as the current. False is
     * returned otherwise including where there is no view to go back to (view stack
     * is empty).
     */
    public boolean restorePreviousView();

    /**
     * Pops the previous view from the stack. The current view is not changed.
     * @return the previous view from the top of the stack. A null object is returned
     * if the view stack is empty.
     */
    public AbstractEditViewBean popPreviousView();

    /**
     * Peeks at the top element of the stack without removing it. The current view
     * is not changed.
     * @return the view at the top of the view stack without removing it. A null
     * object is returned if the view stack is empty.
     */
    public AbstractEditViewBean peekPreviousView();

    /**
     * Returns the status of the view stack.
     * @return true if there is a view on the stack.
     */
    public boolean hasPreviousView();

    // Search methods

    /**
     * Gets SPTR Proteins via SRS.
     * @param pid the primary id to search for.
     * @param max the maximum number of proteins allowed.
     * @return a wrapper containing<code>Protein</code> instances for <code>pid</code>.
     */
//    public ResultWrapper getSPTRProteins(String pid, int max) throws IntactException;

    /**
     * Returns the last protein parse exception.
     * @return the last protein parse exception`.
     */
//    public Exception getProteinParseException();

    /**
     * Utility method to handle the logic for lookup.
     *
     * @param clazz the intact class type to search on
     * @param value the user-specified value.
     * @param max the maximum number of items to retrieve.
     * @return the result wrapper which contains the result of the search.
     * @throws IntactException for errors in searching for persistent system. This
     * is not thrown if the search produces no output.
     */
    public ResultWrapper lookup(Class clazz, String param, String value, int max)
            throws IntactException;

    /**
     * Collection of AnnotatedObjects to add to the search cache. Previous
     * results are removed before adding the new result.
     * @param results a collection of <code>AnnotatedObjects</code> from
     * the search.
     *
     * <pre>
     * pre: results->forall(obj: Object | obj.oclIsTypeOf(AnnotatedObjects))
     * </pre>
     */
    public void addToSearchCache(Collection<ResultRowData> results);

    /**
     * Clears existing search cache and replace it with given bean.
     * @param annotobj the AnnotatedObject to set as the search cache.
     */
    public void updateSearchCache(AnnotatedObject annotobj);

    /**
     * Check for duplicity of short label for the current edit object.
     * @param shortlabel the short label to check for duplicity.
     * @return true if <code>shortlabel</code> already exists (for the current edit object)
     * in the database.
     * @exception IntactException for errors in acccessing the database.
     */
    public boolean shortLabelExists(String shortlabel) throws IntactException;

    /**
     * Returns the next available short label from the persistent system.
     * @param clazz the calss or the type for the search.
     * @param label the starting short label; this must contain the suffix '-x'
     * @return the next available short label from the persistent system. Basically,
     * this takes the form of <code>label</code> with -x substituted with the last
     * persistent number. For example, it could be abc-2 provided that <code>label</code>
     * is abc-x and abc-1 is the last persistent suffix. This could be as same
     * as <code>label</code> if there is an error in accessing the database to
     * access other similar objects. Null is returned if <code>label</code> has
     * invalid format.
     */
    public String getNextAvailableShortLabel(Class<? extends AnnotatedObject> clazz, String label);

    /**
     * Returns the search result as a list.
     * @return the search result; an empty list is returned if there are no search
     * results.
     *
     * <pre>
     * post: return != Null
     * post: return->forall(obj: Object | obj.oclIsTypeOf(ResultBean))
     * </pre>
     */
    public List getSearchResult();

    /**
     * Returns the Newt server proxy assigned for the current session.
     * @return an instance of Newt server. A new instance is created if no server
     * is created for the current session.
     */
    public NewtServerProxy getNewtProxy();

    /**
     * Returns the Go server proxy assigned for the current session.
     * @return an instance of Go server. A new instance is created if no server
     * is created for the current session.
     */
    public GoServerProxy getGoProxy();

    /**
     * Returns the help tag for the current view bean.
     * @return the help tag for the current view bean as a String object.
     */
    public String getHelpTag();

    // Session methods

    /**
     * Adds the experiment to the currently edited/added experiment list.
     * @param row the row to add to the current experiment list.
     */
    public void addToCurrentExperiment(ExperimentRowData row);

    /**
     * Removes the current experiment from the currently edited/added
     * experiment list.
     * @param row the row to remove from the current experiment list.
     */
    public void removeFromCurrentExperiment(ExperimentRowData row);

    /**
     * Returns a list of currently edited/added experiments.
     * @return a set consists currently edited/added experiments.
     * An empty set is returned if there are no
     * experiments added or edited during the current session.
     *
     * <pre>
     * post: results->forall(obj: Object | obj.oclIsTypeOf(ExperimentRowData))
     * </pre>
     */
    public Set<ExperimentRowData> getCurrentExperiments();

    /**
     * Adds the AC as the currently edited/added interaction.
     * @param row the row to add to the current interaction list.
     */
    public void addToCurrentInteraction(InteractionRowData row);

    /**
     * Removes the interaction for given ac from the currently edited/added
     * interaction list.
     * @param row the row to remove from the current interaction list.
     */
    public void removeFromCurrentInteraction(InteractionRowData row);

    /**
     * Returns a list of currently edited/added interaction data rows.
     * @return a set consists currently edited/added interactions.
     * An empty set is returned if there are no interactions added or edited
     * during the current session.
     *
     * <pre>
     * post: results->forall(obj: Object | obj.oclIsTypeOf(InteractionRowData))
     * </pre>
     */
    public Set<InteractionRowData> getCurrentInteractions();
}
