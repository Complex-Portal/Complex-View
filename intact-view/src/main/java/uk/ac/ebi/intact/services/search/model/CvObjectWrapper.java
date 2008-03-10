package uk.ac.ebi.intact.services.search.model;

import uk.ac.ebi.intact.model.CvObject;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class CvObjectWrapper<T extends CvObject> extends AnnotatedObjectWrapper<T> {

    public CvObjectWrapper(T data) {
        super(data);
    }

    public String getMiIdentifier() {
        return getData().getMiIdentifier();
    }
}