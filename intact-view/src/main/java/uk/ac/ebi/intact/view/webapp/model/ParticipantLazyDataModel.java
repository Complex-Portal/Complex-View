package uk.ac.ebi.intact.view.webapp.model;

import org.apache.commons.collections.map.LRUMap;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.springframework.transaction.TransactionStatus;
import sun.misc.LRUCache;
import uk.ac.ebi.intact.core.context.DataContext;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.model.Component;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Lazy data model for participants
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>02/10/12</pre>
 */

public class ParticipantLazyDataModel extends LazyDataModel<ParticipantWrapper> {

    private EntityManager entityManager;
    private DataContext dataContext;
    private String interactionAc;
    private int participantCount=0;
    private LRUMap participantCache;

    public ParticipantLazyDataModel(DataContext dataContext, EntityManager entityManager, String interactionAc, int participantCount){
        this.dataContext = dataContext;
        this.entityManager = entityManager;
        this.interactionAc = interactionAc;
        this.participantCount = participantCount;
        this.participantCache = new LRUMap(30);
    }

    @Override
    public List<ParticipantWrapper> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {

        String key = first+"_"+pageSize;
        if (participantCache.containsKey(key)){
           return (List<ParticipantWrapper>)participantCache.get(key);
        }

        TransactionStatus status = dataContext.beginTransaction();

        List<Component> components = entityManager.createQuery("select p from InteractionImpl i join i.components as p join p.experimentalRoles as expRole " +
                "where i.ac = :ac order by expRole.shortLabel, p.ac").setParameter("ac", interactionAc)
                .setFirstResult(first).setMaxResults(pageSize).getResultList();
        List<ParticipantWrapper> participants = new ArrayList<ParticipantWrapper>(components.size());

        for (Component comp : components){
            participants.add(new ParticipantWrapper(comp));
        }

        dataContext.commitTransaction(status);

        participantCache.put(key, participants);

        return participants;
    }

    public int getRowCount() {

        return participantCount;
    }
}
