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
 * @author Michael Kleen
 * @version identificationMethods.java Date: Feb 18, 2005 Time: 1:06:33 PM
 */
@Entity
@Table( name = "ia_detectionmethodsstatistics" )
public class IdentificationMethodStatistics extends StatsBase implements Comparable {

    private String detectionName;
    private int numberInteractions;

    public IdentificationMethodStatistics() {
    }

    @Column( name = "fullname" )
    public String getDetectionName() {
        return detectionName;
    }

    public void setDetectionName( String detectionName ) {
        this.detectionName = detectionName;
    }

    @Column( name = "number_interactions" )
    public int getNumberInteractions() {
        return numberInteractions;
    }

    public void setNumberInteractions( int numberInteractions ) {
        this.numberInteractions = numberInteractions;
    }

    public int compareTo( Object o ) {
        String detectionName = ( ( IdentificationMethodStatistics ) o ).getDetectionName();
        return detectionName.compareTo( detectionName );
    }


}
