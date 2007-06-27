/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.statisticView.struts.view;

/**
 * Use for the &lt;display:&gt; tag in order to display one line of a table.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public final class DisplayStatisticsBean {

    ///////////////////////////
    // Instance variables

    private String statObject;
    private String description;
    private String count;

    //////////////////////////
    // Constructor

    public DisplayStatisticsBean( final String object,
                                  final String count,
                                  final String description
    ) {
        this.statObject = object;
        this.count = count;
        this.description = description;
    }

    /////////////////////////
    // Getters & Setters

    public String getStatObject() {
        return statObject;
    }

    public void setStatObject( final String object ) {
        this.statObject = object;
    }

    public String getCount() {
        return count;
    }

    public void setCount( final String count ) {
        this.count = count;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription( final String description ) {
        this.description = description;
    }
}