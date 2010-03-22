package uk.ac.ebi.intact.editor.controller.cvobject;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.model.CvObject;

import javax.faces.event.ActionEvent;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public interface CvObjectPopulator {
    @Transactional
    void refresh(ActionEvent evt);

    <T extends CvObject> T findCvObject(Class<T> clazz, String identifier);
}
