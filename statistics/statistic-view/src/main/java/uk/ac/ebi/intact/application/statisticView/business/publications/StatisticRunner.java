package uk.ac.ebi.intact.application.statisticView.business.publications;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.application.statisticView.business.publications.jfreechart.GraphRenderer;
import uk.ac.ebi.intact.application.statisticView.business.publications.jfreechart.StackedBarData;
import uk.ac.ebi.intact.application.statisticView.business.publications.model.ExperimentBean;
import uk.ac.ebi.intact.application.statisticView.business.publications.model.PublicationStatisticsBean;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is the main class for the Statistics project. furthermore the necessary Dataset to build up the chart is
 * populated.
 *
 * @author ckohler (ckohler@ebi.ac.uk)
 * @version $Id$
 * @since <pre>14-Feb-2006</pre>
 */
public class StatisticRunner {

    private static final Log logger = LogFactory.getLog( StatisticRunner.class );

    public static void main( String[] args ) throws IntactException, SQLException {

        long startTime = System.currentTimeMillis();

        Connection connection = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().connection();

        QueryRunner queryRunner = new QueryRunner();

        List pubmedExperimentList;

        // run SQL-Statement for building the precalculated Table
        //System.out.print("Loading publication stats ...");
        //System.out.flush();
        ArrayList publicationBeans = ( ArrayList ) DbUtilsBeanFactory.createBean( queryRunner,
                                                                                  PublicationStatisticsBean.class,
                                                                                  connection,
                                                                                  StatisticUtils.getPrecalculatedTable() );
        //System.out.println( publicationBeans.size() + " row(s) retreived." );

        // keep data in order
        List stackedBarGraphData = new LinkedList();

        // count variable in order to leave loop.
        // iteration duration depends on the value of StatisticConstants.MAXIMUM_NUMBER_OF_BARS_TO_BE_DISPLAYED
        int count = 0;

        for ( Iterator iterator = publicationBeans.iterator(); iterator.hasNext(); ) {

            PublicationStatisticsBean dataBean = ( PublicationStatisticsBean ) iterator.next();

            if ( count < StatisticConstants.MAXIMUM_NUMBER_OF_BARS_TO_BE_DISPLAYED ) {

                String pID = dataBean.getPubmed_id();

                // run SQL-Statement to get the Experiment's shortlabel by its pubmedID.
                //System.out.print("Loading experiment details ...");
                //System.out.flush();
                pubmedExperimentList = DbUtilsBeanFactory.createBean( queryRunner,
                                                                      ExperimentBean.class,
                                                                      connection,
                                                                      StatisticUtils.getExperimentShortlabelByPubmedID( pID ) );
                //System.out.println("done");

                if ( pubmedExperimentList.isEmpty() ) {

                    System.out.println( "ERROR: Could not find an experiment for pubmed " + pID );

                } else {


                    ExperimentBean experiment = ( ExperimentBean ) pubmedExperimentList.iterator().next();

                    StackedBarData barData = new StackedBarData( dataBean, experiment );
                    stackedBarGraphData.add( barData );

                    if ( StatisticConstants.USE_LOGGING ) {
                        logger.info( "Stacked Bar Data: " + barData );
                        logger.info( "" );
                    }

                    count++;
                }
            }
        }

        new GraphRenderer( stackedBarGraphData );

        //calculates the total runTime of this application
        StatisticUtils.getTotalRunTime( startTime );

        try {
            connection.close();
        }
        catch ( SQLException e ) {
            e.printStackTrace();
        }
    }
}


