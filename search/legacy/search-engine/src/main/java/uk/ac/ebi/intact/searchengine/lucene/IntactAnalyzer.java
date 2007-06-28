/*
* Copyright (c) 2002 The European Bioinformatics Institute, and others.
* All rights reserved. Please see the file LICENSE
* in the root directory of this distribution.
*/
package uk.ac.ebi.intact.searchengine.lucene;

import org.apache.lucene.analysis.*;

import java.io.Reader;
import java.util.Set;

/**
 * This class classifies an Analyzer specific for IntAct. It is a modification of the lucene StandardAnalyzer.
 *
 * @author Anja Friedrichsen
 * @version $Id$
 */
public class IntactAnalyzer extends Analyzer {

    // set containing a number a words that the analyzer should ignore (not inserting into the index)
    private Set stopSet;

    /**
     * An array containing some common English words that are usually not useful for searching.
     */
    public static final String[] STOP_WORDS = {
            "a", "an", "and", "are", "as", "at", "be", "but", "by",
            "for", "if", "in", "into", "is", "it",
            "not", "of", "on", "or", "s", "such",
            "t", "that", "the", "their", "then", "there", "these",
            "they", "this", "to", "was", "will", "with"
    };

    /**
     * An array containing all fields that should be analyzed with the IntactTokenizer
     */
    // TODO load those terms from the database, you may hardCode the CvObject (CvTopic & CvAliasType) name, but not the shortlabel as they can change without notice.
    private static final String[] TEXT_FIELDS = {
            "annotation", "agonist", "author-confidence", "caution",
            "comment", "confidence-mapping", "definition", "disease",
            "example", "exp-modification", "function", "fullname", "inhibition",
            "isoform-comment", "kinetics", "negative", "on-hold", "pathway",
            "prerequisite-ptm", "remark-internal", "resulting-ptm", "stimulation",
            "submitted", "uniprot-cc-note", "uniprot-dr-export", "interaction_fulllname",
            "interactiontype_fullname", "identification_fullname"
    };


    /**
     * Builds an analyzer.
     */
    public IntactAnalyzer() {
        this( STOP_WORDS );
    }

    /**
     * Builds an analyzer with the given stop words.
     *
     * @param stopWords an array of stop word
     */
    public IntactAnalyzer( String[] stopWords ) {
        stopSet = StopFilter.makeStopSet( stopWords );
    }

    /**
     * This method accomplishes the analyzing. It takes the fieldName and distingiushes if the field should be analyzed
     * with the IntactTokenizer, which stops at stopwords and at the specific characters, or with the
     * WhitespaceTokenizer which stops only at whitespaces.
     *
     * @param fieldName the field name
     * @param reader    a reader
     *
     * @return a tokenStream.
     */
    public TokenStream tokenStream( String fieldName, Reader reader ) {

        // analyze the fields that contains text with the IntActTokenizer
        if ( this.isTextField( fieldName ) ) {
            TokenStream result = new IntactTokenizer( reader );
            result = new LowerCaseFilter( result );
            result = new StopFilter( result, stopSet );
            return result;
            // analyze the fields that contain keywords with the WhitespaceTokenizer
        } else {
            TokenStream result = new WhitespaceTokenizer( reader );
            result = new LowerCaseFilter( result );
            return result;
        }
    }

    /**
     * check if the field name is one of the textfield names
     *
     * @param fieldName
     *
     * @return 'true' if the field contains text, 'false' if it contains a keyword
     */
    private boolean isTextField( String fieldName ) {
        for ( int i = 0; i < TEXT_FIELDS.length; i++ ) {
            if ( fieldName.equalsIgnoreCase( TEXT_FIELDS[i] ) ) {
                return true;
            }
        }
        return false;
    }
}