/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.commons.go;
import com.sun.org.apache.xerces.internal.parsers.SAXParser;
import org.xml.sax.*;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

import java.io.IOException;

import uk.ac.ebi.intact.core.IntactException;
import uk.ac.ebi.intact.util.SearchReplace;

/**
 * The proxy to the Go server. An example for the use of this class:
 * <pre>
 * GoServerProxy proxy = new GoServerProxy( "http://www.ebi.ac.uk/ego/DisplayGoTerm" );
 * // Could use the default: GoServerProxy proxy = new GoServerProxy( );
 * GoResponse response = proxy.query( "GO:0000074" );
 * System.out.println ( response );
 * </pre>
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @see uk.ac.ebi.intact.util.test.GoServerProxyTest
 */
public class GoServerProxy {

    ////////////////
    // Class Data
    ///////////////

    public static final String GOID_FLAG = "${GO_ID}";

    public static final String DEFAULT_EGO_URL = "http://www.ebi.ac.uk/ego/DisplayGoTerm";

    public static final String EGO_QUERY = "?id=" + GOID_FLAG + "&intact=xml";

    ///////////////////
    // Instance Data
    ///////////////////

    /**
     * The URL to connect to Newt server.
     */
    private String myURL;

    /**
     * Constructs an instance of this class using the URL to connect to the
     * Newt server.
     *
     * @param url the URL to connect to the server.
     */
    public GoServerProxy( String url ) {
        if( url == null )
            myURL = DEFAULT_EGO_URL;
        else
            myURL = url;
    }

    public GoServerProxy() {
        myURL = DEFAULT_EGO_URL;
    }


    /**
     * Queries the ego (http://www.ebi.ac.uk/ego) server with given GO term id.
     *
     * @param goId the GO term to query the ego server.
     *
     * @return the GO term definition.
     *
     * @throws IOException           for network errors.
     * @throws GoIdNotFoundException thrown when the server fails to find a response for GO id.
     */
    public GoResponse query( String goId )
            throws IOException,
                   GoIdNotFoundException {

        GoResponse goRes = null;

        // Query the ego server.
        GoHandler goHandler ; // = getGoResponse( goId );

        try {
            goHandler = getGoResponse( goId );
        } catch( SAXException e ) {
            throw new GoIdNotFoundException( goId, e );
        }

        if( goHandler == null ) {
            throw new GoIdNotFoundException( goId );
        }

        // Values from newt stored in
        goRes = new GoResponse( goId,
                                goHandler.getName(),
                                goHandler.getCategory() );
        return goRes;
    }

    /**
     * Queries the ego (http://www.ebi.ac.uk/ego) server with given GO term id.
     *
     * @param is   the content of the GO term's XML definition.
     * @param goId the GO term to query the ego server.
     *
     * @return the GO term definition.
     *
     * @throws IOException           for network errors.
     * @throws GoIdNotFoundException thrown when the server fails to find a response for GO id.
     */
    public GoResponse query( InputStream is, String goId )
            throws IOException,
                   GoIdNotFoundException {

        GoResponse goRes = null;

        // Query the ego server.
        GoHandler goHandler = null;
        try {
            goHandler = getGoResponse( is );
        } catch( SAXException e ) {
            throw new GoIdNotFoundException( goId, e );
        }

        if( goHandler == null ) {
            throw new GoIdNotFoundException( goId );
        }

        // Values from newt stored in
        goRes = new GoResponse( goId,
                                goHandler.getName(),
                                goHandler.getCategory() );

        return goRes;
    }

    // Helper methods

    private GoHandler getGoResponse( String goId )
            throws IOException,
                   SAXException {

        String query = SearchReplace.replace( EGO_QUERY, GOID_FLAG, goId );

        URL url = new URL( myURL + query );
        URLConnection servletConnection = url.openConnection();

        // Turn off caching
        servletConnection.setUseCaches( false );

        // Wrting to the server.
        servletConnection.setDoOutput( true );

        // The reader to read response from the server.
        GoHandler goHandler = null;
        InputStream inputStream = null;
        try {
            inputStream = servletConnection.getInputStream();
            goHandler = getGoResponse( inputStream );
        } finally {
            if( inputStream != null ) {
                try {
                    inputStream.close();
                } catch( IOException ioe ) {
                }
            }
        }

        return goHandler;
    } // getGoResponse


    private GoHandler getGoResponse( InputStream inputStream )
            throws IOException,
                   SAXException {

        GoHandler goHandler = null;
        InputSource source = new InputSource( inputStream );
        SAXParser parser = new SAXParser();
        goHandler = new GoHandler();
        parser.setContentHandler( goHandler );

        try {
            parser.parse( source );
        } catch( IOException e ) {
            throw e;
        }

        return goHandler;
    }

    ///////////////////////
    // Inner class
    ///////////////////////

    public static class GoResponse {

        private String goId;
        private String name;
        private String category;

        public GoResponse( String goId, String name, String category ) {
            this.goId = goId;
            this.name = name;
            this.category = category;
        }

        public String getGoId() {
            return goId;
        }

        public String getName() {
            return name;
        }

        public String getCategory() {
            return category;
        }

        public String toString() {
            StringBuffer sb = new StringBuffer( 128 );

            sb.append( GoResponse.class.getName() );
            sb.append( "\nID: " + goId );
            sb.append( "\nName: " + name );
            sb.append( "\nCategory: " + category );

            return sb.toString();
        }
    }

    /**
     * Parse the XML output given by the EGO web site.
     * <p/>
     * <p/>
     * Example:
     * <p/>
     * http://www.ebi.ac.uk/ego/DisplayGoTerm?id=GO:0000074&intact=xml
     * <p/>
     * gives
     * <p/>
     * <pre>
     *   &lt;?xml version="1.0" encoding="iso-8859-1"?&gt;
     *   &lt;page&gt;
     *     &lt;name&gt;
     *        <b>regulation of cell cycle</b>
     *     &lt;/name&gt;
     *     &lt;category&gt;
     *        <b>process</b>
     *     &lt;/category&gt;
     *   &lt;/page&gt;
     * </pre>
     * </p>
     *
     * @author Samuel Kerrien (skerrien@ebi.ac.uk)
     * @version $Id$
     */
    public class GoHandler implements ContentHandler {

        //////////////////
        // Constants
        //////////////////

        /**
         * Tag containing the name of the GO term.
         */
        private static final String NAME = "name";

        /**
         * Tag containing the category of the GO term.
         */
        private static final String CATEGORY = "category";

        ///////////////////////
        // Instance variables
        ///////////////////////

        /**
         * Name of the Go term.
         */
        private String name;

        /**
         * Category of the GO term.
         */
        private String category;

        /**
         * Buffer containing temporarily the content of the curently parsed tag.
         */
        private StringBuffer contentTagBuffer = new StringBuffer( 64 );

        ///////////////////////////////////////
        // getter for the extracted attributes
        ///////////////////////////////////////
        public String getName() {
            return name;
        }

        public String getCategory() {
            return category;
        }

        ///////////////////
        // Parsing logic
        ///////////////////

        final public void characters( final char[] ch, final int start, final int len ) {
            contentTagBuffer.append( ch, start, len );
        }

        /**
         * Called when the end of a tag is encoutered.
         * We are just interrested in the tags &lt;/name&gt; and &lt;/category&gt; and
         * store the content of the tag in the appropriate instance variable.
         *
         * @param namespaceURI The Namespace URI, or the empty string if the element has no Namespace URI or
         *                     if Namespace processing is not being performed.
         * @param localName    The local name (without prefix), or the empty string if Namespace processing
         *                     is not being performed.
         * @param rawName      The qualified XML 1.0 name (with prefix), or the empty string if qualified
         *                     names are not available.
         * @throws SAXException
         */
        public void endElement( String namespaceURI,
                                String localName,
                                String rawName )
                throws SAXException {

            if( localName.equals( NAME ) ) {

                this.name = contentTagBuffer.toString().trim();
                contentTagBuffer = new StringBuffer( 64 );

            } else if( localName.equals( CATEGORY ) ) {

                this.category = contentTagBuffer.toString().trim();
                contentTagBuffer = new StringBuffer( 64 );
            }
        }


        /*
         * Methods that aren't necessary in this parser but need to be
         * declared in order to respect the ContentHandler interface.
         */
        public void startDocument() throws SAXException {
        }

        public void endDocument() throws SAXException {
        }

        public void startElement( String namespaceURI, String localName, String rawName, Attributes atts ) throws SAXException {
        }

        public void processingInstruction( String target, String data ) throws SAXException {
        }

        public void startPrefixMapping( String prefix, String uri ) throws SAXException {
        }

        public void ignorableWhitespace( char[] text, int start, int length ) throws SAXException {
        }

        public void endPrefixMapping( String prefix ) throws SAXException {
        }

        public void skippedEntity( String name ) throws SAXException {
        }

        public void setDocumentLocator( Locator locator ) {
        }
    } // class GoHandler


    /*
     * Exception class for when a tax id is not found.
     */
    public static class GoIdNotFoundException extends IntactException {

        public GoIdNotFoundException( String goId ) {
            super( "Failed to find a match for " + goId );
        }

        public GoIdNotFoundException( String goId, Exception nested ) {
            super( "Failed to find a match for " + goId, nested );
        }
    } // class GoIdNotFoundException

} // class GoServerProxy