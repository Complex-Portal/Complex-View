/**
 * Copyright 2011 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.editor.controller.curate.interaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.bridges.taxonomy.TaxonomyService;
import uk.ac.ebi.intact.bridges.taxonomy.UniprotTaxonomyService;
import uk.ac.ebi.intact.core.persistence.dao.BioSourceDao;
import uk.ac.ebi.intact.model.BioSource;
import uk.ac.ebi.intact.util.biosource.BioSourceServiceException;
import uk.ac.ebi.intact.util.biosource.BioSourceServiceImpl;

/**
 * Checks if the biosource exist in IntAct first.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class EditorBioSourceService extends BioSourceServiceImpl {

    @Autowired
    private BioSourceDao bioSourceDao;

    public EditorBioSourceService() {
       super(new UniprotTaxonomyService());
    }

    public EditorBioSourceService(TaxonomyService taxonomyService) {
        super(taxonomyService);
    }

    @Override
    @Transactional(value = "transactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public BioSource getBiosourceByTaxid(String taxid) throws BioSourceServiceException {
        BioSource biosource = bioSourceDao.getByTaxonIdUnique(taxid);

        if (biosource == null) {
          biosource = super.getBiosourceByTaxid(taxid);
        }

        return biosource;
    }
}
