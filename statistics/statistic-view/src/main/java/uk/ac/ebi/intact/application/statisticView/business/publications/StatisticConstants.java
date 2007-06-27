/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.statisticView.business.publications;

/**
 * Contains constants required for the Statistics charts application.
 *
 * @author ckohler (ckohler@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08-Feb-2006</pre>
 */
public interface StatisticConstants {


    /**
     * The rotation angle for the labels within the BarChart.
     */
    public static final double ROTATION_ANGLE_OF_CATEGORY_ITEM_LABEL_POSITION = 0.0;

    /**
     * The amount of Bars to be displayed.
     */
    public static final int MAXIMUM_NUMBER_OF_BARS_TO_BE_DISPLAYED = 10;

    /**
     * The category label for the known interactions.
     */
    public static final String LABEL_KNOWN_INTERACTIONS = "Known Interactions";

    /**
     * The category label for the new interactions.
     */
    public static final String LABEL_NEW_INTERACTIONS = "New Interactions";

    /**
     * The name of the diagram.
     */
    public static final String DIAGRAM_TITLE = "IntAct - Top " + MAXIMUM_NUMBER_OF_BARS_TO_BE_DISPLAYED + " Experiments";

    /**
     * The name of the chart.
     */
    public static final String CHART_TITLE = "IntAct Statistics";

    /**
     * The width of the chart.
     */
    public static final int CHART_WIDTH = 1024;

    /**
     * The height of the chart.
     */
    public static final int CHART_HEIGHT = 768;

    /**
     * The label for the X-Axis.
     */
    public static final String LABEL_XAXIS = "EXPERIMENT";

    /**
     * The label for the Y-Axis.
     */
    public static final String LABEL_YAXIS = "# BINARY INTERACTIONS";

    /**
     * Use the logger?.
     */
    public static final boolean USE_LOGGING = false;

    /**
     * By clicking on one of the Bars, the URL where you will be forwarded to.
     */
    public static final String URL_FOR_BAR = "http://www.ebi.ac.uk/intact/search/do/search?searchString=";

    /**
     * The prefix of the File, where the chart is saved.
     */
    public static final String FILE_NAME = "IntAct_StatisticsChart_";

    /**
     * The html-suffix of the File where the chart is saved to.
     */
    public static final String HTML_SUFFIX = ".html";

    /**
     * The suffix for the Fileformat the charts is saved to.
     */
    public static final String FILE_NAME_SUFFIX = ".png";
}