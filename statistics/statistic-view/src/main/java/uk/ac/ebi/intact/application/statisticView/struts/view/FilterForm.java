/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.statisticView.struts.view;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * Form bean for the main form of the view.jsp page.
 * This form has the following fields, with default values in square brackets:
 * <ul>
 * <li><b>queryString</b> - Entered queryString value
 * <li><b>method</b> - Selected method value
 * </ul>
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public final class FilterForm extends ActionForm {

    // --------------------------------------------------- Instance Variables

    private String start;
    private String stop;

    public String getStart() {
        return start;
    }

    public void setStart( String start ) {
        this.start = start;
    }

    public String getStop() {
        return stop;
    }

    public void setStop( String stop ) {
        this.stop = stop;
    }

    /**
     * Reset all properties to their default values.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public void reset( ActionMapping mapping, HttpServletRequest request ) {
        this.start = null;
        this.stop = null;
    } // reset

    public String toString() {
        StringBuffer sb = new StringBuffer( "FilterForm[start=" );
        sb.append( start );
        sb.append( ", stop=" );
        sb.append( stop );
        sb.append( "]" );
        return ( sb.toString() );
    }
}
