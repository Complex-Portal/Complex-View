package uk.ac.ebi.intact.service.imex.xmlformatter;

import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * Utility that reformats an XML file into a well indented one.
 *
 * @author Samuel Kerrien
 * @version $Id$
 * @since 1.6.0
 */
public class XmlFormatter {

    public void formatXml( File input, File output ) throws XmlFormatterException {

        if ( input == null ) {
            throw new NullPointerException( "You must give a non null input file" );
        }

        if( ! input.exists() ) {
            throw new IllegalArgumentException( "The given input file doesn't exist:" + input.getAbsolutePath() );
        }

        if( !input.canRead() ) {
            throw new IllegalArgumentException( "Could not read the input file: " + input.getAbsolutePath() );
        }

        if ( output == null ) {
            throw new NullPointerException( "You must give a non null output file" );
        }

        if( output.exists() && !output.canWrite() ) {
            throw new IllegalArgumentException( "Could not write to output file: " + output.getAbsolutePath() );
        }

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            Writer writer = new FileWriter( output );
            DefaultHandler handler = new XmlIndenterHandler( writer );
            SAXParser parser = factory.newSAXParser();
            parser.parse( input.getAbsolutePath(), handler );
        } catch ( Exception e ) {
            throw new XmlFormatterException( "An error occured while reformatting the input XML:" + input.getAbsolutePath(), e );
        }
    }
}
