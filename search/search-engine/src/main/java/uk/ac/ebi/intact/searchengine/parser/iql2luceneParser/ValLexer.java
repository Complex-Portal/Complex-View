// $ANTLR 2.7.2: "search3/antlr/valuelexer.g" -> "ValLexer.java"$

package uk.ac.ebi.intact.searchengine.parser.iql2luceneParser;

import antlr.*;
import antlr.collections.impl.BitSet;

import java.io.InputStream;
import java.io.Reader;
import java.util.Hashtable;

/**
 * This Lexer lexes a value
 * that means it can contain any number, letter ...
 */
public class ValLexer extends antlr.CharScanner implements valtagTokenTypes, TokenStream {

    public ValLexer( InputStream in ) {
        this( new ByteBuffer( in ) );
    }

    public ValLexer( Reader in ) {
        this( new CharBuffer( in ) );
    }

    public ValLexer( InputBuffer ib ) {
        this( new LexerSharedInputState( ib ) );
    }

    public ValLexer( LexerSharedInputState state ) {
        super( state );
        caseSensitiveLiterals = false;
        setCaseSensitive( false );
        literals = new Hashtable();
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
                    if ( ( LA( 1 ) == '\t' || LA( 1 ) == '\n' || LA( 1 ) == '\r' ) ) {
                        mWhitespace( true );
                        theRetToken = _returnToken;
                    } else if ( ( _tokenSet_0.member( LA( 1 ) ) ) ) {
                        mVALUE( true );
                        theRetToken = _returnToken;
                    } else {
                        if ( LA( 1 ) == EOF_CHAR ) {
                            uponEOF();
                            _returnToken = makeToken( Token.EOF_TYPE );
                        } else {
                            throw new NoViableAltForCharException( ( char ) LA( 1 ), getFilename(), getLine(), getColumn() );
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

    public final void mWhitespace( boolean _createToken ) throws RecognitionException, CharStreamException, TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = Whitespace;
        int _saveIndex;

        {
            switch ( LA( 1 ) ) {
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

    protected final void mSpecialChar( boolean _createToken ) throws RecognitionException, CharStreamException, TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = SpecialChar;
        int _saveIndex;

        switch ( LA( 1 ) ) {
            case'-': {
                match( '-' );
                break;
            }
            case':': {
                match( ':' );
                break;
            }
            case' ': {
                match( ' ' );
                break;
            }
            case'(': {
                match( '(' );
                break;
            }
            case')': {
                match( ')' );
                break;
            }
            case'/': {
                match( '/' );
                break;
            }
            case'*': {
                match( '*' );
                break;
            }
            case'%': {
                match( '%' );
                break;
            }
            case'!': {
                match( '!' );
                break;
            }
            case'?': {
                match( '?' );
                break;
            }
            case'~': {
                match( '~' );
                break;
            }
            case'"': {
                match( '"' );
                break;
            }
            case'[': {
                match( '[' );
                break;
            }
            case']': {
                match( ']' );
                break;
            }
            case'{': {
                match( '{' );
                break;
            }
            case'}': {
                match( '}' );
                break;
            }
            case'^': {
                match( '^' );
                break;
            }
            case'&': {
                match( '&' );
                break;
            }
            case'+': {
                match( '+' );
                break;
            }
            case'\\': {
                match( '\\' );
                break;
            }
            case'|': {
                match( '|' );
                break;
            }
            case'.': {
                match( '.' );
                break;
            }
            case';': {
                match( ';' );
                break;
            }
            case'<': {
                match( '<' );
                break;
            }
            case'>': {
                match( '>' );
                break;
            }
            case'`': {
                match( '`' );
                break;
            }
            case'$': {
                match( '$' );
                break;
            }
            default: {
                throw new NoViableAltForCharException( ( char ) LA( 1 ), getFilename(), getLine(), getColumn() );
            }
        }
        if ( _createToken && _token == null && _ttype != Token.SKIP ) {
            _token = makeToken( _ttype );
            _token.setText( new String( text.getBuffer(), _begin, text.length() - _begin ) );
        }
        _returnToken = _token;
    }

    public final void mVALUE( boolean _createToken ) throws RecognitionException, CharStreamException, TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = VALUE;
        int _saveIndex;

        {
            int _cnt8 = 0;
            _loop8:
            do {
                switch ( LA( 1 ) ) {
                    case'0':
                    case'1':
                    case'2':
                    case'3':
                    case'4':
                    case'5':
                    case'6':
                    case'7':
                    case'8':
                    case'9': {
                        mDigit( false );
                        break;
                    }
                    case' ':
                    case'!':
                    case'"':
                    case'$':
                    case'%':
                    case'&':
                    case'(':
                    case')':
                    case'*':
                    case'+':
                    case'-':
                    case'.':
                    case'/':
                    case':':
                    case';':
                    case'<':
                    case'>':
                    case'?':
                    case'[':
                    case'\\':
                    case']':
                    case'^':
                    case'`':
                    case'{':
                    case'|':
                    case'}':
                    case'~': {
                        mSpecialChar( false );
                        break;
                    }
                    default:
                        if ( ( _tokenSet_1.member( LA( 1 ) ) ) ) {
                            mLetter( false );
                        } else {
                            if ( _cnt8 >= 1 ) {
                                break _loop8;
                            } else {
                                throw new NoViableAltForCharException( ( char ) LA( 1 ), getFilename(), getLine(), getColumn() );
                            }
                        }
                }
                _cnt8++;
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
        data[0] = -2305861155450519552L;
        data[1] = 9223372036720558081L;
        for ( int i = 2; i <= 1022; i++ ) {
            data[i] = -1L;
        }
        data[1023] = 9223372036854775807L;
        return data;
    }

    public static final BitSet _tokenSet_0 = new BitSet( mk_tokenSet_0() );

    private static final long[] mk_tokenSet_1() {
        long[] data = new long[3072];
        data[0] = 34359738368L;
        data[1] = 576460745860972545L;
        for ( int i = 2; i <= 1022; i++ ) {
            data[i] = -1L;
        }
        data[1023] = 9223372036854775807L;
        return data;
    }

    public static final BitSet _tokenSet_1 = new BitSet( mk_tokenSet_1() );

}
