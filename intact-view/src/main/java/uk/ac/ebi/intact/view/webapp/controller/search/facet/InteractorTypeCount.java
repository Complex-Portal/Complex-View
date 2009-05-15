/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.view.webapp.controller.search.facet;

import org.apache.solr.client.solrj.response.FacetField;
import uk.ac.ebi.intact.model.CvInteractorType;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class InteractorTypeCount extends AbstractCount {

    public InteractorTypeCount(FacetField facetField) {
        super(facetField);
    }

    public long getPeptideCount() {
        return getCount(CvInteractorType.PEPTIDE_MI_REF);
    }

    public long getProteinCount() {
        return getCount(CvInteractorType.PROTEIN_MI_REF);
    }

    public long getSmallMoleculeCount() {
        return getCount(CvInteractorType.SMALL_MOLECULE_MI_REF);
    }

    public long getDnaCount() {
        return getCount(CvInteractorType.DNA_MI_REF);
    }

    public long getRnaCount() {
        return getCount(CvInteractorType.RNA_MI_REF);
    }

    public long getNucleicAcidCount() {
        return getCount(CvInteractorType.NUCLEIC_ACID_MI_REF);
    }
}
