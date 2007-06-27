/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */

package uk.ac.ebi.intact.application.mine.struts.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.MessageResources;
import uk.ac.ebi.intact.application.mine.business.Constants;
import uk.ac.ebi.intact.application.mine.business.IntactUser;
import uk.ac.ebi.intact.application.mine.business.IntactUserI;
import uk.ac.ebi.intact.application.mine.struts.view.AmbiguousBean;
import uk.ac.ebi.intact.application.mine.struts.view.ErrorBean;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.searchengine.SearchClass;
import uk.ac.ebi.intact.searchengine.SearchHelper;
import uk.ac.ebi.intact.searchengine.SearchHelperI;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * Performs a database search for the given search phrases. If the results are
 * ambiguous the application is forwarded to a page to display all results.
 * Otherwise the application is forwarded to start the algorithm.
 *
 * @author Andreas Groscurth
 */
public class SearchAction extends Action {

    private static final Log logger = LogFactory.getLog(SearchAction.class);

    private static final String PROTEIN_PARAMETER = "on";

    public ActionForward execute( ActionMapping mapping,
                                  ActionForm form,
                                  HttpServletRequest request,
                                  HttpServletResponse response ) {
        HttpSession session = request.getSession( true );
        IntactUserI user = (IntactUserI) session.getAttribute( Constants.USER );

        try {
            // if no user exists in the session a new user is created
            // this is done because via the external management this action
            // can be called as first action
            if( user == null ) {
                user = new IntactUser();
                session.setAttribute( Constants.USER, user );
            } else {
                // clear all former found paths and singletons
                user.clearAll();
            }

            // the collection stores all values provided by the
            // parametername 'AC'. Because it is not sure if these values are
            // proteins, interactions or experiments one has to search for them
            Collection searchAc = new HashSet();

            // the collection stores all proteins which were selected via a
            // checkbox in the ambiguous page. No search is needed because we
            // know they are proteins.
            Collection notSearchAc = new HashSet();

            // all given parameters are fetched
            Map parameters = request.getParameterMap();
            String key, tmp;
            String[] values;
            StringTokenizer tok;
            StringBuffer search = new StringBuffer( 96 );

            for ( Iterator iter = parameters.keySet().iterator();
                  iter.hasNext();
                    ) {
                key = (String) iter.next();
                values = (String[]) parameters.get( key );
                if( key.equals( "action" ) ) {
                    continue;
                }
                // if a checkbox with a protein was ticked the protein ac
                // number is the key and stored in the list
                if( PROTEIN_PARAMETER.equals( values[ 0 ] ) ) {
                    tmp = key.trim();
                    notSearchAc.add( tmp );
                    search.append( tmp );
                } else if( key.equals( Constants.PARAMETER ) ) {
                    tok = new StringTokenizer( values[ 0 ], Constants.COMMA );
                    while ( tok.hasMoreTokens() ) {
                        // the parameter can be something else
                        // than a protein -> it has to be searched for
                        tmp = tok.nextToken().trim();
                        searchAc.add( tmp );
                        search.append( tmp );
                        if( tok.hasMoreTokens() ) {
                            search.append( Constants.COMMA );
                        }
                    }
                }
                if( iter.hasNext() ) {
                    search.append( Constants.COMMA );
                }
            }
            user.setSearch( search.toString() );

            if( ( searchAc.size() + notSearchAc.size() )
                > Constants.MAX_SEARCH_NUMBER ) {
                logger.warn( "too many searches" );
                MessageResources mr = getResources( request );
                request.setAttribute( Constants.ERROR, new ErrorBean( mr.getMessage( "searchAction.tooMuchProteins", Integer.toString( Constants.MAX_SEARCH_NUMBER ) ) ) );
                return mapping.findForward( Constants.ERROR );
            }

            // flag whether the search returned an ambiguous result
            boolean ambiguous = false;
            // the collection stores all found ambiguous results by the search
            Collection ambiguousResults = new HashSet();

            String searchPhrase;
            AmbiguousBean ab;
            // collection stores the final ac to use for the dijkstra algorithm
            Collection mineSearchAc = new HashSet();

            // the search helper provides the search for the ac numbers
            SearchHelper sh = new SearchHelper();
            // for every ac number of the list a search is done
            for ( Iterator iter = searchAc.iterator(); iter.hasNext(); ) {
                searchPhrase = (String) iter.next();

                // the given search phrase is searched in proteins, interactions
                // and experiments
                ab = searchForAll( searchPhrase, sh, user );

                // if the search returned an ambiguous result
                if( ab.hasAmbiguousResult() ) {
                    ambiguous = true;
                } else {
                    // if not the found protein is added to the
                    // collection used for the dijkstra algorithm
                    for ( Iterator it = ab.getProteins().iterator();
                          it.hasNext();
                            ) {
                        mineSearchAc.add( ( (Interactor) it.next() ).getAc() );
                    }
                }
                // the searchPhrase is stored to distinguish which search result
                // belongs to which search phrase
                ab.setSearchAc( searchPhrase );
                ambiguousResults.add( ab );
            }

            // if the results are ambiguous the application is forwarded to a
            // special page to display all search results.
            if( ambiguous ) {
                logger.warn( "forward to the ambiguous page" );
                // because we want also the information of the proteins
                // which were not used in the first search - the informations
                // are now fetched
                for ( Iterator iter = notSearchAc.iterator(); iter.hasNext(); ) {
                    searchPhrase = iter.next().toString();
                    ab = searchForProteins( searchPhrase, sh, user );
                    ab.setSearchAc( searchPhrase );
                    ambiguousResults.add( ab );
                }
                request.setAttribute( Constants.AMBIGOUS, ambiguousResults );
                return mapping.findForward( Constants.AMBIGOUS );
            } else {
                // the search result is not ambiguous therefore
                // the other proteins are added to the mine search collection
                mineSearchAc.addAll( notSearchAc );
            }

            logger.info( "forward to the algorithm" );
            request.setAttribute( Constants.SEARCH, mineSearchAc );
            return mapping.findForward( Constants.SUCCESS );
        } catch ( IntactException e ) {
            logger.error( "An error occured in SearchAction", e );
            request.setAttribute( Constants.ERROR, new ErrorBean( e.getMessage() ) );
            return mapping.findForward( Constants.ERROR );
        }
    }

    /**
     * Searches in the database for the given accession number. <br>
     * Returns a bean which stores all found results, which can be proteins,
     * interactions or experiments.
     *
     * @param ac   the accession number to search for
     * @param sh   the searchhelper
     * @param user the intact user
     * @return @throws IntactException
     */
    private AmbiguousBean searchForAll( String ac,
                                        SearchHelperI sh,
                                        IntactUserI user )
            throws IntactException {
        AmbiguousBean ab = new AmbiguousBean();

        List<SearchClass> searchClasses = new ArrayList<SearchClass>();
        searchClasses.add(SearchClass.PROTEIN);
        searchClasses.add(SearchClass.INTERACTION);
        searchClasses.add(SearchClass.EXPERIMENT);

        Collection<IntactObject> results = sh.doLookup(searchClasses, ac, user);

        Collection<ProteinImpl> proteins = new ArrayList<ProteinImpl>();
        Collection<InteractionImpl> interactions = new ArrayList<InteractionImpl>();
        Collection<Experiment> experiments = new ArrayList<Experiment>();

        for (IntactObject result : results)
        {
            if (result instanceof ProteinImpl)
            {
                proteins.add((ProteinImpl)result);
            }
            else if (result instanceof InteractionImpl)
            {
                interactions.add((InteractionImpl)result);
            }
            else if (result instanceof Experiment)
            {
                experiments.add((Experiment)result);
            }
        }

        ab.setProteins( proteins );
        ab.setInteractions( interactions );
        ab.setExperiments( experiments );

        logger.debug("Results: Prot "+proteins.size()+" Int "+interactions.size()+" Exp "+experiments.size());
        
        return ab;
    }

    /**
     * Searches in the database for the given accession number. <br>
     * Returns a bean which stores all found results in proteins.
     *
     * @param ac   the accession number to search for
     * @param sh   the searchhelper
     * @param user the intact user
     * @return @throws IntactException
     */
    private AmbiguousBean searchForProteins( String ac,
                                             SearchHelperI sh,
                                             IntactUserI user )
            throws IntactException {
        AmbiguousBean ab = new AmbiguousBean();

        Collection results = sh.doLookup(SearchClass.PROTEIN, ac, user);

        ab.setProteins( (Collection<ProteinImpl>) results );
        return ab;
    }
}