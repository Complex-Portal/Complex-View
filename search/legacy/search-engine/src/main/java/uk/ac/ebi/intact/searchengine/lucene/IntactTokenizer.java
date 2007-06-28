/*
* Copyright (c) 2002 The European Bioinformatics Institute, and others.
* All rights reserved. Please see the file LICENSE
* in the root directory of this distribution.
*/
package uk.ac.ebi.intact.searchengine.lucene;

import org.apache.lucene.analysis.WhitespaceTokenizer;

import java.io.Reader;

/**
 * The main part of this class I copied from the MedlineTokenizer. This Tokenizer defines that characters where the
 * Analyzer should stop.
 *
 * @author Anja Friedrichsen, Mark R. (markr@ebi.ac.uk)
 * @version $Id$
 */
public class IntactTokenizer extends WhitespaceTokenizer {

    /**
     * Constructs an IntactTokenizer object.
     *
     * @param in a Reader object
     */
    public IntactTokenizer( Reader in ) {
        super( in );
    }

    /**
     * this method defines the characters where the tokenizer should stop.
     *
     * @param c the char to check upon.
     *
     * @return true if the given character is a simple character.
     */
    protected boolean isTokenChar( char c ) {
        boolean x = super.isTokenChar( c );
        boolean y = !(
                ( c == '?' ) ||
                ( c == '!' ) ||
                ( c == ';' ) ||
                ( c == '.' ) ||
                ( c == '\'' ) ||
                ( c == '\\' ) ||
                ( c == '/' ) ||
                ( c == ',' ) ||
                ( c == '"' ) ||
                ( c == '~' ) ||
                ( c == '{' ) ||
                ( c == '}' ) ||
                ( c == '>' ) ||
                ( c == '<' ) ||
                ( c == '+' ) ||
                ( c == ':' ) ||
                ( c == '%' ) ||
                ( c == '&' ) ||
                ( c == '+' ) ||
                ( c == ')' ) ||
                ( c == '(' ) ||
                ( c == '[' ) ||
                ( c == ']' ) ||
                ( c == '#' ) ||
                ( c == '|' ) ||
                ( c == '^' ) ||
                ( c == '@' ) );

        // % & * ( ) [ ] # | ^

        return x & y;

    }
}