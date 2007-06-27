/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.statisticView.struts.view;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.StandardEntityCollection;
import uk.ac.ebi.intact.application.statisticView.business.data.StatisticHelper;
import uk.ac.ebi.intact.application.statisticView.webapp.ChartSessionInfo;

import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * User: Michael Kleen mkleen@ebi.ac.uk Date: Mar 22, 2005 Time: 4:24:29 PM
 */
public class ViewBeanFactory {

    private static final Log logger = LogFactory.getLog( ViewBeanFactory.class );

    private StatisticHelper helper;
    private String contextPath;

    public ViewBeanFactory( String contextPath ) {
        this.helper = new StatisticHelper();
        this.contextPath = contextPath;
    }


    public IntactStatisticsBean createViewBean( String start, String stop, HttpSession session ) throws IOException {

        final JFreeChart cvChart = helper.getCvChart( start, stop );

        // building charts that support filtering
        final JFreeChart experimentChart = helper.getExperimentChart( start, stop );
        final JFreeChart interactionChart = helper.getInteractionChart( start, stop );
        final JFreeChart proteinChart = helper.getProteinChart( start, stop );
        final JFreeChart binaryChart = helper.getBinaryInteractionChart( start, stop );

        // build charts that don't support filtering
        final JFreeChart identificationChart = helper.getIdentificationChart();
        final JFreeChart bioSourceChart = helper.getBioSourceChart();

        final ChartRenderingInfo info = new ChartRenderingInfo( new StandardEntityCollection() );
        IntactStatisticsBean intactBean = new IntactStatisticsBean( contextPath );
        intactBean.setCvTermChartUrl( putChartInSession( "cvTermChart", cvChart, 600, 400, info, session ) );
        intactBean.setExperimentChartUrl( putChartInSession( "experimentChart", experimentChart, 600, 400, info, session ) );
        intactBean.setInteractionChartUrl( putChartInSession( "interactionChart", interactionChart, 600, 400, info, session ) );
        intactBean.setProteinChartUrl( putChartInSession( "proteinChart", proteinChart, 600, 400, info, session ) );
        intactBean.setBinaryChartUrl( putChartInSession( "binaryChart", binaryChart, 600, 400, info, session ) );

        intactBean.setCvTermCount( helper.getCvCount() );
        intactBean.setExperimentCount( helper.getExperimentCount() );
        intactBean.setInteractionCount( helper.getInteractionCount() );
        intactBean.setProteinCount( helper.getProteinCount() );
        intactBean.setBinaryInteractionCount( helper.getBinaryInteractionCount() );
        intactBean.setBioSourceChartUrl( putChartInSession( "bioSourcechart", bioSourceChart, 600, 400, info, session ) );
        intactBean.setDetectionChartUrl( putChartInSession( "identificationChart", identificationChart, 600, 400, info, session ) );

        return intactBean;
    }

    private static String putChartInSession( String name, JFreeChart chart, int height, int width, ChartRenderingInfo info, HttpSession session )
            throws IOException {
        ChartSessionInfo chartSessionInfo = new ChartSessionInfo( chart, height, width, info );

        session.setAttribute( name, chartSessionInfo );

        return "/servlet/ChartProvider?name=" + name;
    }
}