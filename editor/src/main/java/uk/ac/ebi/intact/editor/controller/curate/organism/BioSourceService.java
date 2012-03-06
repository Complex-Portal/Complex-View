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
package uk.ac.ebi.intact.editor.controller.curate.organism;

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
import uk.ac.ebi.intact.core.persistence.dao.BioSourceDao;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.model.BioSource;

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
public class BioSourceService extends JpaAwareController {

    private static final Log log = LogFactory.getLog( BioSourceService.class );

    private List<BioSource> allBioSources;
    private List<SelectItem> bioSourceSelectItems;

    @Autowired
    private BioSourceDao bioSourceDao;

    @PostConstruct
    public void loadData() {
        refresh( null );
    }

    @Transactional(propagation = Propagation.NEVER)
    public void refresh( ActionEvent evt ) {
        if ( log.isDebugEnabled() ) log.debug( "Loading BioSources" );

        final DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

        final TransactionStatus transactionStatus = dataContext.beginTransaction();

        allBioSources = bioSourceDao.getAllSorted(0,Integer.MAX_VALUE, "shortLabel", true);

        bioSourceSelectItems = new ArrayList<SelectItem>(allBioSources.size());

        bioSourceSelectItems.add( new SelectItem( null, "-- Select BioSource --", "-- Select BioSource --", false, false, true ) );

        for (BioSource bioSource : allBioSources) {
            bioSourceSelectItems.add(new SelectItem(bioSource, bioSource.getShortLabel(), bioSource.getFullName()));
        }

        dataContext.commitTransaction(transactionStatus);
    }


    public BioSource findBioSourceByAc( String ac ) {
        return bioSourceDao.getByAc( ac );
    }

    public List<SelectItem> getBioSourceSelectItems() {
        return bioSourceSelectItems;
    }

    public List<BioSource> getAllBioSources() {
        return allBioSources;
    }
}
