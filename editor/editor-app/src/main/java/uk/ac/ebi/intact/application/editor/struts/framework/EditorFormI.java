/*
Copyright (c) 2002-2005 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.framework;

import uk.ac.ebi.intact.application.editor.struts.view.CommentBean;
import uk.ac.ebi.intact.application.editor.struts.view.XreferenceBean;

import java.util.List;
import java.util.Date;

/**
 * The methods common to all the editor forms.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public interface EditorFormI {
    void setShortLabel(String label);

    String getShortLabel();

    void setCreator(String creator);

    String getCreator();

    void setUpdator(String updator);

    String getUpdator();

    void setCreated(Date created);

    String getCreated();

    void setUpdated(Date updated);

    String getUpdated();

    void setFullName(String fullname);

    String getFullName();

    void setAc(String ac);

    String getAc();

    void setAnnotations(List annotations);

    List getAnnotations();

    void setAnnotCmd(int index, String value);

    CommentBean getSelectedAnnotation();

    void setXrefs(List xrefs);

    List getXrefs();

    void setXrefCmd(int index, String value);

    XreferenceBean getSelectedXref();

    CommentBean getNewAnnotation();

    XreferenceBean getNewXref();

    void clearNewBeans();

    String getAnchor();

    void setAnchor(String anchor);

    String getDispatch();

    void resetDispatch();
}
