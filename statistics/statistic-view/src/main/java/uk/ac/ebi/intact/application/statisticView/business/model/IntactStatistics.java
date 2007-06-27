/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.statisticView.business.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * This class represents the Statistics table in the database.
 * <p/>
 * The Script sql/[oracle|postgres]/insert_count_statistics.sql
 * should be run before to use of this class.
 * <p/>
 * The corresponding mapping between the both JAVA object and the SQL table
 * is described in the repository_user.xml
 *
 * @author shuet (shuet@ebi.ac.uk), Samuel Kerrien (skerrien:ebi.ac.uk), Michael Kleen (mkleen@ebi.ac.uk)
 * @version : $Id$
 */
@Entity
@Table( name = "ia_statistics" )
public class IntactStatistics extends StatsBase implements Comparable {

    private int proteinNumber;
    private int interactionNumber;
    private int binaryInteractions;
    private int complexInteractions;
    private int experimentNumber;
    private int termNumber;

    public IntactStatistics() {
        super.setTimestamp( new java.sql.Timestamp( System.currentTimeMillis() ) );
    }

    @Column( name = "protein_number" )
    public int getNumberOfProteins() {
        return ( this.proteinNumber );
    }

    public void setNumberOfProteins( int proteinNumb ) {
        this.proteinNumber = proteinNumb;
    }

    @Column( name = "interaction_number" )
    public int getNumberOfInteractions() {
        return ( this.interactionNumber );
    }

    public void setNumberOfInteractions( int interactionNumb ) {
        this.interactionNumber = interactionNumb;
    }

    @Column( name = "binary_interactions" )
    public int getNumberOfBinaryInteractions() {
        return ( this.binaryInteractions );
    }

    public void setNumberOfBinaryInteractions( int binaryInteraction ) {
        this.binaryInteractions = binaryInteraction;
    }

    @Column( name = "complex_interactions" )
    public int getNumberOfComplexInteractions() {
        return ( this.complexInteractions );
    }

    public void setNumberOfComplexInteractions( int complexInteraction ) {
        this.complexInteractions = complexInteraction;
    }

    @Column( name = "experiment_number" )
    public int getNumberOfExperiments() {
        return ( this.experimentNumber );
    }

    public void setNumberOfExperiments( int experimentNumb ) {
        this.experimentNumber = experimentNumb;
    }

    @Column( name = "term_number" )
    public int getNumberOfCvTerms() {
        return ( this.termNumber );
    }

    public void setNumberOfCvTerms( int termNumb ) {
        this.termNumber = termNumb;
    }

    @Override
    public String toString() {
        return " Timestamp: " + this.getTimestamp()
               + "; Number of proteins: " + this.getNumberOfProteins()
               + "; Number of interactions: " + this.getNumberOfInteractions()
               + " of which " + this.getNumberOfBinaryInteractions() + " with 2 interactors "
               + " and " + this.getNumberOfComplexInteractions() + "with more than 2 interactors"
               + "; Number of experiments: " + this.getNumberOfExperiments()
               + "; Number of terms in Go: " + this.getNumberOfProteins()
               + "\n";
    }

    public int compareTo( Object o ) {

        Timestamp t = ( ( IntactStatistics ) o ).getTimestamp();
        return getTimestamp().compareTo( t );
    }
}
