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
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.jami.dao.OrganismDao;
import uk.ac.ebi.intact.jami.model.extension.IntactOrganism;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.model.SelectItem;
import java.util.*;

/**
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 */
@Service
@Lazy
public class EditorOrganismService extends JpaAwareController {

    private static final Log log = LogFactory.getLog( EditorOrganismService.class );

    private List<SelectItem> organismSelectItems;

    private boolean isInitialised = false;

    private Map<String, IntactOrganism> acOrganismMap;
    private Map<Integer, IntactOrganism> taxidOrganismMap;


    public EditorOrganismService() {
        acOrganismMap = new HashMap<String, IntactOrganism>();
        taxidOrganismMap = new HashMap<Integer, IntactOrganism>();
    }

    public synchronized void clearAll(){
        this.organismSelectItems = null;
        isInitialised = false;
        acOrganismMap.clear();
        taxidOrganismMap.clear();
    }

    private void loadOrganisms() {
        clearAll();
        organismSelectItems = new ArrayList<SelectItem>();
        organismSelectItems.add(new SelectItem( null, "select organism", "select organism", false, false, true ));

        OrganismDao organismDao = getIntactDao().getOrganismDao();

        List<IntactOrganism> loadedOrganisms = new ArrayList<IntactOrganism>(organismDao.getAllOrganisms(false, false));
        Collections.sort(loadedOrganisms, new Comparator<IntactOrganism>() {
            @Override
            public int compare(IntactOrganism o, IntactOrganism o2) {
                return o.getCommonName().compareTo(o2.getCommonName());
            }
        });
        for (IntactOrganism organism : loadedOrganisms){
            acOrganismMap.put(organism.getAc(), organism);
            taxidOrganismMap.put(organism.getTaxId(), organism);
            organismSelectItems.add(createSelectItem(organism));
        }

    }

    private SelectItem createSelectItem( IntactOrganism organism ) {
        return new SelectItem( organism, organism.getCommonName(), organism.getScientificName());
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public synchronized void loadDataIfNotDone( ComponentSystemEvent event ) {
        if (!FacesContext.getCurrentInstance().isPostback()) {
            if (!isInitialised){
                loadOrganisms();
                isInitialised = true;
            }
        }
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public synchronized void loadDataIfNotDone( ) {
        if (!isInitialised){
            loadOrganisms();
            isInitialised = true;
        }
    }

    public List<SelectItem> getOrganismSelectItems() {
        return organismSelectItems;
    }

    public Collection<IntactOrganism> getAllOrganisms() {
        return this.acOrganismMap.values();
    }

    public IntactOrganism findOrganismByAc(String ac){
        return acOrganismMap.get(ac);
    }

    public IntactOrganism findOrganismByTaxid(int taxid){
        return taxidOrganismMap.get(taxid);
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public synchronized void refresh( ActionEvent evt ) {
        if ( log.isDebugEnabled() ) log.debug( "Loading BioSources" );

        loadOrganisms();
    }
}
