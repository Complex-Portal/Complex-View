/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */

package uk.ac.ebi.intact.application.hierarchview.business.image;

// intact

import org.apache.log4j.Logger;
import uk.ac.ebi.intact.application.hierarchview.business.Constants;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.business.graph.InteractionNetwork;
import uk.ac.ebi.intact.util.simplegraph.BasicGraphI;
import uk.ac.ebi.intact.util.simplegraph.EdgeI;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * Purpose : ------- This class allows to tranform a graph to an image (binary
 * content)
 * 
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */

public class DrawGraph {

    static Logger logger = Logger.getLogger( Constants.LOGGER_NAME );

    /** ******** CONSTANTS *********** */
    private final static int DEFAULT_BORDER_SIZE = 5;
    private final static int DEFAULT_IMAGE_LENGTH = 300;
    private final static int DEFAULT_IMAGE_HEIGHT = 300;
    private final static String DEFAULT_MAP_NAME = "networkMap";

    private final static Color DEFAULT_BACKGROUND_COLOR = new Color( 255, 255,
            255 );

    private final static Color DEFAULT_NODE_COLOR = new Color( 75, 158, 179 );

    private final static Color DEFAULT_EDGE_COLOR = new Color( 75, 158, 179 );

    private final static Color DEFAULT_HIGHLIGHT_EDGE_COLOR = new Color( 255,
            0, 0 );
    private final static String DEFAULT_SHAPE_STATE = "disable";
    private final static String DEFAULT_BORDER_STATE = "enable";

    private final static Color DEFAULT_BORDER_COLOR = new Color( 255, 255, 255 );

    private final static String DEFAULT_LABEL_FONT_NAME = "Arial";
    private final static int DEFAULT_LABEL_FONT_SIZE = 10;

    private final static String DEFAULT_TEXT_ANTIALIASED = "disable";
    private final static String DEFAULT_NODE_ANTIALIASED = "disable";
    private final static String DEFAULT_EDGE_ANTIALIASED = "disable";

    private final static float DEFAULT_EDGE_THICKNESS = 1f;

    private final static int DEFAULT_INTERNAL_TOP_MARGIN = 2;
    private final static int DEFAULT_INTERNAL_BOTTOM_MARGIN = 2;
    private final static int DEFAULT_INTERNAL_LEFT_MARGIN = 5;
    private final static int DEFAULT_INTERNAL_RIGHT_MARGIN = 5;

    /** ******** INSTANCE VARIABLES *********** */

    /**
     * Allows to store the HTML MAP code we will use with the image we generate
     */
    private StringBuffer mapCode = null;

    /**
     * where we will write the content of the image
     */
    private BufferedImage bufferedImage;

    /**
     * The Javascript Array containing all of the nodes coordinates
     */
    private StringBuffer nodeCoordinates = null;

    /**
     * The name of the HTML MAP
     */
    private static String mapName;

    /**
     * The Interaction Network graph which will allow to create the image
     */
    private InteractionNetwork graph = null;

    /**
     * The centralnodes of the network
     */
    private Collection centralNodes;

    /**
     * Size of the image
     */
    private int imageSizex;
    private int imageSizey;

    /**
     * allow to apply a resizing of the image components
     */
    private float dimensionRateX;
    private float dimensionRateY;

    private String applicationPath;
    private String currentTime;

    /**
     *  
     */
    private static int borderSize;

    /**
     * Internal margin of a node
     */
    private static int internalTopMargin, internalBottomMargin,
            internalLeftMargin, internalRightMargin;

    /**
     * The used font to write the label
     */
    private static Font fontLabel;
    private static Font boldFontLabel;

    private static Color backgroundColor;
    private static Color nodeDefaultColor;
    private static Color edgeDefaultColor;
    private static Color edgeHighlightDefaultColor;
    private static Color borderColor;
    private static String borderEnable;

    private static int imageLength, imageHeight;

    private static String shapeEnable;

    private static String edgeAntialiased;
    private static String textAntialiased;
    private static String nodeAntialiased;

    private static float edgeThickness;

    // stores the nodes of the path found by MiNe
    private String minePath = null;

    static {
        Properties properties = IntactUserI.GRAPH_PROPERTIES;

        // read the background and border color in the property file
        String stringBgColor = null;
        String stringBorderColor = null;
        String stringNodeColorDefault = null;
        String stringEdgeColorDefault = null;
        String stringEdgeHighlightColor = null;

        String border = null;
        String xSize = null;
        String ySize = null;
        String fontName = null;
        String fontSize = null;
        String thicknessStr = null;
        int intFontSize = 0;

        String internalTopMarginStr = null;
        String internalBottomMarginStr = null;
        String internalLeftMarginStr = null;
        String internalRightMarginStr = null;
        internalTopMargin = 0;
        internalBottomMargin = 0;
        internalLeftMargin = 0;
        internalRightMargin = 0;

        borderEnable = null;
        nodeAntialiased = null;
        mapName = null;
        edgeAntialiased = null;
        textAntialiased = null;
        shapeEnable = null;

        // read all the needed properties in the file
        if ( null != properties ) {
            stringBgColor = properties
                    .getProperty( "hierarchView.image.color.default.background" );
            stringBorderColor = properties
                    .getProperty( "hierarchView.image.color.default.border" );
            stringNodeColorDefault = properties
                    .getProperty( "hierarchView.image.color.default.node" );
            stringEdgeColorDefault = properties
                    .getProperty( "hierarchView.image.color.default.edge" );
            stringEdgeHighlightColor = properties
                    .getProperty( "hierarchView.image.color.highlight.edge" );
            borderEnable = properties.getProperty( "hierarchView.image.border" );
            border = properties
                    .getProperty( "hierarchView.image.size.default.border" );
            xSize = properties
                    .getProperty( "hierarchView.image.size.default.image.length" );
            ySize = properties
                    .getProperty( "hierarchView.image.size.default.image.height" );
            fontName = properties
                    .getProperty( "hierarchView.image.font.name.label" );
            fontSize = properties
                    .getProperty( "hierarchView.image.font.size.label" );
            mapName = properties.getProperty( "hierarchView.image.map.name" );
            edgeAntialiased = properties
                    .getProperty( "hierarchView.image.edge.antialiased" );
            textAntialiased = properties
                    .getProperty( "hierarchView.image.font.antialiased" );
            thicknessStr = properties
                    .getProperty( "hierarchView.image.edge.thickness" );
            nodeAntialiased = properties
                    .getProperty( "hierarchView.image.node.antialiased" );
            shapeEnable = properties.getProperty( "hierarchView.image.shape" );
            internalTopMarginStr = properties
                    .getProperty( "hierarchView.image.node.internalTopMargin" );
            internalBottomMarginStr = properties
                    .getProperty( "hierarchView.image.node.internalBottomMargin" );
            internalLeftMarginStr = properties
                    .getProperty( "hierarchView.image.node.internalLeftMargin" );
            internalRightMarginStr = properties
                    .getProperty( "hierarchView.image.node.internalRightMargin" );
        }

        backgroundColor = Utilities.parseColor( stringBgColor,
                DEFAULT_BACKGROUND_COLOR );

        nodeDefaultColor = Utilities.parseColor( stringNodeColorDefault,
                DEFAULT_NODE_COLOR );

        edgeDefaultColor = Utilities.parseColor( stringEdgeColorDefault,
                DEFAULT_EDGE_COLOR );

        edgeHighlightDefaultColor = Utilities.parseColor(
                stringEdgeHighlightColor, DEFAULT_HIGHLIGHT_EDGE_COLOR );

        borderColor = Utilities.parseColor( stringBorderColor,
                DEFAULT_BORDER_COLOR );

        if ( null == borderEnable ) {
            borderEnable = DEFAULT_BORDER_STATE;
        }

        borderSize = Utilities.parseProperty( border, DEFAULT_BORDER_SIZE );
        imageLength = Utilities.parseProperty( xSize, DEFAULT_IMAGE_LENGTH );
        imageHeight = Utilities.parseProperty( ySize, DEFAULT_IMAGE_HEIGHT );

        if ( null == mapName ) {
            mapName = DEFAULT_MAP_NAME;
        }

        if ( null == fontName ) {
            fontName = DEFAULT_LABEL_FONT_NAME;
        }

        intFontSize = Utilities.parseProperty( fontSize,
                DEFAULT_LABEL_FONT_SIZE );

        fontLabel = new Font( fontName, Font.PLAIN, intFontSize );
        boldFontLabel = new Font( fontName, Font.BOLD, intFontSize + 1 );

        if ( null == edgeAntialiased ) {
            edgeAntialiased = DEFAULT_EDGE_ANTIALIASED;
        }

        if ( null == textAntialiased ) {
            textAntialiased = DEFAULT_TEXT_ANTIALIASED;
        }

        edgeThickness = Utilities.parseProperty( thicknessStr,
                DEFAULT_EDGE_THICKNESS );

        if ( null == nodeAntialiased ) {
            nodeAntialiased = DEFAULT_NODE_ANTIALIASED;
        }

        if ( null == shapeEnable ) {
            shapeEnable = DEFAULT_SHAPE_STATE;
        }

        internalTopMargin = Utilities.parseProperty( internalTopMarginStr,
                DEFAULT_INTERNAL_TOP_MARGIN );
        internalBottomMargin = Utilities.parseProperty(
                internalBottomMarginStr, DEFAULT_INTERNAL_BOTTOM_MARGIN );
        internalLeftMargin = Utilities.parseProperty( internalLeftMarginStr,
                DEFAULT_INTERNAL_LEFT_MARGIN );
        internalRightMargin = Utilities.parseProperty( internalRightMarginStr,
                DEFAULT_INTERNAL_RIGHT_MARGIN );
    }

    /**
     * Constructor
     */
    public DrawGraph(InteractionNetwork in, String applicationPath,
            String minePath) {
        graph = in;
        centralNodes = in.getCentralProteins();
        this.minePath = minePath;
        this.applicationPath = applicationPath;

        // Initialization of mapCode container
        this.mapCode = new StringBuffer();
        this.nodeCoordinates = new StringBuffer();

        // Compute the size of the final image
        updateProteinData( in );

        ImageDimension dimension = in.getImageDimension();

        dimensionRateX = dimension.length() / imageLength;
        dimensionRateY = dimension.height() / imageHeight;
        imageSizex = imageLength + borderSize * 2;
        imageSizey = imageHeight + borderSize * 2;
    }

    /**
     * Compute the size of the image according to proteins data (coordinates,
     * size).
     * 
     * @param in the interaction network we build the SVG DOM from
     */
    private void updateProteinData(InteractionNetwork in) {
        FontMetrics fontMetrics = null;

        ArrayList listOfProtein = graph.getOrderedNodes();
        int numberOfProtein = in.sizeNodes();

        BasicGraphI protein;

        // that's a fake to precalculate the image size
        bufferedImage = new BufferedImage( 1, 1, BufferedImage.TYPE_BYTE_BINARY );
        Graphics2D g = (Graphics2D) bufferedImage.getGraphics();

        g.setFont( fontLabel );
        fontMetrics = g.getFontMetrics();

        ImageDimension dimension = in.getImageDimension();
        int j;

        // update the image dimension according to the proteins coordinates and
        // their size's label
        for (j = 0; j < numberOfProtein; j++) {
            protein = (BasicGraphI) listOfProtein.get( j );

            if ( protein.get( Constants.ATTRIBUTE_VISIBLE ) == Boolean.TRUE ) {
                // get the protein label
                // get Tulip coordinate
                float proteinX = ( (Float) protein
                        .get( Constants.ATTRIBUTE_COORDINATE_X ) ).floatValue();
                float proteinY = ( (Float) protein
                        .get( Constants.ATTRIBUTE_COORDINATE_Y ) ).floatValue();

                dimension.adjust( proteinX, proteinY );

                if ( centralNodes.contains( protein ) ) {
                    g.setFont( boldFontLabel );
                }
                else {
                    g.setFont( fontLabel );
                }

                fontMetrics = g.getFontMetrics();

                // calculate height and width
                float height = fontMetrics.getHeight() + internalTopMargin
                        + internalBottomMargin;
                float length = fontMetrics.stringWidth( protein.getLabel() )
                        + internalLeftMargin + internalRightMargin;

                // The dimension rate depends of the size of the picture.
                // so, we have to calculate at each iteration to keep a right
                // value.
                this.dimensionRateX = dimension.length() / imageLength;
                this.dimensionRateY = dimension.height() / imageHeight;

                // update data in the protein
                protein.put( Constants.ATTRIBUTE_LENGTH, new Float( length ) );
                protein.put( Constants.ATTRIBUTE_HEIGHT, new Float( height ) );

                // update the image dimension according to the protein label
                // size
                dimension.adjustCadre( length * dimensionRateX, height
                        * dimensionRateY, proteinX, proteinY );
            }
        } //  for

        g.dispose();
        bufferedImage = null;
    } // updateProteinData

    /**
     * Return the element "dimensionRate"
     *
     * @return the dimension rate
     */
    private float getDimensionRateX() {
        return dimensionRateX;
    }

    private float getDimensionRateY() {
        return dimensionRateY;
    }

    /**
     * Modify the coordinate for an edge. i.e. Convert the Tulip coordinate to
     * the image coordinate (the one we will draw). The coordinate we want to
     * use is in the middle of the node. This modification allows to create a
     * border in the image by applying a shift to the coordinate.
     * 
     * @param old The tulip coordinate
     * @param min The coordinate minimal in the graph
     * @param rate The rate
     * 
     * @return float
     */
    private float newCoordinateEdge(float old, float min, float rate) {
        return ( old - min ) / rate + borderSize;
    }

    /**
     * Modify the coordinate for a node. i.e. Convert the Tulip coordinate to
     * the image coordinate (the one we will draw). The coordinate we want to
     * use is the upper left corner of the node. This modification allows to
     * create a border in the image by applying a shift to the coordinate.
     * 
     * @param old The tulip coordinate
     * @param min The coordinate minimal in the graph
     * @param length The length
     * @param rate The rate
     * 
     * @return the new coordinate
     */
    private float newCoordinateNode(float old, float min, float length,
            float rate) {
        return ( old - min ) / rate - length / 2 + borderSize;
    }

    /**
     * Allows to draw an element "node" in the image
     * 
     * @param protein The protein to draw
     * @param g The graphic where we draw
     * @param labelFont the Font with which to draw the label
     */
    private void drawNode(BasicGraphI protein, Graphics2D g, Font labelFont) {
        String proteinLabel = protein.getLabel();

        float proteinLength = ( (Float) protein
                .get( Constants.ATTRIBUTE_LENGTH ) ).floatValue();
        float proteinHeight = ( (Float) protein
                .get( Constants.ATTRIBUTE_HEIGHT ) ).floatValue();
        float proteinX = ( (Float) protein
                .get( Constants.ATTRIBUTE_COORDINATE_X ) ).floatValue();
        float proteinY = ( (Float) protein
                .get( Constants.ATTRIBUTE_COORDINATE_Y ) ).floatValue();

        ImageDimension dimension = graph.getImageDimension();

        // Convert coordinates from Tulip space to hierarchview space
        float x1 = newCoordinateNode( proteinX, dimension.xmin(),
                proteinLength, getDimensionRateX() );
        float y1 = newCoordinateNode( proteinY, dimension.ymin(),
                proteinHeight, getDimensionRateY() );

        int x2 = (int) x1 + ( (int) proteinLength );
        int y2 = (int) y1 + ( (int) proteinHeight );

        if ( nodeAntialiased.equalsIgnoreCase( "enable" ) ) {
            // Enable antialiasing for shape
            g.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON );
        }

        if ( shapeEnable.equalsIgnoreCase( "enable" ) ) {
            g.setColor( (Color) protein.get( Constants.ATTRIBUTE_COLOR_NODE ) );
        }
        else {
            // create a transparent color to allow to see edges
            // opacity : 0=transparent, 255=opaque
            // dont display the node but clear edges to display label
            g.setColor( new Color( backgroundColor.getRed(), backgroundColor
                    .getGreen(), backgroundColor.getBlue(), 180 ) );
        }

        g.fillOval( (int) x1, (int) y1, (int) proteinLength,
                (int) proteinHeight );

        // In anycase turn the shape antialiasing off.
        g.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF );

        // Write the map
        mapCode.append( "<area shape=\"rect\" href=\"" + applicationPath
                + "/click.do?AC=" + protein.getAc() + currentTime
                + "\" COORDS=" + (int) x1 + "," + (int) y1 + "," + x2 + ","
                + y2 + ">" );

        // Write label
        g.setFont( labelFont );
        g.setColor( (Color) protein.get( Constants.ATTRIBUTE_COLOR_LABEL ) );
        g.drawString( proteinLabel, (int) x1 + internalLeftMargin,
                (int) ( y1 + proteinHeight / 2 ) + internalTopMargin );
    } // drawNode

    /**
     * Allows to draw an element "edge" in the image
     * 
     * @param interaction The interaction to draw
     * @param g The graphic where we draw
     */
    private void drawEdge(EdgeI interaction, Graphics2D g) {
        BasicGraphI proteinR, proteinL;
        float xline1, xline2, yline1, yline2;

        float proteinRx, proteinRy, proteinLx, proteinLy;

        ImageDimension dimension = graph.getImageDimension();

        // proteinRight
        proteinR = (BasicGraphI) interaction.getNode1();
        proteinRx = ( (Float) proteinR.get( Constants.ATTRIBUTE_COORDINATE_X ) )
                .floatValue();
        proteinRy = ( (Float) proteinR.get( Constants.ATTRIBUTE_COORDINATE_Y ) )
                .floatValue();

        // proteinLeft
        proteinL = (BasicGraphI) interaction.getNode2();
        proteinLx = ( (Float) proteinL.get( Constants.ATTRIBUTE_COORDINATE_X ) )
                .floatValue();
        proteinLy = ( (Float) proteinL.get( Constants.ATTRIBUTE_COORDINATE_Y ) )
                .floatValue();

        // calcul
        xline1 = newCoordinateEdge( proteinRx, dimension.xmin(),
                getDimensionRateX() );

        xline2 = newCoordinateEdge( proteinLx, dimension.xmin(),
                getDimensionRateX() );

        yline1 = newCoordinateEdge( proteinRy, dimension.ymin(),
                getDimensionRateY() );
        yline2 = newCoordinateEdge( proteinLy, dimension.ymin(),
                getDimensionRateY() );

        if ( edgeAntialiased.equalsIgnoreCase( "enable" ) ) {
            // Enable antialiasing for shape
            g.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON );
        }

        // if a path was given by mine (the application was called from mine)
        if ( minePath != null ) {
            // if either of the proteins are part of the minimal connecting
            // network -> the edge between the proteins is highlighted
            if ( minePath.indexOf( proteinL.getLabel() ) != -1
                    && minePath.indexOf( proteinR.getLabel() ) != -1 ) {
                g.setColor( edgeHighlightDefaultColor );
            }
            else {
                g.setColor( edgeDefaultColor );
            }
        }
        // the application was not called from MiNe -> so the edge colors are
        // the default color
        else {
            g.setColor( edgeDefaultColor );
        }

        // draw the edge
        g.drawLine( (int) xline1, (int) yline1, (int) xline2, (int) yline2 );

        // In anycase turn the antialiasing off.
        g.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF );
    } // drawEdge

    /**
     * drawing process, call methods to draw nodes, edges ...
     *  
     */
    public void draw() {
        int i;
        int numberOfProtein = graph.sizeNodes();
        int numberOfInteraction = graph.sizeEdges();
        currentTime = "&now=" + System.currentTimeMillis(); // this will be
        // added at each
        // link of the graph

        List listOfProtein = graph.getOrderedNodes();
        List listOfInteraction = (List) graph.getEdges();

        EdgeI interaction;
        BasicGraphI proteinR, proteinL;

        bufferedImage = new BufferedImage( imageSizex, imageSizey,
                BufferedImage.TYPE_INT_RGB );

        Graphics2D g = (Graphics2D) bufferedImage.getGraphics();

        // Display the background
        g.setColor( backgroundColor );
        g.fillRect( 0, 0, imageSizex, imageSizey );

        if ( textAntialiased.equalsIgnoreCase( "enable" ) ) {
            // Enable antialiasing for text
            g.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
        }
        else {
            // Disable antialiasing for text
            g.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_OFF );
        }

        Stroke defaultStroke = g.getStroke();
        BasicStroke stroke = new BasicStroke( edgeThickness );
        g.setStroke( stroke );

        // We draw edges whose nodes are visible
        for (i = 0; i < numberOfInteraction; i++) {
            interaction = (EdgeI) listOfInteraction.get( i );

            proteinR = (BasicGraphI) interaction.getNode1();
            proteinL = (BasicGraphI) interaction.getNode2();

            // draw edge only if both nodes are visible
            if ( proteinR.get( Constants.ATTRIBUTE_VISIBLE ) == Boolean.TRUE
                    && proteinL.get( Constants.ATTRIBUTE_VISIBLE ) == Boolean.TRUE ) {
                drawEdge( interaction, g );
            }
        } // for

        // restore the default Stroke (thickness)
        g.setStroke( defaultStroke );

        BasicGraphI currentNode;

        // We draw all visible nodes
        mapCode.append( "<map name=\"" + mapName + "\">" );

        // iterate over the nodes of the network to draw them
        // if the current node is a central protein it is not drawn now
        // so that the central proteins are at the top layer of the image
        for (int j = 0; j < numberOfProtein; j++) {
            currentNode = (BasicGraphI) listOfProtein.get( j );

            // if the current node is not a central protein
            if ( !centralNodes.contains( currentNode ) ) {
                // if the current node is visible
                // to avoid drawing the node : uncomment the following line
                // currentNode.put ( Constants.ATTRIBUTE_VISIBLE, Boolean.FALSE );
                if ( ( currentNode.get( Constants.ATTRIBUTE_VISIBLE ) == Boolean.TRUE ) ) {
                    drawNode( currentNode, g, fontLabel );
                }
            }
        }

        // Draw the central node with a bold font to be sure it is visible and
        // distinguishable.
        for (Iterator iter = centralNodes.iterator(); iter.hasNext();) {
            // draw visible central protein over the rest, with bold font.
            currentNode = (BasicGraphI) iter.next();

            if ( currentNode == null ) {
                continue;
            }

            // to avoid drawing the node : uncomment the following line
            // currentNode.put( Constants.ATTRIBUTE_VISIBLE, Boolean.FALSE );
            if ( currentNode.get( Constants.ATTRIBUTE_VISIBLE ) == Boolean.TRUE ) {
                drawNode( currentNode, g, boldFontLabel );
            }
        }

        mapCode.append( "</map>" );

        if ( borderEnable.equals( "enable" ) ) {
            g.setColor( borderColor );
            g.drawRect( 0, 0, imageSizex - 1, imageSizey - 1 );
        }
        // release
        g.dispose();

        nodeCoordinates.append( graph.exportJavascript( getDimensionRateX(), getDimensionRateY(), borderSize ) );
    } // draw


    /**
     * Create an Container with the image and the HTML MAP code.
     * 
     * @return the bean initialized with the SVG DOM and the MAP code.
     */
    public ImageBean getImageBean() {
        ImageBean ib = new ImageBean();
        ib.setMapCode( mapCode.toString() );
        ib.setImageHeight( imageSizey );
        ib.setImageWidth( imageSizex );
        ib.setImageData( bufferedImage );
        return ib;
    }


    public String getNodeCoordinates() {
        return nodeCoordinates.toString();
    }

}