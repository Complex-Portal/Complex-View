/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.imex.idassigner.update;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.imex.idassigner.helpers.CvHelper;
import uk.ac.ebi.intact.imex.idassigner.helpers.ExperimentHelper;
import uk.ac.ebi.intact.imex.idassigner.helpers.InteractionHelper;
import uk.ac.ebi.intact.imex.idassigner.helpers.PublicationHelper;
import uk.ac.ebi.intact.imex.idassigner.id.IMExIdTransformer;
import uk.ac.ebi.intact.imex.idassigner.id.IMExRange;
import uk.ac.ebi.intact.imex.idassigner.keyassigner.KeyAssignerService;
import uk.ac.ebi.intact.imex.idassigner.keyassigner.KeyAssignerServiceException;
import uk.ac.ebi.intact.imex.idassigner.keyassigner.KeyAssignerServiceI;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.persistence.dao.ExperimentDao;
import uk.ac.ebi.intact.persistence.dao.PublicationDao;
import uk.ac.ebi.intact.persistence.dao.XrefDao;

import java.util.*;

/**
 * Update of a Publication.
 * <p/>
 * It updates all interactions of a given publication with a range of IMEx IDs.
 * <p/>
 * Imex IDs are retreived using a tool called IMEx Key Assigner (accessible via web service)
 * <p/>
 * During the process, the publication gets updated so we can track: <ul> <li> Ranges of IMEx ID requested from the Key
 * Assigner  </li> <li> Ranges of IMEx id assigned to interactions. </li> </ul> A single range will be used as long as
 * the set is continuous.
 * <p/>
 * <p/>
 * <p/>
 * Algorithm
 * <pre>
 * </pre>
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>11-May-2006</pre>
 */
public class UpdatePublication {

    public static final Log log = LogFactory.getLog( UpdatePublication.class );

    /**
     * Threshold above which we will request 50 additional IMEx IDs.
     */
    private static final int LARGE_PUBLICATION_THRESHOLD = 1000;
    private static final int SMALL_PUBLICATION_MARGIN = 10;
    private static final int LARGE_PUBLICATION_MARGIN = 50;


    private final KeyAssignerServiceI keyAssignerService;

    public UpdatePublication( KeyAssignerServiceI keyAssignerService ) {
        if ( keyAssignerService == null ) {
            throw new NullPointerException( "keyAssignerService must not be null." );
        }
        this.keyAssignerService = keyAssignerService;
    }

    ///////////////////////////////
    // Utility methods

    private void displayInteractors( Collection<Interactor> interactors ) {
        System.out.print( "[" );
        System.out.flush();
        for ( Iterator<Interactor> iterator = interactors.iterator(); iterator.hasNext(); ) {
            Interactor interactor = iterator.next();

            System.out.print( interactor.getShortLabel() );
            System.out.flush();

            if ( iterator.hasNext() ) {
                System.out.print( ", " );
            }
        }
        log.debug( "]" );
    }

    /**
     * Builds a Map modeling the assignment of IMEx id to set of interactions.
     * <p/>
     * Reminder: in the scope of a Publication, an IMEx ID is specific to a set of Interactor upon which an interaction
     * is built.
     *
     * @param experiments the collection of experiment from which we get the interactions.
     *
     * @return a non null map.
     */
    private Map<InteractionClassifier, Set<Interaction>> build( Collection<Experiment> experiments ) {

        Map<InteractionClassifier, Set<Interaction>> map = new HashMap<InteractionClassifier, Set<Interaction>>();

        for ( Experiment experiment : experiments ) {

            for ( Interaction interaction : experiment.getInteractions() ) {

                Set<Interactor> interactors = InteractionHelper.selectDistinctInteractors( interaction );

                InteractionClassifier ic = new InteractionClassifier( interactors );

                log.debug( interaction.getAc() + " " + interaction.getShortLabel() );
                displayInteractors( interactors );

                Set<Interaction> relatedInteractions = null;

                if ( map.containsKey( ic ) ) {

                    // we already came across that combination of interactors
                    log.debug( "Was already in ..." );
                    relatedInteractions = map.get( ic );

                    // if the existing InteractionClassifier doesn't have an IMExId, we

                } else {
                    // create a new entry
                    relatedInteractions = new HashSet<Interaction>();
                    map.put( ic, relatedInteractions );
                }

                // add the current interaction in the set related to the set of Interactor.
                relatedInteractions.add( interaction );
            }
        }

        return map;
    }

    private int getInteractionCount( Collection<Experiment> experiments ) {

        Set<Interaction> interactions = new HashSet<Interaction>();

        for ( Experiment experiment : experiments ) {
            for ( Interaction interaction : experiment.getInteractions() ) {
                interactions.add( interaction );
            }
        }

        return interactions.size();
    }

    /**
     * Updates an IntAct publication according to IMEx Standards.
     *
     * @param pmid the pubmed id describing the publication.
     *
     * @throws IntactException
     * @throws KeyAssignerServiceException
     */
    public void update( String pmid ) throws IntactException, KeyAssignerServiceException {
        try {
//            log.debug( "Database: " + helper.getDbName() );

            // load necessary CVs - if one is not found, the program stops here...
            final CvDatabase pubmed = CvHelper.getPubmed();
            final CvDatabase imex = CvHelper.getImex();
            final CvDatabase intact = CvHelper.getIntact();
            final CvXrefQualifier primaryReference = CvHelper.getPrimaryReference();
            final CvXrefQualifier imexPrimary = CvHelper.getImexPrimary();
            final CvInteractorType proteinType = CvHelper.getProteinType();
            final CvTopic imexRangeRequested = CvHelper.getImexRangeRequested();
            final CvTopic imexRangeAssigned = CvHelper.getImexRangeAssigned();

            // load the publication
            Publication publication = PublicationHelper.loadPublication( pmid );

            if ( publication == null ) {

                log.debug( "Could not find that publication in the database." );

                Institution owner = IntactContext.getCurrentInstance().getConfig().getInstitution();

                // create a new publication
                publication = new Publication( owner, pmid );
                log.debug( "Creating new publication (" + pmid + ")..." );
                DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
                PublicationDao dao = daoFactory.getPublicationDao();
                dao.persist( publication );
                log.debug( "done." );

                // add Xref to it too
                PublicationXref xref = new PublicationXref( owner, pubmed, pmid, primaryReference );
                publication.addXref( xref );
                XrefDao xdao = daoFactory.getXrefDao();
                xdao.persist( xref );
            }

            // Retreive related experiments
            log.debug( "Searching for corresponding experiments..." );
            Collection<Experiment> experiments = ExperimentHelper.searchByPrimaryReference( pmid );
            log.debug( experiments.size() + " experiment(sortedClassifier) found." );

            // update all of these experiment
            for ( Experiment experiment : experiments ) {
                if ( experiment.getPublication() == null ) {
                    log.debug( "Experiment[ " + experiment.getShortLabel() + " ] doesn't have a publication assigned. Fixing it now ..." );
                    experiment.setPublication( publication );

                    DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
                    ExperimentDao edao = daoFactory.getExperimentDao();

                    edao.update( experiment );
                    log.debug( "Done." );
                }
            }

            // Check that all of these experiments have been accepted
            boolean allAccepted = ExperimentHelper.areAccepted( experiments, true );

            if ( !allAccepted ) {
                log.debug( "Not all experiment were accepted. abort." );

                // terminate the process
                System.exit( 1 );
            }

            // Build interaction index
            log.debug( "Building interaction index ..." );

            // TODO what if some of the interactions already have an IMEx ID ?
            //      check on the publication if there is a imex-range-requested and if
            //      the imex-range-assigned has covered the whole space.
            //      (!) the key of the Map should be: Set<Interactor> + IMEx ID (that can be null)
            //      (!) We may end up with multiple request for a single publication

            //      Solution:
            //        1. Assess how many IMEx ID have been requested already
            //        2. Assess which ID have already been assigned
            //        3. Assess how many IMEx ID are required to update that publication
            //        4. only if we cannot cope with the number, request the missing range + margin.
            //        5. Assign IMEx IDs to interactions that do not have one yet.

            Map<InteractionClassifier, Set<Interaction>> map = build( experiments );
            int interactionCount = getInteractionCount( experiments );
            log.debug( "Found " + interactionCount + " interaction" + ( interactionCount > 1 ? "s" : "" ) + "." );

            // Note: initialy, we only export interaction involving proteins.
            // Sort Interactions so PPI come first, that way, the exported set to IMEx is continuous (eg. 33..56)
            List<InteractionClassifier> sortedClassifier = new ArrayList<InteractionClassifier>( map.keySet() );
            Collections.sort( ( List ) sortedClassifier, new Comparator<InteractionClassifier>() {

                /**
                 * Sets with proteins only should come out first.
                 *
                 * @param c1 wrapper on a set of interactor.
                 * @param c2 other wrapper on a set of interactor.
                 *
                 * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to,
                 *         or greater than the second.
                 */
                public int compare( InteractionClassifier c1, InteractionClassifier c2 ) {

                    boolean set1hasOnlyProteins = hasOnlyProteins( c1.getDistinctInteractors() );
                    boolean set2hasOnlyProteins = hasOnlyProteins( c2.getDistinctInteractors() );

                    if ( set1hasOnlyProteins && set2hasOnlyProteins ) {
                        return 0;
                    } else if ( set1hasOnlyProteins ) {
                        return -1; // set1 comes first
                    } else {
                        return 1;  // set2 comes first
                    }
                }

                /**
                 * check weither all Interactors in the set are Proteins.
                 *
                 * @param set the set of Interactor to test.
                 *
                 * @return true of all are Protein, false otherwise.
                 */
                private boolean hasOnlyProteins( Set<Interactor> set ) {

                    if ( set.isEmpty() ) {
                        throw new IllegalStateException( "The set of Interactors must not be empty." );
                    }

                    for ( Interactor interactor : set ) {
                        if ( !proteinType.equals( interactor.getCvInteractorType() ) ) {
                            return false;
                        }
                    }
                    return true;
                }
            } );

            boolean moreThanOneImexId = false;
            int imexIdMissing = 0;
            Set<String> assignedIMExIds = new HashSet<String>();

            // Calculating the count of IMEx ID needed to update that publication.
            for ( Map.Entry<InteractionClassifier, Set<Interaction>> entry : map.entrySet() ) {
                InteractionClassifier classifier = entry.getKey();
                Set<Interaction> interactions = entry.getValue();

                boolean foundImexId = false;

                for ( Interaction interaction : interactions ) {

                    String imexId = InteractionHelper.getIMExId( interaction );

                    if ( imexId != null ) {

                        foundImexId = true;
                        assignedIMExIds.add( imexId );

                        if ( classifier.getImexId() == null ) {

                            // no IMEx id stored yet, update the qualifier
                            classifier.setImexId( imexId );

                        } else {

                            if ( !classifier.getImexId().equals( imexId ) ) {

                                log.debug( "ERROR - More than one IMEx ID assigned to that set of interactor: " +
                                           imexId + " and " + classifier.getImexId() );
                                displayInteractors( classifier.getDistinctInteractors() );

                                moreThanOneImexId = true; // processing will stop after that loop.
                            }
                        }
                    }
                } // interactions

                // if none of the interaction has an IMEx ID, we need to request one.
                if ( !foundImexId ) {
                    imexIdMissing++;
                }
            } // entries of the map


            if ( moreThanOneImexId ) {
                log.debug( "We found more than one IMEx ID in use for a same set of Interactors. This needs to be fixed. Abort." );
                System.exit( 1 );
            }

            // Now calculate how many we already have requested in that publication
            // amongst which how many have already been assigned
            // that gives us, how many we can use without requesting IDs to Key Assigner.

            // Build a list of all requested IMEx Ids (not formatted)
            List<IMExRange> requestedRanges = PublicationHelper.getRequestedRanges( publication );
            List<Long> requestedIds = new ArrayList<Long>();
            for ( IMExRange range : requestedRanges ) {
                requestedIds.addAll( IMExIdTransformer.getUnformattedIMExIds( range ) );
            }

            // Build a list of all assigned IMEx ids in the scope of that publication (not formatted)
            Collection<Long> assignedIds = IMExIdTransformer.parseIMExIds( assignedIMExIds );

            // build the list of non assigned IDs
            Collection<Long> nonAssignedIds = CollectionUtils.subtract( requestedIds, assignedIds );
            Collection<String> freeImexIds = IMExIdTransformer.formatIMExIds( nonAssignedIds );

            log.debug( "We have " + freeImexIds.size() + " free IMEx IDs..." );
            for ( String id : freeImexIds ) {
                log.debug( id );
            }

            // Evaluate how many IMEx IDs are necessary
            int howManyToRequestFromKeyAssigner = imexIdMissing - freeImexIds.size();

            if ( howManyToRequestFromKeyAssigner > 0 ) {

                // Request ID from Key Assigner
                log.debug( "We need to request " + howManyToRequestFromKeyAssigner + " IMEx ID" +
                           ( howManyToRequestFromKeyAssigner > 1 ? "sortedClassifier" : "" ) + " from the Key Assigner." );

                // Extend slightly the IMEx Range so we keep a margin in case we add interactions later.
                if ( interactionCount < LARGE_PUBLICATION_THRESHOLD ) {
                    log.debug( "Dataset has less than " + LARGE_PUBLICATION_THRESHOLD + " interactions, request " +
                               SMALL_PUBLICATION_MARGIN + " additional IDs" );
                    howManyToRequestFromKeyAssigner += SMALL_PUBLICATION_MARGIN;
                } else {
                    log.debug( "Dataset has at least " + LARGE_PUBLICATION_THRESHOLD + " interactions, request " +
                               LARGE_PUBLICATION_MARGIN + " additional IDs" );
                    howManyToRequestFromKeyAssigner += LARGE_PUBLICATION_MARGIN;
                }

                // Request IMEx IDs from Key Assigner
                IMExRange imexRange = keyAssignerService.getAccessions( howManyToRequestFromKeyAssigner );

                // Update Publication: store the Key Assigner range requested
                PublicationHelper.addRequestedAnnotation( publication, imexRange );

                Collection<String> imexIDs = IMExIdTransformer.getFormattedIMExIds( imexRange );

                // top up the collection of IMEx ID free for assignement
                freeImexIds.addAll( imexIDs );

            } else {

                log.debug( "Reuse previously requested IDs..." );
            }

            // Assign IMEx IDs to Interactions
            // Keep a sorted collection of ID and do, get First, get last (3..5 / 10..13)
            for ( InteractionClassifier classifier : sortedClassifier ) {

                // reminder - if any of the respective interaction has already an IMEx id, we stored it in the classifier.
                String imexId = classifier.getImexId();

                if ( imexId == null ) {
                    // get it from the pool of free IMEx ID
                    Iterator<String> iterator = freeImexIds.iterator();

                    imexId = iterator.next();

                    iterator.remove(); // remove it from the pool
                }

                Set<Interaction> interactions = map.get( classifier );

                // update all interactions
                for ( Interaction interaction : interactions ) {

                    // add the interaction's AC as Xref( CvDatabase( IntAct ), CvXrefQUalifier( imex-primary ) )
                    InteractionHelper.addIMExPrimary( interaction );

                    String id = InteractionHelper.getIMExId( interaction );

                    if ( id == null ) {

                        InteractionHelper.addIMExId( interaction, imexId );
                        assignedIMExIds.add( imexId );

                    } else {

                        log.debug( "Interaction " + interaction.getAc() + " " + interaction.getShortLabel() +
                                   " had already an IMEx ID: " + id + "." );
                        assignedIMExIds.add( id );
                    }
                } // interactions
            } // classifiers

            log.debug( "There are " + freeImexIds.size() + " ID" + ( freeImexIds.size() > 1 ? "s" : "" ) +
                       " non assigned after finishing updating the publication." );

            // Update Publication: store the Key Assigner range assigned
            log.debug( "Updating the assigned IDs on the publication..." );
            Collection<Long> unformattedIds = IMExIdTransformer.parseIMExIds( assignedIMExIds );
            List<IMExRange> assignedRanges = IMExIdTransformer.buildRanges( unformattedIds );

            for ( IMExRange assignedRange : assignedRanges ) {
                PublicationHelper.addAssignedAnnotation( publication, assignedRange );
            }

            log.debug( "Publication( " + PublicationHelper.getPubmedId( publication ) + " ) update completed." );

        } finally {
        }
    }

    // TODO put a Publication as input parameter.

    //////////////////
    // M A I N

    public static void main( String[] args ) throws IntactException, KeyAssignerServiceException {

        UpdatePublication updator = new UpdatePublication( new KeyAssignerService() );

        // First IMEx export
//        updator.update( "16470656" );
        updator.update( "16267818" );

        // Other Tests
//        updator.update( "16469704" );
//        updator.update( "16564010" );
//        updator.update( "16564014" );
//        updator.update( "16294310" );

    }
}