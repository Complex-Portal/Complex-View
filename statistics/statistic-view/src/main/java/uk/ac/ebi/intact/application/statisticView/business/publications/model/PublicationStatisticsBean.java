package uk.ac.ebi.intact.application.statisticView.business.publications.model;

import java.math.BigDecimal;

/**
 * this class is used to store information about the total binary interactions corresponding to a pubmedID. It is used
 * to store the pubmed_id , the number of total binary interactions, as well as the already known and new interactions.
 *
 * @author ckohler (ckohler@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29-Nov-2005</pre>
 */
public class PublicationStatisticsBean {

    ///////////////////////////
    // Instance variable

    private String pubmed_id;
    private BigDecimal total_interactions;
    private BigDecimal known_interactions;
    private BigDecimal new_interactions;

    public PublicationStatisticsBean() {
    }

    //////////////////////////
    // Getters and Setters

    public String getPubmed_id() {
        return pubmed_id;
    }

    public void setPubmed_id( String pubmed_id ) {
        this.pubmed_id = pubmed_id;
    }

    public BigDecimal getTotal_interactions() {
        return total_interactions;
    }

    public void setTotal_interactions( BigDecimal total_interactions ) {
        this.total_interactions = total_interactions;
    }

    public BigDecimal getKnown_interactions() {
        return known_interactions;
    }

    public void setKnown_interactions( BigDecimal known_interactions ) {
        this.known_interactions = known_interactions;
    }

    public BigDecimal getNew_interactions() {
        return new_interactions;
    }

    public void setNew_interactions( BigDecimal new_interactions ) {
        this.new_interactions = new_interactions;
    }

    ///////////////////////////
    // Object overload

    public String toString() {
        return "PublicationStatisticsBean{" +
               "pubmed_id='" + pubmed_id + '\'' +
               ", total_interactions=" + total_interactions +
               ", known_interactions=" + known_interactions +
               ", new_interactions=" + new_interactions +
               '}';
    }
}
