package uk.ac.ebi.intact.editor.controller.curate;

import javax.faces.event.AjaxBehaviorEvent;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public interface ValueChangeAware {
    void changed(AjaxBehaviorEvent evt);
}
