/*
Copyright (c) 2002-2005 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.action.biosrc;

import uk.ac.ebi.intact.application.editor.struts.action.CommonDispatchAction;

/**
 * The dispatcher action for the BioSource editor.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 *
 * @struts.action
 *      path="/bs/submit"
 *      name="bsForm"
 *      input="edit.layout"
 *      scope="session"
 *      parameter="dispatch"
 *
 * @struts.action-forward
 *      name="success"
 *      path="edit.layout"
 */
public class BioSourceDispatchAction extends CommonDispatchAction {
}
