/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.statisticView.business.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.application.statisticView.business.model.IntactStatistics;
import uk.ac.ebi.intact.business.IntactException;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Bean which allows you to hold the data to display in the view.
 * It includes the statistics data and the information related to
 * the database it come from.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public final class StatisticsBean {

    public static final SimpleDateFormat dateFormater = new SimpleDateFormat( "dd MMM yyyy" );

    private static final Log logger = LogFactory.getLog( StatisticsBean.class );

    /**
     * the statistics
     */
    private final List<IntactStatistics> statistics;

    /**
     * Information related to the origin of the loaded data
     */
    private final String databaseName;
    private final String userName;

    public StatisticsBean( final List<IntactStatistics> statistics,
                           final String databaseName,
                           final String userName
    ) {
        this.statistics = statistics;
        this.databaseName = databaseName;
        this.userName = userName;
    }

    public final List<IntactStatistics> getStatistics() {
        return statistics;
    }

    public final String getDatabaseName() {
        return databaseName;
    }

    public final String getUserName() {
        return userName;
    }

    /**
     * Retrieve the latest timestamp of the Statistics table
     *
     * @return Timestamp the timestamp of the last line in the Statistics table or null if no data found.
     */
    public final Timestamp getLastTimestamp() throws NoDataException {

        final IntactStatistics item = getLastRow();
        if ( item != null ) {
            logger.info( "latest timestamp found" );
            return item.getTimestamp();
        } else {
            logger.info( "No data found" );
            return null;
        }
    }

    /**
     * Retrieve the latest data of the Statistics table
     *
     * @return Collection which contains the latest data of the Statistics table
     */
    public final IntactStatistics getLastRow() throws NoDataException {

        final int size = statistics.size();
        if ( size > 0 ) {
            logger.info( "last row found" );
            return statistics.get( size - 1 );
        } else {
            logger.info( "No data found" );
            throw new NoDataException();
        }
    }


    /**
     * Retrieve the eldest data of the Statistics table
     *
     * @return Collection which contains the latest data of the Statistics table
     */
    public final IntactStatistics getFirstRow() throws NoDataException {

        final int size = statistics.size();
        if ( size > 0 ) {
            logger.info( "last row found" );
            return statistics.get( 0 );
        } else {
            logger.info( "No data found" );
            throw new NoDataException();
        }
    }


    public final String getMoreRecentStatisticsDate() throws IntactException, NoDataException {

        final Timestamp timestamp = getLastTimestamp();
        if ( timestamp == null ) {
            logger.error( "Could not get the last imestamp." );
            throw new IntactException( "There is no statistics to display" );
        }

        final Date date = new Date( timestamp.getTime() );
        return dateFormater.format( date );
    }
}
