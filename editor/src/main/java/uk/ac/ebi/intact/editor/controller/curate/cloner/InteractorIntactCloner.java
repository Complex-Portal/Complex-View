package uk.ac.ebi.intact.editor.controller.curate.cloner;

import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.clone.IntactCloner;
import uk.ac.ebi.intact.model.clone.IntactClonerException;

/**
 * Cloner for interactors. The biosource and institution will not be cloned
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>13/06/11</pre>
 */

public class InteractorIntactCloner extends IntactCloner {

    public InteractorIntactCloner(){
        setExcludeACs(true);
    }

    @Override
    public Institution cloneInstitution(Institution institution) throws IntactClonerException {
        return institution;
    }

    @Override
    public BioSource cloneBioSource(BioSource bioSource) throws IntactClonerException  {
        return bioSource;
    }

    @Override
    protected AnnotatedObject cloneAnnotatedObjectCommon(AnnotatedObject<?, ?> ao, AnnotatedObject clone) throws IntactClonerException {
        if (clone == null ) {
            return null;
        }

        if (clone instanceof BioSource ||
                clone instanceof CvObject ||
                clone instanceof Institution) {
            return clone;
        }

        return super.cloneAnnotatedObjectCommon( ao, clone );
    }
}
