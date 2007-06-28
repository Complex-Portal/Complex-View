/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.webapp.search.struts.view.beans;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.util.SearchReplace;

import java.util.*;

/**
 * <p/>
 * A bean used to support display of a single Protein. This view is different to other AnnotatedObjects and so must be
 * handled seperately. The bean is used by JSPs to provide easy access to the Protein's data in a form suitable for
 * display in a web page. Xrefs of the Protein are an exception, because they each contain detailed String data and
 * therefore the JSP itself should extract what it requires from each Xref for display. </p>
 * <p/>
 * The methods available from this bean are based on the data  required by new search interface mock pages, created June
 * 2004. According to that simple web page, the information to be supplied from this bean is as follows: <ul> <li>Intact
 * name</li> <li>Source (ie BioSource beans)</li> <li>Description (ie the full name)</li> <li>gene names (found via the
 * Aliases)</li> <li>Xrefs</li> <li>sequence length</li> <li>CRC64 checksum</li> <li>the Protein sequence itself</li>
 * </ul> </p>
 *
 * @author Chris Lewington
 * @version $Id$
 */
public class InteractorViewBean extends AbstractViewBean {

    private static final Log logger = LogFactory.getLog(InteractorViewBean.class);

    /**
     * The Protein for which the view is required.
     */
    private Interactor interactor;

    /**
     * Map of retrieved DB URLs already retrieved from the DB. This is basically a cache to avoid recomputation every
     * time a CvDatabase URL is requested.
     */
    private Map dbUrls;

    /**
     * String URL for searching on the Protein itself
     */
//    private String protSearchURL; // 3 usage in this class
    private String interactorSearchURL;

    /**
     * String URL for searching on the Protein's BioSource (uses the source AC)
     */
    private String bioSearchURL;

    /**
     * The bean constructor requires a Protein to wrap, plus beans on the context path to the search webapp and the
     * help link.
     *
     * @param interactor  The Interactor whose beans are to be displayed
     */
    public InteractorViewBean( Interactor interactor ) {
        super( );
        dbUrls = new HashMap();
        this.interactor = interactor;
    }

    /**
     * Adds the shortLabel of the Protein to an internal list used later for highlighting in a display. NOT SURE IF WE
     * STILL NEED THIS!!
     */
    public void initHighlightMap() {
        Set set = new HashSet( 1 );
        set.add( interactor.getShortLabel() );
        setHighlightMap( set );
    }


    /**
     * Returns the help section.
     */
    public String getHelpSection() {
        return "protein.single.view";
    }

    /**
     * Basic accessor, provided in case anything ever needs access to the wrapped object.
     *
     * @return Protein the Protein instance wrapped by this view bean.
     */
    public Interactor getInteractor() {
        return interactor;
    }

    /**
     * Returns the shortlabel of the Protein as String
     *
     * @return String the shortlabel of the Protein
     */
    public String getInteractorIntactName() {
        return interactor.getShortLabel();
    }

    public String getInteractorType() {
        CvInteractorType cvInteractorType = interactor.getCvInteractorType();
        if ( cvInteractorType != null && cvInteractorType.getShortLabel() != null ) {
            return cvInteractorType.getShortLabel();
        } else {
            logger.error( "Interactor " + interactor.getAc() + " having no interactorType." );
            if ( interactor instanceof Protein ) {
                return "Protein";
            } else if ( interactor instanceof NucleicAcid ) {
                return "Nucleic Acid";
            }  else if ( interactor instanceof SmallMolecule ) {
                return "Small Molecule";
            } else {
                return "Interactor";
            }
        }

    }

    /**
     * Returns the Ac of the wrapped Protein Object
     *
     * @return String the Ac of the Protein
     */
//    public String getProteinAc() { // 2 usage in singleProtein.jsp
    public String getInteractorAc() {
        return interactor.getAc();
    }

    /**
     * NB In the webpage mockup, this is a hyperlink to the BioSource help page...
     *
     * @return String the fullname of the Protein
     */
//    public String getProteinDescription() { // 2 usage in singleProtein.jsp
    public String getInteractorDescription() {
        return interactor.getFullName();
    }

    /**
     * Provides the AC for this Protein's BioSource.
     *
     * @return String the BioSource AC
     */
    public String getBioAc() {
        BioSource bioSource = interactor.getBioSource();

        if (bioSource != null)
        {
            return bioSource.getAc();
        }

        return null;
    }

    /**
     * Provides the Intact Name used to identify this Protein's BioSource.
     *
     * @return String the BioSource's intact name (ie shortLabel)
     */
    public String getBioIntactName() {
        return interactor.getBioSource().getShortLabel();
    }

    /**
     * Provides the full name of this Protein's BioSource.
     *
     * @return String the BioSource's 'common' name.
     */
    public String getBioSourceName() {
        BioSource bioSource = interactor.getBioSource();

        if (bioSource != null)
        {
            return bioSource.getShortLabel();
        }

        return "-";
    }

    /**
     * Provides a String representation of a URL to perform a search on this Protein's BioSource beans (curently via
     * AC)
     *
     * @return String a String representation of a search URL link for BioSource
     */
    public String getBioSearchURL() {

        if ( bioSearchURL == null ) {
            //set it on the first call
            String ac = getBioAc();

            if (ac != null)
            {
                bioSearchURL = super.getSearchLink() + getBioAc() + "&amp;searchClass=BioSource";
            }
        }

        return bioSearchURL;
    }

    /**
     * Provides a String representation of a URL to perform a search on this Protein's beans (curently via AC)
     *
     * @return String a String representation of a search URL link for Protein
     */
    public String getInteractorSearchURL() {

        if ( interactorSearchURL == null ) {
            //set it on the first call
            interactorSearchURL = super.getSearchLink() + interactor.getAc() + "&amp;searchClass=Protein&amp;filter=ac";
        }
        return interactorSearchURL;
    }

    /**
     * Provides a String representation of a URL to access the CV related to the Xref (ie the Cv beans describing the
     * Xref's database).
     *
     * @param xref The Xref for which the URL is required
     *
     * @return String a String representation of a URL link for the Xref beans (CvDatabase)
     */
    public String getCvDbURL( Xref xref ) {

        return ( super.getSearchLink() + xref.getCvDatabase().getAc() + "&amp;searchClass=CvDatabase&amp;filter=ac" );
    }

    /**
     * Generated a link showing the count of distinct interactions related to a protein and link to them. The
     * Interactions are selected of the components and then a non redondant list of AC is generated.
     *
     * @return a link showing the count of distinct interactions related to a protein and link to them
     */
    public int getInteractionsCount() {

        // count the number of interaction to which related that protein
        return getDaoFactory().getInteractorDao().countInteractionsForInteractorWithAc(interactor.getAc());
    }

    /**
     * Provides a String representation of a URL to access the CV qualifier info related to the Xref (ie the Cv beans
     * describing the Xref's qualifier info).
     *
     * @param xref The Xref for which the URL is required
     *
     * @return String a String representation of a URL link for the Xref beans (CvXrefQualifier)
     */
    public String getCvQualifierURL( Xref xref ) {

        return ( super.getSearchLink() + xref.getCvXrefQualifier().getAc() + "&amp;searchClass=CvXrefQualifier&amp;filter=ac" );
    }


    /**
     * Provides a String representation of a URL to provide acces to an Xrefs' database (curently via AC). The URL is at
     * present stored via an Annotation for the Xref in the Intact DB itself.
     *
     * @param xref The Xref for which the DB URL is required
     *
     * @return String a String representation of a DB URL link for the Xref, or a '-' if there is no stored URL link for
     *         this Xref
     */
    public String getPrimaryIdURL( Xref xref ) {
        // Check if the id can be hyperlinked
        String searchUrl = (String) dbUrls.get( xref.getCvDatabase() );
        if ( searchUrl == null ) {
            //not yet requested - do it now and cache it..
            Collection annotations = xref.getCvDatabase().getAnnotations();
            Annotation annot = null;
            for ( Iterator it = annotations.iterator(); it.hasNext(); ) {
                annot = (Annotation) it.next();
                if ( annot.getCvTopic().getShortLabel().equals( "search-url" ) ) {
                    //found one - we are done
                    searchUrl = annot.getAnnotationText();
                    break;
                }
            }

            //cache it - even if the URL is null, because it may be
            //requested again
            dbUrls.put( xref.getCvDatabase(), searchUrl );
        }

        //if it isn't null, fill it in properly and return
        if ( searchUrl != null ) {
            //An Xref's primary can't be null - the constructor doesn't allow it..
            searchUrl = SearchReplace.replace( searchUrl, "${ac}", xref.getPrimaryId() );

        }
        return searchUrl;
    }

    public Collection getGeneNames() {

        Collection geneNames = getDaoFactory().getInteractorDao().getGeneNamesByInteractorAc(interactor.getAc());

        //now strip off trailing comma - if there are any names....
        if ( geneNames.size() == 0 ) {
            geneNames.add( "-" );
        }
        return geneNames;
    }

    /**
     * @return String The length of the Protein sequence, as a String
     */
    public String getSeqLength() {
        if ( interactor instanceof Polymer ) {
            return Integer.toString( ( (Polymer) interactor ).getSequence().length() );
        } else {
            return "no sequence";
        }
    }

    /**
     * @return String the Protein's sequence.
     */
    public String getSequence() {
        if ( interactor instanceof Polymer ) {
            return ( (Polymer) interactor ).getSequence();
        } else {
            return null;
        }
    }

    /**
     * @return String the Protein's sequence checksum
     */
    public String getCheckSum() {
        if ( interactor instanceof Polymer ) {
            return ( (Polymer) interactor ).getCrc64();
        } else {
            return "";
        }
    }

    /**
     * Provides access to the Xrefs of the Protein. Note that because these are complex objects containing their own
     * display data, the calling JSP must access the beans that it requires from each Xref.
     *
     * @return The Protein's Xrefs.
     */
    public Collection getXrefs() {
        return interactor.getXrefs();
    }

    public String getBinaryViewUrl() {
        return ( super.getSearchLink() + interactor.getAc() + "&amp;filter=ac&amp;searchClass=Interactor&amp;view=partner" );
    }

    private DaoFactory getDaoFactory()
    {
        return IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
    }
}
