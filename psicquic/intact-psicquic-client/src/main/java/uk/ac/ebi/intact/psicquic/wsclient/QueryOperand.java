package uk.ac.ebi.intact.psicquic.wsclient;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public enum QueryOperand {

    AND("AND"),
    OR("OR");

    private String strOperand;

    private QueryOperand(String strOperand) {
        this.strOperand = strOperand;
    }

    public String toString() {
        return strOperand;
    }
}
