/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
/**
 * That class allow to retrieve the statistics store in the current IntAct node.
 * The data are loaded one per day (cf. <code>MAXIMUM_DATA_AVAILABILITY</code>).
 *
 * This is a singleton class
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
package uk.ac.ebi.intact.application.statisticView.business.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.application.statisticView.business.model.IntactStatistics;
import uk.ac.ebi.intact.application.statisticView.business.persistence.dao.StatsDaoFactory;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.persistence.dao.BaseDao;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Holds statistics data.
 *
 * @author Samuel Kerrien
 */
public final class StatisticsDataSet {

    ////////////////////////////////
    // Constants
    ////////////////////////////////

    public static final long DATA_NOT_LOADED = -1;
    public static final long NONE = -1;

    public static final long MAXIMUM_DATA_AVAILABILITY = 1000 * 60 * 60 * 24; // one days in ms.

    public static final SimpleDateFormat dateFormater = new SimpleDateFormat( "dd-MMM-yyyy" );

    ///////////////////////////////
    // Instance data
    ///////////////////////////////

    /**
     * single Instance of this class
     */
    private static StatisticsDataSet ourInstance;

    /**
     * Data loading logger
     */
    private static final Log logger = LogFactory.getLog( StatisticsDataSet.class );

    /**
     * the statistics
     */
    private static List<IntactStatistics> statistics;

    /**
     * Information related to the origin of the loaded data
     */
    private static String databaseName;
    private static String userName;

    /**
     * When the last data have been loaded ?
     */
    private static long timestamp = DATA_NOT_LOADED;

    ///////////////////////////////
    // Instanciation methods
    ///////////////////////////////

    public synchronized static StatisticsDataSet getInstance() {

        if ( ourInstance == null ) {
            ourInstance = new StatisticsDataSet();
        } else {
            if ( dataOutDated() ) {
                try {
                    collectStatistics();
                } catch ( IntactException e ) {
                    logger.error( "Error when trying to refresh the existing data.", e );
                }
            } else {
                logger.info( "Data are not out dated, reuse them." );
            }
        }

        return ourInstance;
    }

    private StatisticsDataSet() {

        // collect all statistic data from IntAct
        try {
            collectStatistics();
        } catch ( IntactException e ) {
            logger.error( "Error when trying to collect the data.", e );
        }
    }

    ///////////////////////////////
    // Data collection
    ///////////////////////////////

    private static boolean dataOutDated() {

        final long currentTime = System.currentTimeMillis();
        final boolean outdated = ( ( timestamp + MAXIMUM_DATA_AVAILABILITY ) < currentTime );
        logger.info( "Data out dated: " + outdated );
        return outdated;
    }

    /**
     * This private method is called by the others public method of this class.
     * <p/>
     * Get all data of the IA_Statistics table in the IntAct database, thanks to the search method managing an
     * IntactHelper object. The null parameter in the search method means to retrieve all the data from a table via OJB
     * and JDBC.
     */
    private static void collectStatistics() throws IntactException {

        logger.info( "retreiving all statistics..." );
        try {

            BaseDao baseDao = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getBaseDao();
            try {
                userName = baseDao.getDbUserName();
                databaseName = baseDao.getDbName();
                logger.info( "Helper created - access to database " + databaseName + " as " + userName );
            } catch ( SQLException e ) {
                logger.error( "Error when trying to get the database and username.", e );
            }

            // search method to get the IntactStatistics object and all statistics in IntAct
            // null parameter means no restrictive criteria.
            logger.info( "Look for statistics..." );

            final Collection<IntactStatistics> intactStatistics = StatsDaoFactory.getStatsBaseDao( IntactStatistics.class ).getAll();

            // keep track of the time.
            timestamp = System.currentTimeMillis();

            logger.info( "Content of the table before sorting" );
            for ( IntactStatistics singleItem : intactStatistics ) {
                logger.info( singleItem );
            }


            if ( intactStatistics.size() > 0 ) {
                statistics = new ArrayList<IntactStatistics>( intactStatistics );
                Collections.sort( statistics );

            } else {
                // empty collection.
                statistics = Collections.EMPTY_LIST;
            }

            logger.info( "Content of the table after sorting" );
            for ( IntactStatistics singleItem : statistics ) {
                logger.info( singleItem );
            }
        }
        catch ( IntactException ie ) {
            if ( null != logger ) {
                logger.error( "when trying to get all statistics, cause: " + ie.getRootCause(), ie );
            }
            throw ie;
        }
    }

    //////////////////////////////////
    // Data access
    /////////////////////////////////

    /**
     * Return the data collected, this is the only public method.
     *
     * @return a bean containing the statistics and the database source details.
     */
    public final synchronized StatisticsBean getStatisticBean() throws NoDataException {

        return getStatisticBean( null, null );
    }

    /**
     * Return the data collected, this is the only public method.
     *
     * @param startDate the date from which we want to display the statistics. null indicated that there is no start
     *                  date. That date has to respect the format defined by the SimpleDateFormater (DD-MMM-YYYY)
     * @param stopDate  the date until which we want to display the statistics. null indicated that there is no end
     *                  date. That date has to respect the format defined by the SimpleDateFormater (DD-MMM-YYYY)
     *
     * @return a bean containing the statistics and the database source details.
     */
    public final synchronized StatisticsBean getStatisticBean( final String startDate,
                                                               final String stopDate
    )
            throws NoDataException {

        if ( statistics == null || statistics.size() == 0 ) {
            throw new NoDataException();
        }

        long start = NONE;
        if ( startDate != null ) {
            try {
                start = dateFormater.parse( startDate ).getTime();
            } catch ( ParseException e ) {
                e.printStackTrace();
            }
        }

        long stop = NONE;
        if ( stopDate != null ) {
            try {
                stop = dateFormater.parse( stopDate ).getTime();
            } catch ( ParseException e ) {
                e.printStackTrace();
            }
        }

        if ( start != NONE || stop != NONE ) {
            // filter
            final List<IntactStatistics> filteredStatistics = new ArrayList<IntactStatistics>();
            for ( IntactStatistics statistic : statistics ) {
                final long timestamp = statistic.getTimestamp().getTime();
                boolean keepIt = true;

                if ( start != NONE ) {
                    if ( timestamp < start ) {
                        keepIt = false;
                    }
                }

                if ( stop != NONE ) {
                    if ( timestamp > stop ) {
                        keepIt = false;
                    }
                }

                if ( keepIt ) {
                    filteredStatistics.add( statistic );
                }
            } // for

            return new StatisticsBean( filteredStatistics, databaseName, userName );
        }

        // full dataset
        return new StatisticsBean( statistics, databaseName, userName );
    }
}