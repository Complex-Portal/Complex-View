/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.editor.struts.action.sm;

import uk.ac.ebi.intact.application.editor.struts.action.CommonDispatchAction;

/**
 * TODO comment it.
 *
 * @author Catherine Leroy (cleroy@ebi.ac.uk)
 * @version $Id$
 */

/**
 * The dispatcher action for the smallMolecule editor.
 *
 * @author cleroy (cleroy@ebi.ac.uk)
 * @version $Id: SmDispatchAction.java,v 1.1 2005/02/22 15:05:29 cleroy Exp $
 *
 * @struts.action
 *      path="/sm/submit"
 *      name="smForm"
 *      input="edit.layout"
 *      scope="session"
 *      parameter="dispatch"
 *
 * @struts.action-forward
 *      name="success"
 *      path="edit.layout"
 */
public class SmDispatchAction extends CommonDispatchAction {
}
