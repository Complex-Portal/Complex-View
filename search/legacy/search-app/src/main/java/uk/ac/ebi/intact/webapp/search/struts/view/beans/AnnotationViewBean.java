package uk.ac.ebi.intact.webapp.search.struts.view.beans;

import uk.ac.ebi.intact.model.Annotation;

/**
 * Content of information to display for an Annotation object.
 *
 * @author Michael Kleen
 * @version AnnotationViewBean.java Date: Nov 23, 2004 Time: 4:24:13 PM
 */
public class AnnotationViewBean {

    /**
     * An annotation.
     */
    private final Annotation anAnnotation;


    /**
     * Constructs an AnnotationViewBean object.
     *
     * @param anAnnotation an Annotation object
     */
    public AnnotationViewBean( final Annotation anAnnotation ) {
        this.anAnnotation = anAnnotation;
    }

    /**
     * Gets this object's text.
     *
     * @return annotation current text
     */
    public String getText() {
        return this.anAnnotation.getAnnotationText();
    }

    /**
     * Gets this annotation's CvTopic's name.
     *
     * @return a String representing this annotation's CvTopic's name.
     */
    public String getName() {
        return this.anAnnotation.getCvTopic().getShortLabel();

    }

    /**
     * Returns the object value.
     *
     * @return an Annotation object representing the object value
     */
    public Annotation getObject() {
        return this.anAnnotation;
    }

 }