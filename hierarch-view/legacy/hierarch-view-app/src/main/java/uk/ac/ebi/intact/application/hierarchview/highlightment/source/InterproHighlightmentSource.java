
package uk.ac.ebi.intact.application.hierarchview.highlightment.source;

import org.apache.log4j.Logger;
import uk.ac.ebi.intact.application.hierarchview.business.Constants;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.business.graph.GraphHelper;
import uk.ac.ebi.intact.application.hierarchview.business.graph.InteractionNetwork;
import uk.ac.ebi.intact.application.hierarchview.struts.StrutsConstants;
import uk.ac.ebi.intact.application.hierarchview.struts.view.utils.SourceBean;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.Interactor;
import uk.ac.ebi.intact.model.Xref;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.util.simplegraph.BasicGraphI;
import uk.ac.ebi.intact.util.simplegraph.Node;
import uk.ac.ebi.intact.util.SearchReplace;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Interface allowing to wrap an highlightment source.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 */

public class InterproHighlightmentSource extends HighlightmentSource {

    static Logger logger = Logger.getLogger( Constants.LOGGER_NAME );

    /**
     * separator of keys, use to create and parse key string.
     */
    private static final String KEY_SEPARATOR = ",";
    private static final String ATTRIBUTE_OPTION_CHILDREN = "CHILDREN";
    private static final String PROMPT_OPTION_CHILDREN = "With children of the selected Interpro term";

    /**
     * The key for this source
     */
    private static final String SOURCE_KEY = "interpro";

    /**
     * Return the html code for specific options of the source to integrate int
     * the highlighting form. if the method return null, the source hasn't
     * options.
     *
     * @return the html code for specific options of the source.
     */
    public String getHtmlCodeOption(HttpSession aSession) {
        String htmlCode;
        IntactUserI user = (IntactUserI) IntactContext.getCurrentInstance().getSession()
                .getAttribute( uk.ac.ebi.intact.application.hierarchview.business.Constants.USER_KEY );
        String check = (String) user
                .getHighlightOption( ATTRIBUTE_OPTION_CHILDREN );

        //String check = (String) aSession.getAttribute
        // (ATTRIBUTE_OPTION_CHILDREN);

        if ( check == null ) {
            check = "";
        }

        htmlCode = "<input type=\"checkbox\" name=\""
                + ATTRIBUTE_OPTION_CHILDREN + "\" " + check
                + " value=\"checked\">" + PROMPT_OPTION_CHILDREN;

        return htmlCode;
    }

    /**
     * Return a collection of keys specific to the selected protein and the
     * current source. e.g. If the source is GO, we will send the collection of
     * GO term owned by the given protein. Those informations are retreived from
     * the Intact database
     *
     * @param aProteinAC a protein identifier (AC)
     * @return a set of keys (this keys are a String) (this Keys are a String[]
     *         which contains the GOterm and a description)
     */
    public Collection getKeysFromIntAct(String aProteinAC, HttpSession aSession) {

        Collection result = null;
        Iterator iterator;
        Collection listInterproTerm = new ArrayList();
        IntactUserI user = (IntactUserI) IntactContext.getCurrentInstance().getSession()
                .getAttribute( uk.ac.ebi.intact.application.hierarchview.business.Constants.USER_KEY );

        if ( null == user ) {
            logger
                    .error( "No user found in the session, unable to search for Interpro terms" );
            return null;
        }

        try {
            logger.debug( "Try to get a list of Interpro term (from protein AC="
                    + aProteinAC + ")" );
            result = getDaoFactory().getProteinDao().getByAcLike(aProteinAC);
        }
        catch ( IntactException ie ) {
            logger.error( "When trying to get a list of Interpro", ie );
            return null;
        }

        // no object
        if ( result.isEmpty() )
            return null;

        iterator = result.iterator();
        Interactor interactor = (Interactor) iterator.next();

        // get Xref collection
        Collection xRef = interactor.getXrefs();
        logger.info( xRef.size() + " Xref found" );
        listInterproTerm = filterInteractorXref( xRef );

        return listInterproTerm;
    } // getKeysFromIntAct


    /**
     * get a collection of XRef and filter to keep only Interpro terms
     *
     * @param xRef the XRef collection
     * @return a Interpro term collection or an empty collection if none exists.
     */
    private Collection filterInteractorXref(Collection xRef) {
        Collection listInterproTerm = new ArrayList( xRef.size() ); // size will be >= to needed capacity
        Iterator xRefIterator = xRef.iterator();

        while ( xRefIterator.hasNext() ) {
            String[] interproTerm = new String[3];
            Xref xref = (Xref) xRefIterator.next();

            if ( ( xref.getCvDatabase().getShortLabel() ).toLowerCase().equals( SOURCE_KEY ) ) {
                interproTerm[0] = "Interpro";
                interproTerm[1] = xref.getPrimaryId();
                interproTerm[2] = xref.getSecondaryId();
                listInterproTerm.add( interproTerm );
                logger.info( xref.getPrimaryId() );
            }
        }

        return listInterproTerm;
    }

    /**
     * get a collection of the xrefs and filter to keep only Interpro terms.
     *
     * The collection has the acutally just one entry which is a map structure.
     * This map maps a source (e.g. GO) to a collection of strings which stores
     * the primary and the secondary IDs of the central proteins.
     *
     * Method is used when the graph was built with the mine database table
     *
     * @param xRef all xrefs of the centralnodes
     * @return a collection with just the Interpro terms
     */
    private Collection filterXref(Collection xRef) {
        Collection listInterproTerm = null;
        String[] interproTerm;
        // set to keep track of the GO Terms which are added to avoid duplicate
        // entries. This is needed because we are adding String[] to the actual
        // collection and the method 'contains' fails when testing if an array
        // (with the same entries) is already in the collection (even if it is
        // in)
        Set doubleEntrieCheckSet = new HashSet( xRef.size() );

        Iterator xRefIterator = xRef.iterator();
        if ( xRefIterator.hasNext() ) {
            // the source map is fetched
            // Map<String, Collection<String> >
            Map sourceMap = (Map) xRefIterator.next();

            Collection interproTerms = (Collection) sourceMap.get( SOURCE_KEY );
            // the collection stores the filterd xrefs - it has the size of the
            // number of GO terms divided by 2 because in the goTerms collection
            // two following entries are represanting one GO Term (even: primary
            // ID, odd: secondary ID)
            listInterproTerm = new ArrayList( interproTerms.size() / 2 );

            Iterator sourceIterator = interproTerms.iterator();
            while ( sourceIterator.hasNext() ) {
                interproTerm = new String[2];
                // the two entries of the collection are stored as one GO Term
                interproTerm[0] = (String) sourceIterator.next();
                interproTerm[1] = (String) sourceIterator.next();

                // if both entries are not already in the collection they are
                // added to the list of GO Terms
                if ( !doubleEntrieCheckSet.contains( interproTerm[0] )
                        && !doubleEntrieCheckSet.contains( interproTerm[1] ) ) {
                    listInterproTerm.add( interproTerm );
                    doubleEntrieCheckSet.add( interproTerm[0] );
                    doubleEntrieCheckSet.add( interproTerm[1] );
                }
            }
        }
        return listInterproTerm;
    }

    /**
     * Returns a collection of nodes to highlight for the display
     *
     * @param aGraph the network to display
     * @param selectedInterproTerm the selected Interpro Term
     * @param children the children Interpro Terms of the selected Interpro Term
     * @param searchForChildren whether it shall be searched for the children
     * @param user
     * @return
     * @throws IntactException
     * @throws SQLException
     */
    private Collection proteinToHighlightInteractor(InteractionNetwork aGraph,
            String selectedInterproTerm, Collection children,
            boolean searchForChildren, IntactUserI user) throws SQLException,
            IntactException {

        // if the source highlight map of the network is empty
        // it is filled with the source informations from each
        // node of the graph
        if ( aGraph.isSourceHighlightMapEmpty() ) {
            GraphHelper gh = new GraphHelper( user );

            ArrayList listOfNode = aGraph.getOrderedNodes();
            int size = listOfNode.size();

            for (int i = 0; i < size; i++) {
                BasicGraphI node = (BasicGraphI) listOfNode.get( i );
                // add all sources to the source highglighting map
                // TODO: every node is set as non central node....
                // this was done because the additional information which is
                // added to the central node is not used when the graph is not
                // built by the mine database table
                gh.addSourcesToNode( node, false, aGraph );
            }
        }
        // return the set of proteins to highlight based on the source
        // highlighting map of the graph
        return proteinToHighlightSourceMap( aGraph, children, selectedInterproTerm,
                searchForChildren );
    }

    private Collection proteinToHighlightDatabase(HttpSession aSession,
            InteractionNetwork aGraph, String selectedInterproTerm,
            Collection children, boolean searchForChildren) {
        Collection nodeList = new ArrayList( 20 ); // should be enough for 90%
        // cases
        ArrayList listOfNode = aGraph.getOrderedNodes();
        int size = listOfNode.size();
        String[] interproTermInfo = null;
        String interproTerm = null;
        for (int i = 0; i < size; i++) {
            Node node = (Node) listOfNode.get( i );
            String ac = node.getAc();
            // Search all Interpro Term for this ac number
            Collection listInterproTerm = this.getKeysFromIntAct( ac, aSession );
            if ( ( listInterproTerm != null ) && ( listInterproTerm.isEmpty() == false ) ) {
                Iterator list = listInterproTerm.iterator();
                while ( list.hasNext() ) {
                    interproTermInfo = (String[]) list.next();
                    interproTerm = interproTermInfo[0];

                    if ( selectedInterproTerm.equals( interproTerm ) ) {
                        nodeList.add( node );
                        break;
                    }

                    //                    if (searchForChildren == true) {
                    Iterator it = children.iterator();
                    while ( it.hasNext() ) {
                        String newInterproTerm = (String) it.next();
                        if ( newInterproTerm.equals( interproTerm ) ) {
                            nodeList.add( node );
                            break;
                        }
                    }
                    //                    }
                } // while
            } // if
        } // for
        return nodeList;
    }

    /**
     * Returns a collection of proteins to be highlighted in the graph.
     *
     * Method is called when the graph was built by the mine database table.
     *
     * @param aGraph the graph
     * @param children the children of the selected Interpro Term
     * @param selectedInterproTerm the selected Interpro Term
     * @param searchForChildren whether it shall be searched for the children
     * @return
     */
    private Collection proteinToHighlightSourceMap(InteractionNetwork aGraph,
            Collection children, String selectedInterproTerm,
            boolean searchForChildren) {

        Collection nodeList = new ArrayList( 20 ); // should be enough for 90%
        // cases

        // retrieve the set of proteins to highlight for the source key (e.g.
        // GO) and the selected Interpro Term
        Set proteinsToHighlight = aGraph.getProteinsForHighlight( SOURCE_KEY,
                selectedInterproTerm );

        // if we found any proteins we add all of them to the collection
        if ( proteinsToHighlight != null ) {
            nodeList.addAll( proteinsToHighlight );
        }

        /*
         * for every children all proteins related to the current GO Term are
         * fetched from the source highlighting map of the graph and added to
         * the collection
         */
        if ( searchForChildren ) {
            for (Iterator iter = children.iterator(); iter.hasNext();) {
                proteinsToHighlight = aGraph.getProteinsForHighlight(
                        SOURCE_KEY, (String) iter.next() );
                if ( proteinsToHighlight != null ) {
                    nodeList.addAll( proteinsToHighlight );
                }
            }
        }
        return nodeList;
    }

    /**
     * Create a set of protein we must highlight in the graph given in
     * parameter. The protein selection is done according to the source keys
     * stored in the IntactUser. Keys are GO terms, so we select (and highlight)
     * every protein which awned that GO term. If the children option is
     * activated, all proteins which owned a children of the selected GO term
     * are selected.
     *
     * @param aSession the session where to find selected keys.
     * @param aGraph the graph we want to highlight
     * @return a collection of node to highlight
     */
    public Collection proteinToHightlight(HttpSession aSession,
            InteractionNetwork aGraph) {

        IntactUserI user = (IntactUserI) IntactContext.getCurrentInstance().getSession()
                .getAttribute( Constants.USER_KEY );
        Collection children = user.getKeys();
        String selectedInterproTerm = user.getSelectedKey();

        if ( children.remove( selectedInterproTerm ) ) {
            logger.info( selectedInterproTerm + " removed from children collection" );
        }

        // get source option
        String check = (String) user
                .getHighlightOption( ATTRIBUTE_OPTION_CHILDREN );
        boolean searchForChildren;
        if ( check != null ) {
            searchForChildren = check.equals( "checked" );
        }
        else {
            searchForChildren = false;
        }
        logger.info( "Children option activated ? " + searchForChildren );
        /**
         * T E S T
         */
        searchForChildren = true;

        // if the graph was built the mine dabase table a differen way to
        // retrieve the proteins to be highlighted are used
        if ( GraphHelper.BUILT_WITH_MINE_TABLE ) {
            return proteinToHighlightSourceMap( aGraph, children,
                    selectedInterproTerm, searchForChildren );
        }
        else {
            try {
                return proteinToHighlightInteractor( aGraph, selectedInterproTerm,
                        children, searchForChildren, user );
            }
            catch ( Exception e ) {
                return proteinToHighlightDatabase( aSession, aGraph,
                        selectedInterproTerm, children, searchForChildren );
            }
        }
    }

    /**
     * Allows to update the session object with parameters' request. These
     * parameters are specific of the implementation.
     *
     * @param aRequest request in which we have to get parameters to save in the
     *            session
     * @param aSession session in which we have to save the parameter
     */
    public void saveOptions(HttpServletRequest aRequest, HttpSession aSession) {

        IntactUserI user = (IntactUserI) IntactContext.getCurrentInstance().getSession()
                .getAttribute( Constants.USER_KEY );
        String[] result = aRequest
                .getParameterValues( ATTRIBUTE_OPTION_CHILDREN );

        if ( result != null )
            user.addHighlightOption( ATTRIBUTE_OPTION_CHILDREN, result[0] );
    } // saveOptions

    public List getSourceUrls(Collection xRefs, Collection selectedXRefs, String applicationPath,
                              IntactUserI user) throws IntactException, SQLException {

        // connection to database
        Connection con = getDaoFactory().connection();

        // get in the Highlightment properties file where is interpro
        Properties props = IntactUserI.HIGHLIGHTING_PROPERTIES;

        if ( null == props ) {
            String msg = "Unable to find the Interpro hostname. "
                    + "The properties file '"
                    + StrutsConstants.HIGHLIGHTING_PROPERTY_FILE
                    + "' couldn't be loaded.";
            logger.error( msg );
            throw new IntactException( msg );
        }

        String interproPath = props.getProperty( "highlightment.source.Interpro.applicationPath" );

        if ( null == interproPath ) {
            String msg = "Unable to find the Interpro hostname. "
                    + "Check the 'highlightment.source.Interpro.applicationPath' property in the '"
                    + StrutsConstants.HIGHLIGHTING_PROPERTY_FILE
                    + "' properties file";
            logger.error( msg );
            throw new IntactException( msg );
        }

        // filter to keep only Interpro terms
        logger.info( xRefs.size() + " Xref before filtering" );
        Collection listInterproTerm = null;
        if ( GraphHelper.BUILT_WITH_MINE_TABLE ) {
            listInterproTerm = filterXref( xRefs );
        }
        else {
            listInterproTerm = filterInteractorXref( xRefs );
        }

        logger.info( listInterproTerm.size() + " Interpro term(s) after filtering" );

        // create url collection with exact size
        List urls = new ArrayList( listInterproTerm.size() );


        // list of Nodes of the Graph
        InteractionNetwork aGraph = user.getInteractionNetwork();
        ArrayList listOfNode = aGraph.getOrderedNodes();
        String listOfNodesSQL = null;


        // transform the list of nodes to fit with SQL syntax
        for( int i=0; i<listOfNode.size(); i++ ) {
            if ( i > 0 ) {
                 listOfNodesSQL = "'" + listOfNode.get(i).toString() + "'," + listOfNodesSQL;
            }
            else {
                 listOfNodesSQL = "'" + listOfNode.get(i).toString() + "'";
            }
        }

        String[] tmpList = listOfNodesSQL.split(",");
        listOfNodesSQL = "";
        for( int i=0; i<tmpList.length ; i++ ) {
            tmpList[i] = tmpList[i].replaceAll( "Node:\\s","" );
            tmpList[i] = tmpList[i].replaceAll( "\\[","" );
            tmpList[i] = tmpList[i].replaceAll( "\\]","" );
            if( i > 0) {
                listOfNodesSQL = listOfNodesSQL + "," + tmpList[i];
            }
            else {
                listOfNodesSQL = listOfNodesSQL + tmpList[i];
            }
        }
        listOfNodesSQL = "(" + listOfNodesSQL + ")";


        // SQL request to fill the "count" column
        PreparedStatement sourceStm = con.prepareStatement( "SELECT count(X.ac) FROM ia_xref X, ia_controlledvocab C "
                + "WHERE C.ac = X.database_ac AND X.primaryid=? AND X.parent_ac in " + listOfNodesSQL );


        // Create a collection of label-value object (Interpro term, URL to access a
        // nice display in interpro)
        String[] interproTermInfo;
        String interproTermId, interproTermType, interproTermDescription;
        int interproTermCount = 0;

        /*
         * In order to avoid the browser to cache the response to that request
         * we stick at its end of the generated URL.
         */
        String randomParam = "&now=" + System.currentTimeMillis();

        if ( listInterproTerm != null && ( false == listInterproTerm.isEmpty() ) ) {
            Iterator list = listInterproTerm.iterator();
            while ( list.hasNext() ) {
                interproTermInfo = (String[]) list.next();
                interproTermId = interproTermInfo[1];
                interproTermType = interproTermInfo[0];
                interproTermDescription = interproTermInfo[2];

                ResultSet set;

                // the current source is fetched (e.g. GO)
                sourceStm.setString( 1, interproTermId );
                set = sourceStm.executeQuery();
                logger.info( "query for (" + interproTermId + "," + listOfNodesSQL + ") done" );
                while ( set.next() ) {
                    interproTermCount = set.getInt( 1 );
                }
                set.close();

                // to summarize
                logger.info("interproTermType=" + interproTermType + " | interproTermId=" + interproTermId
                + " | interproTermDescription=" + interproTermDescription + " | interproTermCount="
                + interproTermCount);


                String directHighlightUrl = applicationPath
                        + "/source.do?keys=${selected-children}&clicked=${id}&type=${type}"
                        + randomParam;
                String hierarchViewURL = null;

                try {
                    hierarchViewURL = URLEncoder.encode( directHighlightUrl,
                            "UTF-8" );
                }
                catch ( UnsupportedEncodingException e ) {
                    logger.error( e );
                }

                // replace ${selected-children}, ${id} by the Interpro id and ${type} by Interpro
                logger.info( "direct highlight URL: " + directHighlightUrl );
                directHighlightUrl = SearchReplace.replace( directHighlightUrl,
                        "${selected-children}", interproTermId );
                directHighlightUrl = SearchReplace.replace( directHighlightUrl,
                        "${id}", interproTermId );
                directHighlightUrl = SearchReplace.replace( directHighlightUrl,
                        "${type}", interproTermType );
                logger.info( "direct highlight URL (modified): "
                        + directHighlightUrl );

                String quickInterproUrl = interproPath + "/DisplayIproEntry?ac=" + interproTermId +"&format=normal";

                String quickInterproGraphUrl = interproPath + "/DisplayInterproTerm?selected="
                        + interproTermId + "&intact=true&format=contentonly&url="
                        + hierarchViewURL + "&frame=_top";
                logger.info( "Xref: " + interproTermId );

                boolean selected = false;
                if ( selectedXRefs != null && selectedXRefs.contains( interproTermId ) ) {
                    logger.info( interproTermId + " SELECTED" );
                    selected = true;
                }

                urls.add( new SourceBean( interproTermId, interproTermType, interproTermDescription,
                        interproTermCount, quickInterproUrl, quickInterproGraphUrl, directHighlightUrl, selected,
                        applicationPath ) );
            }
        }

        // sort the source list by count
        Collections.sort(urls);

        return urls;
    } // getSourceUrls

    /**
     * Parse the set of key generate by the source and give back a collection of
     * keys.
     *
     * @param someKeys a string which contains some key separates by a character.
     * @return the splitted version of the key string as a collection of String.
     */
    public Collection parseKeys(String someKeys) {
        Collection keys = new Vector();

        if ( ( null == someKeys ) || ( someKeys.length() < 1 ) ) {
            return null;
        }

        StringTokenizer st = new StringTokenizer( someKeys, KEY_SEPARATOR );

        while ( st.hasMoreTokens() ) {
            String key = st.nextToken();
            keys.add( key );
        }

        return keys;
    }

    private DaoFactory getDaoFactory()
    {
        return IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
    }
}

