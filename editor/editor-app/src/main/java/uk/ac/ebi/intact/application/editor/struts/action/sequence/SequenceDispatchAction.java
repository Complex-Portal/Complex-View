/*
 Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
 All rights reserved. Please see the file LICENSE
 in the root directory of this distribution.
 */

package uk.ac.ebi.intact.application.editor.struts.action.sequence;

import uk.ac.ebi.intact.application.editor.struts.action.CommonDispatchAction;

/**
 * The dispatcher action class for the Sequence editor. This class is for
 * annotating struts tags for xdoclet.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 *
 * @struts.action
 *      path="/seq/submit"
 *      name="seqForm"
 *      input="edit.layout"
 *      scope="session"
 *      parameter="dispatch"
 *
 * @struts.action-forward
 *      name="success"
 *      path="edit.layout"
 */
public class SequenceDispatchAction extends CommonDispatchAction {
}