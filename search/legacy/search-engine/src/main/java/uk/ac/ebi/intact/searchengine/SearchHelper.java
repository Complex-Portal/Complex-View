/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.searchengine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.persistence.dao.AnnotatedObjectDao;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.persistence.dao.SearchItemDao;
import uk.ac.ebi.intact.searchengine.business.IntactUserI;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Performs an intelligent search on the intact database.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class SearchHelper implements SearchHelperI {

    /**
     * The Logger
     */
    private static final Log logger = LogFactory.getLog( SearchHelper.class );

    /**
     * The table for the initial search request in the searchFast method
     */
    private final static String SEARCH_TABLE = "ia_search";

    /**
     * Collection of CriteriaBean
     */
    private Collection searchCriteria = new ArrayList();

    /**
     * Boolean to check if the database is connected
     */
    private Boolean connected;

    private HttpServletRequest request;

    /**
     * Create a search helper for which all the log message will be written by the provided logger.
     */
    public SearchHelper() {
        this( null );
    }

    /**
     * Create a search helper for which all the log message will be written by the provided logger.
     */
    public SearchHelper( HttpServletRequest request ) {
        this.request = request;
    }

    public Collection getSearchCritera() {
        return searchCriteria;
    }

    public Collection<IntactObject> doLookup( SearchClass searchClass, String values, IntactUserI user )
            throws IntactException {

        List<SearchClass> searchClasses = new ArrayList<SearchClass>();
        searchClasses.add( searchClass );

        return doLookup( searchClasses, values, user );
    }

    public Collection<IntactObject> doLookup( List<SearchClass> searchClasses, String values, IntactUserI user )
            throws IntactException {

        searchCriteria.clear();
        Collection<String> queries = splitQuery( values );

        // avoid to have duplicate intact object in the dataset.
        Collection<IntactObject> results = new HashSet<IntactObject>();

        SearchClass searchClass = null;
        boolean itemFound = false;
        for ( String subQuery : queries ) {
            logger.info( "Search for subquery '" + subQuery + "' in search classes: " + searchClasses );

            final int size = searchClasses.size();
            for ( int i = 0; i < size; i++ ) {
                if ( false == itemFound ) {
                    searchClass = searchClasses.get( i );
                } else {
                    // if there is an item found (i.e. only one class to look for)
                    // we need only one iteration.
                    if ( i > 0 ) {
                        break;
                    }
                }

                logger.debug( "Searching for '" + subQuery + "' of search class: " + searchClass.getMappedClass() );

                Collection<? extends IntactObject> subResult = doSearch( searchClass, subQuery, user );
                logger.debug( "sub result count: " + subResult.size() );

                if ( subResult.size() > 0 ) {
                    results.addAll( subResult );

                    Class<? extends IntactObject> clazz = subResult.iterator().next().getClass();
                    logger.info( "found search match - class: " + clazz + ", value: " + subQuery );
                    itemFound = true;
                    break; // exit the inner for
                }
            } // inner for
            logger.debug( "total result count: " + results.size() );
        } // main for

        return results;
    }


    /**
     * Split the query string. It generated one sub query by comma separated parameter. e.g.
     * {a,b,c,d} will gives {{a}, {b}, {c}, {d}}
     *
     * @param query the query string to split
     *
     * @return one to many subquery of the comma separated list.
     */
    private Collection<String> splitQuery( String query ) {
        Collection<String> queries = new LinkedList<String>();

        StringTokenizer st = new StringTokenizer( query, "," );
        while ( st.hasMoreTokens() ) {
            queries.add( st.nextToken().trim() );
        }

        return queries;
    }

    /**
     * utility method to handle the logic for lookup, ie trying AC, label etc. Isolating it here
     * allows us to change initial strategy if we want to. NB this will probably be refactored out
     * into the IntactHelper class later on.
     *
     * @param searchClass The class to search on (only comes from a link clink) - useful for
     *                    optimizing search
     * @param value       the user-specified value
     * @param user        object holding the IntactHelper for a given user/session (passed as a
     *                    parameter to avoid using an instance variable, which may cause thread
     *                    problems).
     *
     * @return Collection the results of the search - an empty Collection if no results found
     *
     * @throws uk.ac.ebi.intact.business.IntactException
     *          thrown if there were any search problems
     */
    private Collection<? extends IntactObject> doSearch( SearchClass searchClass, String value, IntactUserI user )
            throws IntactException {

        Class<? extends AnnotatedObject> mappedClass = searchClass.getMappedClass();

        AnnotatedObjectDao<? extends AnnotatedObject> dao = getDaoFactory().getAnnotatedObjectDao( mappedClass );

        //try search on AC first...
        Collection results = dao.getByAcLike( value );

        String currentCriteria = "ac";

        if ( results.isEmpty() ) {
            // No matches found - try a search by label now...
            logger.info( "no match found for " + mappedClass + " with ac = '" + value + "'" );
            logger.info( "now searching for class " + mappedClass + " with label " + value );

            results = dao.getByShortLabelLike( value );

            currentCriteria = "shortLabel";

            if ( results.isEmpty() ) {
                //no match on label - try by alias.
                logger.info( "no match on label - looking for: " + mappedClass +
                             " with name alias ID " +
                             value );

                Collection<Alias> aliases = getDaoFactory().getAliasDao( Alias.class ).getByNameLike( value );

                //could get more than one alias, eg if the name is a wildcard search value -
                //then need to go through each alias found and accumulate the results...
                for ( Alias alias : aliases ) {
                    IntactObject obj = dao.getByAc( alias.getParentAc() );

                    if ( obj != null ) {
                        results.add( obj );
                    }
                }
                currentCriteria = "alias";
            }

            if ( results.isEmpty() ) {
                //no match on label - try by xref....
                logger.info( "no match on label - looking for: " + mappedClass +
                             " with primary xref ID " +
                             value );

                Collection<Xref> xrefs = getDaoFactory().getXrefDao().getByPrimaryIdLike( value );

                //could get more than one xref, eg if the primary id is a wildcard search value -
                //then need to go through each xref found and accumulate the results...
                for ( Xref xref : xrefs ) {
                    IntactObject obj = dao.getByAc( xref.getParentAc() );

                    if ( obj != null ) {
                        results.add( obj );
                    }
                }
                currentCriteria = "xref";

                if ( results.isEmpty() ) {
                    //no match by xref - try finally by name....
                    logger.info( "no matches found using ac, shortlabel or xref - trying fullname..." );
                    results = user.search( mappedClass, "fullName", value );
                    currentCriteria = "fullName";

                    if ( results.isEmpty() ) {
                        //no match by xref - try finally by name....
                        logger.info( "no matches found using ac, shortlabel, xref or fullname... give up." );
                        currentCriteria = null;
                    }
                }
            }
        }

        CriteriaBean cb = new CriteriaBean( value, currentCriteria );
        searchCriteria.add( cb );
        return results;
    }  // doSearch


    /**
     * Returns true  if a simple count on the ia_search table works, if not false
     *
     * @return true if a simple count on the ia_search table works, if not return false
     */
    public boolean connected() {

        logger.info( "check if is connected" );
        logger.warn( "Current implementation will always return true for connected (BA)" );

        return true;

        /*
        logger.info("check if is connected");
        //set up with first call
        if (connected == null) {
            final String testQuery = "SELECT COUNT(*) FROM " + SEARCH_TABLE;
            IntactHelper helper = null;
            ResultSet rs = null;
            Statement stmt = null;
            try {
                helper = new IntactHelper();
                final Connection conn = helper.getJDBCConnection();
                stmt = conn.createStatement();
                rs = stmt.executeQuery(testQuery);
                // if we got a result, the search table exists
                if (rs.next()) {
                    connected = true;
                }
                // if we got no result,the search table exists
                else {
                    connected = false;
                }
                //  an exception means that something is wrong
            }
            catch (IntactException e) {
                connected = false;
            }
            catch (SQLException e) {
                connected = false;
            }
            finally {
                if (rs != null) {
                    try {
                        rs.close();
                    }
                    catch (SQLException e) {
                        // what i should do here ?
                        // throw an exception makes no sense
                    }
                }

                if (stmt != null) {
                    try {
                        stmt.close();
                    }
                    catch (SQLException e) {
                        // what i should do here ?
                        // throw an exception makes no sense
                    }
                }

                if (helper != null) {
                    try {
                        helper.closeStore();
                    }
                    catch (IntactException e) {
                        // what i should do here ?
                        // throw an exception makes no sense
                    }
                }
            }
        }
        return connected.booleanValue();
        */
    }

    /**
     * This Method provides a full index search on the ia_search table and returns a ResultWrapper which contains
     * the Object which fits by the query. If the result size is too large, you will get back an empty resultwrapper
     * which contains a statistic for the results. This method is private and only for internal usage. it should
     * replaced as soon as possible with a lucene based solution.
     *
     * @param query       the user-specified value
     * @param searchClass String which represents the name of the class  to search on.
     * @param type        String  the filter type (ac, shortlabel, xref etc.) if type is null it will be 'all'
     *
     * @return the result wrapper which contains the result of the search
     */
    private ResultWrapper search( String query, SearchClass searchClass, String type, int maximumResultSize, int firstResult, boolean paginatedSearch ) {

        // first check if we got a type, we have to search for a type if the type is not null
        // and not "all"
        boolean hasType = ( type != null ) && ( !type.trim().equals( "" ) ) && !type.equals( "all" );

        logger.debug( "search with value with query : " + query + " searchClass :" + searchClass.getMappedClass().getName() );
        // replace  the "*" with "%"

        String sqlValue = query.replaceAll( "\\*", "%" );
        sqlValue = sqlValue.replaceAll( "\\'", "" );
        sqlValue = sqlValue.toLowerCase();
        logger.debug( "SQL value: " + sqlValue );

        // split the query
        Collection<String> someSearchValues = this.splitQuery( sqlValue );
        String[] values = someSearchValues.toArray( new String[someSearchValues.size()] );

        // in this variable the results of the search will be stored
        List<AnnotatedObject> searchResult = new ArrayList<AnnotatedObject>();

        // If only one accession is in the query and the object type is specified
        // there is no need to go to the search table. We can retrieve the object directly
        if ( isQuerySearchingOnlyOneAc( values ) && searchClass.isSpecified() ) {
            logger.debug( "Search is for only one AC, and search class is specified. No need to go through ia_search" );
            AnnotatedObject result = getDaoFactory().getAnnotatedObjectDao( searchClass.getMappedClass() ).getByAc( values[0].toUpperCase() );

            if ( result == null ) {
                throw new IntactException( "Searching for one AC and with specified class did not return any result: " + searchClass.getMappedClass() + " " + values[0].toUpperCase() );
            }

            logger.debug( "Object found in the database: " + result.getAc() );

            searchResult.add( result );
            return new ResultWrapper( searchResult, 1 );
        }

        SearchItemDao searchItemDao = getDaoFactory().getSearchItemDao();

        //  type and objClass have to be null if they are not to be used in the query
        if ( !hasType ) type = null;

        if ( logger.isDebugEnabled() )
            logger.debug( "Getting counts from the IA_SEARCH table" );

        // We create the array of classes to use in the query for the IA_SEARCH table
        // If it is a unspecific CvObject, we need to subclass for all the CvObject subclasses
        String[] objClasses = null;

        if ( searchClass == SearchClass.CV_OBJECT ) {
            objClasses = SearchClass.cvObjectClassesAsStringArray();
        } else if ( searchClass.isSpecified() ) {
            objClasses = new String[]{searchClass.getMappedClass().getName()};
        } else {
            if ( logger.isDebugEnabled() )
                logger.debug( "SearchClass not specified, will search for anything in IA_SEARCH" );
            //objClasses = SearchClass.annotatedObjectClassesAsStringArray();
        }


        Map<String, Integer> resultInfo = getCountResultsUsingSessionCache( sqlValue, values, objClasses, type );

        // count the results, iterating the groups and adding each subtotal
        int count = 0;

        for ( Map.Entry<String, Integer> entry : resultInfo.entrySet() ) {
            logger.debug( "Class: " + entry.getKey() + " - count: " + entry.getValue() );
            count = count + entry.getValue();
        }

        logger.debug( "Count = " + count );

        // check the result size if the result is too large return an empty ResultWrapper

        if ( count > maximumResultSize && !paginatedSearch ) {
            logger.debug( "Result too Large return an empty result Wrapper" );
            return new ResultWrapper( count, maximumResultSize, resultInfo );
        }

        // if there is only one result, and the search is paginated, we have probably clicked
        // in a page of the partner results. So the first result have to be retrieved (the protein
        // being queried)
        if ( count == 1 && paginatedSearch ) {
            logger.debug( "The query only returns one result, and it is paginated. This must be a partner view, with paginated parnters" );
            firstResult = 0;
        }

        boolean searchedForCvObject = false;

        for ( String className : resultInfo.keySet() ) {

            String[] classesToSearch = new String[]{className};

            // we determine the class to use in the search
            Class<? extends AnnotatedObject> clazzToSearch;
            try {
                clazzToSearch = ( Class<? extends AnnotatedObject> ) Class.forName( className );
            }
            catch ( Exception e ) {
                throw new RuntimeException( e );
            }

            // if we have many different subclasses of CvObject to search, it is better to do only
            // one search for all the cvObjects. If this query has been already done in the iteration
            // it is not done again
            if ( CvObject.class.isAssignableFrom( clazzToSearch ) ) {
                clazzToSearch = CvObject.class;
                classesToSearch = SearchClass.cvObjectClassesAsStringArray();

                if ( searchedForCvObject ) {
                    continue;
                } else {
                    searchedForCvObject = true;
                }
            }

            // the query is paginated, so if there is more than a certain number of results, pagination will appear
            logger.info( "Getting ACs for class: " + clazzToSearch + "; firstResult: " + firstResult );
            List<String> acList = searchItemDao.getDistinctAc( values, classesToSearch, type, firstResult, maximumResultSize );
            String[] acs = acList.toArray( new String[acList.size()] );

            // we perform a query for all the ACs. This kind of query is limited in oracle to 1000 items,
            // far from our situation now, so no problem
            logger.info( "\t" + acList );
            List<? extends AnnotatedObject> res = getDaoFactory().getAnnotatedObjectDao( clazzToSearch ).getByAc( acs );

            searchResult.addAll( res );

        }

        return new ResultWrapper( searchResult, maximumResultSize, resultInfo, count );


    }   // end searchFast


    /**
     * Workaround to provide an Interactor search with the ia_search table.
     *
     * @param searchValue the user-specified search value
     * @param type        type String  the filter type (ac, shortlabel, xref etc.) if type is null it will be 'all'
     *
     * @return the result wrapper which contains the result of the search
     */
    private ResultWrapper getInteractors( final String searchValue, String type, int numberOfResults, int firstResult, boolean paginatedSearch ) {

        logger.info( "search Interactor" );

        // getting all results for proteins and interactions
        ResultWrapper proteins = this.search( searchValue, SearchClass.PROTEIN, type, numberOfResults, firstResult, paginatedSearch );
        ResultWrapper interactions = this.search( searchValue, SearchClass.INTERACTION, type, numberOfResults, firstResult, paginatedSearch );

        // now check whats going on with the results and calculate the summ of both
        if ( proteins.isTooLarge() || interactions.isTooLarge() ) {

            logger.info( "Search Helper: getInteractors method, result too large" );
            int count;
            int proteinCount = 0;
            int interactionCount = 0;
            Map<String, Integer> resultInfo = new HashMap<String, Integer>();

            // the result is too large just getting the info a return an empty resultwrapper
            if ( proteins.isTooLarge() ) {
                Map<String, Integer> proteinInfo = proteins.getInfo();
                proteinCount =
                        proteinInfo.get( SearchClass.PROTEIN.getMappedClass().getName() );
                resultInfo.put( SearchClass.PROTEIN.getMappedClass().getName(), proteinCount );
            }

            if ( interactions.isTooLarge() ) {
                Map<String, Integer> interactionInfo = interactions.getInfo();
                interactionCount = interactionInfo.get( SearchClass.INTERACTION.getMappedClass().getName() );
                resultInfo.put( SearchClass.INTERACTION.getMappedClass().getName(),
                                interactionCount );
            }

            count = proteinCount + interactionCount;

            // create the info

            logger.info( "return empty resultwrapper" );

            return new ResultWrapper( count, numberOfResults, resultInfo );

        } else {
            // we are in the limit, add everything to a new resultwrapper
            Collection temp = new ArrayList( proteins.getResult().size() + interactions.getResult().size() );
            temp.addAll( proteins.getResult() );
            temp.addAll( interactions.getResult() );

            // we convert to a Set, so we are sure that there are only unique elements
            Set set = new HashSet( temp );

            return new ResultWrapper( new ArrayList( set ), numberOfResults );
        }
    }

    /**
     * This Method provides a full index search on the ia_search table and returns a ResultWrapper which contains
     * the object which fits by the query. If the result size is too large, you will get back an empty
     * uk.ac.ebi.intact.application.commons.search.ResultWrapper which contains a statistic for the results
     *
     * @param query the user-specified search value
     * @param type  String the filter type (ac, shortlabel, xref etc.) if type is null it will be 'all'
     *
     * @return the result wrapper which contains the result of the search
     *
     * @throws uk.ac.ebi.intact.business.IntactException
     *          thrown if there were any search problems
     */
    public ResultWrapper searchFast( String query, SearchClass searchClass, String type, int numberOfResults, int firstResult, boolean paginatedSearch )
            throws IntactException {

        if ( searchClass == SearchClass.INTERACTOR ) {
            return this.getInteractors( query, type, numberOfResults, firstResult, paginatedSearch );
        } else {
            return this.search( query, searchClass, type, numberOfResults, firstResult, paginatedSearch );
        }
    }

    /**
     * Checks if there is only one value, and then if this value is an accession returns true
     */
    private static boolean isQuerySearchingOnlyOneAc( String[] values ) {
        if ( values.length > 1 || values.length == 0 ) {
            return false;
        }

        String value = values[0];

        String institutionPrefix = IntactContext.getCurrentInstance().getConfig().getAcPrefix();

        return ( value.toUpperCase().startsWith( institutionPrefix ) && !value.endsWith( "%" ) );

    }

    /**
     * When paginating, it is not necessary to count the results again,
     * use the same results than the previous query
     */
    private Map<String, Integer> getCountResultsUsingSessionCache( String searchValues, String[] values, String[] objClasses, String type ) {
        String attCurrentSearch = "CurrentSearch";
        String attCounts = "CurrentSearchCounts";

        String classesToSearchInfo = null;
        if ( objClasses != null && objClasses.length > 0 ) {
            classesToSearchInfo = Arrays.asList( objClasses ).toString();
        }

        // the value of the attribute is different, to identify the exact search
        String searchAttValue = searchValues + "_" + classesToSearchInfo + "_" + null;

        Map<String, Integer> resultInfo;

        if ( logger.isDebugEnabled() ) {
            if ( request == null ) logger.debug( "Request is null, so session cache is disabled for this search" );
        }

        if ( request != null &&
             request.getSession().getAttribute( attCurrentSearch ) != null &&
             request.getSession().getAttribute( attCurrentSearch ).equals( searchAttValue ) ) {
            resultInfo = ( Map<String, Integer> ) request.getSession().getAttribute( attCounts );
        } else {
            if ( logger.isDebugEnabled() ) {
                logger.debug( "Executing count query - values: " + Arrays.asList( values ) + " ; type: " + type + " ; objClasses: " + classesToSearchInfo );
            }

            resultInfo = getDaoFactory().getSearchItemDao().countGroupsByValuesLike( values, objClasses, type );

            if ( request != null ) {
                request.getSession().setAttribute( attCurrentSearch, searchAttValue );
                request.getSession().setAttribute( attCounts, resultInfo );
            }

        }

        return resultInfo;
    }

    private DaoFactory getDaoFactory() {
        return IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
    }

}