/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.searchengine;

import java.io.Serializable;

/**
 * Holds the criteria who gave a search result.
 * <br>
 * Those criteria's query are saved in the session for eventual context reload.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public final class CriteriaBean implements Serializable {

    /**
     * The query given by the user
     */
    private String query;

    /**
     * The property which matches the user's query (could be shortlabel, ac ...).
     * The field is <code>null</code> if the value hasn't given any result.
     */
    private String target;

    public CriteriaBean( String query, String target ) {
        this.query = query;
        this.target = target;
    }

    public String getQuery() {
        return query;
    }

    public String getTarget() {
        return target;
    }

    public boolean hasGivenResults() {
        return ( target != null );
    }

    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( !( o instanceof CriteriaBean ) ) return false;

        final CriteriaBean criteriaBean = ( CriteriaBean ) o;

        if ( query != null ? !query.equals( criteriaBean.query ) : criteriaBean.query != null ) return false;
        if ( target != null ? !target.equals( criteriaBean.target ) : criteriaBean.target != null ) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = ( query != null ? query.hashCode() : 0 );
        result = 29 * result + ( target != null ? target.hashCode() : 0 );
        return result;
    }

    public String toString() {
        return "CriteriaBean[query=" + query + "; target=" + target + "]";
    }
}
