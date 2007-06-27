/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.statisticView.webapp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;

import java.io.Serializable;

/**
 * Contains the info about a chart in one convenience object
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since <pre>03-Aug-2006</pre>
 */
public class ChartSessionInfo implements Serializable {

    private static final Log log = LogFactory.getLog( ChartSessionInfo.class );

    /**
     * Chart to be rendered.
     */
    private JFreeChart chart;

    /**
     * Height of the generated chart.
     */
    private int height;

    /**
     * Width of the generated chart.
     */
    private int width;

    /**
     * Chart rendering information.
     */
    private ChartRenderingInfo chartRenderingInfo;

    public ChartSessionInfo( JFreeChart chart, int height, int width, ChartRenderingInfo chartRenderingInfo ) {
        this.chart = chart;
        this.height = height;
        this.width = width;
        this.chartRenderingInfo = chartRenderingInfo;
    }

    public JFreeChart getChart() {
        return chart;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public ChartRenderingInfo getChartRenderingInfo() {
        return chartRenderingInfo;
    }
}