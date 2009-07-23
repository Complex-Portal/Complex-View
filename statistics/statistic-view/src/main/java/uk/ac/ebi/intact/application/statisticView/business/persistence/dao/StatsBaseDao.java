/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.statisticView.business.persistence.dao;

import uk.ac.ebi.intact.application.statisticView.business.model.StatsBase;

import java.util.Collection;

/**
 * TODO comment this!
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since <pre>17-Jul-2006</pre>
 */
public interface StatsBaseDao<T extends StatsBase> {

    T getByAc( int ac );

    Collection<T> getAll();

    void setEntityClass(Class<T> stats);
}
