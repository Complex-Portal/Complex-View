/*
Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.predict.struts.view;

import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.model.Protein;

/**
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class ResultBean {

    /**
     * The rank of the protein.
     */
    private int myRank;

    /**
     * The short label of the protein as a link.
     */
    private String myShortLabelLink;

    /**
     * The full name of the protein.
     */
    private String myFullName;

    public ResultBean(Protein protein, int rank) throws IntactException {
        myRank = rank;
        myFullName = protein.getFullName();
        myShortLabelLink = "<a href=\"" + "javascript:show('Protein', '"
                + protein.getShortLabel() + "')\">" + protein.getShortLabel()
                + "</a>";

    }

    // Get methods to access info from JSP pages.

    public int getRank() {
        return myRank;
    }

    public String getShortLabelLink() {
        return myShortLabelLink;
    }

    public String getFullName() {
        return myFullName;
    }
}
