/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.action.util;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.tiles.ComponentContext;
import org.apache.struts.tiles.actions.TilesAction;
import uk.ac.ebi.intact.application.commons.struts.taglibs.DocumentationTag;
import uk.ac.ebi.intact.application.editor.business.EditUserI;
import uk.ac.ebi.intact.application.editor.business.EditorService;
import uk.ac.ebi.intact.application.editor.exception.SessionExpiredException;
import uk.ac.ebi.intact.application.editor.struts.framework.util.EditorConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * This action class is responsible for appending the class type to the header.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 *
 * @struts.action
 *      path="/tiles/headerSwitch"
 */
public class HeaderSwitchAction extends TilesAction {

    /**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response (or forward to another web component that will create it),
     * with provision for handling exceptions thrown by the business logic.
     * Override this method to provide functionality
     *
     * @param context the current Tile context, containing Tile attributes.
     * @param mapping the ActionMapping used to select this instance.
     * @param form the optional ActionForm bean for this request (if any).
     * @param request the HTTP request we are processing.
     * @param response the HTTP response we are creating
     * @return null because there is no forward associated with this action.
     * @throws java.lang.Exception if the application business logic throws an exception.
     */
    public ActionForward execute(ComponentContext context,
                                 ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws Exception {
        // The existing title; the attribute name must match with the title
        // given in the tiles definition file.
        String oldtitle = (String) context.getAttribute("header.title");

        // Don't create a new session.
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new SessionExpiredException();
        }
        EditUserI user = (EditUserI)
                session.getAttribute(EditorConstants.INTACT_USER);

        // Only change the header if we are editing; need to check for null
        // here because 'user' is null when the logout action is selected.
        if ((user != null) && user.isEditing()) {
            // Service to get the help tags.
            EditorService service = (EditorService)
                    super.servlet.getServletContext().getAttribute(
                            EditorConstants.EDITOR_SERVICE);

            // The new title without the help tag.
            String newtitle = oldtitle + " - " + user.getSelectedTopic();

            String tag = user.getHelpTag();
            // Only process if we have tag.
            if (tag != null) {
                // The link title (superscript and reduced font).
                String title = "<sup><font color=\"red\" size=\"-1\">"
                        + EditorConstants.HELP_TITLE + "</font></sup>";
                newtitle += DocumentationTag.getHtmlVersion(
                        service.getHelpURL(request), tag, title);
            }
            context.putAttribute("header.title", newtitle);
        }
        return null;
    }
}
