package uk.ac.ebi.intact.application.statisticView.business.publications.model;

/**
 * Generic IntAct Data Bean.
 *
 * @author ckohler (ckohler@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29-Nov-2005</pre>
 */
public class ExperimentBean {

    private String ac;
    private String shortlabel;

    public ExperimentBean() {
    }

    //////////////////////
    // Getters and setters

    public String getAc() {
        return ac;
    }

    public void setAc( String ac ) {
        this.ac = ac;
    }

    public String getShortlabel() {
        return shortlabel;
    }

    public void setShortlabel( String shortlabel ) {
        this.shortlabel = shortlabel;
    }

    /////////////////////////////////
    // Object overload

    public boolean equals( Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( o == null || getClass() != o.getClass() ) {
            return false;
        }

        final ExperimentBean that = ( ExperimentBean ) o;

        if ( ac != null ? !ac.equals( that.ac ) : that.ac != null ) {
            return false;
        }
        if ( shortlabel != null ? !shortlabel.equals( that.shortlabel ) : that.shortlabel != null ) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = ( ac != null ? ac.hashCode() : 0 );
        result = 29 * result + ( shortlabel != null ? shortlabel.hashCode() : 0 );
        return result;
    }

    public String toString() {
        return "ExperimentBean{" +
               "ac='" + ac + '\'' +
               ", shortlabel='" + shortlabel + '\'' +
               '}';
    }
}