/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.statisticView.business.util;

import uk.ac.ebi.intact.application.statisticView.business.model.IdentificationMethodStatistics;

import java.util.Comparator;

/**
 * User: Michael Kleen mkleen@ebi.ac.uk
 * Date: Mar 18, 2005
 * Time: 1:32:26 PM
 */
public class IdentificationComparator implements Comparator {

    public int compare( Object o1, Object o2 ) {

        final IdentificationMethodStatistics ident1 = ( IdentificationMethodStatistics ) o1;
        final IdentificationMethodStatistics ident2 = ( IdentificationMethodStatistics ) o2;

        if ( ident1.getNumberInteractions() < ident2.getNumberInteractions() ) {
            return -1;
        } else if ( ident1.getNumberInteractions() == ident2.getNumberInteractions() ) {
            return 0;
        } else if ( ident1.getNumberInteractions() > ident2.getNumberInteractions() ) {
            return 1;
        } else {
            throw new RuntimeException( "not possible to compare these objects" );
        }
    }
}
