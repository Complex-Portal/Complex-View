/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.imex.idassigner.keyassigner;

import uk.ac.ebi.intact.imex.idassigner.id.IMExRange;


/**
 * Contract of a KeyAssignerService
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>05-May-2006</pre>
 */
public interface KeyAssignerServiceI {

    /**
     * Request a range of IMEx IDs from the Key Assigner.
     *
     * @param howMany the count of IMEx ID requested.
     *
     * @return an IMExRange.
     *
     * @throws KeyAssignerServiceException
     *
     */
    public IMExRange getAccessions( int howMany ) throws KeyAssignerServiceException;
}