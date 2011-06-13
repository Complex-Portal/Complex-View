package uk.ac.ebi.intact.editor.controller.curate.cloner;

import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.CvObject;
import uk.ac.ebi.intact.model.Institution;
import uk.ac.ebi.intact.model.clone.IntactCloner;
import uk.ac.ebi.intact.model.clone.IntactClonerException;

/**
 * Cloner for bioSources
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>13/06/11</pre>
 */

public class BiosourceIntactCloner extends IntactCloner {

    public BiosourceIntactCloner(){
        setExcludeACs(true);
    }

    @Override
    public Institution cloneInstitution(Institution institution) throws IntactClonerException {
        return institution;
    }

    @Override
    public CvObject cloneCvObject(CvObject cvObject) throws IntactClonerException {
        return cvObject;
    }

    @Override
    protected AnnotatedObject cloneAnnotatedObjectCommon(AnnotatedObject<?, ?> ao, AnnotatedObject clone) throws IntactClonerException {
        if (clone == null ) {
            return null;
        }

        if (clone instanceof CvObject ||
                clone instanceof Institution) {
            return clone;
        }

        return super.cloneAnnotatedObjectCommon( ao, clone );
    }
}
