/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.statisticView.business.data;

/**
 * Use for the &lt;display:&gt; tag in order to display one line of a table.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public final class DisplayStatisticsBean {

    private String object;
    private String description;
    private int count;

    public DisplayStatisticsBean( final String object,
                                  final int count,
                                  final String description
    ) {
        this.object = object;
        this.count = count;
        this.description = description;
    }


    public final String getObject() {
        return object;
    }

    public final void setObject( final String object ) {
        this.object = object;
    }

    public final int getCount() {
        return count;
    }

    public final void setCount( final int count ) {
        this.count = count;
    }

    public final String getDescription() {
        return description;
    }

    public final void setDescription( final String description ) {
        this.description = description;
    }
}
