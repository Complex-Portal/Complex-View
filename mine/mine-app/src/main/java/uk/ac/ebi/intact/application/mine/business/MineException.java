/*
 * Created on 01.06.2004
 */

package uk.ac.ebi.intact.application.mine.business;

/**
 * Exception structure which is thrown whenever an exception occurs during the
 * mine application.
 * 
 * @author Andreas Groscurth
 */
public class MineException extends Exception {

    /**
     *  
     */
    public MineException() {
        super();
    }

    /**
     * @param message
     */
    public MineException(String message) {
        super( message );
    }
}