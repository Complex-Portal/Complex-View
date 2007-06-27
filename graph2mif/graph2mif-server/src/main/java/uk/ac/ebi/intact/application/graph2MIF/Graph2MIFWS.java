/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.graph2MIF;

import uk.ac.ebi.intact.application.graph2MIF.exception.GraphNotConvertableException;
import uk.ac.ebi.intact.application.graph2MIF.exception.MIFSerializeException;
import uk.ac.ebi.intact.application.graph2MIF.exception.NoGraphRetrievedException;
import uk.ac.ebi.intact.application.graph2MIF.exception.NoInteractorFoundException;
import uk.ac.ebi.intact.business.IntactException;

/**
 *  Graph2MIFWS Interface
 *
 *  This is the interface of The graph2MIF-WebService.
 *  Implementation in Graph2MIFWSService.java
 *
 *  @author Henning Mersch <hmersch@ebi.ac.uk>
 *  @version $Id$
 */
public interface Graph2MIFWS {

    /**
	 * getMIF is the only method which is necessary.
	 * @param ac String ac in IntAct
	 * @param depth Integer of the depth the graph should be expanded
     * @param strictmif
	 * @return String including a XML-Document in PSI-MIF-Format
	 * @exception IntactException thrown if search for interactor failed
	 * @exception uk.ac.ebi.intact.application.graph2MIF.exception.NoGraphRetrievedException thrown if DOM-Object could not be serialized
	 * @exception uk.ac.ebi.intact.application.graph2MIF.exception.MIFSerializeException thrown if IntactHelper could not be created
	 * @exception uk.ac.ebi.intact.application.graph2MIF.exception.NoInteractorFoundException thrown if no Interactor found for ac
	 */
    String getMIF(String ac, Integer depth, Boolean strictmif)
            throws IntactException,
                   NoGraphRetrievedException,
                   MIFSerializeException,
                   NoInteractorFoundException;

    /**
	 * getMIF is the only method which is necessary.
	 * @param ac String ac in IntAct
	 * @param depth Integer of the depth the graph should be expanded
     * @param strictmif
     * @param psiVersion version of PSI to use
	 * @return String including a XML-Document in PSI-MIF-Format
	 * @exception IntactException thrown if search for interactor failed
	 * @exception uk.ac.ebi.intact.application.graph2MIF.exception.GraphNotConvertableException thrown if Graph failed requirements of MIF.
	 * @exception uk.ac.ebi.intact.application.graph2MIF.exception.MIFSerializeException thrown if IntactHelper could not be created
	 * @exception uk.ac.ebi.intact.application.graph2MIF.exception.NoInteractorFoundException thrown if no Interactor found for ac
	 */
    String getMIF(String ac, Integer depth, Boolean strictmif, String psiVersion)
            throws IntactException,
                   NoGraphRetrievedException,
                   MIFSerializeException,
                   NoInteractorFoundException;
}
