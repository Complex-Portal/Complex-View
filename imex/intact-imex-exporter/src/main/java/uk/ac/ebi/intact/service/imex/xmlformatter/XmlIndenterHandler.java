package uk.ac.ebi.intact.service.imex.xmlformatter;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.Writer;
import java.util.Stack;

/**
 * A SAX handler that prints the parsed file.
 *
 * @author Samuel Kerrien
 * @version $Id$
 * @since 1.6.0
 */
public class XmlIndenterHandler extends DefaultHandler {

    private class Marker {
        String element;
        boolean nestedContent = false;
        boolean subElement = false;

        private Marker( String element ) {
            this.element = element;
        }

        public String getElement() {
            return element;
        }

        public boolean hasNestedContent() {
            return nestedContent;
        }

        public void setHasNestedContent( boolean hasNestedContent ) {
            this.nestedContent = hasNestedContent;
        }

        public boolean hasSubElement() {
            return subElement;
        }

        public void setSubElement( boolean subElement ) {
            this.subElement = subElement;
        }
    }

    private Stack<Marker> stack = new Stack<Marker>();

    private int indentation = 0;

    private enum LastCall {
        START,
        END;
    }

    private LastCall last;

    private Writer writer;

    private int indentationIncrement = 2;

    private boolean startClosed = true;

    public XmlIndenterHandler( Writer writer ) {
        if ( writer == null ) {
            throw new IllegalArgumentException( "You must give a non null writer" );
        }
        this.writer = writer;
    }

    private void write( String s ) {
        try {
            writer.write( s );
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }

    private void write( char c ) {
        try {
            writer.write( c );
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }

    public void endDocument() throws SAXException {
        try {
            // makes sure the buffer is emptied upon completion
            writer.flush();
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }

    /**
     * When you see a start tag, print it out and then increase indentation by two spaces. If the element has
     * attributes, place them in parens after the element name.
     */
    public void startElement( String namespaceUri, String localName, String qualifiedName, Attributes attributes )
            throws SAXException {

        if ( !stack.empty() ) {
            XmlIndenterHandler.Marker marker = stack.pop();
            marker.setSubElement( true );
            stack.push( marker );
        }

        indent( indentation );
        stack.push( new Marker( qualifiedName ) );
        write( "<" + qualifiedName );

        int numAttributes = attributes.getLength();
        if ( numAttributes > 0 ) {
            write( ' ' );
            for ( int i = 0; i < numAttributes; i++ ) {
                write( attributes.getQName( i ) + "=\"" + attributes.getValue( i ) + "\"" );
                if ( i < numAttributes ) {
                    write( ' ' );
                }
            }
        }

        indentation += indentationIncrement;

        last = LastCall.START;
        startClosed = false;
    }

    /**
     * When you see the end tag, print it out and decrease
     * indentation level by 2.
     */
    public void endElement( String namespaceUri, String localName, String qualifiedName ) throws SAXException {
        indentation -= indentationIncrement;

        XmlIndenterHandler.Marker marker = stack.pop();

        if ( !qualifiedName.equals( marker.getElement() ) ) {
            throw new IllegalStateException();
        }

        if ( marker.getElement().equals( qualifiedName ) ) {
            if ( !marker.hasNestedContent() ) {
                write( "/>" );
            } else {
                if ( marker.hasSubElement() ) {
                    indent( indentation );
                }
                write( "</" + qualifiedName + ">" );
            }
        }

        last = LastCall.END;
    }

    /**
     * Print tag body.
     */
    public void characters( char[] chars, int startIndex, int endIndex ) {
        if ( last == LastCall.START && startClosed == false) {
            write( ">" );
            startClosed = true;
        }

        XmlIndenterHandler.Marker marker = stack.pop();
        marker.setHasNestedContent( true );
        stack.push( marker );

        String data = new String( chars, startIndex, endIndex );
        if ( last == LastCall.START ) {
            // remove data after the first line return
            int idx = data.indexOf( '\n' );
            if( idx != -1 ) {
                data = data.substring( 0, idx + 1 ); // substring inclusive of the line return.
            }
            write( data );
        } else if ( last == LastCall.END ) {
            write( "\n" );
        }
    }

    private void indent( int indentation ) {
        for ( int i = 0; i < indentation; i++ ) {
            write( ' ' );
        }
    }
}