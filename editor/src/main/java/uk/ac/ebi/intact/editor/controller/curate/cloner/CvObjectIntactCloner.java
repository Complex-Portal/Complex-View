package uk.ac.ebi.intact.editor.controller.curate.cloner;

import uk.ac.ebi.intact.core.persistence.util.CgLibUtil;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.clone.IntactCloner;
import uk.ac.ebi.intact.model.clone.IntactClonerException;

import java.lang.reflect.Constructor;

/**
 * Cloner for cv objects. It will not clone parents and children
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>13/06/11</pre>
 */

public class CvObjectIntactCloner extends IntactCloner {

    public CvObjectIntactCloner(){
        setExcludeACs(true);
        setCloneCvObjectTree(false);
    }

    @Override
    public Institution cloneInstitution(Institution institution) throws IntactClonerException {
        return institution;
    }

    @Override
    protected Xref cloneXref(Xref xref) throws IntactClonerException {
        if (xref == null) return null;

        Class clazz = CgLibUtil.removeCglibEnhanced(xref.getClass());
        Xref clone = null;

        try {
            final Constructor constructor = clazz.getConstructor();
            clone = (Xref) constructor.newInstance();

            clone.setPrimaryId(xref.getPrimaryId());
            clone.setSecondaryId(xref.getSecondaryId());
            clone.setDbRelease(xref.getDbRelease());

            // does not clone cv database and qualifier
            clone.setCvDatabase(xref.getCvDatabase());
            clone.setCvXrefQualifier(xref.getCvXrefQualifier());

        } catch (Exception e) {
            throw new IntactClonerException("An error occured upon building a " + clazz.getSimpleName(), e);
        }

        clone.setParent(clone(xref.getParent()));

        return clone;
    }

    @Override
    protected Annotation cloneAnnotation(Annotation annotation) throws IntactClonerException {
        if (annotation == null) return null;
        Annotation clone = new Annotation();

        // don't clone cvTopic
        clone.setCvTopic(annotation.getCvTopic());
        clone.setAnnotationText(annotation.getAnnotationText());
        return clone;
    }

    @Override
    protected Alias cloneAlias(Alias alias) throws IntactClonerException {
        if (alias == null) return null;

        Class clazz = CgLibUtil.removeCglibEnhanced(alias.getClass());
        Alias clone = null;
        try {
            final Constructor constructor = clazz.getConstructor();
            clone = (Alias) constructor.newInstance();

            // don't clone alias type
            clone.setCvAliasType(alias.getCvAliasType());
            clone.setName(alias.getName());
        } catch (Exception e) {
            throw new IntactClonerException("An error occured upon building a " + clazz.getSimpleName(), e);
        }
        return clone;
    }

    @Override
    protected AnnotatedObject cloneAnnotatedObjectCommon(AnnotatedObject<?, ?> ao, AnnotatedObject clone) throws IntactClonerException {
        if (clone == null ) {
            return null;
        }

        if (clone instanceof Institution) {
            return clone;
        }

        return super.cloneAnnotatedObjectCommon( ao, clone );
    }
}
