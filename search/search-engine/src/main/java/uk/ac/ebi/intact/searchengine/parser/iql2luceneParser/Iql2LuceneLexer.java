// $ANTLR 2.7.2: "search3/antlr/iql2lucene.g" -> "Iql2LuceneLexer.java"$

package uk.ac.ebi.intact.searchengine.parser.iql2luceneParser;

import antlr.*;
import antlr.collections.impl.BitSet;

import java.io.InputStream;
import java.io.Reader;
import java.util.Hashtable;

public class Iql2LuceneLexer extends antlr.CharScanner implements Iql2LuceneParserTokenTypes, TokenStream {

    public Iql2LuceneLexer( InputStream in ) {
        this( new ByteBuffer( in ) );
    }

    public Iql2LuceneLexer( Reader in ) {
        this( new CharBuffer( in ) );
    }

    public Iql2LuceneLexer( InputBuffer ib ) {
        this( new LexerSharedInputState( ib ) );
    }

    public Iql2LuceneLexer( LexerSharedInputState state ) {
        super( state );
        caseSensitiveLiterals = false;
        setCaseSensitive( false );
        literals = new Hashtable();
        literals.put( new ANTLRHashString( "author-confidence", this ), new Integer( 59 ) );
        literals.put( new ANTLRHashString( "prerequisite-ptm", this ), new Integer( 82 ) );
        literals.put( new ANTLRHashString( "interactiontype_fullname", this ), new Integer( 32 ) );
        literals.put( new ANTLRHashString( "ensembl", this ), new Integer( 35 ) );
        literals.put( new ANTLRHashString( "example", this ), new Integer( 72 ) );
        literals.put( new ANTLRHashString( "3d-structure", this ), new Integer( 55 ) );
        literals.put( new ANTLRHashString( "author-list", this ), new Integer( 60 ) );
        literals.put( new ANTLRHashString( "exp-modification", this ), new Integer( 73 ) );
        literals.put( new ANTLRHashString( "shortlabel", this ), new Integer( 19 ) );
        literals.put( new ANTLRHashString( "uniprot-cc-note", this ), new Integer( 90 ) );
        literals.put( new ANTLRHashString( "orf name", this ), new Integer( 100 ) );
        literals.put( new ANTLRHashString( "interpro", this ), new Integer( 41 ) );
        literals.put( new ANTLRHashString( "flybase", this ), new Integer( 36 ) );
        literals.put( new ANTLRHashString( "submitted", this ), new Integer( 88 ) );
        literals.put( new ANTLRHashString( "annotation", this ), new Integer( 22 ) );
        literals.put( new ANTLRHashString( "comment", this ), new Integer( 62 ) );
        literals.put( new ANTLRHashString( "gene name", this ), new Integer( 95 ) );
        literals.put( new ANTLRHashString( "database", this ), new Integer( 108 ) );
        literals.put( new ANTLRHashString( "huge", this ), new Integer( 38 ) );
        literals.put( new ANTLRHashString( "uniparc", this ), new Integer( 51 ) );
        literals.put( new ANTLRHashString( "identification_fullname", this ), new Integer( 29 ) );
        literals.put( new ANTLRHashString( "interaction", this ), new Integer( 103 ) );
        literals.put( new ANTLRHashString( "go synonym", this ), new Integer( 97 ) );
        literals.put( new ANTLRHashString( "disease", this ), new Integer( 71 ) );
        literals.put( new ANTLRHashString( "where", this ), new Integer( 11 ) );
        literals.put( new ANTLRHashString( "on-hold", this ), new Integer( 80 ) );
        literals.put( new ANTLRHashString( "definition", this ), new Integer( 70 ) );
        literals.put( new ANTLRHashString( "gene name synonym", this ), new Integer( 96 ) );
        literals.put( new ANTLRHashString( "3d-r-factors", this ), new Integer( 53 ) );
        literals.put( new ANTLRHashString( "locus name", this ), new Integer( 99 ) );
        literals.put( new ANTLRHashString( "stimulation", this ), new Integer( 87 ) );
        literals.put( new ANTLRHashString( "select", this ), new Integer( 10 ) );
        literals.put( new ANTLRHashString( "agonist", this ), new Integer( 57 ) );
        literals.put( new ANTLRHashString( "interaction_shortlabel", this ), new Integer( 25 ) );
        literals.put( new ANTLRHashString( "figure-legend", this ), new Integer( 74 ) );
        literals.put( new ANTLRHashString( "pubmed", this ), new Integer( 46 ) );
        literals.put( new ANTLRHashString( "remark-internal", this ), new Integer( 83 ) );
        literals.put( new ANTLRHashString( "contact-email", this ), new Integer( 66 ) );
        literals.put( new ANTLRHashString( "copyright", this ), new Integer( 67 ) );
        literals.put( new ANTLRHashString( "and", this ), new Integer( 12 ) );
        literals.put( new ANTLRHashString( "experiment", this ), new Integer( 104 ) );
        literals.put( new ANTLRHashString( "uniprot-dr-export", this ), new Integer( 91 ) );
        literals.put( new ANTLRHashString( "identification_shortlabel", this ), new Integer( 28 ) );
        literals.put( new ANTLRHashString( "from", this ), new Integer( 107 ) );
        literals.put( new ANTLRHashString( "cv", this ), new Integer( 105 ) );
        literals.put( new ANTLRHashString( "like", this ), new Integer( 17 ) );
        literals.put( new ANTLRHashString( "caution", this ), new Integer( 61 ) );
        literals.put( new ANTLRHashString( "psi-mi", this ), new Integer( 45 ) );
        literals.put( new ANTLRHashString( "reactome-complex", this ), new Integer( 47 ) );
        literals.put( new ANTLRHashString( "function", this ), new Integer( 75 ) );
        literals.put( new ANTLRHashString( "fullname", this ), new Integer( 20 ) );
        literals.put( new ANTLRHashString( "inhibition", this ), new Integer( 76 ) );
        literals.put( new ANTLRHashString( "resid", this ), new Integer( 49 ) );
        literals.put( new ANTLRHashString( "interaction_ac", this ), new Integer( 24 ) );
        literals.put( new ANTLRHashString( "used-in-class", this ), new Integer( 93 ) );
        literals.put( new ANTLRHashString( "author assigned-name", this ), new Integer( 94 ) );
        literals.put( new ANTLRHashString( "interactiontype_shortlabel", this ), new Integer( 31 ) );
        literals.put( new ANTLRHashString( "identification_ac", this ), new Integer( 27 ) );
        literals.put( new ANTLRHashString( "dataset", this ), new Integer( 69 ) );
        literals.put( new ANTLRHashString( "or", this ), new Integer( 13 ) );
        literals.put( new ANTLRHashString( "accepted", this ), new Integer( 56 ) );
        literals.put( new ANTLRHashString( "contact-comment", this ), new Integer( 65 ) );
        literals.put( new ANTLRHashString( "any", this ), new Integer( 106 ) );
        literals.put( new ANTLRHashString( "confidence-mapping", this ), new Integer( 64 ) );
        literals.put( new ANTLRHashString( "protein", this ), new Integer( 102 ) );
        literals.put( new ANTLRHashString( "imex", this ), new Integer( 39 ) );
        literals.put( new ANTLRHashString( "complex-properties", this ), new Integer( 63 ) );
        literals.put( new ANTLRHashString( "newt", this ), new Integer( 42 ) );
        literals.put( new ANTLRHashString( "isoform synonym", this ), new Integer( 98 ) );
        literals.put( new ANTLRHashString( "3d-resolution", this ), new Integer( 54 ) );
        literals.put( new ANTLRHashString( "intact", this ), new Integer( 40 ) );
        literals.put( new ANTLRHashString( "kinetics", this ), new Integer( 78 ) );
        literals.put( new ANTLRHashString( "isoform-comment", this ), new Integer( 77 ) );
        literals.put( new ANTLRHashString( "interaction_fullname", this ), new Integer( 26 ) );
        literals.put( new ANTLRHashString( "search-url", this ), new Integer( 85 ) );
        literals.put( new ANTLRHashString( "resulting-ptm", this ), new Integer( 84 ) );
        literals.put( new ANTLRHashString( "pathway", this ), new Integer( 81 ) );
        literals.put( new ANTLRHashString( "sgd", this ), new Integer( 50 ) );
        literals.put( new ANTLRHashString( "omim", this ), new Integer( 43 ) );
        literals.put( new ANTLRHashString( "afcs", this ), new Integer( 33 ) );
        literals.put( new ANTLRHashString( "alias", this ), new Integer( 21 ) );
        literals.put( new ANTLRHashString( "pdb", this ), new Integer( 44 ) );
        literals.put( new ANTLRHashString( "data-processing", this ), new Integer( 68 ) );
        literals.put( new ANTLRHashString( "interactiontype_ac", this ), new Integer( 30 ) );
        literals.put( new ANTLRHashString( "to-be-reviewed", this ), new Integer( 89 ) );
        literals.put( new ANTLRHashString( "uniprotkb", this ), new Integer( 52 ) );
        literals.put( new ANTLRHashString( "search-url-ascii", this ), new Integer( 86 ) );
        literals.put( new ANTLRHashString( "go", this ), new Integer( 37 ) );
        literals.put( new ANTLRHashString( "ac", this ), new Integer( 18 ) );
        literals.put( new ANTLRHashString( "no-uniprot-update", this ), new Integer( 109 ) );
        literals.put( new ANTLRHashString( "xref", this ), new Integer( 23 ) );
        literals.put( new ANTLRHashString( "url", this ), new Integer( 92 ) );
        literals.put( new ANTLRHashString( "antagonist", this ), new Integer( 58 ) );
        literals.put( new ANTLRHashString( "negative", this ), new Integer( 79 ) );
        literals.put( new ANTLRHashString( "reactome-protein", this ), new Integer( 48 ) );
        literals.put( new ANTLRHashString( "cabri", this ), new Integer( 34 ) );
        literals.put( new ANTLRHashString( "obsolete term", this ), new Integer( 110 ) );
    }

    public Token nextToken() throws TokenStreamException {
        Token theRetToken = null;
        tryAgain:
        for ( ; ; ) {
            Token _token = null;
            int _ttype = Token.INVALID_TYPE;
            resetText();
            try {   // for char stream error handling
                try {   // for lexical error handling
                    switch ( LA( 1 ) ) {
                        case'\'': {
                            mQUOTE( true );
                            theRetToken = _returnToken;
                            break;
                        }
                        case';': {
                            mSEMICOLON( true );
                            theRetToken = _returnToken;
                            break;
                        }
                        case'(': {
                            mLPAREN( true );
                            theRetToken = _returnToken;
                            break;
                        }
                        case')': {
                            mRPAREN( true );
                            theRetToken = _returnToken;
                            break;
                        }
                        case'=': {
                            mEQUALS( true );
                            theRetToken = _returnToken;
                            break;
                        }
                        case'\t':
                        case'\n':
                        case'\r':
                        case' ': {
                            mWhitespace( true );
                            theRetToken = _returnToken;
                            break;
                        }
                        default:
                            if ( ( _tokenSet_0.member( LA( 1 ) ) ) ) {
                                mIdentifier( true );
                                theRetToken = _returnToken;
                            } else {
                                if ( LA( 1 ) == EOF_CHAR ) {
                                    uponEOF();
                                    _returnToken = makeToken( Token.EOF_TYPE );
                                } else {
                                    throw new NoViableAltForCharException( ( char ) LA( 1 ), getFilename(), getLine(), getColumn() );
                                }
                            }
                    }
                    if ( _returnToken == null ) continue tryAgain; // found SKIP token
                    _ttype = _returnToken.getType();
                    _returnToken.setType( _ttype );
                    return _returnToken;
                }
                catch ( RecognitionException e ) {
                    throw new TokenStreamRecognitionException( e );
                }
            }
            catch ( CharStreamException cse ) {
                if ( cse instanceof CharStreamIOException ) {
                    throw new TokenStreamIOException( ( ( CharStreamIOException ) cse ).io );
                } else {
                    throw new TokenStreamException( cse.getMessage() );
                }
            }
        }
    }

    public final void mQUOTE( boolean _createToken ) throws RecognitionException, CharStreamException, TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = QUOTE;
        int _saveIndex;

        {
            match( '\'' );
        }
        if ( _createToken && _token == null && _ttype != Token.SKIP ) {
            _token = makeToken( _ttype );
            _token.setText( new String( text.getBuffer(), _begin, text.length() - _begin ) );
        }
        _returnToken = _token;
    }

    public final void mSEMICOLON( boolean _createToken ) throws RecognitionException, CharStreamException, TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = SEMICOLON;
        int _saveIndex;

        match( ';' );
        if ( _createToken && _token == null && _ttype != Token.SKIP ) {
            _token = makeToken( _ttype );
            _token.setText( new String( text.getBuffer(), _begin, text.length() - _begin ) );
        }
        _returnToken = _token;
    }

    public final void mLPAREN( boolean _createToken ) throws RecognitionException, CharStreamException, TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = LPAREN;
        int _saveIndex;

        match( '(' );
        if ( _createToken && _token == null && _ttype != Token.SKIP ) {
            _token = makeToken( _ttype );
            _token.setText( new String( text.getBuffer(), _begin, text.length() - _begin ) );
        }
        _returnToken = _token;
    }

    public final void mRPAREN( boolean _createToken ) throws RecognitionException, CharStreamException, TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = RPAREN;
        int _saveIndex;

        match( ')' );
        if ( _createToken && _token == null && _ttype != Token.SKIP ) {
            _token = makeToken( _ttype );
            _token.setText( new String( text.getBuffer(), _begin, text.length() - _begin ) );
        }
        _returnToken = _token;
    }

    public final void mEQUALS( boolean _createToken ) throws RecognitionException, CharStreamException, TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = EQUALS;
        int _saveIndex;

        match( '=' );
        if ( _createToken && _token == null && _ttype != Token.SKIP ) {
            _token = makeToken( _ttype );
            _token.setText( new String( text.getBuffer(), _begin, text.length() - _begin ) );
        }
        _returnToken = _token;
    }

    public final void mWhitespace( boolean _createToken ) throws RecognitionException, CharStreamException, TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = Whitespace;
        int _saveIndex;

        {
            switch ( LA( 1 ) ) {
                case' ': {
                    match( ' ' );
                    break;
                }
                case'\t': {
                    match( '\t' );
                    break;
                }
                case'\n': {
                    match( '\n' );
                    break;
                }
                case'\r': {
                    match( '\r' );
                    break;
                }
                default: {
                    throw new NoViableAltForCharException( ( char ) LA( 1 ), getFilename(), getLine(), getColumn() );
                }
            }
        }
        _ttype = Token.SKIP;
        if ( _createToken && _token == null && _ttype != Token.SKIP ) {
            _token = makeToken( _ttype );
            _token.setText( new String( text.getBuffer(), _begin, text.length() - _begin ) );
        }
        _returnToken = _token;
    }

    protected final void mLetter( boolean _createToken ) throws RecognitionException, CharStreamException, TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = Letter;
        int _saveIndex;

        switch ( LA( 1 ) ) {
            case'a':
            case'b':
            case'c':
            case'd':
            case'e':
            case'f':
            case'g':
            case'h':
            case'i':
            case'j':
            case'k':
            case'l':
            case'm':
            case'n':
            case'o':
            case'p':
            case'q':
            case'r':
            case's':
            case't':
            case'u':
            case'v':
            case'w':
            case'x':
            case'y':
            case'z': {
                matchRange( 'a', 'z' );
                break;
            }
            case'_': {
                match( '_' );
                break;
            }
            case'#': {
                match( '#' );
                break;
            }
            case'-': {
                match( '-' );
                break;
            }
            case'@': {
                match( '@' );
                break;
            }
            default:
                if ( ( ( LA( 1 ) >= '\u0080' && LA( 1 ) <= '\ufffe' ) ) ) {
                    matchRange( '\u0080', '\ufffe' );
                } else {
                    throw new NoViableAltForCharException( ( char ) LA( 1 ), getFilename(), getLine(), getColumn() );
                }
        }
        if ( _createToken && _token == null && _ttype != Token.SKIP ) {
            _token = makeToken( _ttype );
            _token.setText( new String( text.getBuffer(), _begin, text.length() - _begin ) );
        }
        _returnToken = _token;
    }

    protected final void mDigit( boolean _createToken ) throws RecognitionException, CharStreamException, TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = Digit;
        int _saveIndex;

        matchRange( '0', '9' );
        if ( _createToken && _token == null && _ttype != Token.SKIP ) {
            _token = makeToken( _ttype );
            _token.setText( new String( text.getBuffer(), _begin, text.length() - _begin ) );
        }
        _returnToken = _token;
    }

    public final void mIdentifier( boolean _createToken ) throws RecognitionException, CharStreamException, TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = Identifier;
        int _saveIndex;

        {
            int _cnt40 = 0;
            _loop40:
            do {
                if ( ( _tokenSet_1.member( LA( 1 ) ) ) ) {
                    mLetter( false );
                } else if ( ( ( LA( 1 ) >= '0' && LA( 1 ) <= '9' ) ) ) {
                    mDigit( false );
                } else {
                    if ( _cnt40 >= 1 ) {
                        break _loop40;
                    } else {
                        throw new NoViableAltForCharException( ( char ) LA( 1 ), getFilename(), getLine(), getColumn() );
                    }
                }

                _cnt40++;
            } while ( true );
        }
        _ttype = testLiteralsTable( _ttype );
        if ( _createToken && _token == null && _ttype != Token.SKIP ) {
            _token = makeToken( _ttype );
            _token.setText( new String( text.getBuffer(), _begin, text.length() - _begin ) );
        }
        _returnToken = _token;
    }


    private static final long[] mk_tokenSet_0() {
        long[] data = new long[3072];
        data[0] = 287984119906828288L;
        data[1] = 576460745860972545L;
        for ( int i = 2; i <= 1022; i++ ) {
            data[i] = -1L;
        }
        data[1023] = 9223372036854775807L;
        return data;
    }

    public static final BitSet _tokenSet_0 = new BitSet( mk_tokenSet_0() );

    private static final long[] mk_tokenSet_1() {
        long[] data = new long[3072];
        data[0] = 35218731827200L;
        data[1] = 576460745860972545L;
        for ( int i = 2; i <= 1022; i++ ) {
            data[i] = -1L;
        }
        data[1023] = 9223372036854775807L;
        return data;
    }

    public static final BitSet _tokenSet_1 = new BitSet( mk_tokenSet_1() );

}
