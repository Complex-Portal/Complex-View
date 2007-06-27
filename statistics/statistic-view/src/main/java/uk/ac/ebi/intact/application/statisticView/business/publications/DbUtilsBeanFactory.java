/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.statisticView.business.publications;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * TODO comment this
 *
 * @author ckohler (ckohler@ebi.ac.uk)
 * @version $Id$
 * @since <pre>24-Feb-2006</pre>
 */
public class DbUtilsBeanFactory {

    public static final Log log = LogFactory.getLog( DbUtilsBeanFactory.class );

    /**
     * Create a List of the specified bean using the given SQL statement.
     *
     * @param queryRunner DbUtils Query Runner
     * @param beanClass   the type of bean we want to create
     * @param connection  database connection
     * @param sql         the SQL statement
     *
     * @return a collection of beans.
     *
     * @throws SQLException
     */
    public static List createBean( QueryRunner queryRunner,
                                   Class beanClass,
                                   Connection connection,
                                   String sql
    ) throws SQLException {

        if ( log.isDebugEnabled() ) {
            log.debug( sql );
        }

        return ( List ) queryRunner.query( connection, sql, new BeanListHandler( beanClass ) );
    }
}
