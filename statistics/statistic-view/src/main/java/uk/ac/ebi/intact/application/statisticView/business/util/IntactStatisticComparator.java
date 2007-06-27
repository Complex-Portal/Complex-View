/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.statisticView.business.util;

import uk.ac.ebi.intact.application.statisticView.business.model.IntactStatistics;

import java.sql.Timestamp;
import java.util.Comparator;

/**
 * User: Michael Kleen mkleen@ebi.ac.uk
 * Date: Mar 18, 2005
 * Time: 3:49:52 PM
 */
public class IntactStatisticComparator implements Comparator {

    public int compare( Object o1, Object o2 ) {

        IntactStatistics stat1 = ( IntactStatistics ) o1;
        IntactStatistics stat2 = ( IntactStatistics ) o2;
        Timestamp t1 = stat1.getTimestamp();
        Timestamp t2 = stat2.getTimestamp();
        return t1.compareTo( t2 );

    }

}
