/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/


package uk.ac.ebi.intact.webapp.search.struts.view.beans;

import uk.ac.ebi.intact.model.Interactor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * <p/>
 * This bean provides JSPs with the information needed to display a 'partners' view of a search result. This view is
 * defined by the mock web page specifications of June 2004. Typically a user will search for a Protein, and a
 * corresponding 'partners' view of related information will be displayed. </p>
 * <p/>
 * This class provides JSPs with as much information as possible in a display-friendly (ie String) format - however for
 * complex beans the JSP itself will need to obtain the information it needs to render the web page. </p>
 * <p/>
 * As at June 2004, the summary page needs to show the following information for each Protein found in the search
 * results: <ul> <li>Intact Name</li> <li>Intact Ac</li> <li>Number of Interactions</li> <li>Uniprot AC</li> <li>Gene
 * Name</li> <li>Description</li> </ul> After a row for the primary result, the Interaction partners are lisetd, with
 * the same information. </p>
 * <p/>
 *
 * @author Chris Lewington
 * @author Bruno Aranda (conversion to real bean)
 * @version $Id$
 */
public class PartnersViewBean extends AbstractViewBean {


    /**
     * The original search result - a single Protein
     */
//    private Protein protein;     // 26 usage in PartnersViewBean only
    private Interactor interactor;

    /**
     * The String representing the uniprot URL for this Protein
     */
    private String identityXrefURL;

    /**
     * The specific search URL for this Protein (ie with its AC)
     */
//    private String protSearchURL;  // 2 usage in PartnersViewBean
    private String interactorSearchURL;

    /**
     * The search URL for all of the Interactions containing this Protein.
     */
    private String interactionsSearchURL;

    /**
     * Assumed a List of these, as that is what is in the single Protein view. BUT the 'summary' web page only has ONE
     * gene name. NEEDS TO BE CLARIFIED
     */
    private Collection<String> geneNames;

    /**
     * A String representation of the number of Interactions this Protein takes part in.
     */
    private int numberOfInteractions;

    /**
     * if true, the bean will only show self interaction. if false (default), the been will show all interactions.
     */
    private String uniprotAc;
    private String interactorPartnerUrl;

    /**
     * The bean constructor requires a Protein to wrap, plus beans on the context path to the search webapp, a
     * general searchURL and the help link.
     *
     * @param interactor        The Interactor whose beans are to be displayed
     */

//    public PartnersViewBean( Protein prot, String helpLink, String searchURL, String contextPath ) {  // 1 usage in BinaryResultAction
    public PartnersViewBean( Interactor interactor ) {
        super( );
        this.interactor = interactor;
    }


    /**
     * Graph buttons are shown.
     *
     * @return whether or not the graph buttons are displayed
     */
    @Override
    public boolean showGraphButtons() {
        return true;
    }

    // Just honouring the contract.
    @Override
    public String getHelpSection() {
        return "interaction.beans.view";
    }

    // Implementing abstract methods

    /**
     * Adds the shortLabel of the Protein to an internal list used later for highlighting in a display. NOT SURE IF WE
     * STILL NEED THIS!!
     */
    @Override
    public void initHighlightMap() {
        Set<String> set = new HashSet<String>( 1 );
        set.add( interactor.getShortLabel() );
        setHighlightMap( set );
    }

    /**
     * Basic accessor, provided in case anything ever needs access to the wrapped object.
     *
     * @return Protein the Protein instance wrapped by this view bean.
     */
    public Interactor getMainInteractor() {
        return interactor;
    }

    /**
     * Provides a comma-separated list of gene names for this Protein.
     *
     * @return String al list of gene names as a String.
     */
    public Collection<String> getGeneNames() {
        return geneNames;
    }

    public void setGeneNames(Collection<String> geneNames)
    {
        this.geneNames = geneNames;
    }

    /**
     * This provides beans of the number of Interactions this Protein participates in. ISSUE: This is a hyperlinked
     * number on the web page mockup, pointing to the 'detail-blue' page mockup. This seems crazy since if eg a Protein
     * has 30 Interactions, all in different Experiments, the amount of detail displayed would be unreadable!! DESIGN
     * DECISION: see search link comment later.
     *
     * @return String a String representing the number of Interactions for this Protein.
     */
    public int getNumberOfInteractions() {
        return numberOfInteractions;
    }

    public void setNumberOfInteractions(int numberOfInteractions)
    {
        this.numberOfInteractions = numberOfInteractions;
    }

    /**
     * Returns a fully populated URL to perform a search for all this Protein's Interactions. ISSUE: This is used in the
     * mockups to fdisplay DETAIL. This would be unmanageable for large Interaction lists spread across Experiments.
     * DESIGN DECISION: make th link go to the main 'simple' page to list the Interactions *there*, so users can then
     * choose which detail they want.
     *
     * @return String The complete search URL to perform a (possibly multiple) search for this Protein's Interactions
     */
    public String getInteractionsSearchURL() {

        return interactionsSearchURL;
    }

    public void setInteractionsSearchURL(String interactionsSearchURL)
    {
        this.interactionsSearchURL = interactionsSearchURL;
    }

    /**
     * Provides this Protein's Uniprot AC (ie its Xref to Uniprot).
     *
     * @return String the Protein's Uniprot AC.
     */
    public String getUniprotAc() {
         return uniprotAc;
    }

    public void setUniprotAc(String uniprotAc)
    {
        this.uniprotAc = uniprotAc;
    }

    /**
     * Provides the URL for linking to Uniprot, already filled in with the correct AC from this Protein (ie it has its
     * PrimaryID added in)
     *
     * @return String the usable Uniprot URL
     */
    public String getIdentityXrefURL() {

       return identityXrefURL;
    }

    public void setIdentityXrefURL(String identityXrefURL)
    {
        this.identityXrefURL = identityXrefURL;
    }

    /**
     * Provides a String representation of a URL to perform a search on this Protein's beans (curently via AC)
     *
     * @return String a String representation of a search URL link for Protein
     */
    public String getInteractorSearchURL() {
        //
        return interactorSearchURL;
    }

    public void setInteractorSearchURL(String interactorSearchURL)
    {
        this.interactorSearchURL = interactorSearchURL;
    }

    /**
     * Convenience method to provide a String representation of a URL to perform a search on Protein. This method will
     * provide a Protein 'partners' view.
     *
     * @return String a String representation of a search URL link for Protein.
     */
    public String getInteractorPartnerURL() {
        return interactorPartnerUrl;
    }

    public void setInteractorPartnerUrl(String interactorPartnerUrl)
    {
        this.interactorPartnerUrl = interactorPartnerUrl;
    }

    /**
     * Have to override this because we accumulate 'partner' viewbeans and even though they are held in a Set, equality
     * based on identity will not be good enough to prevent duplicates being added. Instead we base bean equality on the
     * equality of the wrapped Proteins.
     *
     * @param obj The bean we want to check
     *
     * @return boolean true if the beans are equal, false otherwise.
     */
    @Override
    public boolean equals( Object obj ) {

        if ( obj == null ) {
            return false;
        }
        if ( !( obj instanceof PartnersViewBean ) ) {
            return false;
        }
        PartnersViewBean other = (PartnersViewBean) obj;
        return ( other.getMainInteractor().equals( this.getMainInteractor() ) );
    }

    /**
     * This just uses the wrapped Protein's hashcode.
     *
     * @return int the bean's hashcode (ie the Protein's)
     */
    @Override
    public int hashCode() {
        return this.getMainInteractor().hashCode();
    }

}
