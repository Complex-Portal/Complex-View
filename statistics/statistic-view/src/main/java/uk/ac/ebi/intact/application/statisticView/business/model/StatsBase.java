/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.statisticView.business.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.sql.Timestamp;

/**
 * TODO comment this!
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since <pre>17-Jul-2006</pre>
 */
@MappedSuperclass
public class StatsBase {

    private static Log log = LogFactory.getLog( StatsBase.class );

    private int ac;
    private Timestamp timestamp;

    @Id
    public int getAc() {
        return ( this.ac );
    }

    public void setAc( int ac ) {
        this.ac = ac;
    }

    public Timestamp getTimestamp() {
        return ( this.timestamp );
    }

    public void setTimestamp( Timestamp timeStamp ) {
        this.timestamp = timeStamp;
    }

}
