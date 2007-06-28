/*
* Copyright (c) 2002 The European Bioinformatics Institute, and others.
* All rights reserved. Please see the file LICENSE
* in the root directory of this distribution.
*/
package uk.ac.ebi.intact.searchengine.parser;

import antlr.CommonAST;
import antlr.TokenStreamSelector;
import org.apache.log4j.Logger;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.searchengine.parser.iql2luceneParser.Iql2LuceneLexer;
import uk.ac.ebi.intact.searchengine.parser.iql2luceneParser.Iql2LuceneParser;
import uk.ac.ebi.intact.searchengine.parser.iql2luceneParser.Iql2LuceneTreeWalker;
import uk.ac.ebi.intact.searchengine.parser.iql2luceneParser.ValLexer;

import java.io.StringReader;

/**
 * This class provides methods, which parse an IQL query statement to an Lucene query statement.
 *
 * @author Anja Friedrichsen
 * @version $Id:IQLParserImpl.java 5081 2006-06-26 12:39:49 +0000 (Mon, 26 Jun 2006) baranda $
 */
public class IQLParserImpl implements IQLParser {

    /**
     * Logger for that class.
     */
    protected transient static final Logger logger = Logger.getLogger( "search" );


    /**
     * This method gets the IQL statement and pass it to the antlr parser. The parser checks if the grammar is right,
     * generates a tree for the search condition and returns the name of the class where to search in. After the
     * treewalker is called with the generated tree and parses this tree into a String that matches the lucene syntax.
     * Last the searchconditon and the search class are connected to build the lucene statement
     *
     * @param IQLStatement the IQL query string
     *
     * @return the lucene query string
     *
     * @throws IntactException upon pasring error.
     */
    public String getLuceneQuery( String IQLStatement ) throws IntactException {
//        if (GenericValidator.isBlankOrNull(IQLStatement)) throw new IntactException("IQLStatement is blank or null");

        //the generated lucene query to be returned
        String luceneStatement = "";
        // the name of the class where to search in, also called the search object
        String searchClass = null;
        // create a selector to switch from one lexer to another,
        // in this case the mainlexer and the valuelexer
        TokenStreamSelector selector = new TokenStreamSelector();
        try {
            // create the mainlexer, which is a lexer for the IQL statement exept the 'value' part
            Iql2LuceneLexer mainlexer = new Iql2LuceneLexer( new StringReader( IQLStatement ) );

            // create a parser
            Iql2LuceneParser parser = new Iql2LuceneParser( selector );
            // create the valuelexer to parse the search value
            // which can contain almost any character
            ValLexer valuelexer = new ValLexer( mainlexer.getInputState() );

            // name the streams
            selector.addInputStream( mainlexer, "mainlexer" );
            selector.addInputStream( valuelexer, "valuelexer" );

            // start with the 'mainlexer'
            selector.select( "mainlexer" );

            // attach the parser to the selector
            parser.init( selector );

            // parse the IQL query and the parser returns the search object, which
            // is used later to generate the lucene query
            searchClass = parser.statement();
            // if the parsing went wrong, write a message to the logger and return null
            if ( searchClass == null ) {
                logger.error( "invalid IQL Statement" );
                throw new IllegalArgumentException( "invalid IQL statement!" );
            }

            // while parsing the IQL statement there was a tree generated, which contains
            // the search conditions. In this tree the AND/OR and LIKE/EQUALS are the nodes
            // get the tree storing the search condition
            CommonAST conditionTree = ( CommonAST ) parser.getAST();

            String searchCondition = null;
            // check if there is a search condition, otherwise the treeWalker is not needed
            if ( conditionTree != null ) {
                // create a tree walker to get the information out of the condition tree
                Iql2LuceneTreeWalker walker = new Iql2LuceneTreeWalker();
                // attach the tree walker to the selector
                walker.init( selector );

                // get the searchCondition in lucene format out of the treeWalker
                searchCondition = walker.criteria( conditionTree );
            }

            // create the lucene statement out of the search class, if it is not any
            // in the case that it is any, don't define the search class, but search in all classes
            if ( !searchClass.equalsIgnoreCase( "any" ) ) {
                luceneStatement = "objclass:uk.ac.ebi.*" + searchClass + "*";
            } else {
                luceneStatement = "-objclass:uk.*.biosource*";
            }
            // and the search condition, if there is one
            if ( searchCondition != null ) {
                luceneStatement += " AND " + searchCondition;
            }
            logger.info( "IQL STATEMENT: " + IQLStatement );
            logger.info( "LUCENE STATEMENT: " + luceneStatement );

        } catch ( Exception e ) {
            throw new IntactException( "IQL Parser error " + e );
        }
        return luceneStatement;
    }
}