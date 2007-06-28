/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.tulip.ws;

/**
 * Purpose : <br>
 * Allows the user to send a TLP file to Tulip and get back the coordinate of the nodes.
 *
 * @author Samuel KERRIEN (skerrien@ebi.ac.uk)
 */

public interface TulipAccess {

    /**
     * get the computed TLP content from tulip
     *
     * @param tlpContent tlp content to compute
     * @param optionMask the option of the Tulip process
     * @return the computed tlp file content or <b>null</b> if an error occurs.
     */
    public ProteinCoordinate[] getComputedTlpContent ( String tlpContent, String optionMask ) ;


    /**
     * Allows the user to get messages produced byte the web service
     *
     * @return an array of messages.
     *
     **/
    public String[] getErrorMessages (boolean hasToBeCleaned) ;


    /**
     * Clean message list
     */
    public void cleanErrorMessages () ;

} // TulipAccess
