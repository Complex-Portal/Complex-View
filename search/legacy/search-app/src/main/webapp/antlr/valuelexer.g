/*
 * $Id: valuelexer.g
 */
header {
package uk.ac.ebi.intact.application.search3.advancedSearch.powerSearch.parser.iql2luceneParser;

import java.util.*;
import java.io.*;
import antlr.collections.*;
import antlr.debug.misc.*;
import antlr.*;
}

/**
 * This Lexer lexes a value
 * that means it can contain any number, letter ...
 */
 
class ValLexer extends Lexer;
options {
    testLiterals = false;
    k = 2;
    caseSensitive = false;
    caseSensitiveLiterals = false;
    charVocabulary='\u0000'..'\uFFFE';
    exportVocab=valtag;
    defaultErrorHandler = false;
}


Whitespace
    : ('\t' | '\n' | '\r')
    { _ttype = Token.SKIP; }
    ;

protected  // generated as part of the Identifier rule
Letter
    : 'a'..'z' | '_' | '#' | '@' | '\u0080'..'\ufffe'
    ;

protected
Digit
    : '0'..'9'
    ;

protected
SpecialChar
    : '-'|':'|' '|'('|')'|'/'|'*'|'%'|'!'|'?'|'~'|'"'|'['|']'|'{'|'}'|'^'|'&'|'+'|'\\'|'|'|'.'
    |';'|'<'|'>'|'`'|'$'
    ;

VALUE
    options { testLiterals = true; }
    :(Letter|Digit|SpecialChar)+
    ;

