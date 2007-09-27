/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.framework;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionServlet;
import uk.ac.ebi.intact.application.editor.business.EditorService;
import uk.ac.ebi.intact.application.editor.event.EventListener;
import uk.ac.ebi.intact.application.editor.struts.framework.util.EditorConstants;
import uk.ac.ebi.intact.application.editor.util.LockManager;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * This is Intact editor specific action servlet class. This class is
 * responsible for initializing application wide resources.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class EditorActionServlet extends ActionServlet {

    private static final Log log = LogFactory.getLog(EditorActionServlet.class);

    public void init() throws ServletException {
        // Make sure to call super's init().
        super.init();

        log.debug("Initializing EditorActionServlet");

        // Save the context to avoid repeat calls.
        ServletContext ctx = super.getServletContext();

        // Create an instance of EditorService. This will throw an exception
        // if the service can't initialize properly (basically the application
        // wouldn't start)
        EditorService service = EditorService.getInstance(ctx);

        // Make them accessible for any servlets within the server.
        ctx.setAttribute(EditorConstants.EDITOR_SERVICE, service);
        ctx.setAttribute(EditorConstants.EDITOR_TOPICS, service.getIntactTypes());
        ctx.setAttribute(EditorConstants.LOCK_MGR, LockManager.getInstance());
        ctx.setAttribute(EditorConstants.EVENT_LISTENER, EventListener.getInstance());

        // Resource bundle to access the message resources to set keys.
        ResourceBundle msgres = ResourceBundle.getBundle(
                "uk.ac.ebi.intact.application.editor.MessageResources");

        // Set the map for all the users (read only map).
        ctx.setAttribute(EditorConstants.ANCHOR_MAP, getAnchorMap(msgres));
    }

    private Map getAnchorMap(ResourceBundle rb) {
        // The map to return (map key -> anchor name).
        Map map = new HashMap();

        // Editing short label.
        map.put("error.label", "info");

        // Adding annotation.
        map.put("error.annotation.topic", "annotation");
        map.put("error.annotation.exists", "annotation");
        map.put(rb.getString("annotations.button.add"), "annotation");
        // Editing annotation.
        map.put("error.edit.annotation.exists", "annot.edit");
        map.put(rb.getString("annotations.button.edit"), "annot.edit");
        map.put(rb.getString("annotations.button.save"), "annot.edit");
        map.put(rb.getString("annotations.button.delete"), "annot.edit");

        // Adding Xrefs.
        map.put("error.xref.database", "xref");
        map.put("error.xref.pid", "xref");
        map.put("error.xref.exists", "xref");

        map.put(rb.getString("xrefs.button.add"), "xref");
        // Editing xref
        map.put("error.edit.xref.exists", "xref.edit");
        map.put(rb.getString("xrefs.button.edit"), "xref.edit");
        map.put(rb.getString("xrefs.button.save"), "xref.edit");
        map.put(rb.getString("xrefs.button.delete"), "xref.edit");

        // Experiment page anchors
        map.put("error.exp.biosrc", "info");
        map.put("error.exp.inter", "info");
        map.put("error.exp.ident", "info");
        // Anchors related search in an Interaction from an experiment.
        map.put("error.exp.int.search.input", "exp.int.search");
        map.put("error.exp.int.search.empty", "exp.int.search");
        map.put("error.exp.int.search.many", "exp.int.search");

        // Saving proteins.
        map.put(rb.getString("int.proteins.button.save"), "int.protein.search");

        // Protein search in the Interaction editor.
        map.put("error.int.protein.edit.role", "int.protein.search");
        // Anchor for pressing the protein search button.
        map.put(rb.getString("int.proteins.button.search"), "int.protein.search");
        // Unsaved proteins
        map.put("error.int.sanity.unsaved.prot", "int.protein.search");
        // No search parameter
        map.put("error.int.protein.search.input", "int.protein.search");
        // Invalid format for AC
        map.put("error.int.protein.search.ac", "int.protein.search");
        // Invalid format for SP AC
        map.put("error.int.protein.search.sp", "int.protein.search");
        // Too many proteins returned.
        map.put("error.int.protein.search.many", "int.protein.search");
        // No proteins found
        map.put("error.int.protein.search.empty", "int.protein.search");
        // Error with yasp
        map.put("error.int.protein.search.empty.parse", "int.protein.search");

        // Experiment search in the Interaction editor.
        map.put("error.int.sanity.exp", "int.exp.search");
        // No criteria specified.
        map.put("error.int.exp.search.input", "int.exp.search");
        // No experiments found for citeria
        map.put("error.int.exp.search.empty", "int.exp.search");
        // Too many experiments
        map.put("error.int.exp.search.many", "int.exp.search");
        // General Interaction page anchors (not sure these were used or not)
        map.put("int.interaction", "info");
        map.put("int.organism", "info");

        // Biosource anchors
        map.put("error.taxid.mask", "info");
		map.put("error.bs.sanity.taxid", "info");
		map.put("error.bs.sanity.taxid.dup", "info");

        // Linking Features - buttons
        map.put(rb.getString("int.proteins.button.feature.link"), "feature.link");
        map.put(rb.getString("int.proteins.button.feature.unlink"), "feature.link");
        // Linking Features - errors
        map.put("error.int.feature.link.error", "feature.link");
        map.put("error.int.feature.unlink.error", "feature.link");

        // Deleting a feature
        map.put(rb.getString("int.proteins.button.feature.delete"), "feature.link");

        return map;
    }
}
