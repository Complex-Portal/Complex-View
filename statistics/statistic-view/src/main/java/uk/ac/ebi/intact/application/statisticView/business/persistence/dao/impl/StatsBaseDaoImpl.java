/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.statisticView.business.persistence.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.application.statisticView.business.model.StatsBase;
import uk.ac.ebi.intact.application.statisticView.business.persistence.dao.StatsBaseDao;
import uk.ac.ebi.intact.context.IntactSession;
import uk.ac.ebi.intact.persistence.dao.impl.HibernateBaseDaoImpl;

import javax.persistence.EntityManager;

/**
 * TODO comment this!
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since <pre>17-Jul-2006</pre>
 */
public class StatsBaseDaoImpl<T extends StatsBase> extends HibernateBaseDaoImpl<T> implements StatsBaseDao<T> {

    private static final Log log = LogFactory.getLog( StatsBaseDaoImpl.class );

    public StatsBaseDaoImpl( Class<T> entityClass, EntityManager entityManager, IntactSession intactSession ) {
        super( entityClass, entityManager, intactSession );
    }

    public T getByAc( int ac ) {
        return getEntityManager().find( getEntityClass(), ac );
    }
}
