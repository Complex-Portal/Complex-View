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
package uk.ac.ebi.intact.editor.controller.curate.institution;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.context.DataContext;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.InstitutionDao;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.model.Institution;

import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Lazy
public class InstitutionService extends JpaAwareController {

    private static final Log log = LogFactory.getLog( InstitutionService.class );

    private List<Institution> allInstitutions;
    private List<SelectItem> institutionSelectItems;

    @Autowired
    private InstitutionDao institutionDao;

    @PostConstruct
    public void loadData() {
        refresh( null );
    }

    @Transactional(propagation = Propagation.NEVER)
    public void refresh( ActionEvent evt ) {
        if ( log.isDebugEnabled() ) log.debug( "Loading Institutions" );

        final DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

        final TransactionStatus transactionStatus = dataContext.beginTransaction(getClass().getSimpleName());

        allInstitutions = institutionDao.getAllSorted(0,Integer.MAX_VALUE, "shortLabel", true);

        institutionSelectItems = new ArrayList<SelectItem>(allInstitutions.size());

        for (Institution institution : allInstitutions) {
            institutionSelectItems.add(new SelectItem(institution, institution.getShortLabel(), institution.getFullName()));
        }

        dataContext.commitTransaction(transactionStatus);
    }


    public Institution findInstitutionByAc( String ac ) {
        return institutionDao.getByAc( ac );
    }

    public List<SelectItem> getInstitutionSelectItems() {
        return getInstitutionSelectItems(true);
    }

    public List<SelectItem> getInstitutionSelectItems(boolean addDefaultNoSelection) {
        List<SelectItem> items = new ArrayList(institutionSelectItems);

        if (addDefaultNoSelection) {
            items.add( new SelectItem( null, "-- Select Institution --", "-- Select Institution --", false, false, true ) );
        }

        return items;
    }

    public List<Institution> getAllInstitutions() {
        return allInstitutions;
    }
}
