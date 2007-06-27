package uk.ac.ebi.intact.application.statisticView.business.publications.jfreechart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.*;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.imagemap.ImageMapUtilities;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.TextAnchor;
import uk.ac.ebi.intact.application.statisticView.business.publications.StatisticConstants;
import uk.ac.ebi.intact.application.statisticView.business.publications.StatisticUtils;
import uk.ac.ebi.intact.application.statisticView.business.publications.model.ExperimentBean;
import uk.ac.ebi.intact.application.statisticView.business.publications.model.PublicationStatisticsBean;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Iterator;
import java.util.List;

/**
 * This class renders the Statistic chart.
 *
 * @author ckohler (ckohler@ebi.ac.uk)
 * @version $Id$
 * @since <pre>14-Feb-2006</pre>
 */
public class GraphRenderer extends JFrame implements Serializable {

    private static final Log logger = LogFactory.getLog( GraphRenderer.class );

    /**
     * Data of the Chart
     */
    private List stackedBarGraphData;

    /**
     * not allowed
     */
    private GraphRenderer() {
    }

    /**
     * Creates a <code>GraphRenderer</code> with data out of the List.
     *
     * @param stackedBarGraphData the data for the graph
     */
    public GraphRenderer( List stackedBarGraphData ) {

        super( StatisticConstants.CHART_TITLE );

        if ( stackedBarGraphData == null || stackedBarGraphData.isEmpty() ) {
            throw new IllegalArgumentException( "Please give some data." );
        }

        this.stackedBarGraphData = stackedBarGraphData;

        initGraph();
    }

    /**
     * initializes the Graph.
     */
    private void initGraph() {

        CategoryDataset dataset = createDataset( stackedBarGraphData );
        JFreeChart chart = createBarChart( dataset );
        ChartPanel chartPanel = new ChartPanel( chart );
        chartPanel.setPreferredSize( new Dimension( StatisticConstants.CHART_WIDTH, StatisticConstants.CHART_HEIGHT ) );
        getContentPane().add( chartPanel );
        pack();
        setLocationRelativeTo( null );
        setVisible( true );
        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    }

    /**
     * Creates the dataset for the Graph.
     *
     * @return CategoryDataset, now filled with Data out of the passed List.
     */
    public CategoryDataset createDataset( List data ) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        int count = 0;

        for ( Iterator iterator = data.iterator(); iterator.hasNext() && count < StatisticConstants.MAXIMUM_NUMBER_OF_BARS_TO_BE_DISPLAYED; )
        {
            StackedBarData barData = ( StackedBarData ) iterator.next();
            count++;

            PublicationStatisticsBean publiBean = barData.getPublicationStatisticsBean();
            ExperimentBean expBean = barData.getExperimentBean();

            String graphLabel = StatisticUtils.clipString( expBean.getShortlabel() );

            dataset.addValue( publiBean.getKnown_interactions(),
                              StatisticConstants.LABEL_KNOWN_INTERACTIONS,
                              graphLabel );

            dataset.addValue( publiBean.getNew_interactions(),
                              StatisticConstants.LABEL_NEW_INTERACTIONS,
                              graphLabel );
        }

        return dataset;
    }


    /**
     * Creates the chart out of the given CategoryDataset
     *
     * @param dataset the CategoryDataset which includes the necessaryt Data for creating the chart.
     *
     * @return the chart
     */
    private JFreeChart createBarChart( final CategoryDataset dataset ) {
        JFreeChart chart = ChartFactory.createStackedBarChart( StatisticConstants.DIAGRAM_TITLE, // title diagram
                                                               StatisticConstants.LABEL_XAXIS, // label x-axis
                                                               StatisticConstants.LABEL_YAXIS, // label y-axis
                                                               dataset, // data
                                                               PlotOrientation.VERTICAL, // orientation
                                                               true, // show legend?
                                                               true, // show tooltips?
                                                               true // show URLs?
        );

        //Get a refernce to the plot for further modifications
        final CategoryPlot plot = chart.getCategoryPlot();

        // create new instance of modified StackedBarRenderer
        final StackedBarRenderer renderer = new IntActStackedBarRenderer();

        //Sets the item label generator for ALL series.
        renderer.setItemLabelGenerator( new StandardCategoryItemLabelGenerator() );

        //Sets the visibility of the item labels for ALL series.
        renderer.setItemLabelsVisible( true );


        final ItemLabelPosition p = new ItemLabelPosition(
                ItemLabelAnchor.CENTER, TextAnchor.CENTER, TextAnchor.CENTER, StatisticConstants.ROTATION_ANGLE_OF_CATEGORY_ITEM_LABEL_POSITION );

        //Sets the item label position for positive values in ALL series.
        renderer.setPositiveItemLabelPosition( p );

        //Sets the tool tip generator for ALL series.
        renderer.setToolTipGenerator( new StandardCategoryToolTipGenerator() );

        // get a reference to the x-axis.
        final CategoryAxis domainAxis = plot.getDomainAxis();

        //Sets the category label position specification for the axis. Here the displayed angle is 45.
        domainAxis.setCategoryLabelPositions( CategoryLabelPositions.UP_45 );

        //Sets a flag that controls whether or not the axis line is visible.
        domainAxis.setAxisLineVisible( true );

        // Sets a flag that controls whether or not the axis is visible.
        domainAxis.setVisible( true );

        //get a reference to the xy-axis.
        final ValueAxis rangeAxis = plot.getRangeAxis();
        //Sets a flag that determines whether or not the axis range is automatically adjusted to fit the data.
        rangeAxis.setAutoRange( true );
        //
        //rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        //Sets the lower margin for the axis (as a percentage of the axis range).
        rangeAxis.setLowerMargin( 0.10 );
        //Sets the upper margin for the axis (as a percentage of the axis range).
        rangeAxis.setUpperMargin( 0.10 );

        /* List plotCategories = plot.getCategories();
StringBuffer categoryURLBuffer = new StringBuffer(512);
categoryURLBuffer.append(StatisticConstants.URL_FOR_BAR);

for (Iterator iterator = plotCategories.iterator(); iterator.hasNext();) {
String string = (String) iterator.next();
categoryURLBuffer.append(string).append("-*");
if (iterator.hasNext()) {
categoryURLBuffer.append(",");
}
}
renderer.setItemURLGenerator(
new StandardCategoryURLGenerator
        (categoryURLBuffer.toString()));*/
        plot.setRenderer( renderer );

        // needed to generate an ImageMap out of the graph.
        try {
            ChartRenderingInfo info = new ChartRenderingInfo(
                    new StandardEntityCollection()
            );
            // save chart in *.png format
            File file1 = new File( StatisticConstants.FILE_NAME +
                                   StatisticConstants.MAXIMUM_NUMBER_OF_BARS_TO_BE_DISPLAYED +
                                   StatisticConstants.FILE_NAME_SUFFIX );
            ChartUtilities.saveChartAsPNG( file1,
                                           chart,
                                           StatisticConstants.CHART_WIDTH,
                                           StatisticConstants.CHART_HEIGHT,
                                           info );

            // write an HTML page incorporating the image with an image map
            File file2 = new File( StatisticConstants.FILE_NAME
                                   + StatisticConstants.MAXIMUM_NUMBER_OF_BARS_TO_BE_DISPLAYED
                                   + StatisticConstants.HTML_SUFFIX );
            OutputStream out = new BufferedOutputStream(
                    new FileOutputStream( file2 )
            );
            PrintWriter writer = new PrintWriter( out );
            writer.println( "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"" );
            writer.println( "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">" );
            writer.println( "<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">" );
            writer.println( "<head><title>" + StatisticConstants.CHART_TITLE + "</title></head>" );
            writer.println( "<body><p>" );
            ImageMapUtilities.writeImageMap( writer, "chart", info );
            writer.println( "<img src=" + StatisticConstants.FILE_NAME
                            + StatisticConstants.MAXIMUM_NUMBER_OF_BARS_TO_BE_DISPLAYED
                            + StatisticConstants.FILE_NAME_SUFFIX
                            + " width=\"" + StatisticConstants.CHART_WIDTH + "\" height=\"" + StatisticConstants.CHART_HEIGHT + "\" usemap=\"#chart\" alt=" + StatisticConstants.FILE_NAME + StatisticConstants.MAXIMUM_NUMBER_OF_BARS_TO_BE_DISPLAYED + StatisticConstants.FILE_NAME_SUFFIX + "/>" );
            writer.println( "</p></body>" );
            writer.println( "</html>" );
            writer.close();
        }
        catch ( IOException e ) {
            logger.info( e.toString() );

        }

        return chart;
    }

}
