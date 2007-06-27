/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.hierarchview.struts.view;

import uk.ac.ebi.intact.application.hierarchview.struts.framework.IntactBaseForm;

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
public final class InitForm extends IntactBaseForm {

    // --------------------------------------------------- Instance Variables

    // the hostname
    private String host = null;
    private String protocol = null;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

}
