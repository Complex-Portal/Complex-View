/**
 * Copyright 2007 The European Bioinformatics Institute, and others.
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
 *  limitations under the License.
 */
package uk.ac.ebi.intact.application.hierarchview.business.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.processor.ClusterInteractorPairProcessor;
import uk.ac.ebi.intact.application.hierarchview.business.graph.HVNetworkBuilder;
import uk.ac.ebi.intact.application.hierarchview.exception.HierarchViewDataException;
import uk.ac.ebi.intact.application.hierarchview.exception.MultipleResultException;
import uk.ac.ebi.intact.application.hierarchview.exception.ProteinNotFoundException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.Interactor;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.psimitab.IntActBinaryInteraction;
import uk.ac.ebi.intact.psimitab.converters.Intact2Tab;
import uk.ac.ebi.intact.psimitab.converters.Intact2TabException;
import uk.ac.ebi.intact.psimitab.converters.IntactBinaryInteractionHandler;
import uk.ac.ebi.intact.psimitab.converters.expansion.SpokeWithoutBaitExpansion;
import uk.ac.ebi.intact.searchengine.SearchClass;
import uk.ac.ebi.intact.searchengine.SearchHelper;
import uk.ac.ebi.intact.searchengine.SearchHelperI;
import uk.ac.ebi.intact.util.Chrono;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Uses DatabaseQueries to get Information for building graph.
 *
 * @author Nadin Neuhauser
 * @version $Id
 * @since 1.6.0-SNAPSHOT
 */
public class DatabaseService implements DataService, Serializable {

    private static final Log logger = LogFactory.getLog( DatabaseService.class );

    private final SearchHelperI searchHelper = new SearchHelper();

    private Collection<String> centralProteins;

    private DaoFactory getDaoFactory() {
        return IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
    }

    public Collection getSearchCritera() {
        return searchHelper.getSearchCritera();
    }

    public Collection getCentralProteins() {
        return centralProteins;
    }

    private Collection<BinaryInteraction> getBinaryInteractions( String ac ) throws HierarchViewDataException {
        Collection<BinaryInteraction> binaryInteractions = new ArrayList<BinaryInteraction>();
        if ( ac != null ) {
            Collection<Interaction> interactions = getDaoFactory().getInteractionDao().getInteractionsByInteractorAc( ac );

            try {
                if ( interactions != null && !interactions.isEmpty() ) {

                    Chrono chrono = new Chrono();
                    chrono.start();

                    Intact2Tab i2t = new Intact2Tab();
                    i2t.setBinaryInteractionClass( IntActBinaryInteraction.class );
                    i2t.setBinaryInteractionHandler( new IntactBinaryInteractionHandler() );
                    i2t.setExpansionStrategy( new SpokeWithoutBaitExpansion() );
                    i2t.setPostProssesorStrategy( new ClusterInteractorPairProcessor() );

                    binaryInteractions = i2t.convert( interactions );

                    chrono.stop();

                    String msg = new StringBuffer( 128 ).append( "Time for converting data (" )
                            .append( interactions.size() ).append( " Interaction(s) to " )
                            .append( binaryInteractions.size() ).append( " BinaryInteraction(s)):" )
                            .append( chrono ).toString();

                    logger.info( msg );

                } else {
                    logger.warn( "No result by database query for " + ac );
                }

            } catch ( Intact2TabException e ) {
                throw new HierarchViewDataException( "Can not convert uk.ac.ebi.intact.model.Interaction to BinaryInteraction." );
            }
        }
        return binaryInteractions;
    }

    public Collection getBinaryInteractionsByQueryString( String query ) throws HierarchViewDataException, MultipleResultException, ProteinNotFoundException {
        Chrono chrono = new Chrono();
        chrono.start();

        centralProteins = new ArrayList<String>();

        Collection<BinaryInteraction> binaryInteractions = new ArrayList<BinaryInteraction>();
        Collection<Interactor> interactors = getInteractorByQuery( query );


        if ( interactors != null && !interactors.isEmpty() ) {
            for ( Interactor interactor : interactors ) {

                if ( interactor.getAc() != null ) {

                    Collection<BinaryInteraction> bis = getBinaryInteractions( interactor.getAc() );
                    if ( bis != null && !bis.isEmpty() ) {
                        binaryInteractions.addAll( bis );
                        centralProteins.add( interactor.getAc() );
                    }
                }

            }
        } else {
            logger.warn( "No result(s) by search in database for query " + query );
        }

        chrono.stop();

        String msg;
        if ( binaryInteractions.isEmpty() ) {
            msg = new StringBuffer( 128 ).append( "No result(s) by search in database for query " )
                    .append( query ).append( chrono ).toString();
        } else {
            msg = new StringBuffer( 128 ).append( "Time for retreiving data from database(" )
                    .append( binaryInteractions.size() ).append( " BinaryInteractions) :" )
                    .append( chrono ).toString();
        }
        logger.info( msg );

        if ( binaryInteractions.size() > HVNetworkBuilder.getMaxInteractions() ) {
            throw new MultipleResultException( "Result of " + query + " get more than " + HVNetworkBuilder.getMaxInteractions() + " interactions." );
        }

        return binaryInteractions;
    }

    /**
     * Search in the database Interactor related to the query string.
     *
     * @param queryString the criteria to search for.
     * @return a collection of interactor or empty if none are found.
     * @throws uk.ac.ebi.intact.business.IntactException
     *          in case of search error.
     */
    private Collection<Interactor> getInteractorByQuery( String queryString ) throws ProteinNotFoundException {

        Collection results;

        //first try search string 'as is' - some DBs allow mixed case....
        results = searchHelper.doLookup( SearchClass.INTERACTOR, queryString, null);

        if ( results.isEmpty() ) {
            //now try all lower case....
            String lowerCaseValue = queryString.toLowerCase();
            results = searchHelper.doLookup( SearchClass.INTERACTOR, lowerCaseValue, null );
            if ( results.isEmpty() ) {
                //finished all current options, and still nothing - return a failure
                logger.info( "No matches were found for the specified search criteria" );
            }
        }
        if ( logger.isDebugEnabled() ) {
            logger.debug( "Found " + results.size() + " interactors for SearchQuery=" + queryString );
        }

        int interactorsFound = results.size();

        if ( interactorsFound == 0 ) {
            throw new ProteinNotFoundException();
        }


        return results;
    }

    public String getDbName() throws HierarchViewDataException {
        try {
            return IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getBaseDao().getDbName();
        } catch ( SQLException e ) {
            throw new HierarchViewDataException( "Could not find Database Name." );
        }
    }

    public static Collection getColByPropertyName( Class objectType, String searchParam, String searchValue ) {
        return IntactContext.getCurrentInstance().getDataContext()
                .getDaoFactory().getIntactObjectDao( objectType ).getColByPropertyName( searchParam, searchValue );
    }

}

//    public Interactor getInteractorByAc( String ac ) {
//        return getDaoFactory().getInteractorDao().getByAc( ac );
//    }

//    public Interactor getProteinByAc( String ac ) {
//        return getDaoFactory().getProteinDao().getByAc( ac );
//    }

//    public Collection getProteinByAcLike( String ac ) {
//        return getDaoFactory().getProteinDao().getByAcLike( ac );
//    }

//    public Collection getPropteriesByAc( String ac){
//        return dataObject.getPropteriesByAc( ac );
//    }

//    public Collection getPropteriesByNode( Node node ) {
//        if (node != null){
//            return dataObject.getPropteriesByVertex( (InteractorVertex) node);
//        }
//
//        logger.info("No Properties found for " + node.getId());
//        return null;
//    }

//    public List getAllPreysByBaitAc( String baitAc ) {
//
//        Query query = getEntityManager().createQuery( "select protein2Ac, shortLabel2 from MineInteraction " +
//                                                     "where protein1Ac = :baitAc" );
//        query.setParameter( "baitAc", baitAc);
//        getEntityManager().clear();
//        return query.getResultList();
//    }

//    public List getAllBaitsByPreyAc( String preyAc ) {
//
//        Query query = getEntityManager().createQuery( "select protein1Ac, shortLabel1 from MineInteraction " +
//                                                     "where protein2Ac = :preyAc" );
//        query.setParameter( "preyAc", preyAc);
//        getEntityManager().clear();
//        return query.getResultList();
//    }

//    public CvTopic getCvTopicByShortLabel( String shortlabel ) {
//
//        return getDaoFactory().getCvObjectDao( CvTopic.class ).getByShortLabel( shortlabel );
//    }

//    public int getDatabaseTermCount( List nodeAcs, String databaseTermId ) {
//
//        String queryString = "select count(*) from InteractorXref x where x.parent.ac in (:acs) and x.primaryId = :id";
//
//        Query query = getDaoFactory().getEntityManager().createQuery( queryString );
//        query.setParameter( "acs", nodeAcs );
//        query.setParameter( "id", databaseTermId );
//        return ( ( Long ) query.getSingleResult() ).intValue();
//    }

//    public List getPrimaryIdByDatabaseLabelAndProtAc( String databaseShortLabel, String protAc ) {
//        logger.warn( "getPrimaryIdByDatabaseLabelAndProtAc:" + databaseShortLabel + "||" + protAc );
//        Query query = getEntityManager().createQuery( "select xref from InteractorXref xref " +
//                                                      "where xref.parentAc = :protAc " +
//                                                      "and xref.cvDatabase.shortLabel = :databaseShortLabel" );
//        query.setParameter( "protAc", protAc );
//        query.setParameter( "databaseShortLabel", databaseShortLabel );
//        getEntityManager().clear();
//        List<InteractorXref> interactorXrefs = query.getResultList();
//
//        List<String> primaryIds = new ArrayList<String>( interactorXrefs.size() );
//        for ( InteractorXref interactorXref : interactorXrefs ) {
//            primaryIds.add( interactorXref.getPrimaryId() );
//        }
//
//        return primaryIds;
//    }

//    private EntityManager getEntityManager() {
//        return getDaoFactory().getEntityManager();
//    }

