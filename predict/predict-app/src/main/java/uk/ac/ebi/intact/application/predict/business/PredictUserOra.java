/*
Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.predict.business;

/**
 * This class implements Predict user for an Oracle database.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class PredictUserOra extends PredictUser {

    // Implement abstract methods.

    protected String getSpeciesSQL() {
        return "SELECT DISTINCT species FROM ia_payg";
    }

    protected String getDbInfoSQL(String taxid) {
        return "SELECT nID FROM ia_payg WHERE ROWNUM<=50 AND really_used_as_bait='N' "
                + " AND species =\'" + taxid + "\' ORDER BY indegree DESC, qdegree DESC";
    }
}
