/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.statisticView.graphic;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYDataset;

import java.awt.*;

/**
 * @author Michael Kleen
 * @version ChartFactory.java Date: Feb 17, 2005 Time: 4:57:50 PM
 *          <p/>
 *          The Chart Factory provides a the basic graphs which are needed for statisticView.
 */
public class ChartFactory {

    /**
     * ChartFactory should not instantiated
     */
    private ChartFactory() {

    }

    /**
     * This method generates a JfreeChart PieChartDiagramm
     *
     * @param dataset a DefaultPieDataset which holds the datavalues for the specific PieChart
     * @param title   a String which represents the title of the specific PieChart
     *
     * @return a JfreeChart PieChart Diagramm
     */
    public static JFreeChart getPieChart( final DefaultPieDataset dataset, final String title ) {

        final DefaultPieDataset pieDataSet = dataset;

        final JFreeChart chart = org.jfree.chart.ChartFactory.createPieChart( title, // chart title
                                                                              pieDataSet, // data
                                                                              false, // exclude legend
                                                                              true,
                                                                              true );

        final PiePlot plot = ( PiePlot ) chart.getPlot();

        // set a new font for the label

        plot.setLabelFont( new Font( "SansSerif", Font.PLAIN, 8 ) );
        plot.setNoDataMessage( "No data available" );

        // rotate the plot
        plot.setStartAngle( 180 );


        plot.setCircular( true );
        plot.setLabelGap( 0.1 );

        // set a background color
        chart.setBackgroundPaint( new Color( 237, 237, 237 ) );

        return chart;


    }

    /**
     * This method generates a JfreeChart BarChartDiagramm.
     *
     * @param dataset     a DefaultCategoryDataset which holds the datavalues for the specific BarChart
     * @param title       a String which represents the title of the specific BarChart
     * @param xAxisTitle  a String which represent the x-axis label of the specific BarChart
     * @param yAxisTitle  a String which represent the y-axis label of the specific BarChart
     * @param rotateLabel a boolean which descripes if the labels should rotate which 3 degree used for long names in
     *                    the Dataset true means rotate the label. false means not to rotate the label
     *
     * @return a JfreeChart BarChart Diagramm
     */
    public static JFreeChart getBarChart( final DefaultCategoryDataset dataset,
                                          final String title,
                                          final String xAxisTitle,
                                          final String yAxisTitle,
                                          final boolean rotateLabel
    ) {

        // cast to dataset
        DefaultCategoryDataset barDataset = dataset;

        JFreeChart chart = org.jfree.chart.ChartFactory.createBarChart( title, // chart title
                                                                        xAxisTitle, // domain axis label
                                                                        yAxisTitle, // range axis label
                                                                        barDataset, // data
                                                                        PlotOrientation.VERTICAL, // orientation
                                                                        true, // include legend
                                                                        true, // tooltips
                                                                        true // no URLs
        );

        // modify the chart and setting nice colour
        CategoryPlot plot = chart.getCategoryPlot();
        chart.setBackgroundPaint( new Color( 237, 237, 237 ) );

        if ( rotateLabel == true ) {
            CategoryAxis domainAxis = plot.getDomainAxis();
            domainAxis.setCategoryLabelPositions( CategoryLabelPositions.createUpRotationLabelPositions( Math.PI / 6.0 ) );
        }

        // set the range axis to display integers only
        final NumberAxis rangeAxis = ( NumberAxis ) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits( NumberAxis.createIntegerTickUnits() );

        // disable bar outlines
        BarRenderer renderer = ( BarRenderer ) plot.getRenderer();
        renderer.setDrawBarOutline( false );
        // renderer.setMaxBarWidth(0.8);
        renderer.setMaximumBarWidth( 0.4 );

        // set up gradient paints for series
        GradientPaint gp0 = new GradientPaint( 0.0f, 0.0f, Color.red,
                                               0.0f, 0.0f, new Color( 64, 0, 0 ) );
        GradientPaint gp1 = new GradientPaint( 0.0f, 0.0f, Color.blue,
                                               0.0f, 0.0f, new Color( 0, 0, 64 ) );

        // renderer.
        renderer.setSeriesPaint( 0, gp0 );
        renderer.setSeriesPaint( 1, gp1 );

        return chart;
    }

    /**
     * @param dataset    a XYDataset which holds the datavalues for the specific XYChart
     * @param title      a String which represents the title of the specific BarChart
     * @param xAxisTitle a String which represent the x-axis label of the specific XYChart
     * @param yAxisTitle a String which represent the y-axis label of the specific XYChart
     *
     * @return a JfreeChart XYChart Diagramm
     */
    public static JFreeChart getXYChart( final XYDataset dataset, final String title, final String xAxisTitle,
                                         final String yAxisTitle
    ) {

        XYDataset xyDataset = dataset;

        final ValueAxis timeAxis = new DateAxis( xAxisTitle );
        final NumberAxis valueAxis = new NumberAxis( yAxisTitle );

        valueAxis.setAutoRangeIncludesZero( false );  // override default
        final StandardXYItemRenderer renderer =
                new StandardXYItemRenderer( StandardXYItemRenderer.LINES + StandardXYItemRenderer.SHAPES,
                                            null, // ToolTip
                                            null ); // URLs
        renderer.setShapesFilled( true );

        final XYPlot plot = new XYPlot( xyDataset, timeAxis, valueAxis, renderer );
        final JFreeChart xyChart = new JFreeChart( title, JFreeChart.DEFAULT_TITLE_FONT, plot,
                                                   false );
        xyChart.setBackgroundPaint( new Color( 237, 237, 237 ) );

        JFreeChart chart = org.jfree.chart.ChartFactory.createXYLineChart( title, // chart title
                                                                           xAxisTitle, // domain axis label
                                                                           yAxisTitle, // range axis label
                                                                           xyDataset, // data
                                                                           PlotOrientation.VERTICAL, // orientation
                                                                           true, // include legend
                                                                           true, // tooltips
                                                                           true // no URLs
        );
        return xyChart;
    }
}
