/**
 * Copyright 2007 The European Bioinformatics Institute, and others.
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
 *  limitations under the License.
 */
package uk.ac.ebi.intact.application.hierarchview.business.dao;

import uk.ac.ebi.intact.application.hierarchview.business.IntactUser;
import uk.ac.ebi.intact.model.InteractorXref;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

/**
 * TODO comment that class header
 *
 * @author Nadin Neuhauser
 * @version $Id
 * @since 1.6.0-SNAPSHOT
 */
public class HierarchViewDao {

    public HierarchViewDao() {
    }

    public EntityManager getEntityManager() {
        return IntactUser.getCurrentInstance().getDataService().getEntityManager();
        //return IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getEntityManager();
    }

    public List<String[]> getAllPreysByBaitAc(String baitAc) {
        Query query = getEntityManager().createQuery( "select protein2Ac, shortLabel2 from MineInteraction " +
                                                     "where protein1Ac = :baitAc" );
        query.setParameter( "baitAc", baitAc);
        return query.getResultList();
    }

    public List<String[]> getAllBaitsByPreyAc(String preyAc) {
        Query query = getEntityManager().createQuery( "select protein1Ac, shortLabel1 from MineInteraction " +
                                                     "where protein2Ac = :preyAc" );
        query.setParameter( "preyAc", preyAc);
        return query.getResultList();
    }

    public List<InteractorXref> getInteractorXrefsByDatabaseLabelAndProtAc(String databaseShortLabel, String protAc) {
         Query query = getEntityManager().createQuery( "select xref from InteractorXref xref " +
                                                       "where xref.parentAc = :protAc " +
                                                       "and xref.cvDatabase.shortLabel = :databaseShortLabel" );
        query.setParameter( "protAc", protAc);
        query.setParameter( "databaseShortLabel", databaseShortLabel);

        return query.getResultList();
    }
}
