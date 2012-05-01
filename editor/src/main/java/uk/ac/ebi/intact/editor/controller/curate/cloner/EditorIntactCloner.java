/**
 * Copyright 2010 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.editor.controller.curate.cloner;

import uk.ac.ebi.intact.core.persister.IntactCore;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.clone.IntactCloner;
import uk.ac.ebi.intact.model.clone.IntactClonerException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class EditorIntactCloner extends IntactCloner {

    private Map<String, String> xrefsMiToExclude;
    
    public EditorIntactCloner() {
        setExcludeACs(true);
        xrefsMiToExclude = new HashMap<String, String>();
        initialiseXrefsToExclude();
    }
    
    private void initialiseXrefsToExclude() {
        xrefsMiToExclude.put(CvDatabase.IMEX_MI_REF, CvXrefQualifier.IMEX_PRIMARY_MI_REF);
    }

    @Override
    public Publication clonePublication(Publication publication) throws IntactClonerException {
        return publication;
    }

    @Override
    public BioSource cloneBioSource(BioSource bioSource) throws IntactClonerException {
        return bioSource;
    }

    @Override
    public CvObject cloneCvObject(CvObject cvObject) throws IntactClonerException {
        return cvObject;
    }

    @Override
    public Institution cloneInstitution(Institution institution) throws IntactClonerException {
        return institution;
    }

    @Override
    public Interactor cloneInteractor(Interactor interactor) throws IntactClonerException {
        return interactor;
    }
    
    @Override
    protected Xref cloneXref(Xref xref) throws IntactClonerException {
        if (xref == null) return null;
        
        // filter xrefs that we don't want to clone
        if (xref.getCvDatabase() != null && xrefsMiToExclude.containsKey(xref.getCvDatabase().getIdentifier())){
            String qualifier = xrefsMiToExclude.get(xref.getCvDatabase().getIdentifier());
            
            if (xref.getCvXrefQualifier() != null && qualifier.equals(xref.getCvXrefQualifier().getIdentifier())){
                return null;
            }
        }

        return super.cloneXref(xref);
    }

    @Override
    protected AnnotatedObject cloneAnnotatedObjectCommon(AnnotatedObject<?, ?> ao, AnnotatedObject clone) throws IntactClonerException {

        if(clone==null){
            return null;
        }

        if (ao == clone) {
            return ao;
        }

        if (!(clone instanceof Interaction) && (clone instanceof Publication ||
                clone instanceof BioSource ||
                clone instanceof CvObject ||
                clone instanceof Institution ||
                clone instanceof Interactor)) {
            return clone;
        }

        // clone xrefs
        Collection<? extends Xref> refs = IntactCore.ensureInitializedXrefs(ao);

        for (Xref xref : refs) {
            Xref clonedXref = clone(xref);

            if (clonedXref != null){
                clone.addXref(clone(xref));
            }
        }
        
        return super.cloneAnnotatedObjectCommon(ao, clone);
    }

    @Override
    protected IntactObject cloneIntactObjectCommon(IntactObject ao, IntactObject clone) throws IntactClonerException {
        if (clone == null){
            return null;
        }

        return super.cloneIntactObjectCommon(ao, clone);
    }
    
    @Override
    protected boolean isCollectionClonable(Collection col){
        
        if (col != null && !col.isEmpty() && col.iterator().next() instanceof Xref){
            return false;
        }
        
        return true;
    }
}
