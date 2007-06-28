package uk.ac.ebi.intact.searchengine.util;

import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.searchengine.lucene.model.*;

import java.util.Collection;

/**
 * Defines the requirements for a Searching.
 *
 * @author Anja Friedrichsen
 * @version $Id$
 */
public interface SearchObjectProvider {

    /**
     * This method searches the database for a specific object with the given accession number and object class. It is
     * used to update the index.
     *
     * @param ac       accession number of the object to be found
     * @param objClass class of the object to be found
     *
     * @return the fetched IntAct object
     *
     * @throws IntactException
     */
    public SearchObject getSearchObject( String ac, String objClass ) throws IntactException;

    /**
     * This method selects all experiment (with ac, shortlabel, fullname , objclass, xref, alias and annotation) out of
     * the database and creates a new ExperimentSearchObject for each Experiment. Every Experiment is going to be a
     * document in the lucene index. Additional to the basic attributes (ac, shortlabel, fullname, objclass, xref, alias
     * and annotation) the CvInteraction and the CvIdentification is selected for every experiment.
     *
     * @param sqlQuery an SQL query.
     *
     * @return a collection containing all ExperimentSearchObjects to create a lucene index of
     *
     * @throws IntactException
     */
    public Collection<ExperimentSearchObject> getAllExperiments( String sqlQuery ) throws IntactException;

    /**
     * This method selects all Interactions (with: ac, shortlabel, fullname, objclass, xref, alias and annotation) out
     * of the database and creates a new InteractionSearchObject for every Interaction. Every InteractionSearchObject is
     * going to be a single lucene document. Additional to the basic attributes the interaction type belonging to the
     * specific Interaction is fetched out of the database.
     *
     * @param sqlQuery an SQL query.
     *
     * @return a collection of InteractionSearchObjects to be inserted into the lucene index
     *
     * @throws IntactException
     */
    public Collection<InteractionSearchObject> getAllInteractions( String sqlQuery ) throws IntactException;

    /**
     * This method selects all Proteins (with: ac, shortlabel, fullname, objclass, xref, alias and annotation) out of
     * the database and creates a new ProteinSearchObject for every Protein. Every ProteinSearchObject is going to be a
     * single lucene document.
     *
     * @param sqlQuery an SQL query.
     *
     * @return a collection containing all proteins to be indexed with lucene
     *
     * @throws IntactException
     */
    public Collection<ProteinSearchObject> getAllProteins( String sqlQuery ) throws IntactException;

    /**
     * This method selects all CvObjects (with: ac, shortlabel, fullname, objclass, xref, alias and annotation) out of
     * the database and creates a new CvSearchObject for every CvObject. Every CvSearchObject is going to be a single
     * lucene document.
     *
     * @param sqlQuery an SQL query.
     *
     * @return a collection of CvSearchObject which are going to be indexed with lucene
     *
     * @throws IntactException
     */
    public Collection<CvSearchObject> getAllCvObjects( String sqlQuery ) throws IntactException;

    /**
     * This method selects all Biosources (with: ac, shortlabel, fullname, objclass, xref, alias and annotation) out of
     * the database and creates a new BiosourceSearchObject for every Biosource. Every BiosourceSearchObject is going to
     * be a single lucene document.
     *
     * @param sqlQuery an SQL query.
     *
     * @return a collection of BioSource, can be empty, never null.
     *
     * @throws IntactException
     */
    public Collection<BioSourceSearchObject> getAllBioSources( String sqlQuery ) throws IntactException;
}