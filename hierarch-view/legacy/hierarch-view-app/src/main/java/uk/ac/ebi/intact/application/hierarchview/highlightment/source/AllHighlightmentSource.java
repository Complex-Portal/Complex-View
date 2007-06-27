package uk.ac.ebi.intact.application.hierarchview.highlightment.source;

import org.apache.log4j.Logger;
import uk.ac.ebi.intact.application.hierarchview.business.Constants;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.business.graph.GraphHelper;
import uk.ac.ebi.intact.application.hierarchview.business.graph.InteractionNetwork;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.Interactor;
import uk.ac.ebi.intact.model.Xref;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.util.simplegraph.BasicGraphI;
import uk.ac.ebi.intact.util.simplegraph.Node;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: alex
 * Date: 24 juin 2005
 * Time: 11:07:24
 * To change this template use File | Settings | File Templates.
 */

 /**
 * Interface allowing to wrap an highlightment source.
 *
 * @author Alexandre Liban (aliban@ebi.ac.uk)
 */
public class AllHighlightmentSource extends HighlightmentSource {

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
        private static final String SOURCE_KEY = "All";

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

            Collection listInterproTerm;
            IntactUserI user = (IntactUserI) IntactContext.getCurrentInstance().getSession()
                    .getAttribute( uk.ac.ebi.intact.application.hierarchview.business.Constants.USER_KEY );

            if ( null == user ) {
                logger.error( "No user found in the session, unable to search for all allowed source terms" );
                return null;
            }

            Interactor interactor = getDaoFactory().getProteinDao().getByAc(aProteinAC);

            // get Xref collection
            Collection xRef = interactor.getXrefs();
            logger.info( xRef.size() + " Xref found" );
            listInterproTerm = filterInteractorXref( xRef );

            return listInterproTerm;
        } // getKeysFromIntAct


        /**
         * get a collection of XRef and filter to keep only allowed source terms
         *
         * @param xRef the XRef collection
         * @return a Interpro term collection or an empty collection if none exists.
         */
        private Collection filterInteractorXref(Collection xRef) {
            Collection listAllSourceTerm = new ArrayList( xRef.size() ); // size will be >= to needed capacity
            Iterator xRefIterator = xRef.iterator();

            while ( xRefIterator.hasNext() ) {
                String[] allSourceTerm = new String[3];
                Xref xref = (Xref) xRefIterator.next();

                // get all the source terms allowed
                Properties properties = IntactUserI.HIGHLIGHTING_PROPERTIES;
                String sourceList = properties.getProperty ("highlightment.source.allowed");
                String[] listSource = sourceList.split(",");

                for(int i=1; i<listSource.length; i++) {

                    if ( ( xref.getCvDatabase().getShortLabel() ).toLowerCase().equals( listSource[i].toLowerCase() ) ) {
                        allSourceTerm[0] = listSource[i];
                        allSourceTerm[1] = xref.getPrimaryId();
                        allSourceTerm[2] = xref.getSecondaryId();
                        listAllSourceTerm.add( allSourceTerm );
                        logger.info( "Type : " + listSource[i] + " | Xref : " + xref.getPrimaryId() );
                    }
                }
            }

            return listAllSourceTerm;
        }


        /**
         * get a collection of the xrefs and filter to keep only allowed source terms.
         *
         * The collection has the acutally just one entry which is a map structure.
         * This map maps a source (e.g. GO) to a collection of strings which stores
         * the primary and the secondary IDs of the central proteins.
         *
         * Method is used when the graph was built with the mine database table
         *
         * @param xRef all xrefs of the centralnodes
         * @return a collection with just the allowed source terms
         */
        private Collection filterXref(Collection xRef) {
            Collection listAllSourceTerm = null;
            String[] sourceTerm;
            // set to keep track of the source Terms which are added to avoid duplicate
            // entries. This is needed because we are adding String[] to the actual
            // collection and the method 'contains' fails when testing if an array
            // (with the same entries) is already in the collection (even if it is
            // in)
            Set doubleEntrieCheckSet = new HashSet( xRef.size() );

            Iterator xRefIterator = xRef.iterator();
            if ( xRefIterator.hasNext() ) {
                // the source map is fetched Map<String, Collection<String> >
                Map sourceMap = (Map) xRefIterator.next();

                Collection sourceTerms = (Collection) sourceMap.get( SOURCE_KEY );
                // the collection stores the filterd xrefs - it has the size of the
                // number of GO terms divided by 2 because in the goTerms collection
                // two following entries are represanting one GO Term (even: primary
                // ID, odd: secondary ID)
                listAllSourceTerm = new ArrayList( sourceTerms.size() / 2 );

                Iterator sourceIterator = sourceTerms.iterator();
                while ( sourceIterator.hasNext() ) {
                    sourceTerm = new String[3];
                    // the two entries of the collection are stored as one source Term
                    sourceTerm[0] = (String) sourceIterator.next();
                    sourceTerm[1] = (String) sourceIterator.next();
                    sourceTerm[2] = (String) sourceIterator.next();

                    // if both entries are not already in the collection they are
                    // added to the list of all allowed source Terms
                    if ( !doubleEntrieCheckSet.contains( sourceTerm[1] )
                            && !doubleEntrieCheckSet.contains( sourceTerm[2] ) ) {
                        listAllSourceTerm.add( sourceTerm );
                        doubleEntrieCheckSet.add( sourceTerm[0] );
                        doubleEntrieCheckSet.add( sourceTerm[1] );
                        doubleEntrieCheckSet.add( sourceTerm[2] );
                    }
                }
            }
            return listAllSourceTerm;
        }


        /**
         * Returns a collection of nodes to highlight for the display
         *
         * @param aGraph the network to display
         * @param selectedSourceTerm the selected source Term
         * @param children the children source Terms of the selected source Term
         * @param searchForChildren whether it shall be searched for the children
         * @param user
         * @return
         * @throws IntactException
         * @throws java.sql.SQLException
         */
        private Collection proteinToHighlightInteractor(InteractionNetwork aGraph,
                String selectedSourceTerm, String selectedSourceTermType, Collection children,
                boolean searchForChildren, IntactUserI user) throws SQLException,
                IntactException {

            // if the source highlight map of the network is empty
            // it is filled with the source informations from each
            // node of the graph
            if ( aGraph.isSourceHighlightMapEmpty() ) {
                GraphHelper gh = new GraphHelper( user );

                ArrayList listOfNode = aGraph.getOrderedNodes();
                int size = listOfNode.size();
                logger.info("Size of list of nodes : " + size);

                for (int i = 0; i < size; i++) {
                    BasicGraphI node = (BasicGraphI) listOfNode.get( i );
                    // add all sources to the source highglighting map
                    // TODO: every node is set as non central node....
                    // this was done because the additional information which is
                    // added to the central node is not used when the graph is not
                    // built by the mine database table
                    logger.info("node " + node + " added");
                    gh.addSourcesToNode( node, false, aGraph );
                }
            }
            // return the set of proteins to highlight based on the source
            // highlighting map of the graph
            return proteinToHighlightSourceMap( aGraph, children, selectedSourceTerm,
                    selectedSourceTermType, searchForChildren );
        }


        private Collection proteinToHighlightDatabase(HttpSession aSession,
                InteractionNetwork aGraph, String selectedSourceTerm,
                Collection children, boolean searchForChildren) {
            Collection nodeList = new ArrayList( 20 ); // should be enough for 90% cases
            ArrayList listOfNode = aGraph.getOrderedNodes();
            int size = listOfNode.size();
            String[] sourceTermInfo = null;
            String sourceTerm = null;
            for (int i = 0; i < size; i++) {
                Node node = (Node) listOfNode.get( i );
                String ac = node.getAc();

                // Search all source Terms for this ac number
                Collection listSourceTerm = this.getKeysFromIntAct( ac, aSession );
                if ( ( listSourceTerm != null ) && ( listSourceTerm.isEmpty() == false ) ) {
                    Iterator list = listSourceTerm.iterator();
                    while ( list.hasNext() ) {
                        sourceTermInfo = (String[]) list.next();
                        sourceTerm = sourceTermInfo[0];

                        if ( selectedSourceTerm.equals( sourceTerm ) ) {
                            nodeList.add( node );
                            break;
                        }

                        //                    if (searchForChildren == true) {
                        Iterator it = children.iterator();
                        while ( it.hasNext() ) {
                            String newSourceTerm = (String) it.next();
                            if ( newSourceTerm.equals( sourceTerm ) ) {
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
         * @param children the children of the selected source Term
         * @param selectedSourceTerm the selected source Term
         * @param searchForChildren whether it shall be searched for the children
         * @return list of nodes
         */
        private Collection proteinToHighlightSourceMap(InteractionNetwork aGraph,
                Collection children, String selectedSourceTerm, String selectedSourceTermType,
                boolean searchForChildren) {

            Collection nodeList = new ArrayList( 20 ); // should be enough for 90% cases

            // retrieve the set of proteins to highlight for the source key (e.g.
            // GO) and the source Term
            logger.info("Source = " + SOURCE_KEY + " | selectedSourceTerm = " + selectedSourceTerm);
            Set proteinsToHighlight = aGraph.getProteinsForHighlight( SOURCE_KEY,
                    selectedSourceTerm );
            logger.info("proteinsToHighlight = " + proteinsToHighlight);

            // if we found any proteins we add all of them to the collection
            if ( proteinsToHighlight != null ) {
                nodeList.addAll( proteinsToHighlight );
            }

            /*
             * for every children all proteins related to the current source term are
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

            IntactUserI user = (IntactUserI) IntactContext.getCurrentInstance().getSession().getAttribute( Constants.USER_KEY );
            Collection children = user.getKeys();
            String selectedSourceTerm = user.getSelectedKey();
            String selectedSourceTermType = user.getSelectedKeyType();

            logger.info("getKeys="+ children);

            if ( children.remove( selectedSourceTerm ) ) {
                logger.info( selectedSourceTerm + " removed from children collection" );
            }

            // get source option
            String check = (String) user.getHighlightOption( ATTRIBUTE_OPTION_CHILDREN );
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
                logger.info("return to : proteinToHighlightSourceMap");
                return proteinToHighlightSourceMap( aGraph, children,
                        selectedSourceTerm, selectedSourceTermType, searchForChildren );
            }
            else {
                try {
                    logger.info("return to : proteinToHighlightInteractor");
                    return proteinToHighlightInteractor( aGraph, selectedSourceTerm,
                            selectedSourceTermType, children, searchForChildren, user );
                }
                catch ( Exception e ) {
                    logger.info("return to : proteinToHighlightDatabase");
                    return proteinToHighlightDatabase( aSession, aGraph,
                            selectedSourceTerm, children, searchForChildren );
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

            // filter to keep only the allowed source terms
            logger.info( xRefs.size() + " Xref before filtering" );
            Collection listAllSourceTerm = null;
            if ( GraphHelper.BUILT_WITH_MINE_TABLE ) {
                listAllSourceTerm = filterXref( xRefs );
            }
            else {
                listAllSourceTerm = filterInteractorXref( xRefs );
            }

            logger.info( listAllSourceTerm.size() + " source term(s) after filtering" );


            // create url collection with exact size
            List urls = new ArrayList( listAllSourceTerm.size() );

            // get the list of all the source terms allowed
            Properties properties = IntactUserI.HIGHLIGHTING_PROPERTIES;
            String sourceList = properties.getProperty ("highlightment.source.allowed");

            // get the delimiter token
            String delimiter = IntactUserI.HIGHLIGHTING_PROPERTIES.getProperty( "highlightment.source.token" );

            // split the list with the delimiter
            String[] listSource = sourceList.split(delimiter);

            
            // sent the source terms to their implementation classes
            List tmp = null;
            logger.info("sending source terms to their implementation classes...");

            for(int i=1; i<listSource.length; i++) {
                HighlightmentSource source = HighlightmentSource.getHighlightmentSource(properties.getProperty("highlightment.source." + listSource[i] + ".class"));
                tmp = source.getSourceUrls( xRefs, selectedXRefs, applicationPath, user );
                urls.addAll(tmp);
                tmp = null;
            }

            logger.info("all source terms have been sent successfully");

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
