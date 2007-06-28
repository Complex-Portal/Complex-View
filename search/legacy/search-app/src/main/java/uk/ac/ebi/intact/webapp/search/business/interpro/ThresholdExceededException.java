package uk.ac.ebi.intact.webapp.search.business.interpro;

/**
 * This <code>Exception</code> is thrown if the maximum number of displayable Proteins is exceeded.
 * (default value is set to InterproSearch#MAXIMUM_NUMBER_OF_SELECTED_PROTEINS).
 * <br>
 * Note: that exception is never propagated outside the scope of the InterproAction, that's why we do not bother
 * collecting the stack trace (performance reason).
 *
 * @author Christian Kohler (ckohler@ebi.ac.uk)
 * @version $Id$
 * @see InterproSearch#MAXIMUM_NUMBER_OF_SELECTED_PROTEINS
 */
public class ThresholdExceededException extends Exception {

    public ThresholdExceededException() {
        // no call to super() here as we don't need the stackTrace.
    }
}
