/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.statisticView.business.persistence.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.config.BeanDefinition;
import uk.ac.ebi.intact.application.statisticView.business.model.StatsBase;
import uk.ac.ebi.intact.application.statisticView.business.persistence.dao.StatsBaseDao;
import uk.ac.ebi.intact.core.persistence.dao.impl.HibernateBaseDaoImpl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.persistence.EntityManagerFactory;

/**
 * TODO comment this!
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since <pre>17-Jul-2006</pre>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Repository
@Transactional(readOnly = true)
public class StatsBaseDaoImpl<T extends StatsBase> extends HibernateBaseDaoImpl<T> implements StatsBaseDao<T> {

    @PersistenceContext(unitName = "intact-statistics")
    private EntityManager entityManager;

    private static final Log log = LogFactory.getLog( StatsBaseDaoImpl.class );

    public StatsBaseDaoImpl( ) {
        super( );
    }

    public T getByAc( int ac ) {
        return getEntityManager().find( getEntityClass(), ac );
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }
}
