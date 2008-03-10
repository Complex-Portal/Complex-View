package uk.ac.ebi.intact.services.search.model;

import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.Annotation;
import uk.ac.ebi.intact.model.Institution;

import java.util.Collection;
import java.util.Date;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class AnnotatedObjectWrapper<T extends AnnotatedObject> {

    private T data;

    public AnnotatedObjectWrapper(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public String getShortLabel() {
        return data.getShortLabel();
    }

    public String getFullName() {
        return data.getFullName();
    }

    public Collection getXrefs() {
        return data.getXrefs();
    }

    public Collection getAliases() {
        return data.getAliases();
    }

    public Collection<Annotation> getAnnotations() {
        return data.getAnnotations();
    }

    public String getCreator() {
        return data.getCreator();
    }

    public Date getUpdated() {
        return data.getUpdated();
    }

    public Date getCreated() {
        return data.getCreated();
    }

    public String getUpdator() {
        return data.getUpdator();
    }

    public String getAc() {
        return data.getAc();
    }

    public Institution getOwner() {
        return data.getOwner();
    }
}