package uk.ac.ebi.intact.webapp.search.struts.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.Protein;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.webapp.search.business.interpro.InterproConstants;
import uk.ac.ebi.intact.webapp.search.business.interpro.InterproSearch;
import uk.ac.ebi.intact.webapp.search.business.interpro.ThresholdExceededException;
import uk.ac.ebi.intact.webapp.search.struts.util.SearchConstants;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * This Action class performs the determination of UniprotKB IDs out of IntAct ACs. <br> These UniprotKB IDs are then
 * used to build up dynamically a link to the corresponding entries in the InterPro Database, like
 * <br><b>http://www.ebi.ac.uk/interpro/ISpy?ac=Q9UKX3%2CQ9UK45%2CP62312</b><br> where <i>Q9UKX3</i>, <i>Q9UK45</i> and
 * <i>P62312</i> specify the UniprotKB IDs related to every IntAct-Protein.
 * <p>
 * Dependent on the user's selection, one has to distinguish from 3 cases:<br> <ol> <li> all of the selected Proteins do
 * have an UniProtKB ID, so it will be forwarded to the <i>Waiting Page</i> <li> no Protein at all does have an
 * UniProtKB ID, so it will be forwarded to the general <i>Error Page</i> <li> at least one Protein do have an UniProtKB
 * ID and at least one Protein has none, so it will be forwarded to the <i>Waiting Page</i> as well, but including one
 * ore more warning hints. </ol>
 * <p>
 * Furthermore, the htmlView corresponding to the Warning-page will be build within this <code>Action Class</code>.
 *
 * @author Christian Kohler (ckohler@ebi.ac.uk)
 * @version $Id$
 */
public class InterProSearchAction extends AbstractResultAction {

    private static final Log logger = LogFactory.getLog(InterProSearchAction.class);

    protected String processResults( HttpServletRequest request ) {

        // Collects the parameters of the request
        //Gets the IntAct ACs from the Proteins ticked by the user within the search webapp
        String intactACs = request.getParameter( "ac" );

        StringTokenizer intactAcTokenizer = new StringTokenizer( intactACs, "," );

        //quantity of selected IntAct ACs
        int intactACsLength = intactAcTokenizer.countTokens();

        // Parses the ACs and separates the protein IDs
        // the parsed parameters of the request are stored in a HasSet, in order to avoid doubled entries
        HashSet proteinACset;

        proteinACset = parseInputParameter( intactACs );

        // contains all UniProt ID we will forward to InterPro
        Set mappedProteins = new HashSet( InterproSearch.MAXIMUM_NUMBER_OF_SELECTED_PROTEINS );

        // Set of Protein where no UniProtId was found
        Set unmappedProteins = new HashSet();

        // key = shortlabel of Protein ,   value=uniprotkb ID, used in the displayed html-message
        Map proteinsWithUniprotKB = new HashMap();

        // indicates if InterproSearch.MAXIMUM_NUMBER_OF_SELECTED_PROTEINS is exceeded
        boolean thresholdExceeded = false;

        // Searches for UniProt primary ID by iterating through the HashSet
        Iterator it = proteinACset.iterator();

        try {
            while ( it.hasNext() ) {

                //indicates a IntAct ProteinAC
                String proteinAC = (String) it.next();
                try {
                    // searches for an annotated object
                    AnnotatedObject ao = searchAO( proteinAC );

                    // if ao is a Protein Object
                    if ( ao instanceof Protein ) {

                        InterproSearch.findUniprotId( (Protein) ao, mappedProteins,
                                                      unmappedProteins, proteinsWithUniprotKB );
                    }
                    //if ao is an Interaction Object
                    else if ( ao instanceof Interaction ) {
                        Collection proteins = InterproSearch.getProteins( (Interaction) ao );
                        for ( Iterator iterator = proteins.iterator(); iterator.hasNext(); ) {
                            Protein protein = (Protein) iterator.next();

                            InterproSearch.findUniprotId( protein, mappedProteins,
                                                          unmappedProteins, proteinsWithUniprotKB );
                        }
                    }

                    //if ao is an Experiment Object
                    else if ( ao instanceof Experiment ) {

                        Collection interactions = InterproSearch.getProteins( (Experiment) ao );
                        for ( Iterator iterator = interactions.iterator(); iterator.hasNext(); ) {
                            Interaction interaction = (Interaction) iterator.next();

                            Collection proteins = InterproSearch.getProteins( interaction );
                            for ( Iterator iterator1 = proteins.iterator(); iterator1.hasNext(); ) {
                                Protein protein = (Protein) iterator1.next();

                                InterproSearch.findUniprotId( protein, mappedProteins,
                                                              unmappedProteins, proteinsWithUniprotKB );
                            }
                        }
                    }
                } catch ( IntactException e ) {
                    logger.error( "Error while resolving \"InterproSearch.findUniprotId (" + proteinAC + ")\"", e );
                }
            } // end while
        } catch ( ThresholdExceededException tee ) {
            // occurs, if the maximum number of displayable Proteins is exceeded => flag (= boolean) is set
            logger.info( "Threshold exceeded." );
            thresholdExceeded = true;
        }

        // if none of the selected Proteins do have an UniProt ID -> forward to general Error page
        if ( mappedProteins.size() == 0 && unmappedProteins.size() != 0 ) {

            //Clears error container
            super.clearErrors();

            //iterates through  List of unmapped Proteins (= Proteins, where no UniprotID was found...)
            Iterator iterator = unmappedProteins.iterator();

            // unmapped Protein objects are appended to a Stringbuffer in order to have access to Protein
            // specific properties eg. Shortlabel, AC, ...
            StringBuffer buffer = new StringBuffer( 512 );
            Protein protein;
            while ( iterator.hasNext() ) {

                protein = (Protein) iterator.next();
                buffer.append( protein.getShortLabel() ).append( " (" );
                buffer.append( protein.getAc() ).append( ") " );

                if ( iterator.hasNext() ) {
                    buffer.append( "<br>" );
                }
            }

            //String contains all unmapped Proteins (e.g. P1, P2, P3, ...)
            String finalUnmappedProteins = buffer.toString();

            //Adds an error with given key ( "error.search.no.uniprot.id" ) and value.
            super.addError( "error.search.no.uniprot.id", "<br><br><b><font color=\"red\">"
                                                          + finalUnmappedProteins + "</font><br><br><p>" +
                                                          " <font size =\"3\">" +
                                                          "Please search again, using the IntAct Searchfield on the left</font></p>" );

            //Saves the errors in given request for <struts:errors> tag.
            super.saveErrors( request );

            // forwards to general Error page
            return SearchConstants.FORWARD_FAILURE;
        } //end 2. if

        /* iterates through List of mapped Proteins and appends them to a StringBuffer*/
        Iterator iterator = mappedProteins.iterator();

        StringBuffer mappedProteinsBuffer = new StringBuffer( 128 );
        while ( iterator.hasNext() ) {
            String id = (String) iterator.next();
            mappedProteinsBuffer.append( id );
            if ( iterator.hasNext() ) {
                mappedProteinsBuffer.append( ',' );
            }
        }

        //String contains the UniprotKB IDs of all mapped Proteins (e.g. P62312,P62312,Q9UK45,Q9UK45 ...)
        String uniprotIds = mappedProteinsBuffer.toString();

        // there is at least one Protein, having an UniprotKB ID.
        if ( mappedProteins.size() != 0 ) {
            // avoid multiple UniprotId output in waiting-page. HashSet contains only unique UniProtIDs
            HashSet uniprotIdSet;

            // String uniprotIds contains doubled entries, so we have to parse this String
            uniprotIdSet = parseInputParameter( uniprotIds );

            // Builds final URL to the InterPro website. URL contains no doubled entries
            String interproURL = createInterproURL( uniprotIdSet );

            // used to append all Html-statements that will finally create the Html-Message
            StringBuffer htmlBuffer = new StringBuffer( 512 );

            // creates the Html-message, displayed in waiting.jsp
            buildHtmlMessage( htmlBuffer, unmappedProteins, thresholdExceeded,
                              intactACsLength, proteinsWithUniprotKB, interproURL, request );

            String htmlMsg = htmlBuffer.toString();

            logger.info( "Buffer Size (original size:512): " + htmlBuffer.length() );

            // sets Attribute, defined in SearchConstants.java
            request.setAttribute( SearchConstants.WAITING_MSG, htmlMsg );

            // sets Attribute, defined in SearchConstants.java
            request.setAttribute( SearchConstants.WAITING_URL, interproURL );
        }
        // Forwarding to the waiting page
        return SearchConstants.FORWARD_WAITING_PAGE;
    }

    /**
     * Builds final URL to the InterPro website. URL contains no doubled entries.
     *
     * @param uniprotIdSet the <code>Set</code> containing only unique UniProtIDs.
     *
     * @return the <code>URL</code> to InterPro.
     */
    private String createInterproURL( HashSet uniprotIdSet ) {
        Iterator i = uniprotIdSet.iterator();
        StringBuffer interproUrlBuffer = new StringBuffer( 512 );
        StringBuffer uniprotIdBuffer = new StringBuffer( 512 );
        interproUrlBuffer.append( InterproConstants.INTERPRO_URL );

        while ( i.hasNext() ) {
            String id = (String) i.next();
            interproUrlBuffer.append( id );
            uniprotIdBuffer.append( id );
            if ( i.hasNext() ) {
                interproUrlBuffer.append( InterproConstants.PROTEIN_ID_SEPARATOR );
                uniprotIdBuffer.append( ", " );
            }
        }
        // the final URL (e.g.: http://www.ebi.ac.uk/interpro/ISpy?ac=Q9UKX3%2CQ9UK45%2CP62312 )
        return interproUrlBuffer.toString();
    }

    /**
     * Builds the Html-View for the waiting page.
     *
     * @param htmlBuffer        used to append all Html-statements that will finally create the Summary-Message.
     * @param unmappedProteins  contains all Proteins having no UniprotKB ID.
     * @param thresholdExceeded indicates if the total number of intact ACs has been exceeded (default value: 20).
     * @param intactACsLength   total number of intact ACs.
     * @param proteinMap        contains: key = shortlabel of Protein , value = corresponding uniprotkb ID.
     * @param interproURL       the <code>URL</code> to InterPro.
     * @param request           the <code>HttpServletRequest</code>.
     */
    private void buildHtmlMessage( StringBuffer htmlBuffer, Set unmappedProteins,
                                   boolean thresholdExceeded, int intactACsLength,
                                   Map proteinMap, String interproURL,
                                   HttpServletRequest request ) {

        // Constructs a simple html-table to organize the output.
        // start of html-table !
        htmlBuffer.append( "<table border=\"0\">" );

        // Message if an UniprotKB ID could be found for every selected Protein
        createUniprotFoundMessage( htmlBuffer, intactACsLength, proteinMap, request );

        // an error occurs. Either a proteins without an uniprotKB ID was found OR the maximum number of
        // selectable Proteins was exceeded.
        if ( unmappedProteins.size() != 0 || thresholdExceeded ) {

            //Builds the html-View Header for that part of the message where problems
            //during the processing of the user request occured.
            createProblemsOccuredMessage( htmlBuffer );

            String unmappedProteinsString = unmappedProteinsIterator( unmappedProteins );

            // only displayed if there are Proteins not having an UniProtKB ID...
            if ( unmappedProteins.size() != 0 ) {
                createUniprotNotFoundMessage( htmlBuffer, unmappedProteinsString, request );
            }

            //only displayed if MAXIMUM_NUMBER_OF_SELECTED_PROTEINS is exceeded...
            if ( thresholdExceeded ) {
                createThresholdExceededMessage( htmlBuffer, request );
            }

            // forwards to InterPro. Button only appears in case of a warning
            createButton( InterproConstants.CONTINUE_BUTTON_LABEL, htmlBuffer, interproURL );

            // closes the active window and user can start a new Search
            createCloseWindowButton( InterproConstants.NEW_SEARCH_BUTTON_LABEL, htmlBuffer );
        }

        // end of html-table
        htmlBuffer.append( "</table>" );
    }

    /**
     * Iterates through a Set of unmapped Proteins (meaning: no corresponding UniprotKB ID was found) and appends the
     * Protein-shortlabel and Protein-AC to a StringBuffer afterwards.
     *
     * @param unmappedProteins contains those Proteins, where no UniprotKB ID exists.
     *
     * @return proteinsWithoutUniprotidBuffer a <code>String</code> containing the shortlabel and proteinAC. of those
     *         Proteins where no uniprotKB ID is available (e.g.: myome-afcs_mouse (EBI-659248), fnlb-afcs_mouse
     *         (EBI-688023)...)
     */
    private String unmappedProteinsIterator( Set unmappedProteins ) {
        Iterator unmappedProteinsIterator = unmappedProteins.iterator();
        StringBuffer proteinsWithoutUniprotidBuffer = new StringBuffer( 512 );
        int count = 1;
        boolean breakLine;
        while ( unmappedProteinsIterator.hasNext() ) {
            // produces a line-break after every four Proteins
            breakLine = ( count % 4 ) == 0;
            Protein protein = (Protein) unmappedProteinsIterator.next();
            proteinsWithoutUniprotidBuffer.append( protein.getShortLabel() ).append( ' ' );
            proteinsWithoutUniprotidBuffer.append( '(' ).append( protein.getAc() ).append( ')' );
            if ( unmappedProteinsIterator.hasNext() ) {
                proteinsWithoutUniprotidBuffer.append( ", " );
                if ( breakLine ) {
                    proteinsWithoutUniprotidBuffer.append( "<br>" );
                }
            }
            count++;
        }
        return proteinsWithoutUniprotidBuffer.toString();
    }

    /**
     * Builds the html-View for that part of the message where an UniprotKB ID could be found for every Intact-AC and no
     * error/warning occured.
     *
     * @param htmlBuffer      used to append all Html-statements that will finally create the Summary-Message.
     * @param intactACsLength total number of intact ACs.
     * @param proteinMap      contains: <code>key</code> = shortlabel of Protein , <code>value</code> = uniprotkb ID.
     * @param request         the <code>HttpServletRequest</code>.
     */
    private void createUniprotFoundMessage( StringBuffer htmlBuffer,
                                            int intactACsLength,
                                            Map proteinMap,
                                            HttpServletRequest request ) {
        // no delay when forwarding
        String shortTime;
        htmlBuffer.append( "<font color=\"#999999\">" );
        htmlBuffer.append( "<u>Step 1:</u>" ).append( "      Converted " ).append( intactACsLength ).
                append( " IntAct AC(s) into UniProtKB ID (s):" ).append( "<br>" );
        htmlBuffer.append( "</font>" );
        htmlBuffer.append( "<br>" );
        htmlBuffer.append( "<font size =\"3\">" );
        htmlBuffer.append( "<u>Step 2:</u>" ).append( "       Searching InterPro ID by UniProtKB ID: " ).append( "</font>" ).
                append( "<br>" ).append( "<br>" );
        htmlBuffer.append( "<img src= ../images/check.gif width=\"16\" height=\"16\">" ).
                append( "      UniProtKB ID(s) found:   (  <i>IntAct AC</i>   ==> <b>UniProtKB ID</b> )" );
        htmlBuffer.append( "<br>" );

        int count = 1;
        boolean breakLine;
        htmlBuffer.append( "<p align =\"left\">" );
        Iterator it = proteinMap.entrySet().iterator();
        while ( it.hasNext() ) {
            breakLine = ( count % 4 ) == 0;
            Map.Entry entry = (Map.Entry) it.next();
            htmlBuffer.append( "<big><i>" ).append( entry.getKey() ).append( "</i></big>" )
                    .append( " ==> " ).append( "<b><big>" ).
                    append( entry.getValue() )
                    .append( "</big></b>" );
            if ( it.hasNext() ) {
                htmlBuffer.append( ", " );
                if ( breakLine ) {
                    htmlBuffer.append( "<br>" );
                }
            }
            count++;
        }
        //indicates, that there are still more Proteins remaining.
        htmlBuffer.append( "<big>" ).append( " . . . " ).append( "</big>" );
        htmlBuffer.append( "</p>" );
        htmlBuffer.append( "<tr><td>" ).append( "</tr></td>" );

        //no errors occur =>  1 sec delay when forwarding to InterPro
        request.setAttribute( SearchConstants.WAITING_TIME, SearchConstants.DEFAULT_WAITING_TIME );
    }

    /**
     * Builds the html-View for that part of the message where no UniProtKB ID was found.
     *
     * @param htmlBuffer             used to append all Html-statements that will finally create the Summary-Message.
     * @param unmappedProteinsString contains those Proteins, where no UniprotKB ID is existing.
     * @param request                the <code>HttpServletRequest</code>.
     */
    private void createUniprotNotFoundMessage( StringBuffer htmlBuffer,
                                               String unmappedProteinsString,
                                               HttpServletRequest request ) {
        //delay when forwarding to InterPro !!
        String longTime;

        htmlBuffer.append( "<img src= ../images/warning.gif width=\"18\" height=\"17\">" );
        htmlBuffer.append( "<font size =\"3\">    No UniProtKB ID(s) found for: </font>" );
        htmlBuffer.append( "<br>" ).append( "<b><big>" ).append( unmappedProteinsString ).append( "</big></b>" );
        htmlBuffer.append( "<br><br>" );
        htmlBuffer.append( "<font size =\"3\">" );
        htmlBuffer.append( "<tr><td>" ).append( "</tr></td>" );
        htmlBuffer.append( "<br>" );

        // We are going to display an error message, we do not want a forward.
        request.setAttribute( SearchConstants.DO_NOT_FORWARD, Boolean.TRUE );
    }

    /**
     * Builds the html-View Header for that part of the message where problems during the processing of the user request
     * occured.
     *
     * @param htmlBuffer used to append all Html-statements that will finally create the Summary-Message.
     */
    private void createProblemsOccuredMessage( StringBuffer htmlBuffer ) {
        htmlBuffer.append( "<br>" );
        htmlBuffer.append( "<h5>" ).append( "<font color=\"red\">" );
        htmlBuffer.append( "<big>" );
        htmlBuffer.append( " Some problems occured during the processing of your request: " );
        htmlBuffer.append( "</big>" );
        htmlBuffer.append( "</h5>" ).append( "</font>" );
    }


    /**
     * Builds the html-View for that part of the message where the Maximum Number of selectable Proteins has been
     * exceeded.
     *
     * @param htmlBuffer used to append all Html-statements that will finally create the Summary-Message.
     * @param request    the <code>HttpServletRequest</code>.
     */
    private void createThresholdExceededMessage( StringBuffer htmlBuffer, HttpServletRequest request ) {
        String longTime;
        htmlBuffer.append( "<img src= ../images/warning.gif width=\"18\" height=\"17\">" );
        htmlBuffer.append( "<font size =\"3\">" );
        htmlBuffer.append( " You have requested the InterPro domain architecture of more than " );
        htmlBuffer.append( InterproSearch.MAXIMUM_NUMBER_OF_SELECTED_PROTEINS ).append( " proteins." );
        htmlBuffer.append( "<br>" );
        htmlBuffer.append( "Due to system limitations, we can only display" );
        htmlBuffer.append( " the domain architecture for" );
        htmlBuffer.append( " the first " );
        htmlBuffer.append( InterproSearch.MAXIMUM_NUMBER_OF_SELECTED_PROTEINS ).append( " proteins only!" );
        htmlBuffer.append( "</font>" );
        htmlBuffer.append( "<tr><td>" ).append( "</tr></td>" );
        htmlBuffer.append( "<br><br><br>" );

        // We are going to display an error message, we do not want a forward.
        request.setAttribute( SearchConstants.DO_NOT_FORWARD, Boolean.TRUE );
    }

    /**
     * Builds a simple Html-Button.
     *
     * @param name       the name of the Button.
     * @param htmlBuffer used to append all Html-statements that will finally create the Summary-Message.
     * @param url        the <code>Url</code> to forward to.
     */
    private void createButton( String name, StringBuffer htmlBuffer, String url ) {
        htmlBuffer.append( "<form>" );
        htmlBuffer.append( "<input type=button value=\"" );
        htmlBuffer.append( name );
        htmlBuffer.append( "\"" );
        htmlBuffer.append( " onClick=\"self.location=\'" );
        htmlBuffer.append( url );
        htmlBuffer.append( "\'\"" );
        htmlBuffer.append( ">" );
        htmlBuffer.append( "</form>" );
    }

    /**
     * Builds a simple Html-Button.
     *
     * @param name       the name of the Button.
     * @param htmlBuffer used to append all Html-statements that will finally create the Summary-Message.
     */
    private void createCloseWindowButton( String name, StringBuffer htmlBuffer ) {
        htmlBuffer.append( "<form>" );
        htmlBuffer.append( "<input type=button value=\"" );
        htmlBuffer.append( name );
        htmlBuffer.append( "\"" );
        htmlBuffer.append( " onClick=\"javascript:window.close();\"" );
        htmlBuffer.append( ">" );
        htmlBuffer.append( "</form>" );
    }

    /**
     * Determines the specific AnnotatedObject object, found for the specific parameter value.
     *
     * @param ac the <code>String</code> of the search Value (here: "ac") of an <code>AnnotatedObject</code> object
     *           (e.g. "shortlabel", "AC", ...) to be searched for.
     *
     * @return the specific <code>AnnotatedObject</code> object, found for the specific parameter value.
     *
     * @throws IntactException if an error occurs.
     */
    public AnnotatedObject searchAO( String ac ) throws IntactException {

        AnnotatedObject ao = getDaoFactory().getProteinDao().getByAc(ac);

        if (ao != null)
        {
            return ao;
        }

        ao = getDaoFactory().getInteractionDao().getByAc(ac);

        if (ao != null)
        {
            return ao;
        }

        ao = getDaoFactory().getExperimentDao().getByAc(ac);

        if (ao != null)
        {
            return ao;
        }

        // should never be reached
        return null;
    }

    /**
     * Parses a <code>String</code>, which might include doubled entries, into a <code>HashSet</code>. By using a
     * HashSet, it is guaranteed that every entry in it is unique.
     *
     * @param ac the <code>String</code> to parse.
     *
     * @return proteinACset  the <code>HashSet</code> with unique entries.
     */
    private HashSet parseInputParameter( String ac ) {
        StringTokenizer st = new StringTokenizer( ac, "," );
        HashSet proteinACset = new HashSet( 32 );
        while ( st.hasMoreTokens() ) {
            String id = st.nextToken();

            if ( ! proteinACset.contains( id ) ) {
                proteinACset.add( id );
            }
        }
        return proteinACset;
    }

    private DaoFactory getDaoFactory()
    {
        return IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
    }
}