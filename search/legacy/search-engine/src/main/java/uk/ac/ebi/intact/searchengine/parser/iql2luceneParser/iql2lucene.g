header
{
package uk.ac.ebi.intact.application.search3.advancedSearch.powerSearch.parser.iql2luceneParser;
import antlr.*;
import antlr.TokenStreamSelector;
import uk.ac.ebi.intact.util.SearchReplace;
import java.util.Locale;
}

// PARSER ********************************************************************************

class Iql2LuceneParser extends Parser;
options {
    k = 2;
    importVocab=valtag;
    buildAST = true; // builds a tree and uses CommonAST as default
}

{
  // the class where to search in
  String searchObject = null;
  // the database where to search in
  String database = null;
  // flag to controll if the IQL statement was right
  boolean isCorrectStatement = true;
  TokenStreamSelector selector;
  public void init(TokenStreamSelector selector){
        this.selector= selector;
    }
 }

// starting rule
statement returns [String searchObj ] throws ANTLRException
     {searchObj = null;}
    : selectStatement (SEMICOLON!)? EOF!
    // check if the parsing was correctly
     { if(isCorrectStatement){
           searchObj = searchObject;
       }else{
           throw new IllegalArgumentException("this was an invalid IQL statement");
       }
     }
    ;
    exception // for statement
    catch [ANTLRException ex] {
       reportError("in statement " + ex.toString());
       return null;
    }

// select statement containing a select-, from- and where-clause
// the select- and where-clause are not part of the tree build by ANTLR (!)
selectStatement throws ANTLRException
    :
    selectClause!
    (fromClause!)?
    (whereClause)?
    ;
     exception
     catch [ANTLRException ex] {
        reportError(ex.toString());
        isCorrectStatement = false;
        throw new ANTLRException("this was an invalid IQL statement");
     }

// select clause: starts always with "SELECT" and then the searchObject
selectClause throws ANTLRException
    : SELECT searchObject
    ;
    exception
    catch [ANTLRException ex] {
        reportError(ex.toString());
        isCorrectStatement = false;
         throw new ANTLRException("this was an invalid IQL statement");
    }

// where clause: starts always with "WHERE" followed by the searchcondition
// the string "WHERE"  should not be part of the tree (!)
whereClause throws ANTLRException
    : WHERE! searchCondition
    ;
     exception
     catch [ANTLRException ex] {
        reportError(ex.toString());
        isCorrectStatement = false;
        throw new ANTLRException("this was an invalid IQL statement");
     }

// search condition can be one or more criterias connected with AND/OR
// the AND/OR should build a node in the tree built by ANTLR (^)
searchCondition throws ANTLRException
    : criteria
    ( (AND^ | OR^ ) criteria )*
    ;
     exception
     catch [ANTLRException ex] {
        reportError(ex.toString());
        isCorrectStatement = false;
        throw new ANTLRException("this was an invalid IQL statement");
     }

// one search criteria
// recursive call of the searchCondition, the search can be surrounded by parenthesis
// the predicate discribes the content of the criteria
// these parenthesis are not part of the tree
criteria throws ANTLRException
    :
    (
      (LPAREN searchCondition RPAREN)  => lp:LPAREN! searchCondition rp:RPAREN!
      | predicate
    )
    ;
     exception
     catch [ANTLRException ex] {
        reportError(ex.toString());
        isCorrectStatement = false;
        throw new ANTLRException("this was an invalid IQL statement");
     }
// predicate describes the search criteria, it has an term, which is connected with 'EQUALS' or 'LIKE'
// to an value.
// the 'EQUALS' and 'LIKE' are nodes in the tree built by ANTLR (^)
predicate throws ANTLRException
    :
    (
     term
        (
          EQUALS^
          ( value )
        |
          LIKE^
          ( value )
        )
    )
    ;
     exception
     catch [ANTLRException ex] {
        reportError(ex.toString());
        isCorrectStatement = false;
        throw new ANTLRException("this was an invalid IQL statement");
     }

// the term specifies the search term
// the term is a leaf in the tree built by ANTLR
term throws ANTLRException
     {String str = "";}
     : (AC
     |  SHORTLABEL
     |  FULLNAME
     |  ALIAS
     |  ANNOTATION
     |  XREF

     | CVINTERACTION_AC
     | CVINTERACTION_SHORTLABEL
     | CVINTERACTION_FULLNAME
     | CVIDENTIFICATION_AC
     | CVIDENTIFICATION_SHORTLABEL
     | CVIDENTIFICATION_FULLNAME
     | CVINTERACTION_TYPE_AC
     | CVINTERACTION_TYPE_SHORTLABEL
     | CVINTERACTION_TYPE_FULLNAME

// CvDatabases
     |  CABRI
     |  FLYBASE
     |  GO
     |  HUGE
     |  INTACT
     |  INTERPRO
     |  NEWT
     |  OMIM
     |  PDB
     |  PSIMI
     |  PUBMED
     |  REACTOME
     |  RESID
     |  SGD
     |  UNIPROT

//CvTopics
     |  AGONIST
     |  ANTAGONIST
     |  AUTHORCONFIDENCE
     |  CAUTION
     |  COMMENT
     |  CONFIDENCEMAPPING
     |  DEFINITION
     |  DISEASE
     |  EXAMPLE
     |  EXPMODIFICATION
     |  FUNCTION
     |  INHIBITION
     |  ISOFORMCOMMENT
     |  KINETICS
     |  NEGATIVE
     |  ONHOLD
     |  PATHWAY
     |  PREREQUISITEPTM
     |  REMARKINTERNAL
     |  RESULTINGPTM
     |  SEARCHURL
     |  SEARCHURLASCII
     |  STIMULATION
     |  SUBMITTED
     |  UNIPROTCCNOTE
     |  UNIPROTDREXPORT
     |  URL

//CvAliasType
     |  GENENAME
     |  GENENAMESYNONYM
     |  GOSYNONYM
     |  ISOFORMSYNONYM
     |  LOCUSNAME
     |  ORFNAME
       )

     ;
     exception
     catch [ANTLRException ex] {
        reportError(ex.toString());
        isCorrectStatement = false;
        throw new ANTLRException("this was an invalid IQL statement");
     }

// the value specifies the value to search for
// it is also a leaf in the ANTLR tree
value throws ANTLRException
    {String val = "";}
    :
    (
      QUOTE
    {selector.push("valuelexer");}
      v:VALUE
    { /** todo in iql2lucene.g: is it right to convert like that? */
      val = v.getText();
      }
    )
    {selector.pop();}
      QUOTE
    ;

// the searchObject is the search class where to search
// the variable 'searchObject' is set here
searchObject throws ANTLRException
    {String str = "";}
    : ( p:PROTEIN
          {str= p.getText();}
    |   i:INTERACTION
          {str= i.getText();}
    |   e:EXPERIMENT
          {str= e.getText();}
    |   c:CV
          {str= c.getText();}
    |   a:ANY
          {str= a.getText();}
      )
    {  if(!str.equals("")){
          str.toLowerCase();
          searchObject = str;
       }
    }
    ;
     exception
     catch [ANTLRException ex] {
        reportError(ex.toString());
        isCorrectStatement = false;
        throw new ANTLRException("this was an invalid IQL statement");
     }

// the from clause always starts with 'FROM' followed by the database where to search in
// the value 'database' is set here
fromClause throws ANTLRException
    : FROM i:INTACT
    { database = i.getText().toLowerCase();}
    ;
     exception
     catch [ANTLRException ex] {
        reportError(ex.toString());
        isCorrectStatement = false;
        throw new ANTLRException("this was an invalid IQL statement");
     }


// LEXER *********************************************************************************

class Iql2LuceneLexer extends Lexer;

options {
    testLiterals = false;
    k = 2;
    caseSensitive = false;
    caseSensitiveLiterals = false;
    charVocabulary='\u0000'..'\uFFFE';
}

tokens {
    AC = "ac";
    ALIAS = "alias";
    AND = "and" ;
    ANNOTATION = "annotation";
    ANY = "any" ;
    CV = "cv";
    DATABASE = "database" ;
    FULLNAME = "fullname";
    EXPERIMENT = "experiment";
    FROM = "from" ;
    INTERACTION = "interaction";
    LIKE = "like";
    OR = "or" ;
    PROTEIN = "protein";
    SELECT = "select" ;
    SHORTLABEL = "shortlabel";
    WHERE = "where" ;
    XREF = "xref";

    CVINTERACTION_AC = "interaction_ac";
    CVINTERACTION_SHORTLABEL = "interaction_shortlabel";
    CVINTERACTION_FULLNAME = "interaction_fullname";
    CVIDENTIFICATION_AC = "identification_ac";
    CVIDENTIFICATION_SHORTLABEL = "identification_shortlabel";
    CVIDENTIFICATION_FULLNAME = "identification_fullname";
    CVINTERACTION_TYPE_AC = "interactiontype_ac";
    CVINTERACTION_TYPE_SHORTLABEL = "interactiontype_shortlabel";
    CVINTERACTION_TYPE_FULLNAME = "interactiontype_fullname";

// CvDatabases
    CABRI = "cabri";
    FLYBASE = "flybase";
    GO = "go";
    HUGE = "huge";
    INTACT = "intact";
    INTERPRO = "interpro";
    NEWT = "newt";
    OMIM = "omim";
    PDB = "pdb";
    PSIMI = "psi-mi";
    PUBMED = "pubmed";
    REACTOME = "reactome";
    RESID = "resid";
    SGD = "sgd";
    UNIPROT = "uniprot";

//CvTopics
    AGONIST = "agonist";
    ANTAGONIST = "antagonist";
    AUTHORCONFIDENCE = "author-confidence";
    CAUTION = "caution";
    COMMENT = "comment";
    CONFIDENCEMAPPING = "confidence-mapping";
    DEFINITION = "definition";
    DISEASE = "disease";
    EXAMPLE = "example";
    EXPMODIFICATION = "exp-modification";
    FUNCTION = "function";
    INHIBITION = "inhibition";
    ISOFORMCOMMENT = "isoform-comment";
    KINETICS = "kinetics";
    NEGATIVE = "negative";
    ONHOLD = "onhold";
    PATHWAY = "pathway";
    PREREQUISITEPTM = "prerequisite-ptm";
    REMARKINTERNAL = "remark-internal";
    RESULTINGPTM = "resulting-ptm";
    SEARCHURL = "search-url";
    SEARCHURLASCII = "search-url-ascii";
    STIMULATION = "stimulation";
    SUBMITTED = "submitted";
    UNIPROTCCNOTE = "uniprot-cc-note";
    UNIPROTDREXPORT = "uniprot-dr-export";
    URL = "url";

//CvAliasType
    GENENAME = "gene-name";
    GENENAMESYNONYM = "gene-name-synonym";
    GOSYNONYM = "go-synonym";
    ISOFORMSYNONYM = "isoform-synonym";
    LOCUSNAME = "locus-name";
    ORFNAME = "orf-name";

}

// Operators

QUOTE: ('\'');

SEMICOLON : ';' ;

LPAREN : '(' ;
RPAREN : ')' ;

EQUALS : '=' ;

Whitespace
    : (' ' | '\t' | '\n' | '\r')
    { _ttype = Token.SKIP; }
    ;


// LITERALS

protected  // generated as part of the Identifier rule
Letter
    : 'a'..'z' | '_' | '#' | '-' | '@' | '\u0080'..'\ufffe'
    ;

protected
Digit
    : '0'..'9'
    ;

Identifier
    options { testLiterals = true; }
    : (Letter|Digit)+
    ;


// TreeParser **************************************************************************

class Iql2LuceneTreeWalker extends TreeParser;

{

  TokenStreamSelector selector;
  // initialize the TokenStreamSelector
  public void init(TokenStreamSelector selector){
        this.selector= selector;
    }
 }

// it returns the String containing the search condition in the Lucene format
criteria returns [String luceneStr]
    {
      luceneStr = null;
      String a  = null;
      String b  = null;
    }
    : #(AND a=criteria b=criteria)
        {luceneStr = "(" + a + " AND " + b + ")";
        }
    | #(OR a=criteria b=criteria)
        {luceneStr = "(" + a + " OR " + b + ")";
        }
    | luceneStr=predicate
    ;

// returns one search condition in the Lucene format
// containing the term, the comparisor and the value
predicate returns [String pred]
    {
     String t,v;
     pred = null;
    }
    : #(LIKE t=term v=value)
     {

        pred = t + ":(" + v + ")";
     }
    | #(EQUALS t=term v=value)
     {
        pred = t + ":(" + v + ")";
     }
    ;

 // returns a String with the name of the term
 term returns [String str]
     {str = "";}
     : ( a:AC
           {str = a.getText();}
     |   s:SHORTLABEL
           {str = s.getText();}
     |   d:FULLNAME
           {str = d.getText();}
     |   al:ALIAS
           {str = al.getText();}
     |   ann:ANNOTATION
           {str = ann.getText();}
     |   x:XREF
           {str = x.getText();}

     | intac:CVINTERACTION_AC
           {str = intac.getText();}
     | intsh:CVINTERACTION_SHORTLABEL
           {str = intsh.getText();}
     | intfull:CVINTERACTION_FULLNAME
           {str = intfull.getText();}
     | idac:CVIDENTIFICATION_AC
           {str = idac.getText();}
     | idsh:CVIDENTIFICATION_SHORTLABEL
           {str = idsh.getText();}
     | idfull:CVIDENTIFICATION_FULLNAME
           {str = idfull.getText();}
     | itac:CVINTERACTION_TYPE_AC
           {str = itac.getText();}
     | itsh:CVINTERACTION_TYPE_SHORTLABEL
           {str = itsh.getText();}
     | itfull:CVINTERACTION_TYPE_FULLNAME
           {str = itfull.getText();}

// CvDatabases:
     |   ca:CABRI
           {str = ca.getText();}
     |   f:FLYBASE
            {str = f.getText();}
     |   g:GO
           {str = g.getText();}
     |   h:HUGE
           {str = h.getText();}
     |   i:INTACT
           {str = i.getText();}
     |   in:INTERPRO
           {str = in.getText();}
     |   n:NEWT
           {str = n.getText();}
     |   o:OMIM
           {str = o.getText();}
     |   p:PDB
           {str = p.getText();}
     |   ps:PSIMI
           {str = ps.getText();}
     |   pu:PUBMED
           {str = pu.getText();}
     |   r:REACTOME
           {str = r.getText();}
     |   re:RESID
           {str = re.getText();}
     |   sg:SGD
           {str = sg.getText();}
     |   u:UNIPROT
           {str = u.getText();}
// CvTopics:
     |   ag:AGONIST
           {str = ag.getText();}
     |   an:ANTAGONIST
           {str = an.getText();}
     |   au:AUTHORCONFIDENCE
           {str = au.getText();}
     |   cau:CAUTION
           {str = cau.getText();}
     |   co:COMMENT
           {str = co.getText();}
     |   con:CONFIDENCEMAPPING
           {str = con.getText();}
     |   de:DEFINITION
           {str = de.getText();}
     |   di:DISEASE
           {str = di.getText();}
     |   exa:EXAMPLE
           {str = exa.getText();}
     |   exp:EXPMODIFICATION
           {str = exp.getText();}
     |   fu:FUNCTION
           {str = fu.getText();}
     |   inh:INHIBITION
           {str = inh.getText();}
     |   iso:ISOFORMCOMMENT
           {str = iso.getText();}
     |   ki:KINETICS
           {str = ki.getText();}
     |   ne:NEGATIVE
           {str = ne.getText();}
     |   on:ONHOLD
           {str = on.getText();}
     |   pa:PATHWAY
           {str = pa.getText();}
     |   pr:PREREQUISITEPTM
           {str = pr.getText();}
     |   rem:REMARKINTERNAL
           {str = rem.getText();}
     |   res:RESULTINGPTM
           {str = res.getText();}
     |   se:SEARCHURL
           {str = se.getText();}
     |   sea:SEARCHURLASCII
           {str = sea.getText();}
     |   st:STIMULATION
           {str = st.getText();}
     |   su:SUBMITTED
           {str = su.getText();}
     |   cc:UNIPROTCCNOTE
           {str = cc.getText();}
     |   dr:UNIPROTDREXPORT
           {str = dr.getText();}
     |   url:URL
           {str = url.getText();}

//CvAliasType
     |   ge:GENENAME
            {str = ge.getText();}
     |   gen:GENENAMESYNONYM
           {str = gen.getText();}
     |   go:GOSYNONYM
           {str = go.getText();}
     |   is:ISOFORMSYNONYM
           {str = is.getText();}
     |   lo:LOCUSNAME
           {str = lo.getText();}
     |   or:ORFNAME
           {str = or.getText();}
       )
     ;

 // returns a String containing the value
 value returns [String val]
     {val = "";}
     :
     (
       QUOTE
     {selector.push("valuelexer");}
       v:VALUE
     {  val = v.getText();
     // escape some lucene syntax characters
        val = SearchReplace.replace(val, "\\\\", "\\\\\\\\");
        val = SearchReplace.replace(val, ":", "\\\\:");
        val = SearchReplace.replace(val, ")", "\\\\)");
        val = SearchReplace.replace(val, "(", "\\\\(");
        val = SearchReplace.replace(val, "+", "\\\\+");
        val = SearchReplace.replace(val, "-", "\\\\-");
        val = SearchReplace.replace(val, "!", "\\\\!");
        val = SearchReplace.replace(val, "&&", "\\\\&&");
        val = SearchReplace.replace(val, "||", "\\\\||");
     }
     )

     {selector.pop();}
       QUOTE
     ;
