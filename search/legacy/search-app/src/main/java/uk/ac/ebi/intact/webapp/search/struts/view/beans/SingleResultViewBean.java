package uk.ac.ebi.intact.webapp.search.struts.view.beans;

import uk.ac.ebi.intact.searchengine.SearchClass;
import uk.ac.ebi.intact.webapp.search.SearchWebappContext;
import uk.ac.ebi.intact.webapp.search.struts.util.SearchConstants;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author Michael Kleen
 * @version SingleResultViewBean.java Date: Dec 15, 2004 Time: 10:43:16 AM
 */
public class SingleResultViewBean {
    private final String intactType;
    private final String searchString;
    private final int count;


    public SingleResultViewBean( final String intactType, final int count, final String searchString ) {

        this.intactType = intactType;
        this.count = count;
        this.searchString = searchString.replaceAll( "\\'", "" );

    }

    public String getIntactName() {
        return this.intactType;
    }

    private String getSearchType() {
        if ( intactType.equalsIgnoreCase( "Controlled vocabulary term" ) ) {
            return "CvObject";
        } else {
            return this.intactType;
        }
    }

    public String getHelpURL() {
        String helpLink = SearchWebappContext.getCurrentInstance().getHelpLink();

        if ( intactType.equalsIgnoreCase( "Protein" ) ) {
            return helpLink + "Interactor";
        }
        if ( intactType.equalsIgnoreCase( "Interaction" ) ) {
            return helpLink + "Interaction";
        }
        if ( intactType.equalsIgnoreCase( "Experiment" ) ) {
            return helpLink + "Experiment";
        }
        if ( intactType.equalsIgnoreCase( "Controlled vocabulary term" ) ) {
            return helpLink + "CVS";
        } else {
            return helpLink + "search.TableLayout";
        }
    }

    public String getCount() {
        return String.valueOf(count);
    }

    public String getSearchLink() {
        SearchWebappContext webappContext = SearchWebappContext.getCurrentInstance();
        String searchLink = webappContext.getSearchUrl();

        String searchQuery = searchString;

        try
        {
            searchQuery = URLEncoder.encode(searchString, "utf-8");
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }

        if ( count < webappContext.getMaxResultsPerPage() ) {
            return searchLink + searchQuery + "&searchClass=" + this.getSearchType()+"&page=";
        } else {
            return searchLink + searchQuery + "&searchClass=" + this.getSearchType()+"&page=1";
            //return "-";
        }
    }

    public String getSearchName() {
        if ( intactType.equalsIgnoreCase( SearchClass.PROTEIN.getShortName() ) ) {
            return "Select by Protein";
        }
        if ( intactType.equalsIgnoreCase( SearchClass.NUCLEIC_ACID.getShortName() ) ) {
            return "Select by Nucleic Acid";
        }
        if ( intactType.equalsIgnoreCase( SearchClass.SMALL_MOLECULE.getShortName() ) ) {
            return "Select by Small Molecule";
        }
        if ( intactType.equalsIgnoreCase( SearchClass.INTERACTION.getShortName() ) ) {
            return "Select by Interaction";
        }
        if ( intactType.equalsIgnoreCase( SearchClass.EXPERIMENT.getShortName() ) ) {
            return "Select by Experiment";
        }
        if ( intactType.equalsIgnoreCase( SearchClass.CV_OBJECT.getShortName() ) ) {
            return "Select by Controlled vocabulary";
        }
        // HACK: this is neeeded by the advance search to work properly
        else if ( intactType.equalsIgnoreCase( "Controlled vocabulary term" ) ) {
            return "cv";
        } else {
            return "-";
        }
    }

    // inserted the following method (afrie)
    public String getSearchObject() {
        if ( intactType.equalsIgnoreCase( SearchClass.PROTEIN.getShortName()) ) {
            return "protein";
        }
        else if ( intactType.equalsIgnoreCase( SearchClass.NUCLEIC_ACID.getShortName()) ) {
            return "nucleicAcid";
        }
        else if ( intactType.equalsIgnoreCase( SearchClass.INTERACTION.getShortName() ) ) {
            return "interaction";
        }
        else if ( intactType.equalsIgnoreCase( SearchClass.EXPERIMENT.getShortName() ) ) {
            return "experiment";
        }
        else if ( intactType.equalsIgnoreCase( SearchClass.CV_OBJECT.getShortName() ) ) {
            return "cv";
        }
        // HACK: this is neeeded by the advance search to work properly
        else if ( intactType.equalsIgnoreCase( "Controlled vocabulary term" ) ) {
            return "cv";
        }
        else {
            return "-";
        }
    }

    public boolean isSearchable() {
        if ( count < SearchConstants.MAXIMUM_RESULT_SIZE ) {
            return true;
        } else {
            return false;
        }
    }
}
