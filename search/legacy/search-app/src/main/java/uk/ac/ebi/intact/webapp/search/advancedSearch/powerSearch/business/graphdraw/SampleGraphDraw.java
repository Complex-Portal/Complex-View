package uk.ac.ebi.intact.webapp.search.advancedSearch.powerSearch.business.graphdraw;

import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.model.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Draw an example graphs.
 */
public class SampleGraphDraw {

    /**
     * Produce a graph for the specified CV into the given File.
     *
     * @param cvClass the type of CV to generate.
     * @param file    the output image file.
     *
     * @throws IntactException if a data access error occurs.
     * @throws IOException     if error with file handling.
     */
    public static void produceImage( Class cvClass, File file ) throws IntactException, IOException {

        String cvName = cvClass.getName().substring( cvClass.getName().lastIndexOf( "." ) + 1 );

        CvGraph graphGenerator = new CvGraph();

        System.out.print( "Creating " + cvName + "'s DAG representation..." );

        BufferedImage image = (BufferedImage) graphGenerator.createImage( cvClass );
        String map = graphGenerator.getImageMap();

        System.out.println( "done." );

        // write to disk
        ImageIO.write( image, "png", new FileOutputStream( file ) );

        PrintWriter pw = new PrintWriter( new FileWriter( cvName + ".html" ) );
        pw.println( "<html><body>" +
                    "<img border=\"0\" src=\"" + cvName + ".png\" usemap=\"#" + cvName + "\" />" +
                    "<map name=\"" + cvName + "\">" + map + "</map>" +
                    "</body></html>" );

        pw.close();
    }

    /**
     * Demo program.
     *
     * @param args no params required.
     *
     * @throws IntactException
     * @throws IOException     
     */
    public static void main( String[] args ) throws IntactException, IOException {

        produceImage( CvInteraction.class, new File( "CvInteraction.png" ) );
        produceImage( CvInteractionType.class, new File( "CvInteractionType.png" ) );
        produceImage( CvFeatureType.class, new File( "CvFeatureType.png" ) );
        produceImage( CvFeatureIdentification.class, new File( "CvFeatureIdentification.png" ) );
        produceImage( CvIdentification.class, new File( "CvIdentification.png" ) );
        produceImage( CvInteractorType.class, new File( "CvInteractorType.png" ) );
    }
}