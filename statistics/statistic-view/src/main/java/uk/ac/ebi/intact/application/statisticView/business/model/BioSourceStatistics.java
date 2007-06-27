/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.statisticView.business.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;


/**
 * This class represents the Biosourcestatistics table in the database.
 * <p/>
 * The Script sql/[oracle|postgres]/insert_biosource.pl
 * should be run before to use of this class.
 * <p/>
 * The corresponding mapping between the both JAVA object and the SQL table
 * is described in the repository_user.xml
 *
 * @author Michael Kleen
 * @version BioSourceStatistics.java Date: Oct 12, 2004 Time: 3:57:09 PM
 */
@Entity
@Table( name = "ia_biosourcestatistics" )
public class BioSourceStatistics extends StatsBase implements Comparable {

    private String taxId;
    private String shortlabel;
    private int binaryInteractions;
    private int proteinNumber;

    public String getShortlabel() {
        return shortlabel;
    }

    public void setShortlabel( String shortlabel ) {
        this.shortlabel = shortlabel;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId( String taxId ) {
        this.taxId = taxId;
    }

    @Column( name = "binary_interactions" )
    public int getBinaryInteractions() {
        return binaryInteractions;
    }

    public void setBinaryInteractions( int binaryInteractions ) {
        this.binaryInteractions = binaryInteractions;
    }

    @Column( name = "protein_number" )
    public int getProteinNumber() {
        return proteinNumber;
    }

    public void setProteinNumber( int proteinNumber ) {
        this.proteinNumber = proteinNumber;
    }

    public int compareTo( Object o ) {
        //  Timestamp t = ( (BioSourceStatistics) o ).getUpdated();

        BioSourceStatistics bs = null;
        if ( o instanceof BioSourceStatistics ) {
            bs = ( BioSourceStatistics ) o;
        }

        // sort the stats by decreasing interaction count
        return bs.getBinaryInteractions() - binaryInteractions;
    }

    @Override
    public String toString() {
        return "BioSourceStatistics[[ac]" + this.getAc() + ",[shortlabel] " + this.shortlabel +
               ", [taxid]" + this.taxId + ", [proteinNumbers] " + this.proteinNumber +
               ", [binaryInteractions]" + this.binaryInteractions;
    }
}